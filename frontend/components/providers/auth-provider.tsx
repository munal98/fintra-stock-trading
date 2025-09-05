"use client"

import { createContext, useContext, useEffect, useState, ReactNode } from "react"
import { usePathname, useRouter } from "next/navigation"
import { LoadingOverlay } from "../ui/loading"

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

// Create context
const AuthContext = createContext<AuthContextType>({
  loading: true,
  userRole: null,
  isAuthenticated: false
})

// Auth provider props
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
    // Check authentication and role on mount
    const checkAuth = () => {
      setLoading(true)
      
      // Check if on login page - always allow
      if (pathname === '/login') {
        setAuthorized(true)
        setLoading(false)
        return
      }
      
      // Check if user is authenticated
      const accessToken = localStorage.getItem('accessToken')
      if (!accessToken) {
        router.push('/login')
        setIsAuthenticated(false)
        setLoading(false)
        return
      }
      
      setIsAuthenticated(true)
      
      // Get user role from localStorage
      const role = localStorage.getItem('role')
      setUserRole(role)
      
      // Check if the current route is allowed for the user's role
      const checkRouteAccess = () => {
        // If no role found, redirect to login
        if (!role) {
          router.push('/login')
          return false
        }
        
        // Admin has access to all routes
        if (role === 'ROLE_ADMIN') {
          return true
        }
        
        // For other roles, check if the current path is in their allowed routes
        const allowedRoutes = roleRouteAccess[role as keyof typeof roleRouteAccess] || []
        
        // Check if the current path starts with any of the allowed routes
        const hasAccess = allowedRoutes.some(route => 
          pathname === route || 
          pathname.startsWith(`${route}/`)
        )
        
        if (!hasAccess) {
          // Redirect to the first allowed route for their role
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
  
  // Show loading indicator while checking authorization
  if (loading) {
    return <LoadingOverlay />
  }
  
  // If not authorized, don't render anything (redirect will happen)
  if (!authorized) {
    return null
  }
  
  // If authorized, render the page
  return (
    <AuthContext.Provider value={{ loading, userRole, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  )
}

// Hook to use auth context
export const useAuth = () => useContext(AuthContext)
