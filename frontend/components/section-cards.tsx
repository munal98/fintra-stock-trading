import { IconTrendingDown, IconTrendingUp } from "@tabler/icons-react"

import { Badge } from "@/components/ui/badge"
import {
  Card,
  CardAction,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"

export type SectionCardProps = {
  title: string
  description: string
  value: string | number
  trend?: 'up' | 'down' | null
  trendValue?: string
  footerText?: string
  footerSubtext?: string
  className?: string
  valueClassName?: string
}

export type SectionCardsProps = {
  cards: SectionCardProps[]
  className?: string
}

export function SectionCard({
  description,
  value,
  trend,
  trendValue,
  footerText,
  footerSubtext,
  className = "",
  valueClassName = ""
}: SectionCardProps) {
  return (
    <Card className={`@container/card ${className}`}>
      <CardHeader>
        <CardDescription>{description}</CardDescription>
        <CardTitle className={`text-2xl font-semibold tabular-nums @[250px]/card:text-3xl ${valueClassName}`}>
          {value}
        </CardTitle>
        {trend && trendValue && (
          <CardAction>
            <Badge variant="outline">
              {trend === 'up' ? <IconTrendingUp /> : <IconTrendingDown />}
              {trendValue}
            </Badge>
          </CardAction>
        )}
      </CardHeader>
      {(footerText || footerSubtext) && (
        <CardFooter className="flex-col items-start gap-1.5 text-sm">
          {footerText && (
            <div className="line-clamp-1 flex gap-2 font-medium">
              {footerText} {trend === 'up' ? <IconTrendingUp className="size-4" /> : trend === 'down' ? <IconTrendingDown className="size-4" /> : null}
            </div>
          )}
          {footerSubtext && (
            <div className="text-muted-foreground">
              {footerSubtext}
            </div>
          )}
        </CardFooter>
      )}
    </Card>
  )
}

export function SectionCards({ cards, className = "" }: SectionCardsProps) {
  return (
    <div className={`*:data-[slot=card]:from-primary/5 *:data-[slot=card]:to-card dark:*:data-[slot=card]:bg-card grid grid-cols-1 gap-4 px-4 *:data-[slot=card]:bg-gradient-to-t *:data-[slot=card]:shadow-xs lg:px-6 @xl/main:grid-cols-2 @5xl/main:grid-cols-4 ${className}`}>
      {cards.map((card, index) => (
        <SectionCard key={index} {...card} />
      ))}
    </div>
  )
}
