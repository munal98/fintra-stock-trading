/* eslint-disable react-hooks/exhaustive-deps */
/* eslint-disable @typescript-eslint/no-explicit-any */

import { useState, useEffect, useRef } from "react"
import { User, Search, X } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import initSendRequest from "@/configs/sendRequest"

export interface CustomerType {
  customerId: number
  firstName: string
  lastName: string
  email: string
  identityNumber: string
  tradingPermission: string
  tradingEnabled: boolean
  accounts?: {
    equities: any
    accountId: number
    accountType: string
    cashBalance: {
      balanceId: number
      freeBalance: number
      blockedBalance: number
      totalBalance: number
    }
  }[]
  createdAt: string
  updatedAt: string
}

interface CustomerSearchProps {
  onSelect: (customer: CustomerType | null) => void
  selectedCustomer?: CustomerType | null
  placeholder?: string
  disabled?: boolean
}

export function CustomerSearch({
  onSelect,
  selectedCustomer,
  placeholder = "Search customer...",
  disabled = false,
}: CustomerSearchProps) {
  const [open, setOpen] = useState(false)
  const [searchQuery, setSearchQuery] = useState("")
  const [customers, setCustomers] = useState<CustomerType[]>([])
  const [loading, setLoading] = useState(false)
  const sendRequest = initSendRequest()
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const searchCustomers = async (query: string) => {
    setLoading(true)
    try {
      const response = await sendRequest.get(`/customers`, {
        params: {
          search: query,
          size: 10
        }
      })
      setCustomers(response.data.content || [])
    } catch (error) {
      console.error("Error searching customers:", error)
      setCustomers([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
    }

    debounceTimerRef.current = setTimeout(() => {
      searchCustomers(searchQuery)
    }, 300)

    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current)
      }
    }
  }, [searchQuery])

  const handleSelect = async (customer: CustomerType) => {
    onSelect(customer)
    setOpen(false)
    setSearchQuery("")
  }

  const clearSelection = () => {
    onSelect(null)
    setSearchQuery("")
  }

  const handleInputClick = (e: React.MouseEvent<HTMLInputElement>) => {
    e.preventDefault()
    setOpen(true)
    inputRef.current?.focus()
  }

  return (
    <div className="space-y-2">
      <Label>Customer</Label>
      <div className="relative">
        {selectedCustomer ? (
          <div className="flex items-center gap-2 p-2 border rounded-md">
            <User className="h-4 w-4 shrink-0 opacity-50" />
            <div className="flex-1">
              <div className="font-medium">
                {selectedCustomer.firstName} {selectedCustomer.lastName}
              </div>
              <div className="text-xs text-muted-foreground">
                {selectedCustomer.email}
              </div>
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
            <PopoverContent className="w-[300px] p-0" align="start">
              <div className="max-h-[300px] overflow-auto p-1">
                {loading ? (
                  <div className="p-2 text-center text-sm text-muted-foreground">
                    Searching...
                  </div>
                ) : customers.length === 0 ? (
                  <div className="p-2 text-center text-sm text-muted-foreground">
                    {!searchQuery 
                      ? "Type to search customers" 
                      : "No customers found"}
                  </div>
                ) : (
                  <div className="space-y-1">
                    {customers.map((customer) => (
                      <Button
                        key={customer.customerId}
                        variant="ghost"
                        className="w-full justify-start text-left"
                        onClick={() => handleSelect(customer)}
                      >
                        <div className="flex flex-col items-start">
                          <span className="font-medium">
                            {customer.firstName} {customer.lastName}
                          </span>
                          <span className="text-xs text-muted-foreground">
                            {customer.email}
                          </span>
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
