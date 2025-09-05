"use client"

import { useEffect, useState } from "react"
import { usePathname, useRouter } from "next/navigation"
import { LoadingOverlay } from "./ui/loading"

const roleRouteAccess = {
    'ROLE_TRADER': [
        '/home-page',
        '/customers',
        '/buy-sell/quick-buy-sell',
        '/operational/stock-transfers',
        '/operational/cash-transfers',
        '/operational/end-of-day'
    ],
    'ROLE_ANALYST': [
        '/home-page',
        '/reports'
    ],
    'ROLE_ADMIN': [] 
}

export function RouteGuard({ children }: { children: React.ReactNode }) {
    const router = useRouter()
    const pathname = usePathname()
    const [authorized, setAuthorized] = useState(false)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const authCheck = () => {
            const accessToken = localStorage.getItem('accessToken')
            if (!accessToken) {
                router.push('/login')
                return
            }

            const userRole = localStorage.getItem('role')

            const checkRouteAccess = () => {
                if (!userRole) {
                    router.push('/login')
                    return false
                }

                if (pathname === '/login') {
                    return true
                }

                if (userRole === 'ROLE_ADMIN') {
                    return true
                }

                const allowedRoutes = roleRouteAccess[userRole as keyof typeof roleRouteAccess] || []
                const hasAccess = allowedRoutes.some(route =>
                    pathname === route ||
                    pathname.startsWith(`${route}/`)
                )

                if (!hasAccess) {
                    if (allowedRoutes.length > 0) {
                        router.push(allowedRoutes[0])
                    } else {
                        router.push('/login')
                    }
                    return false
                }

                return true
            }

            const hasAccess = checkRouteAccess()
            setAuthorized(hasAccess)
            setLoading(false)
        }

        authCheck()

        const handleRouteChange = () => {
            setLoading(true)
            authCheck()
        }

        return () => {
        }
    }, [pathname, router])

    if (loading) {
        return <LoadingOverlay />
    }

    return authorized ? <>{children}</> : null
}
