/* eslint-disable @typescript-eslint/no-explicit-any */
"use client"

import { useState, useRef } from "react"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { AlertCircle, TrendingUp, TrendingDown } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { LoadingButton } from "@/components/ui/loading"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import { HomePageChart } from "@/components/home-page-chart"
import { DataTable } from "@/components/data-table"
import { ColumnDef } from "@tanstack/react-table"
import { IconLoader } from "@tabler/icons-react"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import initSendRequest from "@/configs/sendRequest"
import { EquitySearchTrade, EquityType } from "@/components/equity-search-trade"
import { CustomerType } from "@/components/ui/customer-search"
import { CustomerSearchTrader } from "@/components/customer-search-trader"
import { format } from "date-fns"
import { tr } from "date-fns/locale"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
interface EquityList {
    assetCode: string
    equityId: number
    assetName: string
    quantity: number
    price?: number
    totalValue?: number
}
export type TradeHistoryType = {
    tradeId: number
    matchId: number
    equityOrderId: number
    tradeQuantity: number
    price: number
    commission: number
    transactionTime: string
}

export type OrderBookType = {
    equityId: number
    equityCode: string
    bids: {
        orderId: number
        price: number
        amount: number
        total: number
    }[]
    asks: {
        orderId: number
        price: number
        amount: number
        total: number
    }[]
}

