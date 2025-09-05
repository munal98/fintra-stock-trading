"use client"

import { useState, useEffect } from "react"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { AlertCircle, ArrowDown, ArrowUp, ArrowRight } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { LoadingButton } from "@/components/ui/loading"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import {
    SidebarInset,
    SidebarProvider,
} from "@/components/ui/sidebar"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import initSendRequest from "@/configs/sendRequest"
import { CustomerSearch, CustomerType } from "@/components/ui/customer-search"
import { CustomerSearchTrader } from "@/components/customer-search-trader"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'


export default function CashTransfers() {
    const { t } = useTranslation()
    const sendRequest = initSendRequest()
    const [userRole, setUserRole] = useState<string>('')
    const [depositAmount, setDepositAmount] = useState<string>("")
    const [depositAccount, setDepositAccount] = useState<string>("")
    const [depositLoading, setDepositLoading] = useState<boolean>(false)
    const [depositSuccess, setDepositSuccess] = useState<boolean>(false)
    const [depositError, setDepositError] = useState<string | null>(null)
    const [selectedDepositCustomer, setSelectedDepositCustomer] = useState<CustomerType | null>(null)
    const [depositCustomerAccounts, setDepositCustomerAccounts] = useState<CustomerType["accounts"]>([])
    const [fetchingDepositAccounts, setFetchingDepositAccounts] = useState<boolean>(false)

    const [withdrawAmount, setWithdrawAmount] = useState<string>("")
    const [withdrawAccount, setWithdrawAccount] = useState<string>("")
    const [withdrawLoading, setWithdrawLoading] = useState<boolean>(false)
    const [withdrawSuccess, setWithdrawSuccess] = useState<boolean>(false)
    const [withdrawError, setWithdrawError] = useState<string | null>(null)
    const [selectedWithdrawCustomer, setSelectedWithdrawCustomer] = useState<CustomerType | null>(null)
    const [withdrawCustomerAccounts, setWithdrawCustomerAccounts] = useState<CustomerType["accounts"]>([])
    const [fetchingWithdrawAccounts, setFetchingWithdrawAccounts] = useState<boolean>(false)

    const [transferAmount, setTransferAmount] = useState<string>("")
    const [sourceAccount, setSourceAccount] = useState<string>("")
    const [targetAccount, setTargetAccount] = useState<string>("")
    const [transferLoading, setTransferLoading] = useState<boolean>(false)
    const [transferSuccess, setTransferSuccess] = useState<boolean>(false)
    const [transferError, setTransferError] = useState<string | null>(null)
    const [selectedSourceCustomer, setSelectedSourceCustomer] = useState<CustomerType | null>(null)
    const [selectedTargetCustomer, setSelectedTargetCustomer] = useState<CustomerType | null>(null)
    const [sourceCustomerAccounts, setSourceCustomerAccounts] = useState<CustomerType["accounts"]>([])
    const [targetCustomerAccounts, setTargetCustomerAccounts] = useState<CustomerType["accounts"]>([])
    const [fetchingSourceAccounts, setFetchingSourceAccounts] = useState<boolean>(false)
    const [fetchingTargetAccounts, setFetchingTargetAccounts] = useState<boolean>(false)

    useEffect(() => {
        const storedRole = localStorage.getItem('role')
        if (storedRole) {
            setUserRole(storedRole)
        }
    }, [])
    
    const handleDeposit = async () => {
        setDepositLoading(true)
        setDepositSuccess(false)
        setDepositError(null)
    
        try {
            if (!depositAmount || parseFloat(depositAmount) <= 0) {
                throw new Error("Please enter a valid amount")
            }
            if (!selectedDepositCustomer) {
                throw new Error("Please select a customer")
            }
            if (!depositAccount) {
                throw new Error("Please select an account")
            }
            
            await sendRequest.post("/cash/deposit", {
                accountId: parseInt(depositAccount),
                amount: parseFloat(depositAmount)
            })
            
            setDepositSuccess(true)
            setDepositAmount("")
            
            if (selectedDepositCustomer) {
                setFetchingDepositAccounts(true)
                try {
                    const customerDetails = await fetchCustomerDetails(selectedDepositCustomer.customerId)
                    if (customerDetails) {
                        setSelectedDepositCustomer(customerDetails)
                        setDepositCustomerAccounts(customerDetails.accounts || [])
                    }
                } catch (error) {
                    console.error("Error re-fetching deposit customer accounts:", error)
                } finally {
                    setFetchingDepositAccounts(false)
                }
            }
        } catch (error) {
            setDepositError(error instanceof Error ? error.message : "An error occurred")
        } finally {
            setDepositLoading(false)
        }
    }

    const handleWithdraw = async () => {
        setWithdrawLoading(true)
        setWithdrawSuccess(false)
        setWithdrawError(null)
    
        try {
            if (!withdrawAmount || parseFloat(withdrawAmount) <= 0) {
                throw new Error("Please enter a valid amount")
            }
            if (!selectedWithdrawCustomer) {
                throw new Error("Please select a customer")
            }
            if (!withdrawAccount) {
                throw new Error("Please select an account")
            }
    
            await sendRequest.post("/cash/withdraw", {
                accountId: parseInt(withdrawAccount),
                amount: parseFloat(withdrawAmount)
            })
            
            setWithdrawSuccess(true)
            setWithdrawAmount("")
            
            if (selectedWithdrawCustomer) {
                setFetchingWithdrawAccounts(true)
                try {
                    const customerDetails = await fetchCustomerDetails(selectedWithdrawCustomer.customerId)
                    if (customerDetails) {
                        setSelectedWithdrawCustomer(customerDetails)
                        setWithdrawCustomerAccounts(customerDetails.accounts || [])
                    }
                } catch (error) {
                    console.error("Error re-fetching withdraw customer accounts:", error)
                } finally {
                    setFetchingWithdrawAccounts(false)
                }
            }
        } catch (error) {
            setWithdrawError(error instanceof Error ? error.message : "An error occurred")
        } finally {
            setWithdrawLoading(false)
        }
    }

    const handleTransfer = async () => {
        setTransferLoading(true)
        setTransferSuccess(false)
        setTransferError(null)
    
        try {
            if (!transferAmount || parseFloat(transferAmount) <= 0) {
                throw new Error("Please enter a valid amount")
            }
            if (!selectedSourceCustomer) {
                throw new Error("Please select a source customer")
            }
            if (!selectedTargetCustomer) {
                throw new Error("Please select a target customer")
            }
            if (!sourceAccount) {
                throw new Error("Please select a source account")
            }
            if (!targetAccount) {
                throw new Error("Please select a target account")
            }
            if (sourceAccount === targetAccount) {
                throw new Error("Source and target accounts cannot be the same")
            }
    
            await sendRequest.post("/cash/transfer", {
                senderAccountId: parseInt(sourceAccount),
                receiverAccountId: parseInt(targetAccount),
                amount: parseFloat(transferAmount)
            })
            
            setTransferSuccess(true)
            setTransferAmount("")
            
            // Re-fetch source customer accounts after transfer
            if (selectedSourceCustomer) {
                setFetchingSourceAccounts(true)
                try {
                    const customerDetails = await fetchCustomerDetails(selectedSourceCustomer.customerId)
                    if (customerDetails) {
                        setSelectedSourceCustomer(customerDetails)
                        setSourceCustomerAccounts(customerDetails.accounts || [])
                    }
                } catch (error) {
                    console.error("Error re-fetching source customer accounts:", error)
                } finally {
                    setFetchingSourceAccounts(false)
                }
            }
            if (selectedTargetCustomer) {
                setFetchingTargetAccounts(true)
                try {
                    const customerDetails = await fetchCustomerDetails(selectedTargetCustomer.customerId)
                    if (customerDetails) {
                        setSelectedTargetCustomer(customerDetails)
                        setTargetCustomerAccounts(customerDetails.accounts || [])
                    }
                } catch (error) {
                    console.error("Error re-fetching target customer accounts:", error)
                } finally {
                    setFetchingTargetAccounts(false)
                }
            }
        } catch (error) {
            setTransferError(error instanceof Error ? error.message : "An error occurred")
        } finally {
            setTransferLoading(false)
        }
    }

    const fetchCustomerDetails = async (customerId: number) => {
        try {
            const response = await sendRequest.get(`/customers/${customerId}`)
            return response.data
        } catch (error) {
            console.error(`Error fetching customer details for ID ${customerId}:`, error)
            return null
        }
    }

    const handleDepositCustomerSelect = async (customer: CustomerType | null) => {
        setSelectedDepositCustomer(customer)
        setDepositAccount("")
        setDepositCustomerAccounts([])
        
        if (customer) {
            setFetchingDepositAccounts(true)
            try {
                const customerDetails = await fetchCustomerDetails(customer.customerId)
                if (customerDetails) {
                    setSelectedDepositCustomer(customerDetails)
                    setDepositCustomerAccounts(customerDetails.accounts || [])
                }
            } catch (error) {
                console.error("Error fetching deposit customer accounts:", error)
            } finally {
                setFetchingDepositAccounts(false)
            }
        }
    }

    const handleWithdrawCustomerSelect = async (customer: CustomerType | null) => {
        setSelectedWithdrawCustomer(customer)
        setWithdrawAccount("")
        setWithdrawCustomerAccounts([])
        
        if (customer) {
            setFetchingWithdrawAccounts(true)
            try {
                const customerDetails = await fetchCustomerDetails(customer.customerId)
                if (customerDetails) {
                    setSelectedWithdrawCustomer(customerDetails)
                    setWithdrawCustomerAccounts(customerDetails.accounts || [])
                }
            } catch (error) {
                console.error("Error fetching withdraw customer accounts:", error)
            } finally {
                setFetchingWithdrawAccounts(false)
            }
        }
    }

    const handleSourceCustomerSelect = async (customer: CustomerType | null) => {
        setSelectedSourceCustomer(customer)
        setSourceAccount("")
        setSourceCustomerAccounts([])
        
        if (customer) {
            setFetchingSourceAccounts(true)
            try {
                const customerDetails = await fetchCustomerDetails(customer.customerId)
                if (customerDetails) {
                    setSelectedSourceCustomer(customerDetails)
                    setSourceCustomerAccounts(customerDetails.accounts || [])
                }
            } catch (error) {
                console.error("Error fetching source customer accounts:", error)
            } finally {
                setFetchingSourceAccounts(false)
            }
        }
    }

    const handleTargetCustomerSelect = async (customer: CustomerType | null) => {
        setSelectedTargetCustomer(customer)
        setTargetAccount("")
        setTargetCustomerAccounts([])
        
        if (customer) {
            setFetchingTargetAccounts(true)
            try {
                const customerDetails = await fetchCustomerDetails(customer.customerId)
                if (customerDetails) {
                    setSelectedTargetCustomer(customerDetails)
                    setTargetCustomerAccounts(customerDetails.accounts || [])
                }
            } catch (error) {
                console.error("Error fetching target customer accounts:", error)
            } finally {
                setFetchingTargetAccounts(false)
            }
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
                    <SiteHeader title={t("Cash Transactions")} />
                    <div className="flex flex-1 flex-col">
                        <div className="@container/main flex flex-1 flex-col gap-2">
                            <div className="container px-4 mx-auto py-4 md:py-6">
                                <Tabs defaultValue="deposit" className="w-full">
                                    <TabsList className="grid w-full grid-cols-3">
                                        <TabsTrigger value="deposit" className="flex items-center gap-2">
                                            <ArrowDown className="h-4 w-4" />
                                            {t("Deposit")}
                                        </TabsTrigger>
                                        <TabsTrigger value="withdraw" className="flex items-center gap-2">
                                            <ArrowUp className="h-4 w-4" />
                                            {t("Withdraw")}
                                        </TabsTrigger>
                                        <TabsTrigger value="transfer" className="flex items-center gap-2">
                                            <ArrowRight className="h-4 w-4" />
                                            Transfer
                                        </TabsTrigger>
                                    </TabsList>
                                    <TabsContent value="deposit">
                                        <Card>
                                            <CardHeader>
                                                <CardTitle>{t("Deposit Funds")}</CardTitle>
                                                <CardDescription>{t("Add funds to a customer account")}</CardDescription>
                                            </CardHeader>
                                            <CardContent className="space-y-4">
                                                {depositSuccess && (
                                                    <Alert className="bg-green-50 text-green-800 border-green-200">
                                                        <AlertCircle className="h-4 w-4" />
                                                        <AlertTitle>{t("Success")}</AlertTitle>
                                                        <AlertDescription>
                                                            {t("Funds have been deposited successfully.")}
                                                        </AlertDescription>
                                                    </Alert>
                                                )}
                                                {depositError && (
                                                    <Alert variant="destructive">
                                                        <AlertCircle className="h-4 w-4" />
                                                        <AlertTitle>{t("Error")}</AlertTitle>
                                                        <AlertDescription>
                                                            {depositError}
                                                        </AlertDescription>
                                                    </Alert>
                                                )}

                                                {(userRole === 'ROLE_TRADER') ? (
                                                    <CustomerSearchTrader
                                                        onSelect={handleDepositCustomerSelect}
                                                        selectedCustomer={selectedDepositCustomer}
                                                        placeholder={t("Search for a customer...")}
                                                        disabled={depositLoading}
                                                    />
                                                ) : (
                                                    <CustomerSearch 
                                                        onSelect={handleDepositCustomerSelect}
                                                        selectedCustomer={selectedDepositCustomer}
                                                        placeholder={t("Search for a customer...")}
                                                        disabled={depositLoading}
                                                    />
                                                )}

                                                <div className="grid gap-2">
                                                    <Label htmlFor="deposit-account">{t("Account")}</Label>
                                                    <Select
                                                        disabled={!selectedDepositCustomer || depositLoading || fetchingDepositAccounts}
                                                        value={depositAccount}
                                                        onValueChange={setDepositAccount}
                                                    >
                                                        <SelectTrigger id="deposit-account">
                                                            <SelectValue placeholder={fetchingDepositAccounts ? t("Loading accounts...") : t("Select account")} />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {depositCustomerAccounts && depositCustomerAccounts.map(account => (
                                                                <SelectItem 
                                                                    key={account.accountId} 
                                                                    value={account.accountId.toString()}
                                                                >
                                                                    {account.accountType} - {t("Balance")}: ₺{account.cashBalance.totalBalance.toLocaleString()}
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>
                                                </div>

                                                <div className="grid gap-2">
                                                    <Label htmlFor="deposit-amount">{t("Amount")}</Label>
                                                    <div className="relative">
                                                        <span className="absolute left-3 top-1/2 -translate-y-1/2">₺</span>
                                                        <Input
                                                            id="deposit-amount"
                                                            type="number"
                                                            placeholder="0.00"
                                                            className="pl-8"
                                                            value={depositAmount}
                                                            onChange={(e) => setDepositAmount(e.target.value)}
                                                            disabled={!selectedDepositCustomer || depositLoading}
                                                            min="0"
                                                            onKeyDown={(e) => {
                                                                if (e.key === '-') {
                                                                    e.preventDefault();
                                                                }
                                                            }}
                                                        />
                                                    </div>
                                                </div>
                                            </CardContent>
                                            <CardFooter>
                                                <LoadingButton 
                                                    onClick={handleDeposit} 
                                                    loading={depositLoading}
                                                    className="w-full"
                                                    disabled={!selectedDepositCustomer || !depositAccount}
                                                >
                                                    {t("Deposit Funds")}
                                                </LoadingButton>
                                            </CardFooter>
                                        </Card>
                                    </TabsContent>
                                    <TabsContent value="withdraw">
                                        <Card>
                                            <CardHeader>
                                                <CardTitle>{t("Withdraw Funds")}</CardTitle>
                                                <CardDescription>{t("Withdraw funds from a customer account")}</CardDescription>
                                            </CardHeader>
                                            <CardContent className="space-y-4">
                                                {withdrawSuccess && (
                                                    <Alert className="bg-green-50 text-green-800 border-green-200">
                                                        <AlertCircle className="h-4 w-4" />
                                                        <AlertTitle>{t("Success")}</AlertTitle>
                                                        <AlertDescription>
                                                            {t("Funds have been withdrawn successfully.")}
                                                        </AlertDescription>
                                                    </Alert>
                                                )}
                                                {withdrawError && (
                                                    <Alert variant="destructive">
                                                        <AlertCircle className="h-4 w-4" />
                                                        <AlertTitle>{t("Error")}</AlertTitle>
                                                        <AlertDescription>
                                                            {withdrawError}
                                                        </AlertDescription>
                                                    </Alert>
                                                )}

                                                {(userRole === 'ROLE_TRADER') ? (
                                                    <CustomerSearchTrader
                                                        onSelect={handleWithdrawCustomerSelect}
                                                        selectedCustomer={selectedWithdrawCustomer}
                                                        placeholder={t("Search for a customer...")}
                                                        disabled={withdrawLoading}
                                                    />
                                                ) : (
                                                    <CustomerSearch 
                                                        onSelect={handleWithdrawCustomerSelect}
                                                        selectedCustomer={selectedWithdrawCustomer}
                                                        placeholder={t("Search for a customer...")}
                                                        disabled={withdrawLoading}
                                                    />
                                                )}

                                                <div className="grid gap-2">
                                                    <Label htmlFor="withdraw-account">{t("Account")}</Label>
                                                    <Select
                                                        disabled={!selectedWithdrawCustomer || withdrawLoading || fetchingWithdrawAccounts}
                                                        value={withdrawAccount}
                                                        onValueChange={setWithdrawAccount}
                                                    >
                                                        <SelectTrigger id="withdraw-account">
                                                            <SelectValue placeholder={fetchingWithdrawAccounts ? t("Loading accounts...") : t("Select account")} />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {withdrawCustomerAccounts && withdrawCustomerAccounts.map(account => (
                                                                <SelectItem 
                                                                    key={account.accountId} 
                                                                    value={account.accountId.toString()}
                                                                >
                                                                    {account.accountType} - Balance: ₺{account.cashBalance.totalBalance.toLocaleString()}
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>
                                                </div>

                                                <div className="grid gap-2">
                                                    <Label htmlFor="withdraw-amount">{t("Amount")}</Label>
                                                    <div className="relative">
                                                        <span className="absolute left-3 top-1/2 -translate-y-1/2">₺</span>
                                                        <Input
                                                            id="withdraw-amount"
                                                            type="number"
                                                            placeholder="0.00"
                                                            className="pl-8"
                                                            value={withdrawAmount}
                                                            onChange={(e) => setWithdrawAmount(e.target.value)}
                                                            disabled={!selectedWithdrawCustomer || withdrawLoading}
                                                            min="0"
                                                            onKeyDown={(e) => {
                                                                if (e.key === '-') {
                                                                    e.preventDefault();
                                                                }
                                                            }}
                                                        />
                                                    </div>
                                                </div>
                                            </CardContent>
                                            <CardFooter>
                                                <LoadingButton 
                                                    onClick={handleWithdraw} 
                                                    loading={withdrawLoading}
                                                    className="w-full"
                                                    disabled={!selectedWithdrawCustomer || !withdrawAccount}
                                                >
                                                    {t("Withdraw Funds")}
                                                </LoadingButton>
                                            </CardFooter>
                                        </Card>
                                    </TabsContent>
                                    <TabsContent value="transfer">
                                        <Card>
                                            <CardHeader>
                                                <CardTitle>{t("Transfer Funds")}</CardTitle>
                                                <CardDescription>{t("Transfer funds between accounts")}</CardDescription>
                                            </CardHeader>
                                            <CardContent className="space-y-4">
                                                {transferSuccess && (
                                                    <Alert className="bg-green-50 text-green-800 border-green-200">
                                                        <AlertCircle className="h-4 w-4" />
                                                        <AlertTitle>{t("Success")}</AlertTitle>
                                                        <AlertDescription>
                                                            {t("Funds have been transferred successfully.")}
                                                        </AlertDescription>
                                                    </Alert>
                                                )}
                                                {transferError && (
                                                    <Alert variant="destructive">
                                                        <AlertCircle className="h-4 w-4" />
                                                        <AlertTitle>{t("Error")}</AlertTitle>
                                                        <AlertDescription>
                                                            {transferError}
                                                        </AlertDescription>
                                                    </Alert>
                                                )}

                                                <div className="space-y-4">
                                                    <div>
                                                        {(userRole === 'ROLE_TRADER') ? (
                                                            <CustomerSearchTrader
                                                                onSelect={handleSourceCustomerSelect}
                                                                selectedCustomer={selectedSourceCustomer}
                                                                placeholder={t("Search for source customer...")}
                                                                disabled={transferLoading}
                                                            />
                                                        ) : (
                                                            <CustomerSearch 
                                                                onSelect={handleSourceCustomerSelect}
                                                                selectedCustomer={selectedSourceCustomer}
                                                                placeholder={t("Search for source customer...")}
                                                                disabled={transferLoading}
                                                            />
                                                        )}
                                                    </div>

                                                    <div className="grid gap-2">
                                                        <Label htmlFor="source-account">{t("Source Account")}</Label>
                                                        <Select
                                                            disabled={!selectedSourceCustomer || transferLoading || fetchingSourceAccounts}
                                                            value={sourceAccount}
                                                            onValueChange={setSourceAccount}
                                                        >
                                                            <SelectTrigger id="source-account">
                                                                <SelectValue placeholder={fetchingSourceAccounts ? t("Loading accounts...") : t("Select source account")} />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                {sourceCustomerAccounts && sourceCustomerAccounts.map(account => (
                                                                    <SelectItem 
                                                                        key={account.accountId} 
                                                                        value={account.accountId.toString()}
                                                                    >
                                                                        {account.accountType} - {t("Balance")}: ₺{account.cashBalance.totalBalance.toLocaleString()}
                                                                    </SelectItem>
                                                                ))}
                                                            </SelectContent>
                                                        </Select>
                                                    </div>

                                                    <div className="flex justify-center my-2">
                                                        <ArrowDown className="h-6 w-6 text-muted-foreground" />
                                                    </div>

                                                    <div>
                                                        {(userRole === 'ROLE_TRADER') ? (
                                                            <CustomerSearchTrader
                                                                onSelect={handleTargetCustomerSelect}
                                                                selectedCustomer={selectedTargetCustomer}
                                                                placeholder={t("Search for target customer...")}
                                                                disabled={transferLoading}
                                                            />
                                                        ) : (
                                                            <CustomerSearch 
                                                                onSelect={handleTargetCustomerSelect}
                                                                selectedCustomer={selectedTargetCustomer}
                                                                placeholder={t("Search for target customer...")}
                                                                disabled={transferLoading}
                                                            />
                                                        )}
                                                    </div>

                                                    <div className="grid gap-2">
                                                        <Label htmlFor="target-account">{t("Target Account")}</Label>
                                                        <Select
                                                            disabled={!selectedTargetCustomer || transferLoading || fetchingTargetAccounts}
                                                            value={targetAccount}
                                                            onValueChange={setTargetAccount}
                                                        >
                                                            <SelectTrigger id="target-account">
                                                                <SelectValue placeholder={fetchingTargetAccounts ? t("Loading accounts...") : t("Select target account")} />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                {targetCustomerAccounts && targetCustomerAccounts.map(account => (
                                                                    <SelectItem 
                                                                        key={account.accountId} 
                                                                        value={account.accountId.toString()}
                                                                    >
                                                                        {account.accountType} - {t("Balance")}: ₺{account.cashBalance.totalBalance.toLocaleString()}
                                                                    </SelectItem>
                                                                ))}
                                                            </SelectContent>
                                                        </Select>
                                                    </div>

                                                    <div className="grid gap-2">
                                                        <Label htmlFor="transfer-amount">{t("Amount")}</Label>
                                                        <div className="relative">
                                                            <span className="absolute left-3 top-1/2 -translate-y-1/2">₺</span>
                                                            <Input
                                                                id="transfer-amount"
                                                                type="number"
                                                                placeholder="0.00"
                                                                className="pl-8"
                                                                value={transferAmount}
                                                                onChange={(e) => setTransferAmount(e.target.value)}
                                                                disabled={!selectedSourceCustomer || !selectedTargetCustomer || transferLoading}
                                                                min="0"
                                                                onKeyDown={(e) => {
                                                                    if (e.key === '-') {
                                                                        e.preventDefault();
                                                                    }
                                                                }}
                                                            />
                                                        </div>
                                                    </div>
                                                </div>
                                            </CardContent>
                                            <CardFooter>
                                                <LoadingButton 
                                                    onClick={handleTransfer} 
                                                    loading={transferLoading}
                                                    className="w-full"
                                                    disabled={!selectedSourceCustomer || !selectedTargetCustomer || !sourceAccount || !targetAccount}
                                                >
                                                    {t("Transfer Funds")}
                                                </LoadingButton>
                                            </CardFooter>
                                        </Card>
                                    </TabsContent>
                                </Tabs>
                            </div>
                        </div>
                    </div>
                </SidebarInset>
            </SidebarProvider>
            {(depositLoading || withdrawLoading || transferLoading) && 
                <LoadingDialog isOpen={depositLoading || withdrawLoading || transferLoading} />
            }
        </div>
    );
}