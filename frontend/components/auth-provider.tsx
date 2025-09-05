"use client"

import { createContext, useContext, useEffect, useState, ReactNode } from "react"
import { usePathname, useRouter } from "next/navigation"
import { LoadingOverlay } from "@/components/ui/loading"

// Define role-based route access configuration
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
  // Admin has access to all routes
  'ROLE_ADMIN': []
}

// Auth context type
type AuthContextType = {
  loading: boolean
  userRole: string | null
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType>({
  loading: true,
  userRole: null,
  isAuthenticated: false
})

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const router = useRouter()
  const pathname = usePathname()
  const [loading, setLoading] = useState(true)
  const [userRole, setUserRole] = useState<string | null>(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [authorized, setAuthorized] = useState(false)

  useEffect(() => {
    const checkAuth = () => {
      setLoading(true)
      if (pathname === '/login' || pathname === '/forgot-password') {
        setAuthorized(true)
        setLoading(false)
        return
      }
      
      const accessToken = localStorage.getItem('accessToken')
      if (!accessToken) {
        router.push('/login')
        setIsAuthenticated(false)
        setLoading(false)
        return
      }
      
      setIsAuthenticated(true)
      
      const role = localStorage.getItem('role')
      setUserRole(role)
      
      const checkRouteAccess = () => {
        if (!role) {
          router.push('/login')
          return false
        }
        
        if (role === 'ROLE_ADMIN') {
          return true
        }
        
        const allowedRoutes = roleRouteAccess[role as keyof typeof roleRouteAccess] || []
        
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
    
    checkAuth()
  }, [pathname, router])
  
  if (loading) {
    return <LoadingOverlay />
  }
  
  if (!authorized) {
    return null
  }
  
  return (
    <AuthContext.Provider value={{ loading, userRole, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
