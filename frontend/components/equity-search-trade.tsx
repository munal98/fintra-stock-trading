/* eslint-disable react-hooks/exhaustive-deps */
/* eslint-disable @typescript-eslint/no-explicit-any */

import { useState, useEffect, useRef } from "react"
import { Search, X, TrendingUp } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover"
import initSendRequest from "@/configs/sendRequest"

export interface EquityType {
    assetCode: string
    equityName: string
    openPrice: number
    equityId: number
    symbol: string
    name?: string
    price?: number
}

interface EquitySearchProps {
    onSelect: (equity: EquityType) => void
    selectedEquity?: EquityType | null
    placeholder?: string
    disabled?: boolean
}

export function EquitySearchTrade({
    onSelect,
    selectedEquity,
    placeholder = "Search equities...",
    disabled = false,
}: EquitySearchProps) {
    const [open, setOpen] = useState(false)
    const [searchQuery, setSearchQuery] = useState("")
    const [equities, setEquities] = useState<EquityType[]>([])
    const [loading, setLoading] = useState(false)
    const sendRequest = initSendRequest()
    const debounceTimerRef = useRef<NodeJS.Timeout | null>(null)
    const inputRef = useRef<HTMLInputElement>(null)

    const searchEquities = async (query: string) => {
        setLoading(true)
        try {
            const response = await sendRequest.get(`/equities`, {
                params: {
                    filter: query,
                    size:100
                }
            })
            setEquities(response.data.content || [])
        } catch (error) {
            console.error("Error searching equities:", error)
            setEquities([])
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current)
        }

        debounceTimerRef.current = setTimeout(() => {
            searchEquities(searchQuery)
        }, 500)

        return () => {
            if (debounceTimerRef.current) {
                clearTimeout(debounceTimerRef.current)
            }
        }
    }, [searchQuery])

    const handleSelect = async (equity: EquityType) => {
        onSelect(equity)
        setOpen(false)
        setSearchQuery("")
    }

    const clearSelection = () => {
        onSelect(null as any)
        setSearchQuery("")
    }

    const handleInputClick = (e: React.MouseEvent<HTMLInputElement>) => {
        e.preventDefault()
        setOpen(true)
        inputRef.current?.focus()
    }

    return (
        <div className="space-y-2">
            <Label>Equity</Label>
            <div className="relative">
                {selectedEquity ? (
                    <div className="flex items-center gap-2 p-2 border rounded-md">
                        <TrendingUp className="h-4 w-4 shrink-0 opacity-50" />
                        <div className="flex-1">
                            <div className="font-medium">
                                {selectedEquity.assetCode} - {selectedEquity.equityName}
                            </div>
                            {selectedEquity.openPrice && (
                                <div className="text-xs text-muted-foreground">
                                    Price: ₺{selectedEquity.openPrice.toFixed(2)}
                                </div>
                            )}
                        </div>
                        <Button
                            variant="ghost"
                            size="sm"
                            className="h-8 w-8 p-0"
                            onClick={clearSelection}
                            disabled={disabled}
                        >
                            <X className="h-4 w-4" />
                            <span className="sr-only">Clear</span>
                        </Button>
                    </div>
                ) : (
                    <Popover open={open} onOpenChange={setOpen}>
                        <PopoverTrigger asChild>
                            <div className="relative cursor-text">
                                <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
                                <Input
                                    ref={inputRef}
                                    placeholder={placeholder}
                                    className="pl-8"
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    disabled={disabled}
                                    onClick={handleInputClick}
                                />
                            </div>
                        </PopoverTrigger>
                        <PopoverContent
                            className="w-[600px] p-0"
                            align="start"
                            side="bottom"
                        >
                            <div className="max-h-[300px] overflow-auto p-1">
                                {loading ? (
                                    <div className="p-2 text-center text-sm text-muted-foreground">
                                        Searching...
                                    </div>
                                ) : equities.length === 0 ? (
                                    <div className="p-2 text-center text-sm text-muted-foreground">
                                        No equities found
                                    </div>
                                ) : (
                                    <div className="space-y-1">
                                        {equities.map((equity) => (
                                            <Button
                                                key={equity.equityId}
                                                variant="ghost"
                                                className="w-full justify-start text-left"
                                                onClick={() => handleSelect(equity)}
                                            >
                                                <div className="flex flex-col items-start">
                                                    <span className="font-medium">
                                                        {equity.assetCode} - {equity.equityName}
                                                    </span>
                                                    {equity.openPrice && (
                                                        <span className="text-xs text-muted-foreground">
                                                            Price: ₺{equity.openPrice.toFixed(2)}
                                                        </span>
                                                    )}
                                                </div>
                                            </Button>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </PopoverContent>

                    </Popover>
                )}
            </div>
        </div>
    )
}
