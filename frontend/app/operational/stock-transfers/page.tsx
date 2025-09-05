/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable react-hooks/exhaustive-deps */
"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { AlertCircle, TrendingUp, CheckCircle2 } from "lucide-react"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import initSendRequest from "@/configs/sendRequest"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import { CustomerSearch, CustomerType } from "@/components/ui/customer-search"
import { CustomerSearchTrader } from "@/components/customer-search-trader"
import { EquitySearch, EquityType } from "@/components/ui/equity-search"
import axios from "axios"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'

interface StockTransferFormData {
    sourcePortfolio: string
    targetPortfolio: string
    stockSymbol: string
    stockName: string
    quantity: string
    description: string
}
interface Equity {
    id: string
    equityId: number
    symbol: string
    name: string
    quantity: number
    price: number
    totalValue: number
}

export default function StockTransfers() {
    const { t } = useTranslation()
    const sendRequest = initSendRequest()
    const [activeTab, setActiveTab] = useState<string>("portfolio-to-portfolio")
    const [isLoading, setIsLoading] = useState<boolean>(false)
    const [senderCustomer, setSenderCustomer] = useState<CustomerType | null>(null)
    const [receiverCustomer, setReceiverCustomer] = useState<CustomerType | null>(null)
    const [availableEquities, setAvailableEquities] = useState<Equity[]>([])
    const [customerError, setCustomerError] = useState<string | null>(null)
    const [successMessage, setSuccessMessage] = useState<string | null>(null)
    const [formData, setFormData] = useState({
        sourcePortfolio: "",
        targetPortfolio: "",
        stockSymbol: "",
        stockName: "",
        quantity: "",
    })
    const [externalCustomer, setExternalCustomer] = useState<CustomerType | null>(null)
    const [externalAccount, setExternalAccount] = useState<string>("")
    const [externalEquity, setExternalEquity] = useState<string>("")
    const [externalQuantity, setExternalQuantity] = useState<string>("")
    const [externalInstitution, setExternalInstitution] = useState<string>("")
    const [externalInstitutions, setExternalInstitutions] = useState<any[]>([])
    const [externalAvailableEquities, setExternalAvailableEquities] = useState<Equity[]>([])
    const [externalLoading, setExternalLoading] = useState<boolean>(false)
    const [externalError, setExternalError] = useState<string | null>(null)
    const [externalSuccess, setExternalSuccess] = useState<string | null>(null)
    const [extToPortCustomer, setExtToPortCustomer] = useState<CustomerType | null>(null)
    const [extToPortAccount, setExtToPortAccount] = useState<string>("")
    const [extToPortInstitution, setExtToPortInstitution] = useState<string>("")
    const [extToPortEquity, setExtToPortEquity] = useState<EquityType | null>(null)
    const [extToPortQuantity, setExtToPortQuantity] = useState<string>("")
    const [extToPortAvgCost, setExtToPortAvgCost] = useState<string>("")
    const [extToPortLoading, setExtToPortLoading] = useState<boolean>(false)
    const [extToPortError, setExtToPortError] = useState<string | null>(null)
    const [extToPortSuccess, setExtToPortSuccess] = useState<string | null>(null)
    const [userRole, setUserRole] = useState<string>("")

    useEffect(() => {
        fetchInstitutions()
        const storedRole = localStorage.getItem('role')
        if (storedRole) {
            setUserRole(storedRole)
        }
    }, [])

    const fetchInstitutions = async () => {
        try {
            const response = await sendRequest.get("/other-institution")
            if (response.status === 200) {
                const institutionsData = Array.isArray(response.data) ? response.data : []
                setExternalInstitutions(institutionsData)
            }
        } catch (error) {
            console.error('Error fetching institutions:', error)
            setExternalInstitutions([])
        }
    }

    const handleInputChange = (field: keyof StockTransferFormData, value: string) => {
        setFormData(prev => ({ ...prev, [field]: value }))
    }

    const handleSourcePortfolioSelect = async (accountId: string) => {
        handleInputChange("sourcePortfolio", accountId)
        if (senderCustomer) {
            if (senderCustomer.accounts) {
                const account = senderCustomer.accounts.find(a => a.accountId.toString() === accountId)
                if (account) {
                    setAvailableEquities(account.equities.map((equity: { assetCode: any; equityId: any; assetName: any; totalQuantity: number; closePrice: any }) => ({
                        id: equity.assetCode,
                        equityId: equity.equityId,
                        symbol: equity.assetCode,
                        name: equity.assetName,
                        quantity: equity.totalQuantity,
                        price: equity.closePrice || 0,
                        totalValue: equity.totalQuantity * (equity.closePrice || 0)
                    })))
                }
            }
        }
        setFormData(prev => ({
            ...prev,
            sourcePortfolio: accountId,
            stockSymbol: "",
            stockName: "",
            quantity: ""
        }))
    }

    const handleSenderCustomerSelect = (customer: CustomerType | null) => {
        setSenderCustomer(customer)
        setCustomerError(null)
        setFormData(prev => ({
            ...prev,
            sourcePortfolio: "",
            stockSymbol: "",
            stockName: "",
            quantity: "",
        }))
        setAvailableEquities([])
        if (customer && receiverCustomer && customer.customerId === receiverCustomer.customerId) {
            setCustomerError("Sender and receiver customer cannot be the same. Please select different customers.")
        }
    }

    const handleReceiverCustomerSelect = (customer: CustomerType | null) => {
        setReceiverCustomer(customer)
        setCustomerError(null)
        setFormData(prev => ({
            ...prev,
            targetPortfolio: "",
        }))
    }

    const handleSubmit = async (transferType: string) => {
        try {
            setCustomerError(null)
            setSuccessMessage(null)

            if (formData.sourcePortfolio === formData.targetPortfolio) {
                setCustomerError("Transfer to the same account is not allowed. Please select different accounts.")
                return
            }

            const equity = availableEquities.find(e => e.symbol === formData.stockSymbol)
            if (!equity) {
                setCustomerError("Selected stock not found.")
                return
            }

            setIsLoading(true)

            const requestBody = {
                fromAccountId: Number(formData.sourcePortfolio),
                toAccountId: Number(formData.targetPortfolio),
                equityId: equity.equityId,
                transferQuantity: Number(formData.quantity)
            }

            const response = await sendRequest.post("/equity-transfers/portfolio-to-portfolio", requestBody)

            if (response.status >= 200 && response.status < 300) {
                setSuccessMessage(`${transferType} transfer success.`)
                setFormData({
                    sourcePortfolio: "",
                    targetPortfolio: "",
                    stockSymbol: "",
                    stockName: "",
                    quantity: ""
                })
                setSenderCustomer(null)
                setReceiverCustomer(null)
                setAvailableEquities([])
            }
        } catch (err: unknown) {

            if (axios.isAxiosError(err)) {
                const resp = err.response
                if (resp) {
                    const data = resp.data
                    const serverMessage =
                        data?.message
                        ?? data?.error
                        ?? (Array.isArray(data?.errors) ? data.errors.map((e: any) => e.message || e).join(", ") : null)
                        ?? JSON.stringify(data)

                    setCustomerError(`${serverMessage}`)
                } else if (err.request) {
                    setCustomerError("Request failed. Network or server issue.")
                } else {
                    setCustomerError(err.message ?? "An unknown error occurred.")
                }
            } else {
                const e = err as Error
                setCustomerError(e?.message ?? "An unknown error occurred.")
            }
        } finally {
            setIsLoading(false)
        }
    }

    const getSelectedStock = () => {
        return availableEquities.find(equity => equity.symbol === formData.stockSymbol)
    }

    const handleExternalCustomerSelect = (customer: CustomerType | null) => {
        setExternalCustomer(customer)
        setExternalAccount("")
        setExternalEquity("")
        setExternalAvailableEquities([])
        setExternalError(null)
        setExternalSuccess(null)

        if (customer) {
            setExternalQuantity("")
            setExternalInstitution("")
        }
    }

    const handleExternalAccountSelect = (accountId: string) => {
        setExternalAccount(accountId)
        setExternalEquity("")
        setExternalError(null)

        if (externalCustomer) {
            if (externalCustomer.accounts) {
                const selectedAccount = externalCustomer.accounts.find(
                    account => account.accountId.toString() === accountId
                )

                if (selectedAccount && selectedAccount.equities) {
                    setExternalAvailableEquities(
                        selectedAccount.equities.map((equity: { equityId: any; assetCode: any; assetName: any; totalQuantity: number; averageCost: any }) => ({
                            id: equity.equityId.toString(),
                            equityId: equity.equityId,
                            symbol: equity.assetCode,
                            name: equity.assetName,
                            quantity: equity.totalQuantity,
                            price: equity.averageCost,
                            totalValue: equity.totalQuantity * equity.averageCost
                        }))
                    )
                } else {
                    setExternalAvailableEquities([])
                }
            } else {
                setExternalAvailableEquities([])
            }
        }
    }

    const handleExternalEquitySelect = (equityId: string) => {
        setExternalEquity(equityId)
        setExternalError(null)
    }

    const handleExternalSubmit = async () => {
        setExternalLoading(true)
        setExternalError(null)
        setExternalSuccess(null)

        try {
            if (!externalCustomer) {
                throw new Error(t("Please select a customer"))
            }

            if (!externalAccount) {
                throw new Error(t("Please select an account"))
            }

            if (!externalEquity) {
                throw new Error(t("Please select a stock"))
            }

            if (!externalQuantity || parseFloat(externalQuantity) <= 0) {
                throw new Error(t("Please enter a valid quantity"))
            }

            if (!externalInstitution) {
                throw new Error(t("Please select an institution"))
            }

            if (!externalCustomer.identityNumber) {
                throw new Error(t("Customer identity number is missing"))
            }

            const selectedEquity = externalAvailableEquities.find(
                equity => equity.id === externalEquity
            )

            if (!selectedEquity) {
                throw new Error(t("Selected stock not found"))
            }

            if (parseFloat(externalQuantity) > selectedEquity.quantity) {
                throw new Error(t(`You can transfer maximum ${selectedEquity.quantity} shares`))
            }

            const payload = {
                fromAccountId: parseInt(externalAccount),
                equityId: parseInt(externalEquity),
                transferQuantity: parseFloat(externalQuantity),
                otherInstitutionId: parseInt(externalInstitution),
                tckn_vergi_no: parseInt(externalCustomer.identityNumber)
            }

            const response = await sendRequest.post("/equity-transfers/portfolio-to-external", payload)

            if (response.status === 200 || response.status === 201) {
                setExternalSuccess(t("Stock transfer to external institution initiated successfully"))
                setExternalEquity("")
                setExternalQuantity("")
                setExternalInstitution("")
                setExternalAccount('')
                setExternalCustomer(null)
                setExternalAvailableEquities([])
            } else {
                throw new Error(t("Failed to process transfer"))
            }
        } catch (error: any) {
            setExternalError(error.message || t("An error occurred during transfer"))
        } finally {
            setExternalLoading(false)
        }
    }

    const getSelectedExternalStock = () => {
        return externalAvailableEquities.find(equity => equity.id === externalEquity)
    }

    // External to Portfolio handlers
    const handleExtToPortCustomerSelect = (customer: CustomerType | null) => {
        setExtToPortCustomer(customer)
        setExtToPortAccount("")
        setExtToPortError(null)
        setExtToPortSuccess(null)
    }

    const handleExtToPortAccountSelect = (accountId: string) => {
        setExtToPortAccount(accountId)
        setExtToPortError(null)
    }

    const handleExtToPortInstitutionSelect = (institutionId: string) => {
        setExtToPortInstitution(institutionId)
        setExtToPortError(null)
        setExtToPortEquity(null)
    }

    const handleExtToPortEquitySelect = (equity: EquityType | null) => {
        setExtToPortEquity(equity)
        setExtToPortError(null)
    }

    const handleExtToPortQuantityChange = (quantity: string) => {
        setExtToPortQuantity(quantity)
        setExtToPortError(null)
    }

    const handleExtToPortAvgCostChange = (avgCost: string) => {
        setExtToPortAvgCost(avgCost)
        setExtToPortError(null)
    }

    const handleExtToPortSubmit = async () => {
        setExtToPortLoading(true)
        setExtToPortError(null)
        setExtToPortSuccess(null)

        try {
            if (!extToPortInstitution) {
                throw new Error(t("Please select an institution"))
            }

            if (!extToPortCustomer) {
                throw new Error(t("Please select a customer"))
            }

            if (!extToPortAccount) {
                throw new Error(t("Please select an account"))
            }

            if (!extToPortEquity) {
                throw new Error(t("Please select an equity"))
            }

            if (!extToPortQuantity || parseFloat(extToPortQuantity) <= 0) {
                throw new Error(t("Please enter a valid quantity"))
            }

            if (!extToPortAvgCost || parseFloat(extToPortAvgCost) <= 0) {
                throw new Error(t("Please enter a valid average cost"))
            }

            const payload = {
                toAccountId: parseInt(extToPortAccount),
                equityId: extToPortEquity.equityId,
                transferQuantity: parseFloat(extToPortQuantity),
                avgCost: parseFloat(extToPortAvgCost),
                otherInstitutionId: parseInt(extToPortInstitution),
                tckn_vergi_no: parseInt(extToPortCustomer.identityNumber)
            }

            const response = await sendRequest.post("/equity-transfers/external-to-portfolio", payload)

            if (response.status === 200 || response.status === 201) {
                setExtToPortSuccess(t("External to Portfolio equity transfer successfully started"))
                setExtToPortEquity(null)
                setExtToPortQuantity("")
                setExtToPortAvgCost("")
                setExtToPortAccount("")
                setExtToPortCustomer(null)
            } else {
                throw new Error(t("Transfer failed"))
            }
        } catch (error: any) {
            setExtToPortError(error.message || t("Transfer failed"))
        } finally {
            setExtToPortLoading(false)
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
                    <SiteHeader title={t("Stock Transfers")} />
                    <div className="flex-1 space-y-4 p-4 md:p-8 pt-6">
                        <div className="flex items-center justify-between space-y-2">
                            <h2 className="text-3xl font-bold tracking-tight">{t("Stock Transfers")}</h2>
                            <Badge variant="outline" className="text-sm">
                                {t("Asset Transfer")}
                            </Badge>
                        </div>
                        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
                            <TabsList className="grid w-full grid-cols-3">
                                <TabsTrigger value="portfolio-to-portfolio">{t("Portfolio to Portfolio")}</TabsTrigger>
                                <TabsTrigger value="portfolio-to-external">{t("Portfolio to External")}</TabsTrigger>
                                <TabsTrigger value="external-to-portfolio">{t("External to Portfolio")}</TabsTrigger>
                            </TabsList>
                            <TabsContent value="portfolio-to-portfolio" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <CardTitle className="flex items-center gap-2">
                                            <TrendingUp className="h-5 w-5" />
                                            {t("Portfolio to Portfolio Stock Transfer")}
                                        </CardTitle>
                                        <CardDescription>
                                            {t("Transfer stocks between customers")}
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-6">
                                        {successMessage && (
                                            <Alert className="bg-green-100 border-green-400">
                                                <CheckCircle2 className="h-4 w-4 text-green-600" />
                                                <AlertDescription className="text-green-800">
                                                    {successMessage}
                                                </AlertDescription>
                                            </Alert>
                                        )}
                                        {customerError && (
                                            <Alert className="bg-destructive/15">
                                                <AlertCircle className="h-4 w-4" />
                                                <AlertDescription>
                                                    {customerError}
                                                </AlertDescription>
                                            </Alert>
                                        )}
                                        <div className="space-y-4">
                                            <h3 className="text-lg font-medium">{t("Sender Customer")}</h3>
                                            <div className="space-y-2">
                                                <div className="flex-1">
                                                    {userRole === "ROLE_TRADER" ? (
                                                        <CustomerSearchTrader
                                                            onSelect={handleSenderCustomerSelect}
                                                            selectedCustomer={senderCustomer}
                                                            placeholder="Search and select a customer to add..."
                                                        />
                                                    ) : (
                                                        <CustomerSearch
                                                            onSelect={handleSenderCustomerSelect}
                                                            selectedCustomer={senderCustomer}
                                                            placeholder="Search and select a customer to add..."
                                                        />
                                                    )}
                                                </div>
                                            </div>
                                            {senderCustomer && (
                                                <>
                                                    <div className="space-y-2">
                                                        <Label htmlFor="source-account">{t("Source Portfolio")}</Label>
                                                        <Select value={formData.sourcePortfolio} onValueChange={handleSourcePortfolioSelect}>
                                                            <SelectTrigger>
                                                                <SelectValue placeholder={t("Portfolio select")} />
                                                            </SelectTrigger>
                                                            <SelectContent>
                                                                {senderCustomer?.accounts?.map((account) => (
                                                                    <SelectItem key={account.accountId} value={account.accountId.toString()}>
                                                                        <div className="flex justify-between items-center w-full">
                                                                            <span>{account.accountType}</span>
                                                                            <span className="text-muted-foreground ml-2">{account.equities?.length || 0} stocks</span>
                                                                        </div>
                                                                    </SelectItem>
                                                                )) || []}
                                                            </SelectContent>
                                                        </Select>
                                                    </div>
                                                    {formData.sourcePortfolio && availableEquities.length > 0 && (
                                                        <div className="space-y-2">
                                                            <Label htmlFor="stock-select">{t("Transfer Stock")}</Label>
                                                            <Select value={formData.stockSymbol} onValueChange={(value) => {
                                                                const stock = availableEquities.find(e => e.symbol === value)
                                                                handleInputChange("stockSymbol", value)
                                                                handleInputChange("stockName", stock ? stock.name : "")
                                                            }}>
                                                                <SelectTrigger>
                                                                    <SelectValue placeholder={t("Select Stock")} />
                                                                </SelectTrigger>
                                                                <SelectContent>
                                                                    {availableEquities.map((equity) => (
                                                                        <SelectItem key={equity.id} value={equity.symbol}>
                                                                            <div className="flex justify-between items-center w-full">
                                                                                <span>{equity.symbol}</span>
                                                                                <span className="text-muted-foreground ml-2">{equity.quantity} piece</span>
                                                                            </div>
                                                                        </SelectItem>
                                                                    ))}
                                                                </SelectContent>
                                                            </Select>
                                                        </div>
                                                    )}
                                                    {formData.stockSymbol && (
                                                        <div className="space-y-2">
                                                            <Label htmlFor="quantity">{t("Transfer Quantity")}</Label>
                                                            <Input
                                                                id="quantity"
                                                                type="number"
                                                                placeholder={t("Enter quantity")}
                                                                value={formData.quantity}
                                                                onChange={(e) => handleInputChange("quantity", e.target.value)}
                                                                min="1"
                                                                max={getSelectedStock()?.quantity.toString() || ""}
                                                            />
                                                            <p className="text-xs text-muted-foreground">
                                                                {t("Maximum transfer quantity")}: {getSelectedStock()?.quantity || 0}
                                                            </p>
                                                        </div>
                                                    )}
                                                </>
                                            )}
                                        </div>
                                        <div className="space-y-4">
                                            <h3 className="text-lg font-medium">{t("Receiver Customer")}</h3>
                                            <div className="space-y-2">
                                                <div className="flex-1">
                                                    {userRole === "ROLE_TRADER" ? (
                                                        <CustomerSearchTrader
                                                            onSelect={handleReceiverCustomerSelect}
                                                            selectedCustomer={receiverCustomer}
                                                            placeholder="Search and select a customer to add..."
                                                        />
                                                    ) : (
                                                        <CustomerSearch
                                                            onSelect={handleReceiverCustomerSelect}
                                                            selectedCustomer={receiverCustomer}
                                                            placeholder="Search and select a customer to add..."
                                                        />
                                                    )}
                                                </div>
                                            </div>

                                            {receiverCustomer && (
                                                <div className="space-y-2">
                                                    <Label htmlFor="target-account">{t("Target Portfolio")}</Label>
                                                    <Select value={formData.targetPortfolio} onValueChange={(value) => handleInputChange("targetPortfolio", value)}>
                                                        <SelectTrigger>
                                                            <SelectValue placeholder={t("Portfolio select")} />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {receiverCustomer?.accounts?.map((account) => (
                                                                <SelectItem key={account.accountId} value={account.accountId.toString()}>
                                                                    <div className="flex justify-between items-center w-full">
                                                                        <span>{account.accountType}</span>
                                                                        <span className="text-muted-foreground ml-2">{account.equities.length} {t("stock")}</span>
                                                                    </div>
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>
                                                </div>
                                            )}
                                        </div>
                                        {formData.sourcePortfolio && availableEquities.length === 0 && (
                                            <Alert>
                                                <AlertCircle className="h-4 w-4" />
                                                <AlertDescription>
                                                    {t("Selected portfolio does not have any stocks. Please select another portfolio.")}
                                                </AlertDescription>
                                            </Alert>
                                        )}

                                        <div className="pt-4">
                                            <Button
                                                onClick={() => handleSubmit("Portfolio to Portfolio")}
                                                disabled={isLoading || !formData.quantity || !formData.sourcePortfolio || !formData.targetPortfolio || !formData.stockSymbol}
                                                className="w-full mt-4"
                                            >
                                                {isLoading ? t("Processing...") : t("Make Stock Transfer")}
                                            </Button>
                                        </div>
                                    </CardContent>
                                </Card>
                            </TabsContent>

                            <TabsContent value="portfolio-to-external" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <CardTitle className="flex items-center gap-2">
                                            <TrendingUp className="h-5 w-5" />
                                            {t("Portfolio to External Institution Transfer")}
                                        </CardTitle>
                                        <CardDescription>
                                            {t("Transfer stocks from your portfolio to an external institution")}
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="space-y-4">
                                            {userRole === "ROLE_TRADER" ? (
                                                <CustomerSearchTrader
                                                    onSelect={handleExternalCustomerSelect}
                                                    selectedCustomer={externalCustomer}
                                                    placeholder={t("Search and select a customer to add...")}
                                                />
                                            ) : (
                                                <CustomerSearch
                                                    onSelect={handleExternalCustomerSelect}
                                                    selectedCustomer={externalCustomer}
                                                    placeholder={t("Search and select a customer to add...")}
                                                />
                                            )}
                                            {externalCustomer && externalCustomer.accounts && externalCustomer.accounts.length > 0 && (
                                                <div className="grid gap-2">
                                                    <Label htmlFor="external-account">{t("Select Account")}</Label>
                                                    <Select
                                                        value={externalAccount}
                                                        onValueChange={handleExternalAccountSelect}
                                                        disabled={externalLoading}
                                                    >
                                                        <SelectTrigger className="" id="external-account">
                                                            <SelectValue placeholder={t("Select account")} />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {externalCustomer.accounts.map(account => (
                                                                <SelectItem key={account.accountId} value={account.accountId.toString()}>
                                                                    <div className="flex justify-between items-center w-full">
                                                                        <span>{account.accountType}</span>
                                                                        <span className="text-muted-foreground ml-2">{account.equities.length} {t("stock")}</span>
                                                                    </div>
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>
                                                </div>
                                            )}

                                            {externalAccount && externalAvailableEquities.length > 0 && (
                                                <div className="grid gap-2">
                                                    <Label htmlFor="external-stock">{t("Select Stock")}</Label>
                                                    <Select
                                                        value={externalEquity}
                                                        onValueChange={handleExternalEquitySelect}
                                                        disabled={externalLoading}
                                                    >
                                                        <SelectTrigger id="external-stock">
                                                            <SelectValue className="p-4" placeholder="Select stock" />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {externalAvailableEquities.map(equity => (
                                                                <SelectItem
                                                                    key={equity.id}
                                                                    value={equity.id}
                                                                >
                                                                    <div className="flex flex-col">
                                                                        <span>{equity.symbol} - {equity.name}</span>
                                                                        <span className="text-muted-foreground ml-2">
                                                                            {equity.quantity} {t("shares")}
                                                                        </span>
                                                                    </div>
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>
                                                </div>
                                            )}
                                            {externalEquity && (
                                                <div className="grid gap-2">
                                                    <Label htmlFor="external-quantity">{t("Quantity")}</Label>
                                                    <Input
                                                        id="external-quantity"
                                                        type="number"
                                                        value={externalQuantity}
                                                        onChange={(e) => setExternalQuantity(e.target.value)}
                                                        placeholder="Enter quantity"
                                                        disabled={externalLoading}
                                                        min="1"
                                                        max={getSelectedExternalStock()?.quantity.toString()}
                                                    />
                                                    {getSelectedExternalStock() && (
                                                        <p className="text-sm text-muted-foreground">
                                                            {t("Available")}: {getSelectedExternalStock()?.quantity} {t("shares")}
                                                        </p>
                                                    )}
                                                </div>
                                            )}
                                            {externalEquity && (
                                                <div className="grid gap-2">
                                                    <Label htmlFor="external-institution">{t("Select Institution")}</Label>
                                                    <Select
                                                        value={externalInstitution}
                                                        onValueChange={setExternalInstitution}
                                                        disabled={externalLoading}
                                                    >
                                                        <SelectTrigger id="external-institution">
                                                            <SelectValue placeholder={t("Select institution")} />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {externalInstitutions.map(institution => (
                                                                <SelectItem
                                                                    key={institution.id}
                                                                    value={institution.id.toString()}
                                                                >
                                                                    {institution.name}
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>
                                                </div>
                                            )}

                                            {externalInstitution && externalCustomer && (
                                                <div className="grid gap-2">
                                                    <Label>{t("TCKN/Vergi No")}</Label>
                                                    <div className="p-2 border rounded-md bg-muted/20">
                                                        {externalCustomer.identityNumber}
                                                    </div>
                                                </div>
                                            )}

                                            {externalError && (
                                                <Alert variant="destructive">
                                                    <AlertCircle className="h-4 w-4" />
                                                    <AlertDescription>{externalError}</AlertDescription>
                                                </Alert>
                                            )}

                                            {externalSuccess && (
                                                <Alert variant="default">
                                                    <CheckCircle2 className="h-4 w-4" />
                                                    <AlertDescription>{externalSuccess}</AlertDescription>
                                                </Alert>
                                            )}

                                            <Button
                                                onClick={handleExternalSubmit}
                                                disabled={
                                                    externalLoading ||
                                                    !externalCustomer ||
                                                    !externalAccount ||
                                                    !externalEquity ||
                                                    !externalQuantity ||
                                                    !externalInstitution
                                                }
                                                className="w-full mt-4"
                                            >
                                                {externalLoading ? t("Processing...") : t("Transfer to External Institution")}
                                            </Button>
                                        </div>
                                    </CardContent>
                                </Card>
                            </TabsContent>

                            <TabsContent value="external-to-portfolio" className="space-y-4">
                                <Card>
                                    <CardHeader>
                                        <CardTitle className="flex items-center gap-2">
                                            <TrendingUp className="h-5 w-5" />
                                            {t("External Institution to Portfolio Transfer")}
                                        </CardTitle>
                                        <CardDescription>
                                            {t("Transfer stocks from an external institution to your portfolio")}
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent className="space-y-4">
                                        <div className="space-y-4">
                                            <div className="grid gap-2">
                                                <Label htmlFor="ext-to-port-institution">{t("Source Institution")}</Label>
                                                <Select
                                                    value={extToPortInstitution}
                                                    onValueChange={handleExtToPortInstitutionSelect}
                                                    disabled={extToPortLoading}
                                                >
                                                    <SelectTrigger id="ext-to-port-institution">
                                                        <SelectValue placeholder={t("Select institution")} />
                                                    </SelectTrigger>
                                                    <SelectContent>
                                                        {externalInstitutions.map(institution => (
                                                            <SelectItem
                                                                key={institution.id}
                                                                value={institution.id.toString()}
                                                            >
                                                                {institution.name}
                                                            </SelectItem>
                                                        ))}
                                                    </SelectContent>
                                                </Select>
                                            </div>
                                            {extToPortInstitution && (
                                                userRole === "ROLE_TRADER" ? (
                                                    <CustomerSearchTrader
                                                        onSelect={handleExtToPortCustomerSelect}
                                                        selectedCustomer={extToPortCustomer}
                                                        placeholder={t("Search and select a customer to add...")}
                                                        disabled={extToPortLoading}
                                                    />
                                                ) : (
                                                    <CustomerSearch
                                                        onSelect={handleExtToPortCustomerSelect}
                                                        selectedCustomer={extToPortCustomer}
                                                        placeholder={t("Search and select a customer to add...")}
                                                        disabled={extToPortLoading}
                                                    />
                                                )
                                            )}
                                            {extToPortInstitution && extToPortCustomer && extToPortCustomer.accounts && extToPortCustomer.accounts.length > 0 && (
                                                <div className="grid gap-2">
                                                    <Label htmlFor="ext-to-port-account">{t("Target Portfolio")}</Label>
                                                    <Select
                                                        value={extToPortAccount}
                                                        onValueChange={handleExtToPortAccountSelect}
                                                        disabled={extToPortLoading}
                                                    >
                                                        <SelectTrigger className="" id="ext-to-port-account">
                                                            <SelectValue placeholder="Select portfolio" />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {extToPortCustomer.accounts.map(account => (
                                                                <SelectItem key={account.accountId} value={account.accountId.toString()}>
                                                                    <div className="flex justify-between items-center w-full">
                                                                        <span>{account.accountType}</span>
                                                                        <span className="text-muted-foreground ml-2">
                                                                            {account.cashBalance.totalBalance.toLocaleString('tr-TR', { style: 'currency', currency: 'TRY' })}
                                                                        </span>
                                                                    </div>
                                                                </SelectItem>
                                                            ))}
                                                        </SelectContent>
                                                    </Select>
                                                </div>
                                            )}
                                            {extToPortInstitution && extToPortAccount && (
                                                <EquitySearch
                                                    onSelect={handleExtToPortEquitySelect}
                                                    selectedEquity={extToPortEquity}
                                                    placeholder="Search for equity..."
                                                    disabled={extToPortLoading}
                                                    institutionId={extToPortInstitution}
                                                />
                                            )}
                                            {extToPortEquity && (
                                                <div className="grid gap-2">
                                                    <Label htmlFor="ext-to-port-quantity">{t("Quantity")}</Label>
                                                    <Input
                                                        id="ext-to-port-quantity"
                                                        type="number"
                                                        value={extToPortQuantity}
                                                        onChange={(e) => handleExtToPortQuantityChange(e.target.value)}
                                                        placeholder="Enter quantity"
                                                        disabled={extToPortLoading}
                                                        min="1"
                                                    />
                                                </div>
                                            )}
                                            {extToPortEquity && (
                                                <div className="grid gap-2">
                                                    <Label htmlFor="ext-to-port-avgcost">{t("Average Cost")}</Label>
                                                    <Input
                                                        id="ext-to-port-avgcost"
                                                        type="number"
                                                        value={extToPortAvgCost}
                                                        onChange={(e) => handleExtToPortAvgCostChange(e.target.value)}
                                                        placeholder="Enter average cost"
                                                        disabled={extToPortLoading}
                                                        min="0.01"
                                                        step="0.01"
                                                    />
                                                </div>
                                            )}
                                            {extToPortCustomer && extToPortEquity && (
                                                <div className="grid gap-2">
                                                    <Label>TCKN/Vergi No</Label>
                                                    <div className="p-2 border rounded-md bg-muted/20">
                                                        {extToPortCustomer.identityNumber}
                                                    </div>
                                                </div>
                                            )}
                                            {extToPortError && (
                                                <Alert variant="destructive">
                                                    <AlertCircle className="h-4 w-4" />
                                                    <AlertDescription>{extToPortError}</AlertDescription>
                                                </Alert>
                                            )}

                                            {extToPortSuccess && (
                                                <Alert variant="default">
                                                    <CheckCircle2 className="h-4 w-4" />
                                                    <AlertDescription>{extToPortSuccess}</AlertDescription>
                                                </Alert>
                                            )}
                                            <Button
                                                onClick={handleExtToPortSubmit}
                                                disabled={
                                                    extToPortLoading ||
                                                    !extToPortInstitution ||
                                                    !extToPortCustomer ||
                                                    !extToPortAccount ||
                                                    !extToPortEquity ||
                                                    !extToPortQuantity ||
                                                    !extToPortAvgCost
                                                }
                                                className="w-full mt-4"
                                            >
                                                {extToPortLoading ? t("Processing...") : t("Transfer from External Institution")}
                                            </Button>
                                        </div>
                                    </CardContent>
                                </Card>
                            </TabsContent>
                        </Tabs>
                    </div>
                </SidebarInset>
            </SidebarProvider>
            {externalLoading || extToPortLoading && <LoadingDialog isOpen={externalLoading || extToPortLoading} />}
        </div>
    )
}
