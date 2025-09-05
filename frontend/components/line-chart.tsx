"use client"

import { TrendingUp } from "lucide-react"
import { CartesianGrid, Line, LineChart, XAxis } from "recharts"

import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import {
    ChartConfig,
    ChartContainer,
    ChartTooltip,
    ChartTooltipContent,
} from "@/components/ui/chart"

export const description = "A line chart"

const chartData = [
    { month: "January", desktop: 186 },
    { month: "February", desktop: 305 },
    { month: "March", desktop: 237 },
    { month: "April", desktop: 73 },
    { month: "May", desktop: 209 },
    { month: "June", desktop: 214 },
]

const chartConfig = {
    desktop: {
        label: "Desktop",
        color: "var(--chart-1)",
    },
} satisfies ChartConfig

export function ChartLineDefault() {
    return (
        <div className="w-full">
            <Card className="overflow-hidden">
                <CardHeader className="p-4">
                    <CardTitle className="text-sm">Line Chart</CardTitle>
                    <CardDescription className="text-xs">January - June 2024</CardDescription>
                </CardHeader>
                <CardContent className="p-4 pt-0">
                    <div className="h-[180px]">
                        <ChartContainer config={chartConfig}>
                            <LineChart
                                accessibilityLayer
                                data={chartData}
                                margin={{
                                    top: 5,
                                    left: 5,
                                    right: 5,
                                    bottom: 0,
                                }}
                                height={180}
                            >
                                <CartesianGrid vertical={false} strokeDasharray="3 3" stroke="var(--border)" />
                                <XAxis
                                    dataKey="month"
                                    tickLine={false}
                                    axisLine={false}
                                    tickMargin={8}
                                    tick={{ fontSize: 10 }}
                                    tickFormatter={(value) => value.slice(0, 3)}
                                />
                                <ChartTooltip
                                    cursor={false}
                                    content={<ChartTooltipContent hideLabel />}
                                />
                                <Line
                                    dataKey="desktop"
                                    type="natural"
                                    stroke="var(--color-desktop)"
                                    strokeWidth={2}
                                    dot={false}
                                />
                            </LineChart>
                        </ChartContainer>
                    </div>
                </CardContent>
                <CardFooter className="flex-col items-start gap-1 p-4 pt-0 text-xs">
                    <div className="flex gap-1 leading-none font-medium">
                        Trending up by 5.2% <TrendingUp className="h-3 w-3 ml-1" />
                    </div>
                    <div className="text-muted-foreground leading-none text-xs">
                        Total visitors for the last 6 months
                    </div>
                </CardFooter>
            </Card>
        </div>
    )
}
