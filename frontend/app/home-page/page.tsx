/* eslint-disable @typescript-eslint/no-explicit-any */
"use client"

import { AppSidebar } from "@/components/app-sidebar"
import { DataTable } from "@/components/data-table"
import { SiteHeader } from "@/components/site-header"
import {
    SidebarInset,
    SidebarProvider,
} from "@/components/ui/sidebar"
import { useEffect, useState } from "react"
import initSendRequest from "@/configs/sendRequest"
import { ColumnDef } from "@tanstack/react-table"
import { IconLoader, } from "@tabler/icons-react"
import { Label } from "@/components/ui/label"
import { HomePageChart } from "@/components/home-page-chart"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'
import { Button } from "@/components/ui/button"


export default function HomePage() {
    const sendRequest = initSendRequest()
    const { t } = useTranslation()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState("")
    const [stockDefinitions, setStockDefinitions] = useState([])
    const [selectedAssetCode, setSelectedAssetCode] = useState("")
    const [priceData, setPriceData] = useState([])
    const [chartLoading, setChartLoading] = useState(false);
    const [stockTableLoading,setStockTableLoading] = useState(false)
    const [stockPagination, setStockPagination] = useState({
        pageIndex: 0,
        pageSize: 5,
    })

    useEffect(() => {
        fetchStockDefinitonsData()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const fetchStockDefinitonsData = async () => {
        setLoading(true)
        try {
            setStockTableLoading(true)
            const response = await sendRequest.get("/equities?size=1000")
            setStockDefinitions(response.data.content)
        } catch (err: any) {
            setLoading(false)
            setStockTableLoading(false)
            setError(err.response?.data?.message || `Fetch Get Stock Definitions Catch Error : ${err.response?.data?.message}`)
            console.error(err)
        } finally {
            setLoading(false)
            setStockTableLoading(false)
        }
    }

    const handleShowDetails = async (ticker: string) => {
        setChartLoading(true);
        setLoading(true)
        setSelectedAssetCode(ticker);
        try {
            const response = await sendRequest.get(`/equities/${ticker}/prices`);
            setPriceData(response.data);
        } catch (err: any) {
            console.error(err)
            setPriceData([]);
        } finally {
            setChartLoading(false);
            setLoading(false)
        }
    };

    const equityColumns: ColumnDef<any>[] = [
        {
            accessorKey: "assetCode",
            header: t("Code"),
            cell: ({ row }) => row.original.assetCode,
        },
        {
            accessorKey: "equityName",
            header: t("Equity Name"),
            cell: ({ row }) => row.original.equityName,
        },
        {
            accessorKey: "closePrice",
            header: t("Instant Price"),
            cell: ({ row }) => {
                const price = row.original.closePrice;
                return (
                    <span>
                        {price} ₺
                    </span>
                );
            },
        },
        {
            accessorKey: "participation",
            header: t("Participation"),
            cell: ({ row }) => {
                const participation = row.original.participation;
                return ( 
                    <div className="text-center w-full">
                        <Label className={participation ? "text-green-600 font-medium" : "text-gray-500"}>
                            {participation ? "(K)" : "-"}
                        </Label>
                    </div>
                );
            },
        },
        {
            id: "changePercent",
            header: t("Change (%"),
            cell: ({ row }) => {
                const open = row.original.openPrice;
                const close = row.original.closePrice;
                if (typeof open !== "number" || typeof close !== "number") return "-";
                const change = ((close - open) / open) * 100;
                const formatted = change.toFixed(2) + " %";
                return (
                    <span style={{ color: change < 0 ? "red" : "green", fontWeight: "bold" }}>
                        {formatted}
                    </span>
                );
            },
        },
        {
            id: "details",
            header: "Details",
            cell: ({ row }) => (
                <Button
                    className="px-2 py-1"
                    onClick={() => {
                        handleShowDetails(row.original.assetCode);
                    }}
                >
                    {t("View")}
                </Button>
            ),
            size: 100,
        }
    ];

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
                    <SiteHeader title={t("Home Page")} />
                    <div className="flex flex-1 flex-col">
                        <div className="@container/main flex flex-1 flex-col gap-2">
                            <div className="flex flex-col gap-4 py-4 md:gap-6 md:py-6">
                                {error ? (
                                    <div className="text-red-500">{error}</div>
                                ) : (<>
                                    <Label className="font-bold text-3xl pl-6">{t("Stocks")}</Label>
                                    <DataTable loading={stockTableLoading} columns={equityColumns} data={stockDefinitions} pagination={stockPagination} setPagination={setStockPagination} />
                                    </>
                                )}
                                <div className="px-4 sm:px-6">
                                    {chartLoading ? (
                                        <div className="flex justify-center">
                                            <IconLoader className="animate-spin" />
                                        </div>
                                    ) : priceData.length > 0 ? (
                                        <HomePageChart title={`${selectedAssetCode} Price History`} data={priceData} />
                                    ) : (
                                        selectedAssetCode && <div>{t("Data not found")}</div>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                </SidebarInset>
            </SidebarProvider>
            {loading && <LoadingDialog isOpen={loading} />}
        </div>
    )
}