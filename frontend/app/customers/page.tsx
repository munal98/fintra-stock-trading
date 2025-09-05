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
import { AlertCircle, Plus } from "lucide-react"
import { CustomerType } from "@/components/ui/customer-search"
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
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Label } from "@/components/ui/label"
import { LoadingButton } from "@/components/ui/loading"
import initSendRequest from "@/configs/sendRequest"
import { IconDotsVertical } from "@tabler/icons-react"
import { Input } from "@/components/ui/input"
import { AxiosError } from "axios"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'

interface AccountType {
    accountId: number;
    accountType: string;
    cashBalance: {
        balanceId: number;
        freeBalance: number;
        blockedBalance: number;
        totalBalance: number;
    };
}

interface EquityType {
    assetCode: string;
    assetName: string;
    totalQuantity: number;
    averageCost: number;
    closePrice: number | null;
    profitLossPercentage: number;
}

export default function CustomersPage() {
    const sendRequest = initSendRequest()
    const { t } = useTranslation()
    const [filteredCustomers, setFilteredCustomers] = useState<CustomerType[]>([])
    const [loading, setLoading] = useState(false)
    const [pagination, setPagination] = useState({
        pageIndex: 0,
        pageSize: 10,
    })
    const [dialogOpen, setDialogOpen] = useState(false)
    const [customerType, setCustomerType] = useState<"INDIVIDUAL" | "CORPORATE">("INDIVIDUAL")
    const [firstName, setFirstName] = useState("")
    const [lastName, setLastName] = useState("")
    const [email, setEmail] = useState("")
    const [identityNumber, setIdentityNumber] = useState("")
    const [tradingPermission, setTradingPermission] = useState("FULL")
    const [createLoading, setCreateLoading] = useState(false)
    const [createError, setCreateError] = useState<string | null>(null)
    const [detailsModalOpen, setDetailsModalOpen] = useState(false)
    const [editModalOpen, setEditModalOpen] = useState(false)
    const [selectedCustomer, setSelectedCustomer] = useState<CustomerType | null>(null)
    const [statusDialogOpen, setStatusDialogOpen] = useState(false)
    const [statusLoading, setStatusLoading] = useState(false)
    const [statusError, setStatusError] = useState<string | null>(null)
    const [accounts, setAccounts] = useState<AccountType[]>([])
    const [accountDialogOpen, setAccountDialogOpen] = useState(false)
    const [accountDetailsDialogOpen, setAccountDetailsDialogOpen] = useState(false)
    const [accountType, setAccountType] = useState<string>("INDIVIDUAL")
    const [accountLoading, setAccountLoading] = useState(false)
    const [accountError, setAccountError] = useState<string | null>(null)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
    const [accountToDelete, setAccountToDelete] = useState<AccountType | null>(null)
    const [deleteLoading, setDeleteLoading] = useState(false)
    const [deleteError, setDeleteError] = useState<string | null>(null)
    const [traders, setTraders] = useState<any[]>([])
    const [selectedTraderId, setSelectedTraderId] = useState<number | null>(null)
    const [searchQuery, setSearchQuery] = useState("")
    const [userRole, setUserRole] = useState<string>("")
    const [error, setError] = useState("")
    const debounceTimerRef = useRef<NodeJS.Timeout | null>(null)

    const columns: ColumnDef<CustomerType>[] = [
        {
            accessorKey: "customerId",
            header: t("ID"),
        },
        {
            accessorKey: "firstName",
            header: t("Name"),
        },
        {
            accessorKey: "lastName",
            header: t("Last Name"),
        },
        {
            accessorKey: "email",
            header: t("Email"),
        },
        {
            accessorKey: "identityNumber",
            header: t("Identity Number"),
        },
        {
            accessorKey: "tradingPermission",
            header: t("Trading Permission"),
            cell: ({ row }) => {
                const permission = row.original.tradingPermission;
                return (
                    <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${permission === "FULL" || permission === "full"
                            ? 'bg-blue-100 text-blue-800'
                            : permission === "PARTICIPATION_ONLY" || permission === "participation_only"
                                ? 'bg-amber-100 text-amber-800'
                                : 'bg-gray-100 text-gray-800'
                            }`}
                    >
                        {permission === "FULL" || permission === "full"
                            ? 'Full'
                            : permission === "PARTICIPATION_ONLY" || permission === "participation_only"
                                ? 'Participation Only'
                                : permission}
                    </span>
                );
            },
        },
        {
            accessorKey: "tradingEnabled",
            header: t("Trading Status"),
            cell: ({ row }) => {
                const status = row.original.tradingEnabled;
                return (
                    <span
                        className={`px-2 py-1 rounded-full text-xs font-medium ${status === true
                            ? 'bg-green-100 text-green-800'
                            : status === false
                                ? 'bg-red-100 text-red-800'
                                : 'bg-gray-100 text-gray-800'
                            }`}
                    >
                        {status === true ? t("Active") : status === false ? t("Passive") : status}
                    </span>
                );
            },
        },
        ...(userRole !== "ROLE_TRADER" ? [
            {
                accessorKey: "Details",
                header: t("Details"),
                cell: ({ row }: any) => {

                    const handleEdit = async () => {
                        setSelectedCustomer(row.original)
                        setEditModalOpen(true)
                    };

                    const handleViewDetails = async () => {
                        setSelectedCustomer(row.original)
                        setDetailsModalOpen(true)
                    };

                    const handleStatusChange = async () => {
                        setSelectedCustomer(row.original)
                        setStatusDialogOpen(true)
                    };

                    const handleAccountDetails = async () => {
                        setSelectedCustomer(row.original);
                        await fetchAccountDetails(row.original);
                        setAccountDetailsDialogOpen(true);
                    };

                    return (
                        <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                                <Button
                                    variant="ghost"
                                    className="data-[state=open]:bg-muted text-muted-foreground flex size-8"
                                    size="icon"
                                >
                                    <IconDotsVertical />
                                    <span className="sr-only">{t("Open menu")}</span>
                                </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end" className="w-32">
                                <DropdownMenuItem onClick={handleEdit}>{t("Edit")}</DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={handleViewDetails}>{t("Details")}</DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={handleStatusChange}>
                                    {row.original.tradingEnabled ? t("Deactivate") : t("Activate")}
                                </DropdownMenuItem>
                                <DropdownMenuSeparator />
                                <DropdownMenuItem onClick={handleAccountDetails}>
                                    {t("Account Details")}
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    );
                },
            }
        ] : [])
    ]
    const fetchCustomers = async (role?: string) => {
        setLoading(true)
        try {
            const currentRole = role || userRole
            const endpoint = currentRole === "ROLE_TRADER" ? "/customers/assigned" : "/customers"
            const response = await sendRequest.get(endpoint, { params: { search: searchQuery, size: 1000 } })
            setFilteredCustomers(response.data.content || [])
        } catch (error: any) {
            setError(error.message)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        const storedRole = localStorage.getItem('role')
        if (storedRole) {
            setUserRole(storedRole)
            if (storedRole !== "ROLE_TRADER") {
                fetchTraders()
            }
        } else {
            fetchTraders()
        }
    }, [])

    useEffect(() => {
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current)
        }
        debounceTimerRef.current = setTimeout(async () => {
            try {
                const storedRole = localStorage.getItem('role')
                if (storedRole) {
                    setUserRole(storedRole)
                    await fetchCustomers(storedRole)
                } else {
                    await fetchCustomers()
                }
            } finally {
                setLoading(false)
            }
        }, 500)
    }, [searchQuery])

    const fetchTraders = async () => {
        setLoading(true)
        try {
            const response = await sendRequest.get("/users", { params: { search: "ROLE_TRADER" } })
            setTraders(response.data.content || [])
        } catch (error: any) {
            setError(error.message)
        } finally {
            setLoading(false)
        }
    }

    const handleCreateCustomer = async () => {
        setCreateLoading(true)
        setCreateError(null)

        try {
            if (!firstName) throw new Error("First name is required")
            if (customerType === "INDIVIDUAL" && !lastName) throw new Error("Last name is required")
            if (!email) throw new Error("Email is required")
            if (!identityNumber) throw new Error("Identity number is required")
            if (!selectedTraderId) throw new Error("Trader selection is required")
            const requiredLength = customerType === "INDIVIDUAL" ? 11 : 10;
            if (identityNumber.length !== requiredLength) {
                throw new Error(
                    customerType === "INDIVIDUAL"
                        ? "TC Identity Number must be exactly 11 digits"
                        : "Tax Identity Number must be exactly 10 digits"
                );
            }

            const newCustomer = {
                firstName,
                lastName: customerType === "CORPORATE" ? "-" : lastName,
                email,
                identityNumber,
                tradingPermission,
                accountType: customerType,
                userId: selectedTraderId
            }

            await sendRequest.post("/customers", newCustomer)

            setFirstName("")
            setLastName("")
            setEmail("")
            setIdentityNumber("")
            setTradingPermission("FULL")
            setCustomerType("INDIVIDUAL")
            setDialogOpen(false)
            fetchCustomers()

        } catch (error: any) {
            if (error instanceof Error) {
                const axiosError = error as AxiosError<any>;
                setCreateError(axiosError.response?.data?.message || error.message);
            } else {
                setCreateError("An error occurred");
            }
        } finally {
            setCreateLoading(false)
        }
    }

    const handleCustomerStatusChange = async () => {
        if (!selectedCustomer) return;

        setStatusLoading(true);
        setStatusError(null);

        try {

            await sendRequest.put(`/customers/${selectedCustomer.customerId}`, {
                ...selectedCustomer,
                tradingEnabled: !selectedCustomer.tradingEnabled
            });
            setStatusDialogOpen(false);
            fetchCustomers();
        } catch (error: any) {
            setError(error.message);
            let errorMessage = "Failed to update customer status";

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

            setStatusError(errorMessage);
        } finally {
            setStatusLoading(false);
        }
    }

    const fetchAccountDetails = async (customer: CustomerType) => {
        if (!customer) return;

        setAccountLoading(true);
        setAccountError(null);

        try {
            const response = await sendRequest.get(`/accounts/customer/${customer.customerId}`);
            setAccounts(response.data || []);
            return response.data;
        } catch (error: any) {
            setError(error.message);
            let errorMessage = "Failed to fetch account details";

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

            setAccountError(errorMessage);
            return [];
        } finally {
            setAccountLoading(false);
        }
    }

    const handleCreateAccount = async () => {
        if (!selectedCustomer) return;

        setAccountLoading(true);
        setAccountError(null);

        try {

            await sendRequest.post(`/accounts`, {
                customerId: selectedCustomer.customerId,
                accountType: accountType
            });

            setAccountDialogOpen(false);
            await fetchAccountDetails(selectedCustomer);
        } catch (error: any) {
            setError(error.message);
            let errorMessage = "Failed to create account";

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

            setAccountError(errorMessage);
        } finally {
            setAccountLoading(false);
        }
    }
    const handleDeleteAccount = async () => {
        if (!accountToDelete) return;

        setDeleteLoading(true);
        setDeleteError(null);

        try {

            await sendRequest.delete(`/accounts/${accountToDelete.accountId}`);

            setDeleteDialogOpen(false);
            if (selectedCustomer) {
                await fetchAccountDetails(selectedCustomer);
            }
        } catch (error: any) {
            setError(error.message);
            let errorMessage = "Failed to delete account";

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

            setDeleteError(errorMessage);
        } finally {
            setDeleteLoading(false);
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
                    <SiteHeader title={t("Customers")} />
                    <div className="flex flex-1 flex-col">
                        <div className="@container/main flex flex-1 flex-col gap-2">
                            <div className="container px-4 mx-auto py-4 md:py-6">
                                <div className="flex justify-between items-center mb-6">
                                    <div className="w-64">
                                        <Input
                                            placeholder={t("Search customers...")}
                                            value={searchQuery}
                                            onChange={(e) => setSearchQuery(e.target.value)}
                                        />
                                    </div>

                                    {userRole !== "ROLE_TRADER" && (
                                        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
                                            <DialogTrigger asChild>
                                                <Button className="flex items-center gap-2">
                                                    <Plus className="h-4 w-4" />
                                                    {t("Create Customer")}
                                                </Button>
                                            </DialogTrigger>
                                            <DialogContent className="sm:max-w-[425px]">
                                                <DialogHeader>
                                                    <DialogTitle>{t("Create New Customer")}</DialogTitle>
                                                    <DialogDescription>
                                                        {t("Fill in the details to create a new customer account.")}
                                                    </DialogDescription>
                                                </DialogHeader>

                                                {createError && (
                                                    <div className="bg-red-50 text-red-800 p-3 rounded-md text-sm mb-4">
                                                        {createError}
                                                    </div>
                                                )}

                                                <div className="grid gap-4 py-4">
                                                    <div className="grid grid-cols-2 gap-4">
                                                        <div className="flex flex-col space-y-1.5">
                                                            <Label htmlFor="customerType">{t("Customer Type")}</Label>
                                                            <Select value={customerType} onValueChange={(value) => setCustomerType(value as "INDIVIDUAL" | "CORPORATE")}>
                                                                <SelectTrigger>
                                                                    <SelectValue placeholder={t("Select customer type")} />
                                                                </SelectTrigger>
                                                                <SelectContent position="popper">
                                                                    <SelectItem value="INDIVIDUAL">{t("Individual")}</SelectItem>
                                                                    <SelectItem value="CORPORATE">{t("Corporate")}</SelectItem>
                                                                </SelectContent>
                                                            </Select>
                                                        </div>
                                                        <div className="flex flex-col space-y-1.5">
                                                            <Label htmlFor="trader">{t("Trader")}</Label>
                                                            <Select
                                                                value={selectedTraderId?.toString() || ""}
                                                                onValueChange={(value) => setSelectedTraderId(Number(value))}
                                                            >
                                                                <SelectTrigger>
                                                                    <SelectValue placeholder={t("Select trader")} />
                                                                </SelectTrigger>
                                                                <SelectContent position="popper">
                                                                    {Array.isArray(traders) && traders.length > 0 ? (
                                                                        traders.map((trader: any) => (
                                                                            <SelectItem key={trader.id} value={trader.id.toString()}>
                                                                                {trader.firstName} {trader.lastName}
                                                                            </SelectItem>
                                                                        ))
                                                                    ) : (
                                                                        <SelectItem value="" disabled>{t("No traders available")}</SelectItem>
                                                                    )}
                                                                </SelectContent>
                                                            </Select>
                                                        </div>
                                                    </div>

                                                    <div className="grid grid-cols-2 gap-4">
                                                        <div className="grid gap-2">
                                                            <Label htmlFor="first-name">
                                                                {customerType === "INDIVIDUAL" ? t("First Name") : t("Corporate Name")}
                                                            </Label>
                                                            <Input
                                                                id="first-name"
                                                                value={firstName}
                                                                onChange={(e) => setFirstName(e.target.value)}
                                                            />
                                                        </div>
                                                        {customerType === "INDIVIDUAL" && (
                                                            <div className="grid gap-2">
                                                                <Label htmlFor="last-name">{t("Last Name")}</Label>
                                                                <Input
                                                                    id="last-name"
                                                                    value={lastName}
                                                                    onChange={(e) => setLastName(e.target.value)}
                                                                />
                                                            </div>
                                                        )}
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
                                                        <Label htmlFor="identity-number">
                                                            {customerType === "INDIVIDUAL" ? t("TC Identity Number") : t("Tax Identity Number")}
                                                        </Label>
                                                        <Input
                                                            id="identity-number"
                                                            value={identityNumber}
                                                            onChange={(e) => {
                                                                const value = e.target.value.replace(/\D/g, '');
                                                                const maxLength = customerType === "INDIVIDUAL" ? 11 : 10;
                                                                if (value.length <= maxLength) {
                                                                    setIdentityNumber(value);
                                                                }
                                                            }}
                                                            placeholder={customerType === "INDIVIDUAL" ? t("11 digit TC Identity Number") : t("10 digit Tax Identity Number")}
                                                            maxLength={customerType === "INDIVIDUAL" ? 11 : 10}
                                                        />
                                                    </div>

                                                    <div className="grid gap-2">
                                                        <Label htmlFor="trading-permission">{t("Trading Permission")}</Label>
                                                        <Select
                                                            value={tradingPermission}
                                                            onValueChange={setTradingPermission}
                                                        >
                                                            <SelectTrigger id="trading-permission">
                                                                <SelectValue placeholder={t("Select permission")} />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                <SelectItem value="FULL">Full</SelectItem>
                                                                <SelectItem value="PARTICIPATION_ONLY">Participation Only</SelectItem>
                                                            </SelectContent>
                                                        </Select>
                                                    </div>
                                                </div>

                                                <DialogFooter>
                                                    <Button variant="outline" onClick={() => setDialogOpen(false)}>
                                                        {t("Cancel")}
                                                    </Button>
                                                    <LoadingButton
                                                        onClick={handleCreateCustomer}
                                                        loading={createLoading}
                                                    >
                                                        {t("Create")}
                                                    </LoadingButton>
                                                </DialogFooter>
                                            </DialogContent>
                                        </Dialog>
                                    )}
                                </div>

                                <DataTable
                                    data={filteredCustomers}
                                    columns={columns}
                                    pagination={pagination}
                                    setPagination={setPagination}
                                    loading={loading}
                                />
                            </div>
                        </div>
                    </div>
                </SidebarInset>
            </SidebarProvider>
            <Dialog open={detailsModalOpen} onOpenChange={setDetailsModalOpen}>
                <DialogContent className="sm:max-w-[800px] max-h-[80vh] overflow-y-auto">
                    <DialogHeader>
                        <DialogTitle>{t("Customer Details")}</DialogTitle>
                        <DialogDescription>
                            {t("Detailed information for")} {selectedCustomer?.firstName} {selectedCustomer?.lastName}
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-6">
                        <div className="grid grid-cols-2 gap-4 p-4 border rounded-lg">
                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">{t("Customer ID")}</Label>
                                <p className="text-sm">{selectedCustomer?.customerId}</p>
                            </div>
                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">{t("Name")}</Label>
                                <p className="text-sm">{selectedCustomer?.firstName} {selectedCustomer?.lastName}</p>
                            </div>
                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">{t("Email")}</Label>
                                <p className="text-sm">{selectedCustomer?.email}</p>
                            </div>
                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">{t("Identity Number")}</Label>
                                <p className="text-sm">{selectedCustomer?.identityNumber}</p>
                            </div>
                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">{t("Trading Permission")}</Label>
                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${selectedCustomer?.tradingPermission === "FULL"
                                    ? 'bg-blue-100 text-blue-800'
                                    : selectedCustomer?.tradingPermission === "PARTICIPATION_ONLY"
                                        ? 'bg-amber-100 text-amber-800'
                                        : 'bg-gray-100 text-gray-800'
                                    }`}>
                                    {selectedCustomer?.tradingPermission === "FULL" ? 'Full'
                                        : selectedCustomer?.tradingPermission === "PARTICIPATION_ONLY" ? 'Participation Only'
                                            : selectedCustomer?.tradingPermission}
                                </span>
                            </div>
                            <div>
                                <Label className="text-sm font-medium text-muted-foreground">{t("Trading Status")}</Label>
                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${selectedCustomer?.tradingEnabled
                                    ? 'bg-green-100 text-green-800'
                                    : 'bg-red-100 text-red-800'
                                    }`}>
                                    {selectedCustomer?.tradingEnabled ? 'Active' : 'Inactive'}
                                </span>
                            </div>
                        </div>

                        {selectedCustomer?.accounts && selectedCustomer.accounts.length > 0 && (
                            <div className="space-y-4">
                                <Label className="text-lg font-semibold">{t("Accounts & Portfolio")}</Label>
                                {selectedCustomer.accounts.map((account) => (
                                    <div key={account.accountId} className="border rounded-lg p-4 space-y-4">
                                        <div className="flex justify-between items-center">
                                            <div>
                                                <h3 className="font-medium">{t("Account")} #{account.accountId}</h3>
                                                <span className="bg-primary/10 text-primary px-2 py-1 rounded text-xs">
                                                    {account.accountType}
                                                </span>
                                            </div>
                                        </div>

                                        <div className="grid grid-cols-3 gap-4 p-3 bg-muted/50 rounded-md">
                                            <div>
                                                <Label className="text-xs text-muted-foreground">{t("Free Balance")}</Label>
                                                <p className="font-medium">{account.cashBalance.freeBalance.toLocaleString()} TL</p>
                                            </div>
                                            <div>
                                                <Label className="text-xs text-muted-foreground">{t("Blocked Balance")}</Label>
                                                <p className="font-medium">{account.cashBalance.blockedBalance.toLocaleString()} TL</p>
                                            </div>
                                            <div>
                                                <Label className="text-xs text-muted-foreground">{t("Total Balance")}</Label>
                                                <p className="font-medium text-primary">{account.cashBalance.totalBalance.toLocaleString()} TL</p>
                                            </div>
                                        </div>
                                        {account.equities && account.equities.length > 0 && (
                                            <div className="space-y-3">
                                                <Label className="text-sm font-medium">{t("Stock Portfolio")} ({account.equities.length} {t("positions")})</Label>
                                                <div className="border rounded-lg max-h-[300px] overflow-y-auto">
                                                    <table className="w-full">
                                                        <thead className="bg-muted/90 sticky top-0">
                                                            <tr>
                                                                <th className="text-left p-3 text-xs font-medium">{t("Symbol")}</th>
                                                                <th className="text-left p-3 text-xs font-medium">{t("Company")}</th>
                                                                <th className="text-right p-3 text-xs font-medium">{t("Quantity")}</th>
                                                                <th className="text-right p-3 text-xs font-medium">{t("Avg Cost")}</th>
                                                                <th className="text-right p-3 text-xs font-medium">{t("Current Price")}</th>
                                                                <th className="text-right p-3 text-xs font-medium">{t("Market Value")}</th>
                                                                <th className="text-right p-3 text-xs font-medium">{t("Profit/Loss")}</th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            {account.equities.map((equity: EquityType, equityIndex: number) => {
                                                                const currentValue = equity.closePrice ? equity.totalQuantity * equity.closePrice : 0;
                                                                const costBasis = equity.totalQuantity * equity.averageCost;
                                                                const profitLossAmount = equity.closePrice ? currentValue - costBasis : 0;
                                                                const isProfitable = profitLossAmount >= 0;

                                                                return (
                                                                    <tr key={equityIndex} className={equityIndex % 2 === 0 ? "bg-background" : "bg-muted/25"}>
                                                                        <td className="p-3 text-sm font-medium text-primary">{equity.assetCode}</td>
                                                                        <td className="p-3 text-xs text-muted-foreground max-w-[200px] truncate" title={equity.assetName}>
                                                                            {equity.assetName}
                                                                        </td>
                                                                        <td className="p-3 text-sm text-right">{equity.totalQuantity.toLocaleString()}</td>
                                                                        <td className="p-3 text-sm text-right">{equity.averageCost.toFixed(2)} TL</td>
                                                                        <td className="p-3 text-sm text-right">
                                                                            {equity.closePrice ? equity.closePrice.toFixed(2) : 'N/A'} TL
                                                                        </td>
                                                                        <td className="p-3 text-sm text-right font-medium">
                                                                            {equity.closePrice
                                                                                ? (equity.totalQuantity * equity.closePrice).toLocaleString()
                                                                                : (equity.totalQuantity * equity.averageCost).toLocaleString()} TL
                                                                        </td>
                                                                        <td className="p-3 text-sm text-right font-medium">
                                                                            <div className={isProfitable ? "text-green-600" : "text-red-600"}>
                                                                                {equity.closePrice ? (
                                                                                    <>
                                                                                        {profitLossAmount.toLocaleString()} TL
                                                                                        <br />
                                                                                        <span className="text-xs">
                                                                                            ({equity.profitLossPercentage.toFixed(2)}%)
                                                                                        </span>
                                                                                    </>
                                                                                ) : 'N/A'}
                                                                            </div>
                                                                        </td>
                                                                    </tr>
                                                                );
                                                            })}
                                                        </tbody>
                                                    </table>
                                                </div>
                                                <div className="text-xs text-muted-foreground text-right">
                                                    {t("Total Portfolio Value")}: {account.equities.reduce((total: number, equity: EquityType) =>
                                                        total + (equity.closePrice
                                                            ? equity.totalQuantity * equity.closePrice
                                                            : equity.totalQuantity * equity.averageCost), 0
                                                    ).toLocaleString()} TL
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setDetailsModalOpen(false)}>
                            {t("Close")}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* Edit Modal */}
            <Dialog open={editModalOpen} onOpenChange={setEditModalOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>{t("Edit Customer")}</DialogTitle>
                    </DialogHeader>
                    {selectedCustomer && (
                        <div className="grid gap-4 py-4">
                            <div className="grid gap-2">
                                <Label>{t("First Name")}</Label>
                                <Input value={selectedCustomer.firstName} onChange={(e) => setSelectedCustomer({ ...selectedCustomer, firstName: e.target.value })} />
                            </div>
                            <div className="grid gap-2">
                                <Label>{t("Last Name")}</Label>
                                <Input value={selectedCustomer.lastName} onChange={(e) => setSelectedCustomer({ ...selectedCustomer, lastName: e.target.value })} />
                            </div>
                            <div className="grid gap-2">
                                <Label>{t("Email")}</Label>
                                <Input value={selectedCustomer.email} onChange={(e) => setSelectedCustomer({ ...selectedCustomer, email: e.target.value })} />
                            </div>
                            <div className="grid gap-2">
                                <Label>{t("Identity Number")}</Label>
                                <Input value={selectedCustomer.identityNumber} onChange={(e) => setSelectedCustomer({ ...selectedCustomer, identityNumber: e.target.value })} />
                            </div>
                        </div>
                    )}
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setEditModalOpen(false)}>
                            {t("Cancel")}
                        </Button>
                        <LoadingButton
                            onClick={async () => {
                                try {
                                    await sendRequest.put(`/customers/${selectedCustomer?.customerId}`, selectedCustomer)
                                    setEditModalOpen(false)
                                    fetchCustomers()
                                } catch (error: any) {
                                    setError("Error editing customer:" + error.message)
                                }
                            }} loading={false}                        >
                            {t("Save")}
                        </LoadingButton>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={statusDialogOpen} onOpenChange={setStatusDialogOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>
                            {selectedCustomer?.tradingEnabled ? t('Deactivate Customer') : t('Activate Customer')}
                        </DialogTitle>
                        <DialogDescription>
                            {selectedCustomer?.tradingEnabled
                                ? t(`Are you sure you want to deactivate ${selectedCustomer?.firstName} ${selectedCustomer?.lastName}? They will not be able to trade while deactivated.`)
                                : t(`Are you sure you want to activate ${selectedCustomer?.firstName} ${selectedCustomer?.lastName}? This will enable their trading capabilities.`)}
                        </DialogDescription>
                    </DialogHeader>

                    {statusError && (
                        <div className="bg-red-50 text-red-800 p-3 rounded-md text-sm mb-4">
                            {statusError}
                        </div>
                    )}

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setStatusDialogOpen(false)}>
                            {t("Cancel")}
                        </Button>
                        <LoadingButton
                            onClick={handleCustomerStatusChange}
                            loading={statusLoading}
                            className={selectedCustomer?.tradingEnabled ? "bg-destructive hover:bg-destructive/90 text-destructive-foreground" : ""}
                        >
                            {selectedCustomer?.tradingEnabled ? 'Deactivate' : 'Activate'}
                        </LoadingButton>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={accountDetailsDialogOpen} onOpenChange={setAccountDetailsDialogOpen}>
                <DialogContent className="sm:max-w-[600px]">
                    <DialogHeader className="flex flex-row items-center justify-between">
                        <div>
                            <DialogTitle>{t("Account Details")}</DialogTitle>
                            <DialogDescription>
                                {selectedCustomer?.firstName} {selectedCustomer?.lastName} &apos;s {t("accounts")}
                            </DialogDescription>
                        </div>
                        <Button onClick={() => setAccountDialogOpen(true)}>{t("Create Account")}</Button>
                    </DialogHeader>

                    {accountError && (
                        <div className="bg-red-50 text-red-800 p-3 rounded-md text-sm mb-4">
                            {accountError}
                        </div>
                    )}

                    <div className="max-h-[400px] overflow-y-auto pr-2">
                        {accountLoading ? (
                            <div className="flex justify-center py-8">
                                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
                            </div>
                        ) : accounts.length === 0 ? (
                            <div className="text-center py-8 text-muted-foreground">
                                {t("No accounts found for this customer.")}
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {accounts.map((account) => (
                                    <div key={account.accountId} className="border rounded-md p-4">
                                        <div className="flex justify-between items-center mb-2">
                                            <div>
                                                <h3 className="font-medium">{t("Account")} #{account.accountId}</h3>
                                                <span className="bg-primary/10 text-primary px-2 py-1 rounded text-xs">
                                                    {account.accountType}
                                                </span>
                                                <Button
                                                    variant="destructive"
                                                    size="sm"
                                                    onClick={() => {
                                                        setAccountToDelete(account);
                                                        setDeleteDialogOpen(true);
                                                    }}
                                                >
                                                    {t("Delete")}
                                                </Button>                                            </div>
                                        </div>
                                        <div className="grid grid-cols-3 gap-2 text-sm">
                                            <div>
                                                <p className="text-muted-foreground">{t("Free Balance")}</p>
                                                <p className="font-medium">{account.cashBalance.freeBalance.toLocaleString()} TL</p>
                                            </div>
                                            <div>
                                                <p className="text-muted-foreground">{t("Blocked Balance")}</p>
                                                <p className="font-medium">{account.cashBalance.blockedBalance.toLocaleString()} TL</p>
                                            </div>
                                            <div>
                                                <p className="text-muted-foreground">{t("Total Balance")}</p>
                                                <p className="font-medium">{account.cashBalance.totalBalance.toLocaleString()} TL</p>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setAccountDetailsDialogOpen(false)}>
                            {t("Close")}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
            <Dialog open={accountDialogOpen} onOpenChange={setAccountDialogOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>{t("Create New Account")}</DialogTitle>
                        <DialogDescription>
                            {t("Create a new account for")} {selectedCustomer?.firstName} {selectedCustomer?.lastName}.
                        </DialogDescription>
                    </DialogHeader>

                    {accountError && (
                        <div className="bg-red-50 text-red-800 p-3 rounded-md text-sm mb-4">
                            {accountError}
                        </div>
                    )}

                    <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                            <Label htmlFor="account-type">{t("Account Type")}</Label>
                            <Select
                                value={accountType}
                                onValueChange={setAccountType}
                            >
                                <SelectTrigger id="account-type">
                                    <SelectValue placeholder="Select account type" />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="INDIVIDUAL">{t("Individual")}</SelectItem>
                                    <SelectItem value="CORPORATE">{t("Corporate")}</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setAccountDialogOpen(false)}>
                            {t("Cancel")}
                        </Button>
                        <LoadingButton
                            onClick={handleCreateAccount}
                            loading={accountLoading}
                        >
                            {t("Create Account")}
                        </LoadingButton>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
            <Dialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>{t("Delete Account")}</DialogTitle>
                        <DialogDescription>
                            {t("Are you sure you want to delete account")} #{accountToDelete?.accountId}? {t("This action cannot be undone.")}
                        </DialogDescription>
                    </DialogHeader>

                    {deleteError && (
                        <div className="bg-red-50 text-red-800 p-3 rounded-md text-sm mb-4">
                            {deleteError}
                        </div>
                    )}

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setDeleteDialogOpen(false)}>
                            {t("Cancel")}
                        </Button>
                        <LoadingButton
                            onClick={handleDeleteAccount}
                            loading={deleteLoading}
                            className="bg-destructive hover:bg-destructive/90 text-destructive-foreground"
                        >
                            {t("Delete Account")}
                        </LoadingButton>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
            {loading && <LoadingDialog isOpen={loading} />}
            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>{t("Error")}</AlertTitle>
                    <AlertDescription>
                        {error}
                    </AlertDescription>
                </Alert>
            )}

        </div>
    )
}
