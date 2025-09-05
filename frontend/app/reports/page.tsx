/* eslint-disable @typescript-eslint/no-explicit-any */
"use client"

import { useState } from "react"
import { format } from "date-fns"
import { tr } from "date-fns/locale"
import jsPDF from "jspdf"
import autoTable from "jspdf-autotable"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { CustomerSearch, CustomerType } from "@/components/ui/customer-search"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { IconDownload, IconPrinter } from "@tabler/icons-react"
import initSendRequest from "@/configs/sendRequest"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { AlertCircle } from "lucide-react"
import { SectionCards } from "@/components/section-cards"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'

type PortfolioReportType = {
  accountId: number
  userFullName: string,
  freeCash: number,
  blockedCash: number,
  totalCash: number,
  totalUnrealizedPnl: number,
  totalUnrealizedPnlPct: number,
  holdingsValue: number,
  accountType: string
  identityNumber: string
  portfolioValue: number
  unrealizedPL: number
  holdings: HoldingType[]
}

type HoldingType = {
  [x: string]: number
  quantity: number
  avgCost: number
  currentPrice: number
  marketValue: number
  unrealizedPnl: number
  unrealizedPnlPct: number
  weight: number
}

export default function PortfolioReportPage() {
  const sendRequest = initSendRequest()
  const { t } = useTranslation()
  const [error, setError] = useState("")
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerType | null>(null)
  const [selectedAccount, setSelectedAccount] = useState<string>("")
  const [reportData, setReportData] = useState<PortfolioReportType | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(false)
  const [isPdfGenerating, setIsPdfGenerating] = useState<boolean>(false)

  const handleCustomerSelect = (customer: CustomerType | null) => {
    setSelectedCustomer(customer)
    setSelectedAccount("")
    setReportData(null)
  }

  const handleAccountSelect = async (accountId: string) => {
    setSelectedAccount(accountId)
    fetchReportData(accountId)
  }

  const fetchReportData = async (accountId: string) => {
    setIsLoading(true)
    try {
      const response = await sendRequest.get(`/report/portfolio?accountId=${accountId}`)
      setReportData(response.data)
    } catch (error: any) {
      setError(error.message)
    } finally {
      setIsLoading(false)
    }
  }

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('tr-TR', {
      style: 'currency',
      currency: 'TRY',
      currencyDisplay: 'code',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value)
  }

  const formatPercent = (value: number) => {
    return new Intl.NumberFormat('tr-TR', { style: 'percent', minimumFractionDigits: 2 }).format(value / 100)
  }

  const generatePDF = () => {
    if (!reportData) return

    setIsPdfGenerating(true)

    try {
      const doc = new jsPDF()
      const pageWidth = doc.internal.pageSize.getWidth()
      const today = format(new Date(), "dd MMMM yyyy", { locale: tr })

      doc.setFontSize(18)
      doc.text(t("Portfolio Report"), pageWidth / 2, 20, { align: "center" })

      doc.setFontSize(10)
      doc.text(today, pageWidth / 2, 28, { align: "center" })

      doc.setFontSize(12)
      doc.text(`${t("Customer")}: ${reportData.userFullName}`, 14, 40)
      doc.text(`${t("Identity Number")}: ${reportData.identityNumber}`, 14, 48)
      doc.text(`${t("Account Type")}: ${reportData.accountType}`, 14, 56)
      doc.text(`${t("Account ID")}: ${reportData.accountId}`, 14, 64)

      const summaryData = [
        [t("Free Cash"), formatCurrency(reportData?.freeCash)],
        [t("Blocked Cash"), formatCurrency(reportData?.blockedCash)],
        [t("Total Cash"), formatCurrency(reportData?.totalCash)],
        [t("Portfolio Value"), formatCurrency(reportData?.portfolioValue)],
        [t("Unrealized P/L"), formatCurrency(reportData?.totalUnrealizedPnl)],
        [t("Unrealized P/L %"), formatPercent(reportData?.totalUnrealizedPnlPct)],
        [t("Holdings Value"), formatCurrency(reportData?.holdingsValue || 0)]
      ]

      autoTable(doc, {
        startY: 75,
        head: [[t("Item"), t("Value")]],
        body: summaryData,
        theme: "grid",
        headStyles: { fillColor: [41, 128, 185], textColor: 255 },
        styles: { fontSize: 10 },
        columnStyles: { 0: { cellWidth: 80 } }
      })

      const holdingsData = reportData.holdings.map(holding => [
        holding.symbol,
        holding.freeQty?.toString(),
        formatCurrency(holding.avgCost),
        formatCurrency(holding.lastClosePrice),
        formatCurrency(holding.marketValue),
        formatCurrency(holding.unrealizedPnl),
        formatPercent(holding.unrealizedPnlPct),
        formatPercent((holding.marketValue / reportData.holdingsValue) * 100)
      ])

      // Second table - use a fixed position with enough space after the first table
      autoTable(doc, {
        startY: 180, // Use a fixed position with enough space for the first table
        head: [[
          t("Code"),
          t("Quantity"),
          t("Avg Cost"),
          t("Price"),
          t("Value"),
          t("Unr. P/L"),
          t("Unr. P/L %"),
          t("Weight")
        ]],
        body: holdingsData,
        theme: "grid",
        headStyles: { fillColor: [41, 128, 185], textColor: 255 },
        styles: { fontSize: 8 },
        columnStyles: {
          0: { cellWidth: 20 },
          1: { cellWidth: 20 }
        }
      })

      // Footer
      const pageCount = (doc.internal as any).getNumberOfPages()
      for (let i = 1; i <= pageCount; i++) {
        doc.setPage(i)
        doc.setFontSize(8)
        doc.text(
          `${t("Page")} ${i} / ${pageCount}`,
          pageWidth / 2,
          doc.internal.pageSize.getHeight() - 10,
          { align: "center" }
        )
      }

      doc.save(`portfolio-report-${reportData.accountId}-${format(new Date(), "yyyyMMdd")}.pdf`)
    } catch (error) {
      console.error("Error generating PDF:", error)
    } finally {
      setIsPdfGenerating(false)
    }
  }

  // Print report
  const printReport = () => {
    if (!reportData) return

    const printWindow = window.open('', '_blank')
    if (!printWindow) return

    const today = format(new Date(), "dd MMMM yyyy", { locale: tr })

    printWindow.document.write(`
      <!DOCTYPE html>
      <html>
        <head>
          <title>${t("Portfolio Report")}</title>
          <style>
            body {
              font-family: Arial, sans-serif;
              margin: 20px;
              color: #333;
            }
            .header {
              text-align: center;
              margin-bottom: 30px;
            }
            .customer-info {
              margin-bottom: 20px;
            }
            table {
              width: 100%;
              border-collapse: collapse;
              margin-bottom: 30px;
            }
            th, td {
              border: 1px solid #ddd;
              padding: 8px;
              text-align: left;
            }
            th {
              background-color: #2980b9;
              color: white;
            }
            .summary-table {
              width: 50%;
            }
            .holdings-table {
              font-size: 12px;
            }
            .text-right {
              text-align: right;
            }
            .footer {
              text-align: center;
              font-size: 12px;
              margin-top: 30px;
            }
            @media print {
              .no-print {
                display: none;
              }
            }
          </style>
        </head>
        <body>
          <div class="header">
            <h1>${t("Portfolio Report")}</h1>
            <p>${today}</p>
          </div>
          
          <div class="customer-info">
            <p><strong>${t("Customer")}:</strong> ${reportData.userFullName}</p>
            <p><strong>${t("Identity Number")}:</strong> ${reportData.identityNumber}</p>
            <p><strong>${t("Account Type")}:</strong> ${reportData.accountType}</p>
            <p><strong>${t("Account ID")}:</strong> ${reportData.accountId}</p>
          </div>
          
          <h2>${t("Summary")}</h2>
          <table class="summary-table">
            <tr>
              <th>${t("Item")}</th>
              <th>${t("Value")}</th>
            </tr>
            <tr>
              <td>${t("Free Cash")}</td>
              <td class="text-right">${formatCurrency(reportData?.freeCash)}</td>
            </tr>
            <tr>
              <td>${t("Blocked Cash")}</td>
              <td class="text-right">${formatCurrency(reportData?.blockedCash)}</td>
            </tr>
            <tr>
              <td>${t("Total Cash")}</td>
              <td class="text-right">${formatCurrency(reportData?.totalCash)}</td>
            </tr>
            <tr>
              <td>${t("Portfolio Value")}</td>
              <td class="text-right">${formatCurrency(reportData?.portfolioValue)}</td>
            </tr>
            <tr>
              <td>${t("Unrealized P/L")}</td>
              <td class="text-right">${formatCurrency(reportData?.totalUnrealizedPnl)}</td>
            </tr> 
            <tr>
              <td>${t("Unrealized P/L %")}</td>
              <td class="text-right">${formatPercent(reportData?.totalUnrealizedPnlPct)}</td>
            </tr>
            <tr>
              <td>${t("Holdings Value")}</td>
              <td class="text-right">${formatCurrency(reportData?.holdingsValue)}</td>
            </tr>
          </table>
          
          <h2>${t("Holdings")}</h2>
          <table class="holdings-table">
            <tr>
              <th>${t("Code")}</th>
              <th>${t("Quantity")}</th>
              <th>${t("Avg Cost")}</th>
              <th>${t("Price")}</th>
              <th>${t("Value")}</th>
              <th>${t("Unr. P/L")}</th>
              <th>${t("Unr. P/L %")}</th>
            </tr>
            ${reportData.holdings.map(holding => `
              <tr>
                <td>${holding.symbol}</td>
                <td class="text-right">${holding.freeQty}</td>
                <td class="text-right">${formatCurrency(holding.avgCost)}</td>
                <td class="text-right">${formatCurrency(holding.lastClosePrice)}</td>
                <td class="text-right">${formatCurrency(holding.marketValue)}</td>
                <td class="text-right">${formatCurrency(holding.unrealizedPnl)}</td>
                <td class="text-right">${formatPercent(holding.unrealizedPnlPct)}</td>
              </tr>
            `).join('')}
          </table>
          
          <div class="footer">
            <p>${t("Generated on")} ${today}</p>
          </div>
          
          <div class="no-print">
            <button onclick="window.print();">${t("Print")}</button>
          </div>
          
          <script>
            window.onload = function() {
              setTimeout(function() {
                window.print();
              }, 500);
            }
          </script>
        </body>
      </html>
    `)

    printWindow.document.close()
  }

  return (
    <div className="flex min-h-screen">
      <SidebarProvider>
        <AppSidebar variant="inset" />
        <SidebarInset>
          <div className="flex-1">
            <SiteHeader title={t("Portfolio Report")} />
            <main className="flex-1 space-y-4 p-4 md:p-8">
              <div className="flex items-center justify-between">
                <h1 className="text-2xl font-bold">{t("Portfolio Report")}</h1>
              </div>

              <div className="grid gap-4 md:grid-cols-1 lg:grid-cols-1">
                {/* Customer Search */}
                <Card>
                  <CardHeader>
                    <CardTitle>{t("Select Customer")}</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <CustomerSearch onSelect={handleCustomerSelect}
                      selectedCustomer={selectedCustomer}
                      placeholder={t("Search for a customer...")} />
                    {selectedCustomer && (
                      <div className="mt-4 text-sm">
                        <p><strong>{t("Selected")}:</strong> {selectedCustomer.firstName + " " + selectedCustomer.lastName}</p>
                        <p><strong>{t("Identity Number")}:</strong> {selectedCustomer.identityNumber}</p>
                      </div>
                    )}
                    <div className="mt-3 mb-3">
                      <Select
                        value={selectedAccount}
                        onValueChange={handleAccountSelect}
                        disabled={!selectedCustomer || !selectedCustomer.accounts?.length}
                      >
                        <SelectTrigger>
                          <SelectValue placeholder={t("Select an account")} />
                        </SelectTrigger>
                        <SelectContent>
                          {selectedCustomer?.accounts?.map((account) => (
                            <SelectItem key={account.accountId} value={account.accountId?.toString()}>
                              <div className="flex justify-between items-center w-full">
                                <span>{account.accountType}</span>
                                <span className="text-muted-foreground ml-2">
                                  {account.equities?.length || 0} {t("stocks")}
                                </span>
                              </div>
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="flex space-x-2">
                      <Button
                        onClick={generatePDF}
                        disabled={!reportData || isPdfGenerating}
                        className="flex-1"
                      >
                        <IconDownload className="mr-2 h-4 w-4" />
                        {isPdfGenerating ? t("Generating...") : t("Download PDF")}
                      </Button>
                      <Button
                        onClick={printReport}
                        disabled={!reportData}
                        variant="outline"
                        className="flex-1"
                      >
                        <IconPrinter className="mr-2 h-4 w-4" />
                        {t("Print")}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </main>
          </div>
          {reportData && (
            <div className="space-y-2 p-4 pt-0">
              <h2 className="text-xl font-bold">{t("Portfolio Summary")}</h2>
              <SectionCards
                className="md:grid-cols-2 @5xl/main:grid-cols-2"
                cards={[
                  {
                    title: "cash-balance",
                    description: t("Cash Balances"),
                    value: formatCurrency(reportData?.freeCash),
                    footerText: t("Free") + ": " + formatCurrency(reportData?.freeCash),
                    footerSubtext: t("Blocked") + ": " + formatCurrency(reportData?.blockedCash)
                  },
                  {
                    title: "portfolio-value",
                    description: t("Portfolio Value"),
                    value: formatCurrency(reportData?.portfolioValue),
                    trend: reportData?.totalUnrealizedPnlPct >= 0 ? 'up' : 'down',
                    trendValue: formatPercent(reportData?.totalUnrealizedPnlPct),
                    footerText: t("Total Unrealized P/L") + ": " + formatCurrency(reportData?.totalUnrealizedPnl),
                    footerSubtext: t("Percentage") + ": " + formatPercent(reportData?.totalUnrealizedPnlPct),
                    valueClassName: reportData?.totalUnrealizedPnl >= 0 ? 'text-green-600' : 'text-red-600'
                  }
                ]}
              />
              <h2 className="text-xl font-bold mt-6">{t("Holdings")}</h2>
              <div className="rounded-md border">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>{t("Code")}</TableHead>
                      <TableHead className="text-right">{t("Quantity")}</TableHead>
                      <TableHead className="text-right">{t("Avg Cost")}</TableHead>
                      <TableHead className="text-right">{t("Price")}</TableHead>
                      <TableHead className="text-right">{t("Value")}</TableHead>
                      <TableHead className="text-right">{t("Unr. P/L")}</TableHead>
                      <TableHead className="text-right">{t("Unr. P/L %")}</TableHead>
                      <TableHead className="text-right">{t("Weight")}</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {reportData.holdings.map((holding, index) => (
                      <TableRow key={`${holding.symbol}-${index}`}>
                        <TableCell className="font-medium">{holding.symbol}</TableCell>
                        <TableCell className="text-right">{holding.freeQty}</TableCell>
                        <TableCell className="text-right">{formatCurrency(holding.avgCost)}</TableCell>
                        <TableCell className="text-right">{formatCurrency(holding.lastClosePrice)}</TableCell>
                        <TableCell className="text-right">{formatCurrency(holding.marketValue)}</TableCell>
                        <TableCell className={`text-right ${holding.unrealizedPnl >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                          {formatCurrency(holding.unrealizedPnl)}
                        </TableCell>
                        <TableCell className={`text-right ${holding.unrealizedPnlPct >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                          {formatPercent(holding.unrealizedPnlPct)}
                        </TableCell>
                        <TableCell className="text-right">
                          {formatPercent(
                            (reportData.holdingsValue && reportData.holdingsValue > 0)
                              ? (holding.marketValue / reportData.holdingsValue) * 100
                              : (reportData.portfolioValue && reportData.portfolioValue > 0)
                                ? (holding.marketValue / reportData.portfolioValue) * 100
                                : 0
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </div>
          )}
        </SidebarInset>
      </SidebarProvider>

      {isLoading && (<LoadingDialog isOpen={isLoading} />)}

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
