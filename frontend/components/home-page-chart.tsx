/* eslint-disable @typescript-eslint/no-explicit-any */
"use client"

import * as React from "react"
import { Area, AreaChart, CartesianGrid, XAxis, YAxis } from "recharts"

import { useIsMobile } from "@/hooks/use-mobile"
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import {
  ChartConfig,
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  ToggleGroup,
  ToggleGroupItem,
} from "@/components/ui/toggle-group"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'


export const description = "An interactive area chart"

interface PriceDataItem {
  date: string;
  price: number;
  [key: string]: any; 
}

interface ChartAreaInteractiveProps {
  data: PriceDataItem[];
  title?: string;
}

const chartConfig = {
  price: {
    label: "Price",
    color: "var(--primary)",
  },
} satisfies ChartConfig

export function HomePageChart({ data }: ChartAreaInteractiveProps) {
  const isMobile = useIsMobile()
  const { t } = useTranslation()
  const [timeRange, setTimeRange] = React.useState("30d")

  React.useEffect(() => {
    if (isMobile) {
      setTimeRange("7d")
    }
  }, [isMobile])


  const formattedData = React.useMemo(() => {
    
    if (!data || !Array.isArray(data) || data.length === 0) {
      return [];
    }
    
    const dateField = 'dataDate';
    const priceField = 'closePrice';
        
    const formatted = data.map(item => {
      const dateValue = item[dateField];
      const priceValue = parseFloat(item[priceField]);
      
      if (!dateValue || isNaN(priceValue)) {
        return null;
      }
      
      return {
        date: dateValue,
        price: priceValue
      };
    }).filter(Boolean) as PriceDataItem[];
    
    return formatted;
  }, [data]);

  const filteredData = React.useMemo(() => {
    if (!formattedData.length) {
      return [];
    }
    
    try {

      const sortedData = [...formattedData].sort((a, b) => {
        // a and b are guaranteed to be non-null after filter(Boolean)
        const dateA = new Date(a.date);
        const dateB = new Date(b.date);
        
        if (isNaN(dateA.getTime()) || isNaN(dateB.getTime())) {
          return 0;
        }
        
        return dateA.getTime() - dateB.getTime();
      });
      
      if (sortedData.length === 0) {
        return [];
      }
      
      const dates = sortedData
        .map(item => {
          // item is guaranteed to be non-null
          const date = new Date(item.date);
          const valid = !isNaN(date.getTime());
          return { date, valid };
        })
        .filter(item => item.valid)
        .map(item => item.date);
      
      if (dates.length === 0) {
        return [];
      }
      
      const dateTimestamps = dates.map(date => date.getTime());
      const maxTimestamp = Math.max(...dateTimestamps);
      const mostRecentDate = new Date(maxTimestamp);      
      let daysToSubtract = 30;
      if (timeRange === "7d") {
        daysToSubtract = 7;
      }
      
      const startDate = new Date(mostRecentDate);
      startDate.setDate(startDate.getDate() - daysToSubtract);      
      const filtered = sortedData.filter(item => {
        // item is guaranteed to be non-null
        const itemDate = new Date(item.date);
        const isValid = !isNaN(itemDate.getTime());
        const isAfterStartDate = itemDate >= startDate;
        return isValid && isAfterStartDate;
      });
      
      return filtered;
    } catch (error) {
      console.error('Error filtering data:', error);
      return [];
    }
  }, [formattedData, timeRange]);

  return (
    <Card className="@container/card">
      <CardHeader>
        <CardTitle>{t("Price History")}</CardTitle>
        <CardDescription>
          <span className="hidden @[540px]/card:block">
            {timeRange === "30d" ? t("Last 30 days") : t("Last 7 days")} {t("price history")}
          </span>
          <span className="@[540px]/card:hidden">
            {timeRange === "30d" ? t("30 days") : t("7 days")} {t("history")}
          </span>
        </CardDescription>
        <CardAction>
          <ToggleGroup
            type="single"
            value={timeRange}
            onValueChange={setTimeRange}
            variant="outline"
            className="hidden *:data-[slot=toggle-group-item]:!px-4 @[767px]/card:flex"
          >
            <ToggleGroupItem value="30d">{t("Last 30 days")}</ToggleGroupItem>
            <ToggleGroupItem value="7d">{t("Last 7 days")}</ToggleGroupItem>
          </ToggleGroup>
          <Select value={timeRange} onValueChange={setTimeRange}>
            <SelectTrigger
              className="flex w-40 **:data-[slot=select-value]:block **:data-[slot=select-value]:truncate @[767px]/card:hidden"
              size="sm"
              aria-label="Select a time range"
            >
              <SelectValue placeholder="Last 30 days" />
            </SelectTrigger>
            <SelectContent className="rounded-xl">
              <SelectItem value="30d" className="rounded-lg">
                {t("Last 30 days")}
              </SelectItem>
              <SelectItem value="7d" className="rounded-lg">
                {t("Last 7 days")}
              </SelectItem>
            </SelectContent>
          </Select>
        </CardAction>
      </CardHeader>
      <CardContent className="px-2 pt-4 sm:px-6 sm:pt-6">
        <ChartContainer
          config={chartConfig}
          className="aspect-auto h-[250px] w-full"
        >
          {filteredData.length > 0 ? (
            <AreaChart data={filteredData}>
              <defs>
                <linearGradient id="fillPrice" x1="0" y1="0" x2="0" y2="1">
                  <stop
                    offset="5%"
                    stopColor="var(--color-price)"
                    stopOpacity={0.8}
                  />
                  <stop
                    offset="95%"
                    stopColor="var(--color-price)"
                    stopOpacity={0.1}
                  />
                </linearGradient>
              </defs>
              <CartesianGrid vertical={false} />
              <XAxis
                dataKey="date"
                tickLine={false}
                axisLine={false}
                tickMargin={8}
                minTickGap={32}
                tickFormatter={(value) => {
                  const date = new Date(value)
                  return date.toLocaleDateString("tr-TR", {
                    month: "numeric",
                    day: "numeric",
                  })
                }}
              />
              <YAxis 
                hide={false} 
                tickLine={false}
                axisLine={false}
                tickFormatter={(value) => `${value} ₺`}
              />
              <ChartTooltip
                cursor={false}
                content={
                  <ChartTooltipContent
                    labelFormatter={(value) => {
                      return new Date(value).toLocaleDateString("tr-TR", {
                        year: "numeric",
                        month: "numeric",
                        day: "numeric",
                      })
                    }}
                    formatter={(value) => `${value} ₺`}
                    indicator="dot"
                  />
                }
              />
              <Area
                dataKey="price"
                type="monotone"
                fill="url(#fillPrice)"
                stroke="var(--color-price)"
                strokeWidth={2}
              />
            </AreaChart>
          ) : (
            <div className="flex h-full w-full items-center justify-center">
              {t("No data available")}
            </div>
          )}
        </ChartContainer>
      </CardContent>
    </Card>
  )
}
