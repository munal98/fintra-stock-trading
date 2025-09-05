/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable react-hooks/exhaustive-deps */
"use client"

import { useState, useEffect, useRef } from "react"
import { ColumnDef } from "@tanstack/react-table"
import { DataTable } from "@/components/data-table"
import { Button } from "@/components/ui/button"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { Plus, MoreHorizontal, Trash2, UserPlus, AlertCircle } from "lucide-react"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog"
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { LoadingButton } from "@/components/ui/loading"
import { CustomerSearch, CustomerType } from "@/components/ui/customer-search"
import initSendRequest from "@/configs/sendRequest"
import axios, { AxiosError } from "axios"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'

interface EmployeeType {
    id: number
    email: string
    firstName: string
    lastName: string
    role: string
    createdAt: string
    updatedAt: string
    enabled: boolean
}

export default function EmployeesPage() {
    const { t } = useTranslation()
    const sendRequest = initSendRequest()
    const [employees, setEmployees] = useState<EmployeeType[]>([])
    const [loading, setLoading] = useState(false)
    const [pagination, setPagination] = useState({
        pageIndex: 0,
        pageSize: 10,
    })
    const [totalElements, setTotalElements] = useState(0)
    const [dialogOpen, setDialogOpen] = useState(false)
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")
    const [firstName, setFirstName] = useState("")
    const [lastName, setLastName] = useState("")
    const [role, setRole] = useState<"ROLE_ADMIN" | "ROLE_ANALYST" | "ROLE_TRADER">("ROLE_ADMIN")
    const [createLoading, setCreateLoading] = useState(false)
    const [createError, setCreateError] = useState<string | null>(null)
    const [selectedEmployee, setSelectedEmployee] = useState<EmployeeType | null>(null)
    const [searchLoading, setSearchLoading] = useState(false)
    const [searchValue, setSearchValue] = useState('')
    const debounceTimerRef = useRef<NodeJS.Timeout | null>(null)
    const [editDialogOpen, setEditDialogOpen] = useState(false)
    const [employeeCustomers, setEmployeeCustomers] = useState<CustomerType[]>([])
    const [loadingCustomers, setLoadingCustomers] = useState(false)
    const [selectedCustomerToAdd, setSelectedCustomerToAdd] = useState<CustomerType | null>(null)
    const [removingAllCustomers, setRemovingAllCustomers] = useState(false)
    const [customerError, setCustomerError] = useState<string | null>(null)
    const [error, setError] = useState<string | null>(null)

    const columns: ColumnDef<EmployeeType>[] = [
        {
            accessorKey: "id",
            header: t("ID"),
            cell: ({ row }) => row.original.id,
            size: 80,
        },
        {
            accessorKey: "firstName",
            header: t("First Name"),
            cell: ({ row }) => row.original.firstName,
            size: 120,
        },
        {
            accessorKey: "lastName",
            header: t("Last Name"),
            cell: ({ row }) => row.original.lastName,
            size: 120,
        },
        {
            accessorKey: "email",
            header: t("Email"),
            cell: ({ row }) => row.original.email,
            size: 200,
        },
        {
            accessorKey: "role",
            header: t("Role"),
            cell: ({ row }) => {
                const role = row.original.role;
                const roleDisplay = role.replace('ROLE_', '');
                return (
                    <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${role === 'ROLE_ADMIN'
                            ? 'bg-red-100 text-red-800'
                            : role === 'ROLE_ANALYST'
                                ? 'bg-blue-100 text-blue-800'
                                : 'bg-green-100 text-green-800'
                            }`}
                    >
                        {roleDisplay}
                    </span>
                );
            },
            size: 100,
        },
        {
            accessorKey: "createdAt",
            header: t("Created At"),
            cell: ({ row }) => {
                const date = row.original.createdAt;
                return date ? new Date(date).toLocaleDateString('tr-TR') : "-";
            },
            size: 120,
        },
        {
            accessorKey: "enabled",
            header: t("Status"),
            cell: ({ row }) => {
                const isEnabled = row.original.enabled;
                return (
                    <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${isEnabled
                                ? 'bg-green-100 text-green-800'
                                : 'bg-gray-100 text-gray-800'
                            }`}
                    >
                        {isEnabled ? 'Active' : 'Passive'}
                    </span>
                );
            },
            size: 100,
        },
        {
            id: "actions",
            header: t("Actions"),
            cell: ({ row }) => {
                const employee = row.original;
                return (
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="ghost" className="h-8 w-8 p-0">
                                <MoreHorizontal className="h-4 w-4" />
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                            <DropdownMenuItem
                                onClick={() => {
                                    handleEditEmployee(employee);
                                }}
                            >
                                {t("Edit")}
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                className={row.original.enabled ? "text-amber-600" : "text-green-600"}
                                onClick={() => {
                                    handleToggleStatus(employee);
                                }}
                            >
                                {row.original.enabled ? t("Deactive") : t("Active")}
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                );
            },
            size: 80,
        },
    ]

    useEffect(() => {
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current)
        }
        debounceTimerRef.current = setTimeout(async () => {
            try {
                await fetchEmployees()
            } finally {
                setSearchLoading(false)
            }
        }, 500)
    }, [searchValue])

    const fetchEmployees = async () => {
        setLoading(true)
        try {

            const response = await sendRequest.get("/users", { params: { search: searchValue, size: 1000 } })

            setEmployees(response.data.content || [])
            setTotalElements(response.data.totalElements || 0)

        } catch (error: any) {
            setError("Error fetching employees:" + error.message)
            setEmployees([])
            setTotalElements(0)
        } finally {
            setLoading(false)
        }
    }

    const handleCreateEmployee = async () => {
        setCreateLoading(true)
        setCreateError(null)

        try {
            if (!email) throw new Error(t("Email is required"))
            if (!password) throw new Error(t("Password is required"))
            if (!firstName) throw new Error(t("First name is required"))
            if (!lastName) throw new Error(t("Last name is required"))

            const newEmployee = {
                email,
                password,
                firstName,
                lastName,
                role
            }

            await sendRequest.post("/users", newEmployee)

            setEmail("")
            setPassword("")
            setFirstName("")
            setLastName("")
            setRole("ROLE_ADMIN")
            setDialogOpen(false)
            fetchEmployees()

        } catch (error) {
            let errorMessage = "An error occurred";

            const axiosError = error as AxiosError;
            if (axiosError.response?.data) {
                const responseData = axiosError.response.data as any;
                if (responseData.message) {
                    errorMessage = responseData.message;
                } else if (responseData.error) {
                    errorMessage = responseData.error;
                }
            } else if (error instanceof Error) {
                errorMessage = error.message;
            }

            setCreateError(errorMessage);
        } finally {
            setCreateLoading(false)
        }
    }
    const fetchEmployeeCustomers = async (employeeId: number) => {
        setLoadingCustomers(true)
        setCustomerError(null)
        try {
            const response = await sendRequest.get(`/users/${employeeId}`)
            setEmployeeCustomers(response.data.customers || [])
        } catch (error) {
            if (axios.isAxiosError(error)) {
                setCreateError(error.response?.data?.message || "An error occurred");
            } else if (error instanceof Error) {
                setCreateError(error.message);
            } else {
                setCreateError("An error occurred");
            }
            setEmployeeCustomers([])
        } finally {
            setLoadingCustomers(false)
        }
    }

    const handleEditEmployee = (employee: EmployeeType) => {
        setSelectedEmployee(employee)
        setEditDialogOpen(true)
        fetchEmployeeCustomers(employee.id)
    }

    const handleAddCustomer = async () => {
        if (!selectedEmployee || !selectedCustomerToAdd) return

        setCustomerError(null)
        try {
            await sendRequest.post(`/users/${selectedEmployee.id}/customers`, {
                customerIds: [selectedCustomerToAdd.customerId]
            })

            await fetchEmployeeCustomers(selectedEmployee.id)
            setSelectedCustomerToAdd(null)
        } catch (error) {
            if (axios.isAxiosError(error)) {
                setCreateError(error.response?.data?.message || "An error occurred");
            } else if (error instanceof Error) {
                setCreateError(error.message);
            } else {
                setCreateError("An error occurred");
            }
        }
    }

    const handleRemoveAllCustomers = async () => {
        if (!selectedEmployee) return

        setRemovingAllCustomers(true)
        setCustomerError(null)
        try {
            await sendRequest.delete(`/users/${selectedEmployee.id}/customers/all`)

            await fetchEmployeeCustomers(selectedEmployee.id)
        } catch (error) {
            if (axios.isAxiosError(error)) {
                setCustomerError(error.response?.data?.message || "An error occurred");
            } else if (error instanceof Error) {
                setCustomerError(error.message);
            } else {
                setCustomerError("An error occurred");
            }
        } finally {
            setRemovingAllCustomers(false)
        }
    }

    const handleRemoveCustomer = async (customerId: number) => {
        if (!selectedEmployee) return

        setCustomerError(null)
        try {
            await sendRequest.delete(`/users/${selectedEmployee.id}/customers`, {
                data: { customerIds: [customerId] }
            })

            await fetchEmployeeCustomers(selectedEmployee.id)
        } catch (error) {
            if (axios.isAxiosError(error)) {
                setCustomerError(error.response?.data?.message || "An error occurred");
            } else if (error instanceof Error) {
                setCustomerError(error.message);
            } else {
                setCustomerError("An error occurred");
            }
        }
    }

    const handleToggleStatus = async (employee: EmployeeType) => {
        try {
            await sendRequest.patch(`/users/${employee.id}`, {
                enabled: !employee.enabled
            })
            await fetchEmployees()
        } catch (error: any) {
            setError("Error toggling employee status:" + error.message)
        }
    }

    return (
        <div>
            <SidebarProvider
                style={
                    {
                        "--sidebar-width": "calc(var(--spacing) * 72)",
                        "--header-height": "calc(var(--spacing) * 12)",
                    } as React.CSSProperties
                }
            >
                <AppSidebar variant="inset" />
                <SidebarInset>
                    <SiteHeader title={t("Employees")} />
                    <div className="flex flex-1 flex-col gap-4 p-4">
                        <div className="min-h-[100vh] flex-1 rounded-xl bg-muted/50 md:min-h-min">
                            <div className="p-6">
                                <div className="flex items-center justify-between mb-6">
                                    <div>
                                        <h1 className="text-3xl font-bold">{t("Employees")}</h1>
                                        <p className="text-muted-foreground">
                                            {t("Manage your employees and their roles")}
                                        </p>
                                    </div>
                                    <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
                                        <DialogTrigger asChild>
                                            <Button>
                                                <Plus className="mr-2 h-4 w-4" />
                                                {t("Add Employee")}
                                            </Button>
                                        </DialogTrigger>
                                        <DialogContent className="sm:max-w-[425px]">
                                            <DialogHeader>
                                                <DialogTitle>{t("Create Employee")}</DialogTitle>
                                                <DialogDescription>
                                                    {t("Add a new employee to your system.")}
                                                </DialogDescription>
                                            </DialogHeader>
                                            <div className="grid gap-4 py-4">
                                                {createError && (
                                                    <div className="bg-red-50 text-red-800 p-3 rounded-md text-sm">
                                                        {createError}
                                                    </div>
                                                )}

                                                <div className="grid gap-2">
                                                    <Label htmlFor="role">{t("Role")}</Label>
                                                    <Select
                                                        value={role}
                                                        onValueChange={(value: "ROLE_ADMIN" | "ROLE_ANALYST" | "ROLE_TRADER") => setRole(value)}
                                                    >
                                                        <SelectTrigger id="role">
                                                            <SelectValue placeholder={t("Select role")} />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            <SelectItem value="ROLE_ADMIN">Admin</SelectItem>
                                                            <SelectItem value="ROLE_ANALYST">Analyst</SelectItem>
                                                            <SelectItem value="ROLE_TRADER">Trader</SelectItem>
                                                        </SelectContent>
                                                    </Select>
                                                </div>

                                                <div className="grid grid-cols-2 gap-4">
                                                    <div className="grid gap-2">
                                                        <Label htmlFor="firstName">{t("First Name")}</Label>
                                                        <Input
                                                            id="firstName"
                                                            value={firstName}
                                                            onChange={(e) => setFirstName(e.target.value)}
                                                        />
                                                    </div>
                                                    <div className="grid gap-2">
                                                        <Label htmlFor="lastName">{t("Last Name")}</Label>
                                                        <Input
                                                            id="lastName"
                                                            value={lastName}
                                                            onChange={(e) => setLastName(e.target.value)}
                                                        />
                                                    </div>
                                                </div>

                                                <div className="grid gap-2">
                                                    <Label htmlFor="email">{t("Email")}</Label>
                                                    <Input
                                                        id="email"
                                                        type="email"
                                                        value={email}
                                                        onChange={(e) => setEmail(e.target.value)}
                                                    />
                                                </div>

                                                <div className="grid gap-2">
                                                    <Label htmlFor="password">{t("Password")}</Label>
                                                    <Input
                                                        id="password"
                                                        type="password"
                                                        value={password}
                                                        onChange={(e) => setPassword(e.target.value)}
                                                    />
                                                </div>
                                            </div>
                                            <DialogFooter>
                                                <Button variant="outline" onClick={() => setDialogOpen(false)}>
                                                    {t("Cancel")}
                                                </Button>
                                                <LoadingButton
                                                    onClick={handleCreateEmployee}
                                                    loading={createLoading}
                                                    className="h-8 bg-primary text-primary-foreground hover:bg-primary/90"
                                                >
                                                    {t("Create Employee")}
                                                </LoadingButton>
                                            </DialogFooter>
                                        </DialogContent>
                                    </Dialog>
                                </div>

                                <div className="mb-4 flex items-center gap-4">
                                    <div className="flex-1 max-w-sm">
                                        <Input
                                            placeholder={t("Search")}
                                            value={searchValue}
                                            onChange={(e) => setSearchValue(e.target.value)}
                                        />
                                    </div>
                                    {searchLoading && (
                                        <div className="text-sm text-muted-foreground">
                                            {t("Searching...")}
                                        </div>
                                    )}
                                    {totalElements > 0 && (
                                        <div className="text-sm text-muted-foreground">
                                            {totalElements} {t("employee")}{totalElements !== 1 ? 's' : ''} {t("found")}
                                        </div>
                                    )}
                                </div>

                                <DataTable
                                    columns={columns}
                                    data={employees}
                                    pagination={pagination}
                                    setPagination={setPagination}
                                    loading={loading}
                                />
                            </div>
                        </div>
                    </div>
                </SidebarInset>
            </SidebarProvider>

            <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
                <DialogContent className="sm:max-w-[900px] max-h-[90vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>{t("Edit Employee")} - {selectedEmployee?.firstName} {selectedEmployee?.lastName}</DialogTitle>
                        <DialogDescription>
                            {t("Manage customers assigned to this employee.")}
                        </DialogDescription>
                    </DialogHeader>

                    <div className="grid gap-6 py-4">
                        {customerError && (
                            <div className="bg-red-50 text-red-800 p-3 rounded-md text-sm">
                                {customerError}
                            </div>
                        )}

                        {/* Current Customers Section */}
                        <div className="space-y-3">
                            <div className="flex items-center justify-between">
                                <Label className="text-base font-semibold">{t("Assigned Customers")} ({employeeCustomers.length})</Label>
                                <LoadingButton
                                    onClick={handleRemoveAllCustomers}
                                    loading={removingAllCustomers}
                                    disabled={employeeCustomers.length === 0}
                                    className="h-8 bg-destructive text-destructive-foreground hover:bg-destructive/90 text-sm px-3"
                                >
                                    <Trash2 className="h-4 w-4 mr-1" />
                                    {t("Remove All")}
                                </LoadingButton>
                            </div>

                            {loadingCustomers ? (
                                <div className="flex items-center justify-center py-8">
                                    <div className="text-sm text-muted-foreground">{t("Loading customers...")}</div>
                                </div>
                            ) : employeeCustomers.length === 0 ? (
                                <div className="text-center py-8 text-muted-foreground">
                                    <div className="text-sm">{t("No customers assigned to this employee")}</div>
                                </div>
                            ) : (
                                <div className="border rounded-lg">
                                    <div className="max-h-[200px] overflow-y-auto">
                                        <table className="w-full">
                                            <thead className="bg-muted/90 sticky top-0">
                                                <tr>
                                                    <th className="text-left p-3 text-sm font-medium">ID</th>
                                                    <th className="text-left p-3 text-sm font-medium">{t("Name")}</th>
                                                    <th className="text-left p-3 text-sm font-medium">{t("Email")}</th>
                                                    <th className="text-left p-3 text-sm font-medium">{t("Trading")}</th>
                                                    <th className="text-left p-3 text-sm font-medium">{t("Actions")}</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {employeeCustomers.map((customer, index) => (
                                                    <tr key={customer.customerId} className={index % 2 === 0 ? "bg-background" : "bg-muted/25"}>
                                                        <td className="p-3 text-sm">{customer.customerId}</td>
                                                        <td className="p-3 text-sm font-medium">
                                                            {customer.firstName} {customer.lastName}
                                                        </td>
                                                        <td className="p-3 text-sm text-muted-foreground">{customer.email}</td>
                                                        <td className="p-3 text-sm">
                                                            <span className={`px-2 py-1 rounded-full text-xs font-medium ${customer.tradingEnabled
                                                                ? 'bg-green-100 text-green-800'
                                                                : 'bg-red-100 text-red-800'
                                                                }`}>
                                                                {customer.tradingEnabled ? t("Enabled") : t("Disabled")}
                                                            </span>
                                                        </td>
                                                        <td className="p-3 text-sm">
                                                            <Button
                                                                onClick={() => handleRemoveCustomer(customer.customerId)}
                                                                className="h-8 bg-destructive text-destructive-foreground hover:bg-destructive/90 text-sm px-3"
                                                            >
                                                                <Trash2 className="h-4 w-4 mr-1" />
                                                                {t("Remove")}
                                                            </Button>
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                </div>
                            )}
                        </div>

                        {/* Add Customer Section */}
                        <div className="space-y-3 border-t pt-4">
                            <Label className="text-base font-semibold">{t("Add New Customer")}</Label>
                            <div className="flex gap-3 items-center">
                                <div className="flex-1">
                                    <CustomerSearch
                                        onSelect={setSelectedCustomerToAdd}
                                        selectedCustomer={selectedCustomerToAdd}
                                        placeholder={t("Search and select a customer to add...")}
                                    />
                                </div>
                                <div className="pt-5.5">
                                    <Button
                                        onClick={handleAddCustomer}
                                        disabled={!selectedCustomerToAdd}
                                        className="shrink-0 bg-primary text-primary-foreground hover:bg-primary/90 h-10 flex items-center px-2 text-xs"
                                    >
                                        <UserPlus className="h-3 w-3 mr-1" />
                                        {t("Add")}
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setEditDialogOpen(false)}>
                            {t("Close")}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>{t("Error")}</AlertTitle>
                    <AlertDescription>
                        {error}
                    </AlertDescription>
                </Alert>
            )}
        </div >
    )
}
