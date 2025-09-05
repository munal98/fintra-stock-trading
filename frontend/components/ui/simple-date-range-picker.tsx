"use client"

import * as React from "react"
import { format } from "date-fns"
import { tr } from "date-fns/locale"
import { Calendar as CalendarIcon } from "lucide-react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"

type DateRange = {
  from?: Date
  to?: Date
}

interface SimpleDateRangePickerProps {
  className?: string
  date: DateRange | undefined
  setDate: (date: DateRange | undefined) => void
}

export function SimpleDateRangePicker({
  className,
  date,
  setDate,
}: SimpleDateRangePickerProps) {
  const [isOpen, setIsOpen] = React.useState(false)

  const formatDateRange = () => {
    if (!date?.from) return "Tarih Aralığı Seçin"
    
    if (date.from && date.to) {
      return `${format(date.from, "dd.MM.yyyy", { locale: tr })} - ${format(date.to, "dd.MM.yyyy", { locale: tr })}`
    }
    
    return format(date.from, "dd.MM.yyyy", { locale: tr })
  }

  const handleFromChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newDate = e.target.value ? new Date(e.target.value) : undefined
    setDate({ ...date, from: newDate })
  }

  const handleToChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newDate = e.target.value ? new Date(e.target.value) : undefined
    setDate({ ...date, to: newDate })
  }

  const clearDates = () => {
    setDate({ from: undefined, to: undefined })
    setIsOpen(false)
  }

  const applyDates = () => {
    setIsOpen(false)
  }

  return (
    <div className={cn("grid gap-2", className)}>
      <Popover open={isOpen} onOpenChange={setIsOpen}>
        <PopoverTrigger asChild>
          <Button
            id="date"
            variant="outline"
            className={cn(
              "w-full justify-start text-left font-normal",
              !date?.from && "text-muted-foreground"
            )}
          >
            <CalendarIcon className="mr-2 h-4 w-4" />
            {formatDateRange()}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-auto p-4" align="start">
          <div className="grid gap-4">
            <div className="space-y-2">
              <h4 className="font-medium leading-none">Tarih Aralığı Seç</h4>
              <p className="text-sm text-muted-foreground">
                Başlangıç ve bitiş tarihlerini seçin
              </p>
            </div>
            <div className="grid gap-3">
              <div className="grid gap-2">
                <Label htmlFor="from-date">Başlangıç Tarihi</Label>
                <Input
                  id="from-date"
                  type="date"
                  value={date?.from ? format(date.from, "yyyy-MM-dd") : ""}
                  onChange={handleFromChange}
                />
              </div>
              <div className="grid gap-2">
                <Label htmlFor="to-date">Bitiş Tarihi</Label>
                <Input
                  id="to-date"
                  type="date"
                  value={date?.to ? format(date.to, "yyyy-MM-dd") : ""}
                  onChange={handleToChange}
                  min={date?.from ? format(date.from, "yyyy-MM-dd") : undefined}
                />
              </div>
            </div>
            <div className="flex justify-between pt-2">
              <Button variant="outline" size="sm" onClick={clearDates}>
                Temizle
              </Button>
              <Button size="sm" onClick={applyDates}>
                Uygula
              </Button>
            </div>
          </div>
        </PopoverContent>
      </Popover>
    </div>
  )
}
