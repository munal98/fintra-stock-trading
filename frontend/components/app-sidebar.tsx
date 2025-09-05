/* eslint-disable @next/next/no-img-element */
"use client"

import * as React from "react"
import { useState, useEffect } from "react"
import { usePathname } from "next/navigation"
import {
  IconChartBar,
  IconHome,
  IconReport,
  IconTransfer,
  IconUser,
  IconUserPlus,
} from "@tabler/icons-react"

import { NavMain } from "@/components/nav-main"
import { NavSecondary } from "@/components/nav-secondary"
import { NavUser } from "@/components/nav-user"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import { useTheme } from "next-themes"
import initSendRequest from "@/configs/sendRequest"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'

export function AppSidebar({ ...props }: React.ComponentProps<typeof Sidebar>) {
  const { t } = useTranslation()
  const sendRequest = initSendRequest()
  const pathname = usePathname()
  const { theme } = useTheme()
  const [userEmail, setUserEmail] = useState('')
  const [userFirstName, setUserFirstName] = useState('')
  const [userLastName, setUserLastName] = useState('')
  const [userRole, setUserRole] = useState('')
  const [mounted, setMounted] = useState(false)
  const [systemDate, setSystemDate] = useState<string>('')
  const [refreshing, setRefreshing] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    setMounted(true)
    const storedEmail = localStorage.getItem('email')
    const storedFirstName = localStorage.getItem('firstName')
    const storedLastName = localStorage.getItem('lastName')
    const storedRole = localStorage.getItem('role')
    if (storedEmail) {
      setUserEmail(storedEmail)
    }
    if (storedFirstName) {
      setUserFirstName(storedFirstName)
    }
    if (storedLastName) {
      setUserLastName(storedLastName)
    }
    if (storedRole) {
      setUserRole(storedRole)
    }
  }, [])

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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      setError(err.message || "Failed to fetch system date")
    } finally {
      setRefreshing(false)
    }
  }

  const isActive = (url: string) => {
    if (url === "#") return false
    return pathname === url
  }

  const hasActiveChild = (items?: { title: string, url: string }[]) => {
    if (!items) return false
    return items.some(item => pathname.startsWith(item.url))
  }

  const data = {
    user: {
      name: userFirstName + ' ' + userLastName,
      email: userEmail,
      avatar: ''
    },
    navMain: userRole === 'ROLE_ANALYST' ? [
      {
        title: t("Home Page"),
        url: "/home-page",
        icon: IconHome,
        isActive: isActive("/home-page"),
      },
      {
        title: t("Reports"),
        url: "/reports",
        icon: IconReport,
        isActive: isActive("/reports"),
      }
    ] : [
      {
        title: t("Home Page"),
        url: "/home-page",
        icon: IconHome,
        isActive: isActive("/home-page"),
      },
      {
        title: t("Customers"),
        url: "/customers",
        icon: IconUser,
        isActive: isActive("/customers"),
      },
      ...(userRole !== 'ROLE_TRADER' ? [
        {
          title: t("Employees"),
          url: "/employees",
          icon: IconUserPlus,
          isActive: isActive("/employees"),
        }
      ] : []),
      {
        title: t("Buy-Sell"),
        url: "#",
        icon: IconChartBar,
        isActive: hasActiveChild([
          { title: t("Order History"), url: "/buy-sell/order-history" },
          { title: t("Stock Definition"), url: "/buy-sell/stock-definition" },
          { title: t("Quick Buy/Sell"), url: "/buy-sell/quick-buy-sell" },
          { title: t("Internal Transfers"), url: "/buy-sell/internal-transfers" },
        ]),
        items: userRole === 'ROLE_TRADER' ? [
          {
            title: t("Quick Buy/Sell"),
            url: "/buy-sell/quick-buy-sell",
          }
        ] : userRole === 'ROLE_ADMIN' ? [
          {
            title: t("Order History"),
            url: "/buy-sell/order-history",
          }
        ] : [
          {
            title: t("Order History"),
            url: "/buy-sell/order-history",
          },
          {
            title: t("Quick Buy/Sell"),
            url: "/buy-sell/quick-buy-sell",
          }
        ]
      },
      ...(userRole !== 'ROLE_TRADER' ? [
        {
          title: t("Reports"),
          url: "/reports",
          icon: IconReport,
          isActive: isActive("/reports"),
        }
      ] : []),
      {
        title: t("Operational Transactions"),
        url: "#",
        icon: IconTransfer,
        isActive: hasActiveChild([
          { title: t("Stock Transfers"), url: "/operational/stock-transfers" },
          { title: t("Cash Transfers"), url: "/operational/cash-transfers" },
          { title: t("End of Day"), url: "/operational/end-of-day" },
        ]),
        items: [
          {
            title: t("Stock Transfers"),
            url: "/operational/stock-transfers",
          },
          {
            title: t("Cash Transfers"),
            url: "/operational/cash-transfers",
          },
          {
            title: t("End of Day"),
            url: "/operational/end-of-day",
          }
        ]
      }
    ],
    navClouds: [],
    navSecondary: []
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("tr-TR", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };


  return (
    <Sidebar collapsible="offcanvas" {...props}>
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton
              asChild
              className="data-[slot=sidebar-menu-button]:!p-1.5"
            >
              <a href="/home-page">
                <img
                  src={!mounted || theme === "light" ? "/logo.png" : "/logo-light.png"}
                  alt="Logo"
                  className="size-8"
                />
                <span className="text-base font-semibold">Fintra Stock Trading</span>
              </a>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
        <div className="mt-2 px-4 py-2 bg-muted/50 rounded-md mx-3">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-muted-foreground">
              {systemDate && !refreshing && !error
                ? `${t("System Date")} : ${formatDate(systemDate)}  `
                : t("System Date")}
            </span>

            <span className="text-sm font-medium">
              {refreshing ? (
                <span className="flex items-center gap-1">
                  <span className="animate-pulse">{t("Loading")}</span>
                  <span className="animate-bounce">...</span>
                </span>
              ) : error ? (
                <span className="text-destructive text-xs">{t("Failed to load system date")}</span>
              ) : null}
            </span>
          </div>
        </div>

      </SidebarHeader>
      <SidebarContent>
        <NavMain items={data.navMain} />
        <NavSecondary items={data.navSecondary} className="mt-auto" />
      </SidebarContent>
      <SidebarFooter>
        <NavUser user={data.user} />
      </SidebarFooter>
    </Sidebar>
  )
}
