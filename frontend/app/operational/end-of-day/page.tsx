/* eslint-disable @typescript-eslint/no-unused-vars */
/* eslint-disable @typescript-eslint/no-explicit-any */
"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { AlertCircle, Calendar, RefreshCw } from "lucide-react"
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert"
import { LoadingButton } from "@/components/ui/loading"
import { AppSidebar } from "@/components/app-sidebar"
import { SiteHeader } from "@/components/site-header"
import { SidebarInset, SidebarProvider } from "@/components/ui/sidebar"
import { LoadingDialog } from "@/components/ui/loading-dialog"
import initSendRequest from "@/configs/sendRequest"
import { format } from "date-fns"
import { tr } from "date-fns/locale"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'


export default function EndOfDay() {
    const { t } = useTranslation()
    const sendRequest = initSendRequest()
    const [systemDate, setSystemDate] = useState<string | null>(null)
    const [loading, setLoading] = useState<boolean>(false)
    const [refreshing, setRefreshing] = useState<boolean>(false)
    const [success, setSuccess] = useState<boolean>(false)
    const [error, setError] = useState<string | null>(null)

    useEffect(() => {
        fetchSystemDate()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const fetchSystemDate = async () => {
        setRefreshing(true)
        setError(null)

        try {
            const response = await sendRequest.get("/system-date")

            if (response && response.data) {
                setSystemDate(response.data)
            } else {
                setError("Could not retrieve system date")
            }
        } catch (err: any) {
            setError(err.message || "Failed to fetch system date")
        } finally {
            setRefreshing(false)
        }
    }

    const triggerEndOfDay = async () => {
        setLoading(true)
        setSuccess(false)
        setError(null)

        try {
            const response = await sendRequest.post("/system-date/trigger-eod")
            setSuccess(true)
            await fetchSystemDate()
        } catch (err: any) {
            setError(err.message || "Failed to trigger end-of-day process")
            setSuccess(false)
        } finally {
            setTimeout(() => {
                setSuccess(false)
            }, 3000)
            setLoading(false)
        }
    }
    const handleReset = async () => {
        setLoading(true)
        setSuccess(false)
        setError(null)

        try {
            const response = await sendRequest.post("/system-date/reset-to-today")
            setSuccess(true)
            await fetchSystemDate()
        } catch (err: any) {
            setError(err.message || "Failed to reset system date to today")
            setSuccess(false)
        } finally {
            setTimeout(() => {
                setSuccess(false)
            }, 3000)
            setLoading(false)
        }
    }

    return (
        <div >
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
                    <SiteHeader title={t("End Of Day")} />
                    <div className="flex flex-1 flex-col">
                        <div className="@container/main flex flex-1 flex-col gap-2">
                            <div className="container px-4 mx-auto py-4 md:py-6">
                                <div className="flex justify-between items-center mb-6">
                                    <Card className="w-full">
                                        <CardHeader>
                                            <CardTitle className="flex items-center gap-2">
                                                <Calendar className="h-5 w-5" />
                                                {t("Current System Date")}
                                            </CardTitle>
                                            <CardDescription>
                                                {t("The current system date used for all transactions")}
                                            </CardDescription>
                                        </CardHeader>
                                        <CardContent>
                                            <div className="flex flex-col gap-4">
                                                {error && (
                                                    <Alert variant="destructive">
                                                        <AlertCircle className="h-4 w-4" />
                                                        <AlertTitle>{t("Error")}</AlertTitle>
                                                        <AlertDescription>{error}</AlertDescription>
                                                    </Alert>
                                                )}

                                                {success && (
                                                    <Alert className="bg-green-50 text-green-800 border-green-200">
                                                        <AlertTitle>Success</AlertTitle>
                                                        <AlertDescription>
                                                            {t("End of day process completed successfully")}
                                                        </AlertDescription>
                                                    </Alert>
                                                )}

                                                <div className="flex items-center justify-between">
                                                    <div className="text-2xl font-semibold">
                                                        {systemDate ? systemDate : t("Loading...")}
                                                    </div>
                                                    <Button
                                                        variant="outline"
                                                        size="sm"
                                                        onClick={handleReset}
                                                        disabled={refreshing}
                                                    >
                                                        <RefreshCw className={`h-4 w-4 mr-2 ${refreshing ? 'animate-spin' : ''}`} />
                                                        {t("Refresh")}
                                                    </Button>
                                                </div>
                                            </div>
                                        </CardContent>
                                        <CardFooter>
                                            <LoadingButton
                                                onClick={triggerEndOfDay}
                                                loading={loading}
                                                className="w-full"
                                                disabled={!systemDate || loading}
                                            >
                                                {t("Trigger End Of Day")}
                                            </LoadingButton>
                                        </CardFooter>
                                    </Card>
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
