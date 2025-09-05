/* eslint-disable @typescript-eslint/no-explicit-any */
"use client"

import { useState, useEffect } from "react"
import { AppSidebar } from "@/components/app-sidebar"
import { DataTable } from "@/components/data-table"
import { SiteHeader } from "@/components/site-header"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Button } from "@/components/ui/button"
import { ColumnDef } from "@tanstack/react-table"
import { IconSearch, IconFilter, IconDownload, IconEdit, IconDots, IconTrash } from "@tabler/icons-react"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { format } from "date-fns"
import { tr } from "date-fns/locale"
import { subDays } from "date-fns"
import initSendRequest from "@/configs/sendRequest"
import { SimpleDateRangePicker } from "@/components/ui/simple-date-range-picker"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { AlertCircle } from "lucide-react"

// DateRange type definition
type DateRange = {
  from?: Date
  to?: Date
}

interface OrderHistoryItem {
  firstName: any
  lastName: any
  orderId: number
  accountId: number
  equityId: number
  orderSide: "BUY" | "SELL"
  oldOrderQuantity: number
  oldPrice: number
  orderStatus: "COMPLETED" | "PENDING" | "CANCELLED" | "REJECTED" | "PROCESSING" | "PARTIALLY_FILLED"
  transactionTime: string
  orderType: "MARKET" | "LIMIT"
}

interface EditOrderData {
  orderQuantity: number
  price: number
  orderType: "MARKET" | "LIMIT"
}