export default function QuickBuySell() {
    const { t } = useTranslation()
    const sendRequest = initSendRequest()
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [selectedStock, setSelectedStock] = useState("")
    const [stockDetails, setStockDetails] = useState<any>(null)
    const [priceData, setPriceData] = useState<any[]>([])
    const [chartLoading, setChartLoading] = useState(false)

    const [buyQuantity, setBuyQuantity] = useState("")
    const [buyPrice, setBuyPrice] = useState("")
    const [buyOrderType, setBuyOrderType] = useState("MARKET")
    const [buyLoading, setBuyLoading] = useState(false)
    const [buySuccess, setBuySuccess] = useState(false)
    const [buyError, setBuyError] = useState<string | null>(null)

    const [sellQuantity, setSellQuantity] = useState("")
    const [sellPrice, setSellPrice] = useState("")
    const [sellOrderType, setSellOrderType] = useState("MARKET")
    const [sellLoading, setSellLoading] = useState(false)
    const [sellSuccess, setSellSuccess] = useState(false)
    const [sellError, setSellError] = useState<string | null>(null)

    const [orderBook, setOrderBook] = useState<OrderBookType | null>(null)
    const [orderBookLoading, setOrderBookLoading] = useState(false)

    // Refs for alert timeouts
    const buySuccessTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const buyErrorTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const sellSuccessTimeoutRef = useRef<NodeJS.Timeout | null>(null);
    const sellErrorTimeoutRef = useRef<NodeJS.Timeout | null>(null);

    const [orderHistory, setOrderHistory] = useState<any[]>([])
    const [orderHistoryLoading, setOrderHistoryLoading] = useState(false)
    const [orderHistoryPagination, setOrderHistoryPagination] = useState({
        pageIndex: 0,
        pageSize: 5,
    })

    const [selectedCustomer, setSelectedCustomer] = useState<CustomerType | null>(null)
    const [selectedAccount, setSelectedAccount] = useState<string>("")
    const [accountEquities, setAccountEquities] = useState<any[]>([])
    const [selectedEquityPrice, setSelectedEquityPrice] = useState<number>(0)
    const [selectedEquityObject, setSelectedEquityObject] = useState<any>(null)
    const [pagination, setPagination] = useState({
        pageIndex: 0,
        pageSize: 5,
    })

    const [tradeHistory, setTradeHistory] = useState<any[]>([])
    const [tradeHistoryLoading, setTradeHistoryLoading] = useState(false)
    const [tradeHistoryPagination, setTradeHistoryPagination] = useState({
        pageIndex: 0,
        pageSize: 5,
    })

    const clearStockSelection = () => {
        setSelectedStock("")
        setStockDetails(null)
        setPriceData([])
        setChartLoading(false)
        setBuySuccess(false)
        setSellSuccess(false)
        setBuyError(null)
        setSellError(null)
        setSelectedCustomer(null)
        setSelectedAccount("")
        setAccountEquities([])
        setBuyQuantity("")
        setSellQuantity("")
        setSelectedEquityPrice(0)
        setSelectedEquityObject(null)
        setBuyOrderType("MARKET")
        setSellOrderType("MARKET")
        if (stockDetails?.currentPrice) {
            setBuyPrice(stockDetails.currentPrice.toString())
            setSellPrice(stockDetails.currentPrice.toString())
        }
    }

    const handleStockSelect = async (equity: EquityType | null) => {
        if (!equity) {
            clearStockSelection()
            return
        }

        const stock = equity.assetCode
        setSelectedStock(stock)
        setSelectedEquityPrice(equity.openPrice)
        setSelectedEquityObject(equity)
        setChartLoading(true)
        fetchOrderHistory(equity.equityId)
        fetchTradeHistory(equity.equityId)
        fetchOrderBook(equity.equityId)
        setBuySuccess(false)
        setSellSuccess(false)
        setBuyError(null)
        setSellError(null)
        setSelectedCustomer(null)
        setSelectedAccount("")
        setAccountEquities([])

        try {
            const detailsResponse = await sendRequest.get(`/equities/${stock}/info`)
            setStockDetails(detailsResponse.data)
            const priceResponse = await sendRequest.get(`/equities/${stock}/prices`)
            setPriceData(priceResponse.data)

            if (priceResponse.data && priceResponse.data.length > 0) {
                setBuyPrice(selectedEquityPrice.toString())
                setSellPrice(selectedEquityPrice.toString())
            }

        } catch (err: any) {
            setError(err.message)
        } finally {
            setChartLoading(false)
        }
    }

    const handleCustomerSelect = async (customer: CustomerType | null) => {
        setSelectedCustomer(customer)
        setSelectedAccount("")
        setAccountEquities([])
        if (customer) {
            try {
                await fetchCustomersDetail(customer.customerId)
            } catch (err: any) {
                setError(err.message || "Failed to fetch customer details")
            } finally {
            }
        }
    }

    const handleAccountSelect = async (accountId: string) => {
        setSelectedAccount(accountId)

        if (selectedCustomer && selectedCustomer.accounts) {
            const account = selectedCustomer.accounts.find(a => a.accountId.toString() === accountId)
            if (account && account.equities) {
                setAccountEquities(account.equities.map((equity: any) => ({
                    id: equity.assetCode,
                    equityId: equity.equityId,
                    assetCode: equity.assetCode,
                    assetName: equity.assetName,
                    quantity: equity.totalQuantity,
                    price: equity.closePrice || 0,
                    totalValue: equity.totalQuantity * (equity.closePrice || 0)
                })))
            } else {
                setAccountEquities([])
            }
        }
    }

    const fetchCustomersDetail = async (customerId: number) => {
        setLoading(true)
        try {
            if (!customerId) {
                return
            }
            const response = await sendRequest.get(`/customers/${customerId}`)
            setSelectedCustomer(response.data)
        } catch (err: any) {
            setError("Failed to fetch customer detail:" + err.message)
        } finally {
            setLoading(false)
        }
    }

    const fetchOrderHistory = async (equityId: number) => {
        setOrderHistoryLoading(true)
        setLoading(true)
        try {
            if (!equityId) {
                return
            }
            const response = await sendRequest.get(`/order-histories`, { params: { equityId: equityId } })
            setOrderHistory(response.data.content)
        } catch (err: any) {
            setError("Failed to fetch order history:" + err.message)
        } finally {
            setOrderHistoryLoading(false)
            setLoading(false)
        }
    }

    const fetchTradeHistory = async (equityId: number) => {
        setTradeHistoryLoading(true)
        setLoading(true)
        try {
            if (!equityId) {
                return
            }
            const response = await sendRequest.get(`/trades/settled`)
            setTradeHistory(response.data || [])
        } catch (err: any) {
            setError("Failed to fetch trade history:" + err.message)
            setTradeHistory([])
        } finally {
            setTradeHistoryLoading(false)
            setLoading(false)
        }
    }

    const fetchOrderBook = async (equityId: number) => {
        setOrderBookLoading(true)
        setLoading(true)
        try {
            if (!equityId) {
                return
            }
            const response = await sendRequest.get(`/orderbook/${equityId}`)
            setOrderBook(response.data || null)
        } catch (err: any) {
            setError("Failed to fetch order book:" + err.message)
            setOrderBook(null)
        } finally {
            setOrderBookLoading(false)
            setLoading(false)
        }
    }


    const handleBuyOrder = async () => {
        if (buySuccessTimeoutRef.current) clearTimeout(buySuccessTimeoutRef.current);
        if (buyErrorTimeoutRef.current) clearTimeout(buyErrorTimeoutRef.current);

        setBuyLoading(true)
        setBuySuccess(false)
        setBuyError(null)
        try {
            if (!selectedStock) {
                throw new Error("Please select a stock")
            }

            if (!buyQuantity || parseInt(buyQuantity) <= 0) {
                throw new Error("Please enter a valid quantity")
            }

            if ((buyOrderType === "LIMIT") && (!buyPrice || parseFloat(buyPrice) <= 0)) {
                throw new Error("Please enter a valid price")
            }

            if (!selectedAccount || !selectedEquityObject) {
                throw new Error("Please select an account and stock")
            }

            const orderPayload: any = {
                accountId: parseInt(selectedAccount),
                equityId: selectedEquityObject.equityId,
                orderSide: "BUY",
                orderQuantity: parseInt(buyQuantity),
                orderType: buyOrderType
            }

            if (buyOrderType === "LIMIT") {
                orderPayload.price = parseFloat(buyPrice)
            } else if (buyOrderType === "MARKET") {
                orderPayload.price = selectedEquityPrice
            }

            const response = await sendRequest.post("/equity-order", orderPayload)
            if (!selectedCustomer) return
            await fetchCustomersDetail(selectedCustomer.customerId)

            if (response.data && response.data.finalStatus === "FILLED") {
                try {
                    await sendRequest.get(`/orderbook/${response.data.equityId}/exclude/${response.data.orderId}`)
                } catch (orderBookErr: any) {
                    setError("Failed to fetch excluded orderbook:" + orderBookErr.message)
                }
            }
            fetchOrderBook(response.data?.equityId)
            fetchOrderHistory(selectedEquityObject.equityId)
            fetchTradeHistory(selectedEquityObject.equityId)
            setBuySuccess(true)
            setBuyQuantity("")

            if ((buyOrderType !== "MARKET") && stockDetails?.currentPrice) {
                setBuyPrice(stockDetails.currentPrice.toString())
            }
            buySuccessTimeoutRef.current = setTimeout(() => {
                setBuySuccess(false);
            }, 5000);
        } catch (err: any) {
            setBuyError(err.response?.data?.message || "Failed to place buy order")
            buyErrorTimeoutRef.current = setTimeout(() => {
                setBuyError(null);
            }, 5000);
        } finally {
            setBuyLoading(false)
        }
    }

    const handleSellOrder = async () => {
        if (sellSuccessTimeoutRef.current) clearTimeout(sellSuccessTimeoutRef.current);
        if (sellErrorTimeoutRef.current) clearTimeout(sellErrorTimeoutRef.current);

        setSellLoading(true)
        setSellSuccess(false)
        setSellError(null)
        try {
            if (!selectedStock) {
                throw new Error("Please select a stock")
            }
            if (!sellQuantity || parseInt(sellQuantity) <= 0) {
                throw new Error("Please enter a valid quantity")
            }
            if ((sellOrderType === "LIMIT") && (!sellPrice || parseFloat(sellPrice) <= 0)) {
                throw new Error("Please enter a valid price")
            }
            if (!selectedAccount || !selectedEquityObject) {
                throw new Error("Please select an account and stock")
            }
            const orderPayload: any = {
                accountId: parseInt(selectedAccount),
                equityId: selectedEquityObject.equityId,
                orderSide: "SELL",
                orderQuantity: parseInt(sellQuantity),
                orderType: sellOrderType
            }
            if (sellOrderType === "LIMIT") {
                orderPayload.price = parseFloat(sellPrice)
            } else if (sellOrderType === "MARKET") {
                orderPayload.price = selectedEquityPrice
            }
            const response = await sendRequest.post("/equity-order", orderPayload)
            if (!selectedCustomer) return
            await fetchCustomersDetail(selectedCustomer.customerId)
            if (response.data && response.data.finalStatus === "FILLED") {
                try {
                    await sendRequest.get(`/orderbook/${response.data.equityId}/exclude/${response.data.orderId}`)
                } catch (orderBookErr: any) {
                    setError("Failed to fetch excluded orderbook:" + orderBookErr.message)
                }
            } 
            fetchOrderBook(response.data?.equityId)
            fetchOrderHistory(selectedEquityObject.equityId)
            fetchTradeHistory(selectedEquityObject.equityId)
            setSellSuccess(true)
            setSellQuantity("")
            if ((sellOrderType !== "MARKET") && stockDetails?.currentPrice) {
                setSellPrice(stockDetails.currentPrice.toString())
            }
            sellSuccessTimeoutRef.current = setTimeout(() => {
                setSellSuccess(false);
            }, 5000);
        } catch (err: any) {
            setSellError(err.response?.data?.message || "Failed to place sell order")
            sellErrorTimeoutRef.current = setTimeout(() => {
                setSellError(null);
            }, 5000);
        } finally {
            setSellLoading(false)
        }
    }

    const orderHistoryColumns: ColumnDef<any>[] = [
        {
            accessorKey: "historyId",
            header: "History ID",
            cell: ({ row }) => <span className="font-medium">{row.original?.historyId}</span>,
        },
        {
            accessorKey: "orderId",
            header: "Order ID",
            cell: ({ row }) => <span className="font-medium">{row.original?.orderId}</span>,
        },
        {
            accessorKey: "orderSide",
            header: "Type",
            cell: ({ row }) => {
                const type = row.original?.orderSide
                return (
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${type === "BUY" ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"
                        }`}>
                        {type}
                    </span>
                )
            },
        },
        {
            accessorKey: "oldOrderQuantity",
            header: "Quantity",
            cell: ({ row }) => row.original.oldOrderQuantity,
        },
        {
            accessorKey: "oldPrice",
            header: "Price",
            cell: ({ row }) => `₺${row.original.oldPrice?.toFixed(2)}`,
        },
        {
            accessorKey: "orderStatus",
            header: "Status",
            cell: ({ row }) => {
                const status = row.original.orderStatus
                let bgColor = "bg-gray-100"
                let textColor = "text-gray-800"

                switch (status) {
                    case "COMPLETED":
                        bgColor = "bg-blue-100"
                        textColor = "text-blue-800"
                        break
                    case "PENDING":
                        bgColor = "bg-yellow-100"
                        textColor = "text-yellow-800"
                        break
                    case "PARTIALLY_FILLED":
                        bgColor = "bg-purple-100"
                        textColor = "text-purple-800"
                        break
                    case "CANCELED":
                        bgColor = "bg-gray-100"
                        textColor = "text-gray-800"
                        break
                    case "REJECTED":
                        bgColor = "bg-red-100"
                        textColor = "text-red-800"
                        break
                    case "PROCESSING":
                        bgColor = "bg-indigo-100"
                        textColor = "text-indigo-800"
                        break
                }

                return (
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${bgColor} ${textColor}`}>
                        {status}
                    </span>
                )
            },
        },
        {
            accessorKey: "transactionTime",
            header: "Date",
            cell: ({ row }) => {
                const date = row.original.transactionTime
                return date ? new Date(date).toLocaleString('tr-TR') : "-"
            },
        },
        {
            accessorKey: "orderType",
            header: "Order Type",
            cell: ({ row }) => row.original.orderType,
        },
    ]

    const tradeHistoryColumns: ColumnDef<TradeHistoryType>[] = [
        {
            accessorKey: "tradeId",
            header: "İşlem No",
            cell: ({ row }) => <div>{row.getValue("tradeId")}</div>,
        },
        {
            accessorKey: "matchId",
            header: "Eşleşme No",
            cell: ({ row }) => <div>{row.getValue("matchId")}</div>,
        },
        {
            accessorKey: "equityOrderId",
            header: "Emir No",
            cell: ({ row }) => <div>{row.getValue("equityOrderId")}</div>,
        },
        {
            accessorKey: "tradeQuantity",
            header: "Miktar",
            cell: ({ row }) => <div>{row.getValue("tradeQuantity")}</div>,
        },
        {
            accessorKey: "price",
            header: "Fiyat",
            cell: ({ row }) => <div>₺{Number(row.getValue("price"))?.toFixed(2)}</div>,
        },
        {
            accessorKey: "commission",
            header: "Komisyon",
            cell: ({ row }) => <div>₺{Number(row.getValue("commission"))?.toFixed(2)}</div>,
        },
        {
            accessorKey: "transactionTime",
            header: "İşlem Zamanı",
            cell: ({ row }) => {
                const date = new Date(row.getValue("transactionTime"))
                return (
                    <div>
                        {format(date, "dd MMM yyyy HH:mm:ss", { locale: tr })}
                    </div>
                )
            },
        },
        {
            accessorKey: "totalValue",
            header: "Toplam Değer",
            cell: ({ row }) => {
                const price = Number(row.getValue("price"))
                const quantity = Number(row.getValue("tradeQuantity"))
                const totalValue = price * quantity
                return <div>₺{totalValue.toFixed(2)}</div>
            },
        }
    ]

    const stockColumns: ColumnDef<EquityList>[] = [
        {
            accessorKey: "assetCode",
            header: "Code",
            cell: ({ row }) => <span className="font-medium">{row.original.assetCode}</span>,
        },
        {
            accessorKey: "assetName",
            header: "Name",
            cell: ({ row }) => <span className="font-medium">{row.original.assetName}</span>,
        },
        {
            accessorKey: "quantity",
            header: "Quantity",
            cell: ({ row }) => <div>{row.original.quantity}</div>,
        },
        {
            accessorKey: "closePrice",
            header: "Price",
            cell: ({ row }) => <div>₺{row.original.price !== undefined ? row.original.price.toFixed(2) : "-"}</div>,
        },
        {
            accessorKey: "totalValue",
            header: "Total Value",
            cell: ({ row }) => <div>₺{row.original.totalValue !== undefined ? row.original.totalValue.toFixed(2) : "-"}</div>,
        },
    ]

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
                    <SiteHeader title={t("Quick Buy/Sell")} />
                    <div className="flex flex-1 flex-col">
                        <div className="@container/main flex flex-1 flex-col gap-2">
                            <div className="container px-4 mx-auto py-4 md:py-6">
                                <Card className="mb-6">
                                    <CardHeader>
                                        <CardTitle>{t("Select Stock")}</CardTitle>
                                        <CardDescription>
                                            {t("Choose a stock to trade")}
                                        </CardDescription>
                                    </CardHeader>
                                    <CardContent>
                                        <div className="grid gap-4">
                                            <div className="grid gap-2">
                                                <EquitySearchTrade
                                                    onSelect={handleStockSelect}
                                                    selectedEquity={selectedStock ? {
                                                        assetCode: selectedStock,
                                                        equityName: stockDetails?.equityName || "",
                                                        openPrice: selectedEquityPrice || 0,
                                                        equityId: stockDetails?.equityId || 0,
                                                        symbol: selectedStock
                                                    } : null}
                                                    placeholder={t("Search stocks...")}
                                                />
                                            </div>
                                        </div>
                                    </CardContent>
                                </Card>
                                {selectedStock && (
                                    <Card className="mb-6">
                                        <CardHeader>
                                            <CardTitle>{selectedStock} - {stockDetails?.equityName}</CardTitle>
                                            <CardDescription>
                                                {t("Current Price")}: ₺{selectedEquityPrice || "Loading..."}
                                            </CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            {chartLoading ? (
                                                <div className="flex justify-center py-8">
                                                    <IconLoader className="animate-spin" />
                                                </div>
                                            ) : priceData.length > 0 ? (
                                                <HomePageChart title={`${selectedStock} Price History`} data={priceData} />
                                            ) : (
                                                <div className="text-center py-8 text-muted-foreground">
                                                    {t("No price data available")}
                                                </div>
                                            )}
                                        </CardContent>
                                    </Card>
                                )}

                                {/* Customer Selection */}
                                {selectedStock && (
                                    <Card className="mb-6">
                                        <CardHeader>
                                            <CardTitle>{t("Select Customer")}</CardTitle>
                                            <CardDescription>
                                                {t("Choose a customer to view their accounts")}
                                            </CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            <div className="grid gap-4">
                                                <div className="grid gap-2">
                                                    <CustomerSearchTrader
                                                        onSelect={handleCustomerSelect}
                                                        selectedCustomer={selectedCustomer}
                                                        placeholder="Search for a customer..."
                                                    />
                                                </div>
                                            </div>
                                        </CardContent>
                                    </Card>
                                )}

                                {/* Account Selection */}
                                {selectedCustomer && (
                                    <Card className="mb-6">
                                        <CardHeader>
                                            <CardTitle>{t("Select Account")}</CardTitle>
                                            <CardDescription>
                                                {t("Choose an account to view available equities")}
                                            </CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            <div className="grid gap-4">
                                                <div className="grid gap-2">
                                                    <Label htmlFor="account-select">{t("Account")}</Label>
                                                    <Select
                                                        value={selectedAccount}
                                                        onValueChange={handleAccountSelect}
                                                    >
                                                        <SelectTrigger id="account-select">
                                                            <SelectValue placeholder={t("Select an account")} />
                                                        </SelectTrigger>
                                                        <SelectContent>
                                                            {selectedCustomer?.accounts?.map((account) => (
                                                                <SelectItem key={account.accountId} value={account.accountId.toString()}>
                                                                    <div className="flex justify-between items-center w-full">
                                                                        <span>{account.accountType}</span>
                                                                        <span className="text-muted-foreground ml-2">
                                                                            {account.equities?.length || 0} {t("stocks")}
                                                                        </span>
                                                                    </div>
                                                                </SelectItem>
                                                            )) || []}
                                                        </SelectContent>
                                                    </Select>
                                                </div>
                                            </div>
                                        </CardContent>
                                    </Card>
                                )}

                                {selectedAccount && selectedCustomer?.accounts && (
                                    <Card className="mb-6">
                                        <CardHeader>
                                            <CardTitle>{t("Account Balance")}</CardTitle>
                                            <CardDescription>
                                                {t("Available funds and portfolio information")}
                                            </CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            <div className="grid gap-4">
                                                {selectedCustomer.accounts.filter(account => account.accountId.toString() === selectedAccount).map((account) => (
                                                    <div key={account.accountId} className="grid grid-cols-2 gap-4">
                                                        <div className="space-y-2">
                                                            <h4 className="text-sm font-medium text-muted-foreground">{t("Free Balance")}</h4>
                                                            <p className="text-2xl font-bold text-green-600">
                                                                ₺{account.cashBalance?.freeBalance?.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0,00'}
                                                            </p>
                                                        </div>
                                                        <div className="space-y-2">
                                                            <h4 className="text-sm font-medium text-muted-foreground">{t("Blocked Balance")}</h4>
                                                            <p className="text-2xl font-bold text-amber-600">
                                                                ₺{account.cashBalance?.blockedBalance?.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0,00'}
                                                            </p>
                                                        </div>
                                                        <div className="space-y-2">
                                                            <h4 className="text-sm font-medium text-muted-foreground">{t("Total Balance")}</h4>
                                                            <p className="text-2xl font-bold text-primary">
                                                                ₺{account.cashBalance?.totalBalance?.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0,00'}
                                                            </p>
                                                        </div>
                                                        <div className="space-y-2">
                                                            <h4 className="text-sm font-medium text-muted-foreground">{t("Number of Stocks")}</h4>
                                                            <p className="text-2xl font-bold text-primary">
                                                                {account.equities?.length || 0}
                                                            </p>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </CardContent>
                                    </Card>
                                )}

                                {selectedAccount && accountEquities.length > 0 && (
                                    <Card className="mb-6">
                                        <CardHeader>
                                            <CardTitle>{t("Account Equities")}</CardTitle>
                                            <CardDescription>
                                                {t("Stocks available in the selected account")}
                                            </CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            <DataTable
                                                columns={stockColumns}
                                                data={accountEquities}
                                                pagination={pagination}
                                                setPagination={setPagination}
                                            />
                                        </CardContent>
                                    </Card>
                                )}
                                {selectedStock && selectedCustomer && selectedAccount && (
                                    <Tabs defaultValue="buy" className="w-full mb-6">
                                        <TabsList className="grid w-full grid-cols-2">
                                            <TabsTrigger value="buy" className="flex items-center gap-2">
                                                <TrendingUp className="h-4 w-4" />
                                                {t("Buy")}
                                            </TabsTrigger>
                                            <TabsTrigger value="sell" className="flex items-center gap-2">
                                                <TrendingDown className="h-4 w-4" />
                                                {t("Sell")}
                                            </TabsTrigger>
                                        </TabsList>
                                        <TabsContent value="buy">
                                            <Card>
                                                <CardHeader>
                                                    <CardTitle>{t("Buy")} {selectedStock} Current Price : {selectedEquityPrice}TL</CardTitle>
                                                    <CardDescription>
                                                        {t("Place an order to buy shares of")} {selectedStock}
                                                    </CardDescription>
                                                </CardHeader>
                                                <CardContent className="space-y-4">
                                                    {buySuccess && (
                                                        <Alert className="bg-green-50 text-green-800 border-green-200">
                                                            <AlertTitle>Success!</AlertTitle>
                                                            <AlertDescription>
                                                                {t("Your buy order has been placed successfully.")}
                                                            </AlertDescription>
                                                        </Alert>
                                                    )}
                                                    {buyError && (
                                                        <Alert variant="destructive">
                                                            <AlertCircle className="h-4 w-4" />
                                                            <AlertTitle>{t("Error")}</AlertTitle>
                                                            <AlertDescription>
                                                                {buyError}
                                                            </AlertDescription>
                                                        </Alert>
                                                    )}
                                                    <div className="grid gap-4">
                                                        <div className="grid gap-2">
                                                            <Label htmlFor="buy-quantity">{t("Quantity")}</Label>
                                                            <Input
                                                                id="buy-quantity"
                                                                type="number"
                                                                placeholder={t("Enter quantity")}
                                                                value={buyQuantity}
                                                                onChange={(e) => setBuyQuantity(e.target.value)}
                                                                min="1"
                                                                onKeyDown={(e) => {
                                                                    if (e.key === '-') {
                                                                        e.preventDefault();
                                                                    }
                                                                }}
                                                            />
                                                        </div>

                                                        <div className="grid gap-2">
                                                            <Label htmlFor="buy-order-type">{t("Order Type")}</Label>
                                                            <Select
                                                                value={buyOrderType}
                                                                onValueChange={setBuyOrderType}
                                                            >
                                                                <SelectTrigger id="buy-order-type">
                                                                    <SelectValue placeholder="Select order type" />
                                                                </SelectTrigger>
                                                                <SelectContent>
                                                                    <SelectItem value="MARKET">{t("Market")}</SelectItem>
                                                                    <SelectItem value="LIMIT">{t("Limit")}</SelectItem>
                                                                </SelectContent>
                                                            </Select>
                                                        </div>

                                                        {(buyOrderType === "LIMIT") && (
                                                            <div className="grid gap-2">
                                                                <Label htmlFor="buy-price">{t("Limit Price")}</Label>
                                                                <div className="relative">
                                                                    <Input
                                                                        id="buy-price"
                                                                        type="number"
                                                                        placeholder={t("Enter price")}
                                                                        value={buyPrice}
                                                                        onChange={(e) => setBuyPrice(e.target.value)}
                                                                        min="0.01"
                                                                        step="0.01"
                                                                        onKeyDown={(e) => {
                                                                            if (e.key === '-') {
                                                                                e.preventDefault();
                                                                            }
                                                                        }}
                                                                    />
                                                                    <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                                                                        <span className="text-muted-foreground">₺</span>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        )}

                                                        <div className="bg-muted p-4 rounded-md">
                                                            <h4 className="font-medium mb-2">{t("Order Summary")}</h4>
                                                            <div className="grid grid-cols-2 gap-2 text-sm">
                                                                <span className="text-muted-foreground">{t("Stock")}:</span>
                                                                <span>{selectedStock}</span>

                                                                <span className="text-muted-foreground">{t("Type")}:</span>
                                                                <span>Buy</span>

                                                                <span className="text-muted-foreground">{t("Order Type")}:</span>
                                                                <span>{buyOrderType}</span>

                                                                <span className="text-muted-foreground">{t("Quantity")}:</span>
                                                                <span>{buyQuantity || "-"}</span>

                                                                {(buyOrderType === "LIMIT") && (
                                                                    <>
                                                                        <span className="text-muted-foreground">{t("Price")}:</span>
                                                                        <span>₺{buyPrice || "-"}</span>
                                                                    </>
                                                                )}

                                                                <span className="text-muted-foreground">{t("Estimated Total")}:</span>
                                                                <span>
                                                                    ₺{
                                                                        buyQuantity && ((buyOrderType === "LIMIT") ? buyPrice : selectedEquityPrice)
                                                                            ? (parseFloat(buyQuantity) * ((buyOrderType === "LIMIT") ? parseFloat(buyPrice) : selectedEquityPrice)).toFixed(2)
                                                                            : "-"
                                                                    }
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </CardContent>
                                                <CardFooter>
                                                    <LoadingButton
                                                        onClick={handleBuyOrder}
                                                        loading={buyLoading}
                                                        className="w-full bg-green-600 hover:bg-green-700"
                                                    >
                                                        {t("Place Buy Order")}
                                                    </LoadingButton>
                                                </CardFooter>
                                            </Card>
                                        </TabsContent>
                                        <TabsContent value="sell">
                                            <Card>
                                                <CardHeader>
                                                    <CardTitle>{t("Sell")} {selectedStock} Current Price : {selectedEquityPrice}TL</CardTitle>
                                                    <CardDescription>
                                                        {t("Place an order to sell shares of")} {selectedStock}
                                                    </CardDescription>
                                                </CardHeader>
                                                <CardContent className="space-y-4">
                                                    {sellSuccess && (
                                                        <Alert className="bg-green-50 text-green-800 border-green-200">
                                                            <AlertTitle>{t("Success")}</AlertTitle>
                                                            <AlertDescription>
                                                                {t("Your sell order has been placed successfully.")}
                                                            </AlertDescription>
                                                        </Alert>
                                                    )}

                                                    {sellError && (
                                                        <Alert variant="destructive">
                                                            <AlertCircle className="h-4 w-4" />
                                                            <AlertTitle>Error</AlertTitle>
                                                            <AlertDescription>
                                                                {sellError}
                                                            </AlertDescription>
                                                        </Alert>
                                                    )}

                                                    <div className="grid gap-4">
                                                        <div className="grid gap-2">
                                                            <Label htmlFor="sell-quantity">{t("Quantity")}</Label>
                                                            <Input
                                                                id="sell-quantity"
                                                                type="number"
                                                                placeholder={t("Enter quantity")}
                                                                value={sellQuantity}
                                                                onChange={(e) => setSellQuantity(e.target.value)}
                                                                min="1"
                                                                onKeyDown={(e) => {
                                                                    if (e.key === '-') {
                                                                        e.preventDefault();
                                                                    }
                                                                }}
                                                            />
                                                        </div>

                                                        <div className="grid gap-2">
                                                            <Label htmlFor="sell-order-type">{t("Order Type")}</Label>
                                                            <Select
                                                                value={sellOrderType}
                                                                onValueChange={setSellOrderType}
                                                            >
                                                                <SelectTrigger id="sell-order-type">
                                                                    <SelectValue placeholder={t("Select order type")} />
                                                                </SelectTrigger>
                                                                <SelectContent>
                                                                    <SelectItem value="MARKET">{t("Market")}</SelectItem>
                                                                    <SelectItem value="LIMIT">{t("Limit")}</SelectItem>
                                                                </SelectContent>
                                                            </Select>
                                                        </div>

                                                        {(sellOrderType === "LIMIT") && (
                                                            <div className="grid gap-2">
                                                                <Label htmlFor="sell-price">{t("Limit Price")}</Label>
                                                                <div className="relative">
                                                                    <Input
                                                                        id="sell-price"
                                                                        type="number"
                                                                        placeholder={t("Enter price")}
                                                                        value={sellPrice}
                                                                        onChange={(e) => setSellPrice(e.target.value)}
                                                                        min="0.01"
                                                                        step="0.01"
                                                                        onKeyDown={(e) => {
                                                                            if (e.key === '-') {
                                                                                e.preventDefault();
                                                                            }
                                                                        }}
                                                                    />
                                                                    <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                                                                        <span className="text-muted-foreground">₺</span>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                        )}

                                                        <div className="bg-muted p-4 rounded-md">
                                                            <h4 className="font-medium mb-2">{t("Order Summary")}</h4>
                                                            <div className="grid grid-cols-2 gap-2 text-sm">
                                                                <span className="text-muted-foreground">{t("Stock")}:</span>
                                                                <span>{selectedStock}</span>

                                                                <span className="text-muted-foreground">{t("Type")}:</span>
                                                                <span>Sell</span>

                                                                <span className="text-muted-foreground">{t("Order Type")}:</span>
                                                                <span>{sellOrderType}</span>

                                                                <span className="text-muted-foreground">{t("Quantity")}:</span>
                                                                <span>{sellQuantity || "-"}</span>

                                                                {(sellOrderType === "LIMIT") && (
                                                                    <>
                                                                        <span className="text-muted-foreground">{t("Price")}:</span>
                                                                        <span>₺{sellPrice || "-"}</span>
                                                                    </>
                                                                )}

                                                                <span className="text-muted-foreground">{t("Estimated Total")}:</span>
                                                                <span>
                                                                    ₺{
                                                                        sellQuantity && ((sellOrderType === "LIMIT") ? sellPrice : selectedEquityPrice)
                                                                            ? (parseFloat(sellQuantity) * ((sellOrderType === "LIMIT") ? parseFloat(sellPrice) : selectedEquityPrice)).toFixed(2)
                                                                            : "-"
                                                                    }
                                                                </span>
                                                            </div>
                                                        </div>
                                                    </div>
                                                </CardContent>
                                                <CardFooter>
                                                    <LoadingButton
                                                        onClick={handleSellOrder}
                                                        loading={sellLoading}
                                                        className="w-full bg-red-600 hover:bg-red-700"
                                                    >
                                                        {t("Place Sell Order")}
                                                    </LoadingButton>
                                                </CardFooter>
                                            </Card>
                                        </TabsContent>
                                    </Tabs>
                                )}
                                {selectedStock && selectedCustomer && selectedAccount && (
                                    <Tabs defaultValue="orderbook" className="w-full mb-6">
                                        <TabsList className="grid w-full grid-cols-3">
                                            <TabsTrigger value="orderbook" className="flex items-center gap-2">
                                                {t("Orderbook")}
                                            </TabsTrigger>
                                            <TabsTrigger value="order-history" className="flex items-center gap-2">
                                                {t("Order History")}
                                            </TabsTrigger>
                                            <TabsTrigger value="trade-history" className="flex items-center gap-2">
                                                {t("Trade History")}
                                            </TabsTrigger>
                                        </TabsList>
                                        <TabsContent value="orderbook">
                                            <Card>
                                                <CardHeader>
                                                    <CardTitle>{t("Orderbook")}</CardTitle>
                                                    <CardDescription>
                                                        {t("Current orders for")} {selectedStock}
                                                    </CardDescription>
                                                </CardHeader>
                                                <CardContent>
                                                    {orderBookLoading ? (
                                                        <div className="flex justify-center py-8">
                                                            <IconLoader className="animate-spin" />
                                                        </div>
                                                    ) : orderBook ? (
                                                        <div className="grid grid-cols-2 gap-4">
                                                            <div>
                                                                <div className="mb-2 flex items-center justify-between">
                                                                    <h3 className="text-lg font-medium text-green-600">{t("Bids")}</h3>
                                                                    <span className="text-sm text-muted-foreground">{t("Buy Orders")}</span>
                                                                </div>
                                                                <div className="rounded-md border h-[350px] overflow-hidden">
                                                                    <div className="h-full overflow-auto">
                                                                        <Table>
                                                                            <TableHeader>
                                                                                <TableRow className="bg-muted/50 hover:bg-muted/50">
                                                                                    <TableHead className="w-[33%]">{t("Price")} (TL)</TableHead>
                                                                                    <TableHead className="text-right w-[33%]">{t("Amount")} ({orderBook?.equityCode})</TableHead>
                                                                                    <TableHead className="text-right w-[33%]">{t("Total")}</TableHead>
                                                                                </TableRow>
                                                                            </TableHeader>
                                                                            <TableBody>
                                                                                {(orderBook.bids || []).map((bid, index) => {
                                                                                    const maxTotal = Math.max(...(orderBook.bids || []).map(bid => bid.total || 0));
                                                                                    const fillPercentage = maxTotal > 0 ? ((bid.total || 0) / maxTotal) * 100 : 0;

                                                                                    return (
                                                                                        <TableRow
                                                                                            key={bid.orderId || index}
                                                                                            style={{
                                                                                                backgroundImage: `linear-gradient(to left, rgba(34, 197, 94, 0.1) ${fillPercentage}%, transparent ${fillPercentage}%)`
                                                                                            }}
                                                                                        >
                                                                                            <TableCell className="font-medium">
                                                                                                {(bid.price || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                                                            </TableCell>
                                                                                            <TableCell className="text-right">
                                                                                                {(bid.amount || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                                                            </TableCell>
                                                                                            <TableCell className="text-right">
                                                                                                {(bid.total || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                                                            </TableCell>
                                                                                        </TableRow>
                                                                                    );
                                                                                })}
                                                                            </TableBody>
                                                                        </Table>
                                                                    </div>
                                                                </div>
                                                                <div className="mt-2 text-center">
                                                                    <span className="text-green-600 font-medium">
                                                                        {orderBook.bids && orderBook.bids.length > 0
                                                                            ? (orderBook.bids[0].price || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
                                                                            : "0.00"}
                                                                    </span>
                                                                </div>
                                                            </div>

                                                            {/* Asks Table */}
                                                            <div>
                                                                <div className="mb-2 flex items-center justify-between">
                                                                    <h3 className="text-lg font-medium text-red-600">{t("Asks")}</h3>
                                                                    <span className="text-sm text-muted-foreground">{t("Sell Orders")}</span>
                                                                </div>
                                                                <div className="rounded-md border h-[350px] overflow-hidden">
                                                                    <div className="h-full overflow-auto">
                                                                        <Table>
                                                                            <TableHeader>
                                                                                <TableRow className="bg-muted/50 hover:bg-muted/50">
                                                                                    <TableHead className="w-[33%]">{t("Price")} (TL)</TableHead>
                                                                                    <TableHead className="w-[33%]">{t("Amount")} ({orderBook?.equityCode})</TableHead>
                                                                                    <TableHead className="text-right w-[33%]">{t("Total")}</TableHead>
                                                                                </TableRow>
                                                                            </TableHeader>
                                                                            <TableBody>
                                                                                {(orderBook.asks || []).map((ask, index) => {
                                                                                    const maxTotal = Math.max(...(orderBook.asks || []).map(ask => ask.total || 0));
                                                                                    const fillPercentage = maxTotal > 0 ? ((ask.total || 0) / maxTotal) * 100 : 0;
                                                                                    return (
                                                                                        <TableRow
                                                                                            key={ask.orderId || index}
                                                                                            style={{
                                                                                                backgroundImage: `linear-gradient(to right, rgba(239, 68, 68, 0.1) ${fillPercentage}%, transparent ${fillPercentage}%)`
                                                                                            }}
                                                                                        >
                                                                                            <TableCell className="font-medium">
                                                                                                {(ask.price || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                                                            </TableCell>
                                                                                            <TableCell>
                                                                                                {(ask.amount || 0).toLocaleString(undefined, { minimumFractionDigits: 8, maximumFractionDigits: 8 })}
                                                                                            </TableCell>
                                                                                            <TableCell className="text-right">
                                                                                                {(ask.total || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                                                                            </TableCell>
                                                                                        </TableRow>
                                                                                    );
                                                                                })}
                                                                            </TableBody>
                                                                        </Table>
                                                                    </div>
                                                                </div>
                                                                <div className="mt-2 text-center">
                                                                    <span className="text-red-600 font-medium">
                                                                        {orderBook.asks && orderBook.asks.length > 0
                                                                            ? (orderBook.asks[0].price || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })
                                                                            : "0.00"}
                                                                    </span>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    ) : (
                                                        <div className="text-center py-8 text-muted-foreground">
                                                            {t("No orderbook data available")}
                                                        </div>
                                                    )}
                                                </CardContent>
                                            </Card>
                                        </TabsContent>
                                        <TabsContent value="order-history">
                                            <Card>
                                                <CardHeader>
                                                    <CardTitle>{t("Order History")}</CardTitle>
                                                    <CardDescription>
                                                        {t("Recent orders for")} {selectedStock}
                                                    </CardDescription>
                                                </CardHeader>
                                                <CardContent>
                                                    <DataTable
                                                        columns={orderHistoryColumns}
                                                        data={orderHistory}
                                                        loading={orderHistoryLoading}
                                                        pagination={orderHistoryPagination}
                                                        setPagination={setOrderHistoryPagination}
                                                    />
                                                </CardContent>
                                            </Card>
                                        </TabsContent>
                                        <TabsContent value="trade-history">
                                            <Card>
                                                <CardHeader>
                                                    <CardTitle>{t("Trade History")}</CardTitle>
                                                    <CardDescription>
                                                        {t("Recent trades for")} {selectedStock}
                                                    </CardDescription>
                                                </CardHeader>
                                                <CardContent>
                                                    <DataTable
                                                        columns={tradeHistoryColumns}
                                                        data={tradeHistory}
                                                        loading={tradeHistoryLoading}
                                                        pagination={tradeHistoryPagination}
                                                        setPagination={setTradeHistoryPagination}
                                                    />
                                                </CardContent>
                                            </Card>
                                        </TabsContent>
                                    </Tabs>
                                )}
                            </div>
                        </div>
                    </div>
                </SidebarInset >
            </SidebarProvider >
            {error && (
                <Alert variant="destructive">
                    <AlertCircle className="h-4 w-4" />
                    <AlertTitle>{t("Error")}</AlertTitle>
                    <AlertDescription>
                        {error}
                    </AlertDescription>
                </Alert>
            )}
            {
                loading && <LoadingDialog isOpen={loading} />
            }
        </div >
    )
}
