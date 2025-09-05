/* eslint-disable react-hooks/exhaustive-deps */

import { useState, useEffect, useRef } from "react"
import { Search, X } from "lucide-react"
import { Input } from "@/components/ui/input"
import initSendRequest from "@/configs/sendRequest"
import { CustomerType } from "./customer-search"

interface CustomerFilterProps {
  onFilter: (customers: CustomerType[]) => void
  placeholder?: string
  disabled?: boolean
}

export function CustomerFilter({
  onFilter,
  placeholder = "Search customers...",
  disabled = false,
}: CustomerFilterProps) {
  const [searchQuery, setSearchQuery] = useState("")
  const [loading, setLoading] = useState(false)
  const sendRequest = initSendRequest()
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null)

  const searchCustomers = async (query: string) => {
    setLoading(true)
    try {
      const response = await sendRequest.get(`/customers`, {params: {firstName: query}})
      const results = response.data.content || []
      onFilter(results)
    } catch (error) {
      console.error("Error searching customers:", error)
      onFilter([])
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

  const clearSearch = () => {
    setSearchQuery("")
    onFilter([])
  }

  return (
    <div className="relative w-64">
      <Search className={`absolute left-2 top-2.5 h-4 w-4 ${loading ? "text-primary" : "text-muted-foreground"}`} />
      <Input
        placeholder={placeholder}
        className="pl-8 pr-8"
        value={searchQuery}
        onChange={(e) => setSearchQuery(e.target.value)}
        disabled={disabled}
      />
      {searchQuery && (
        <button 
          className="absolute right-2 top-2.5 text-muted-foreground hover:text-foreground"
          onClick={clearSearch}
        >
          <X className="h-4 w-4" />
          <span className="sr-only">Clear</span>
        </button>
      )}
    </div>
  )
}