export default function OrderHistory() {
  const { t } = useTranslation()
  const sendRequest = initSendRequest()
  const [error,setError]=useState("")
  const [loading, setLoading] = useState(false)
  const [orders, setOrders] = useState<OrderHistoryItem[]>([])
  const [filteredOrders, setFilteredOrders] = useState<OrderHistoryItem[]>([])
  const [editLoading, setEditLoading] = useState(false)
  const [editDialogOpen, setEditDialogOpen] = useState(false)
  const [editingOrder, setEditingOrder] = useState<OrderHistoryItem | null>(null)
  const [editFormData, setEditFormData] = useState<EditOrderData>({
    orderQuantity: 0,
    price: 0,
    orderType: "MARKET"
  })

  const [searchQuery, setSearchQuery] = useState("")
  const [orderSideFilter, setOrderSideFilter] = useState<string | undefined>("all")
  const [statusFilter, setStatusFilter] = useState<string | undefined>("all")
  const [dateRange, setDateRange] = useState<DateRange | undefined>({
    from: subDays(new Date(), 30),
    to: new Date()
  })
  const [pagination, setPagination] = useState({
    pageIndex: 0,
    pageSize: 10,
  })

  useEffect(() => {
    fetchOrderHistory()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    applyFilters()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchQuery, orderSideFilter, statusFilter, dateRange, orders])


  const fetchOrderHistory = async () => {
    setLoading(true)
    try {
      const response = await sendRequest.get("/order-histories")
      setOrders(response.data.content)
      setFilteredOrders(response.data.content)
      console.log(response.data)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const fetchOrderHistoryDetail = async (orderId: number) => {
    setEditLoading(true)
    try {
      const response = await sendRequest.get(`/equity-order/${orderId}`)
      const orderData = response.data
      setEditFormData({
        orderQuantity: orderData.orderQuantity,
        price: orderData.price,
        orderType: orderData.orderType
      })
    } catch (err: any) {
      setError(err.message)
    } finally {
      setEditLoading(false)
    }
  }

  const handleDeleteOrder = async (order: OrderHistoryItem) => {
    try {
      setLoading(true)
      await sendRequest.delete(`/equity-order/${order.orderId}`)
      setTimeout(() => fetchOrderHistory(), 3000)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleEditOrder = (order: OrderHistoryItem) => {
    setEditingOrder(order)
    fetchOrderHistoryDetail(order.orderId)
    setEditDialogOpen(true)
  }

  const handleSaveEdit = async () => {
    if (!editingOrder) return

    setEditLoading(true)
    try {
      setLoading(true)
      await sendRequest.patch(`/equity-order/${editingOrder.orderId}`, editFormData)

      setTimeout(() => fetchOrderHistory(), 3000)

      setOrders(prevOrders =>
        prevOrders.map(order =>
          order.orderId === editingOrder.orderId
            ? { ...order, ...editFormData }
            : order
        )
      )

      setEditDialogOpen(false)
      setEditingOrder(null)
    } catch (err: any) {
      setError(err.message)
    } finally {
      setEditLoading(false)
      setLoading(false)
    }
  }

  const handleCancelEdit = () => {
    setEditDialogOpen(false)
    setEditingOrder(null)
    setEditFormData({
      orderQuantity: 0,
      price: 0,
      orderType: "MARKET"
    })
  }

  const applyFilters = () => {
    let filtered = [...orders]
    if (searchQuery) {
      filtered = filtered.filter(order =>
        order.firstName.toString().toLowerCase().includes(searchQuery.toLowerCase()) ||
        order.lastName.toString().toLowerCase().includes(searchQuery.toLowerCase())
      )
    }
    if (orderSideFilter && orderSideFilter !== "all") {
      filtered = filtered.filter(order => order.orderSide === orderSideFilter)
    }
    if (statusFilter && statusFilter !== "all") {
      filtered = filtered.filter(order => order.orderStatus === statusFilter)
    }
    if (dateRange?.from && dateRange?.to) {
      filtered = filtered.filter(order => {
        const transactionTime = new Date(order.transactionTime)
        return transactionTime >= dateRange.from! && transactionTime <= dateRange.to!
      })
    }
    setFilteredOrders(filtered)
  }

  const resetFilters = () => {
    setSearchQuery("")
    setOrderSideFilter("all")
    setStatusFilter("all")
    setDateRange({ from: subDays(new Date(), 30), to: new Date() })
  }

  const exportToCSV = () => {
    const headers = [
      "Order ID",
      "First Name",
      "Last Name",
      "Account ID",
      "Equity ID",
      "Side",
      "Type",
      "Quantity",
      "Price",
      "Total Value",
      "Transaction Time",
      "Status"
    ]

    const csvData = filteredOrders.map(order => [
      order.orderId,
      order.firstName,
      order.lastName,
      order.accountId,
      order.equityId,
      order.orderSide,
      order.orderType,
      order.oldOrderQuantity,
      order.oldPrice,
      (order.oldOrderQuantity * order.oldPrice).toFixed(2),
      format(new Date(order.transactionTime), "dd.MM.yyyy HH:mm", { locale: tr }),
      order.orderStatus
    ])

    const csvContent = [headers, ...csvData]
      .map(row => row.join(","))
      .join("\n")

    const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" })
    const link = document.createElement("a")
    const url = URL.createObjectURL(blob)
    link.setAttribute("href", url)
    link.setAttribute("download", `order-history-${format(new Date(), "yyyy-MM-dd")}.csv`)
    link.style.visibility = "hidden"
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }

  const columns: ColumnDef<OrderHistoryItem>[] = [
    {
      accessorKey: "orderId",
      header: "Order ID",
      cell: ({ row }) => <span className="font-medium">#{row.original.orderId}</span>,
    },
    {
      accessorKey: "accountId",
      header: "Account",
      cell: ({ row }) => <span className="text-sm">ACC-{row.original.accountId}</span>,
    },
    {
      accessorKey: "customerName",
      header: "Customer",
      cell: ({ row }) => (
        <span className="text-sm">
          {`${row.original.firstName} ${row.original.lastName}`}
        </span>
      ),
    },
    {
      accessorKey: "equityId",
      header: "Equity",
      cell: ({ row }) => <span className="text-sm">EQ-{row.original.equityId}</span>,
    },
    {
      accessorKey: "orderSide",
      header: "Side",
      cell: ({ row }) => {
        const side = row.original.orderSide
        return (
          <Badge variant={side === "BUY" ? "default" : "outline"} className={
            side === "BUY"
              ? "bg-green-100 text-green-800 hover:bg-green-100 hover:text-green-800"
              : "bg-red-100 text-red-800 hover:bg-red-100 hover:text-red-800"
          }>
            {side}
          </Badge>
        )
      },
    },
    {
      accessorKey: "orderType",
      header: "Type",
      cell: ({ row }) => {
        const type = row.original.orderType
        return (
          <Badge variant="secondary" className="bg-blue-100 text-blue-800 hover:bg-blue-100 hover:text-blue-800">
            {type}
          </Badge>
        )
      },
    },
    {
      accessorKey: "oldOrderQuantity",
      header: "Quantity",
      cell: ({ row }) => row.original.oldOrderQuantity?.toLocaleString(),
    },
    {
      accessorKey: "oldPrice",
      header: "Price",
      cell: ({ row }) => `₺${row.original.oldPrice?.toFixed(2)}`,
    },
    {
      accessorKey: "totalValue",
      header: "Total Value",
      cell: ({ row }) => `₺${(row.original.oldOrderQuantity * row.original.oldPrice)?.toFixed(2)}`,
    },
    {
      accessorKey: "transactionTime",
      header: "Transaction Time",
      cell: ({ row }) => {
        try {
          const dateValue = row.original.transactionTime
          if (!dateValue) return "N/A"
          const date = new Date(dateValue)
          if (isNaN(date.getTime())) return "Invalid Date"
          return format(date, "dd.MM.yyyy HH:mm", { locale: tr })
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (error: any) {
          return "Invalid Date"
        }
      },
    },
    {
      accessorKey: "orderStatus",
      header: "Status",
      cell: ({ row }) => {
        const status = row.original.orderStatus
        let badgeClass = ""

        switch (status) {
          case "COMPLETED":
            badgeClass = "bg-green-100 text-green-800 hover:bg-green-100 hover:text-green-800"
            break
          case "PENDING":
            badgeClass = "bg-yellow-100 text-yellow-800 hover:bg-yellow-100 hover:text-yellow-800"
            break
          case "PROCESSING":
            badgeClass = "bg-blue-100 text-blue-800 hover:bg-blue-100 hover:text-blue-800"
            break
          case "PARTIALLY_FILLED":
            badgeClass = "bg-orange-100 text-orange-800 hover:bg-orange-100 hover:text-orange-800"
            break
          case "CANCELLED":
            badgeClass = "bg-gray-100 text-gray-800 hover:bg-gray-100 hover:text-gray-800"
            break
          case "REJECTED":
            badgeClass = "bg-red-100 text-red-800 hover:bg-red-100 hover:text-red-800"
            break
          default:
            badgeClass = "bg-gray-100 text-gray-800 hover:bg-gray-100 hover:text-gray-800"
        }

        return (
          <Badge variant="outline" className={badgeClass}>
            {status}
          </Badge>
        )
      },
    },
    {
      accessorKey: "actions",
      header: "",
      cell: ({ row }) => {
        const status = row.original.orderStatus;
        const orderType = row.original.orderType;
        
        if (status === "COMPLETED" || status === "CANCELLED" || status === "REJECTED") {
          return null;
        }

        return (
          <Popover>
            <PopoverTrigger asChild>
              <Button
                variant="ghost"
                size="icon"
                className="h-8 w-8 p-0"
              >
                <IconDots className="h-4 w-4" />
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-48 p-1" align="end">
              <div className="flex flex-col gap-1">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => orderType !== "MARKET" ? handleEditOrder(row.original) : null}
                  className={`flex items-center gap-2 justify-start h-8 px-2 ${
                    orderType === "MARKET" ? "opacity-50 cursor-not-allowed" : ""
                  }`}
                  disabled={orderType === "MARKET"}
                  title={orderType === "MARKET" ? t("Market orders cannot be edited") : ""}
                >
                  <IconEdit className="h-4 w-4" />
                  {t("Edit")}
                  {orderType === "MARKET" && (
                    <span className="text-xs text-muted-foreground ml-1">({t("Market")})</span>
                  )}
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleDeleteOrder(row.original)}
                  className="flex items-center gap-2 justify-start h-8 px-2 text-red-600 hover:text-red-700 hover:bg-red-50"
                >
                  <IconTrash className="h-4 w-4" />
                  {t("Delete")}
                </Button>
              </div>
            </PopoverContent>
          </Popover>
        );
      },
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
          <SiteHeader title={t("Order History")} />
          <div className="flex flex-1 flex-col">
            <div className="@container/main flex flex-1 flex-col gap-2">
              <div className="container px-4 mx-auto py-4 md:py-6">
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <IconFilter className="h-5 w-5" />
                      {t("Filters")}
                    </CardTitle>
                    <CardDescription>
                      {t("Filter and search order history")}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="flex flex-col gap-4">
                      <div className="flex flex-row space-x-4">
                        <div className="w-1/2">
                          <Label htmlFor="search" className="block mb-2">{t("Search")}</Label>
                          <div className="relative">
                            <IconSearch className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                            <Input
                              id="search"
                              placeholder={t("Search by Order ID, Account, or Equity...")}
                              className="pl-8"
                              value={searchQuery}
                              onChange={(e) => setSearchQuery(e.target.value)}
                            />
                          </div>
                        </div>
                        <div className="w-1/2">
                          <Label htmlFor="date-range" className="block mb-2">{t("Date Range")}</Label>
                          <SimpleDateRangePicker
                            date={dateRange}
                            setDate={setDateRange}
                          />
                        </div>
                      </div>
                      <div className="flex flex-row space-x-4">
                        <div className="">
                          <Label htmlFor="order-side" className="block mb-2">{t("Order Side")}</Label>
                          <Select
                            value={orderSideFilter}
                            onValueChange={setOrderSideFilter}
                          >
                            <SelectTrigger id="order-side">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="all">{t("All Sides")}</SelectItem>
                              <SelectItem value="BUY">{t("Buy")}</SelectItem>
                              <SelectItem value="SELL">{t("Sell")}</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                        <div className="">
                          <Label htmlFor="status" className="block mb-2">{t("Status")}</Label>
                          <Select
                            value={statusFilter}
                            onValueChange={setStatusFilter}
                          >
                            <SelectTrigger id="status">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="all">{t("All Statuses")}</SelectItem>
                              <SelectItem value="COMPLETED">{t("Completed")}</SelectItem>
                              <SelectItem value="PENDING">{t("Pending")}</SelectItem>
                              <SelectItem value="PROCESSING">{t("Processing")}</SelectItem>
                              <SelectItem value="PARTIALLY_FILLED">{t("Partially Filled")}</SelectItem>
                              <SelectItem value="CANCELED">{t("Cancelled")}</SelectItem>
                              <SelectItem value="REJECTED">{t("Rejected")}</SelectItem>
                            </SelectContent>
                          </Select>
                        </div>
                      </div>
                    </div>

                    <div className="mt-4 flex justify-between">
                      <Button variant="outline" onClick={resetFilters}>
                        {t("Reset Filters")}
                      </Button>
                      <Button
                        variant="outline"
                        className="flex items-center gap-2"
                        onClick={exportToCSV}
                      >
                        <IconDownload className="h-4 w-4" />
                        {t("Export to CSV")}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
                <Card className="mt-4">
                  <CardHeader>
                    <CardTitle>{t("Order History")}</CardTitle>
                    <CardDescription>
                      {t(`Showing ${filteredOrders.length} orders`)}
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <DataTable
                      columns={columns}
                      data={filteredOrders}
                      pagination={pagination}
                      setPagination={setPagination}
                      loading={loading}
                    />
                  </CardContent>
                </Card>
              </div>
            </div>
          </div>
        </SidebarInset>
      </SidebarProvider>
      {loading && <LoadingDialog isOpen={loading} />}
      <Dialog open={editDialogOpen} onOpenChange={setEditDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>{t("Edit Order")}</DialogTitle>
            <DialogDescription>
              {editingOrder && `${t("Order ID")}: #${editingOrder.orderId}`}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid gap-2">
              <Label htmlFor="edit-quantity">{t("Quantity")}</Label>
              <Input
                id="edit-quantity"
                type="number"
                value={editFormData.orderQuantity}
                onChange={(e) => setEditFormData(prev => ({
                  ...prev,
                  orderQuantity: parseInt(e.target.value) || 0
                }))}
                disabled={editLoading}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="edit-price">{t("Price")}</Label>
              <Input
                id="edit-price"
                type="number"
                step="0.01"
                value={editFormData.price}
                onChange={(e) => setEditFormData(prev => ({
                  ...prev,
                  price: parseFloat(e.target.value) || 0
                }))}
                disabled={editLoading || editFormData.orderType === "MARKET"}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="edit-order-type">{t("Order Type")}</Label>
              <Select
                value={editFormData.orderType}
                onValueChange={(value: "MARKET" | "LIMIT") => setEditFormData(prev => ({
                  ...prev,
                  orderType: value
                }))}
                disabled
              >
                <SelectTrigger id="edit-order-type">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MARKET">{t("Market")}</SelectItem>
                  <SelectItem value="LIMIT">{t("Limit")}</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant="outline"
              onClick={handleCancelEdit}
              disabled={editLoading}
            >
              {t("Cancel")}
            </Button>
            <Button
              onClick={handleSaveEdit}
              disabled={editLoading}
            >
              {editLoading ? t("Saving...") : t("Save Changes")}
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
    </div>
  )
}