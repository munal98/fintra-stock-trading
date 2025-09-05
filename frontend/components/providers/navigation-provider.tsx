/* eslint-disable @typescript-eslint/no-unused-vars */
"use client";

import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { usePathname, useRouter } from "next/navigation";

// Define the routes that require authentication
const PROTECTED_ROUTES = [
  "/dashboard",
  "/dashboard/1",
  "/dashboard/2",
  "/profile",
  "/settings",
];

// Define the authentication context type
type AuthContextType = {
  isAuthenticated: boolean;
  login: (username: string) => void;
  logout: () => void;
  user: { username: string } | null;
};

// Create the authentication context
const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  login: () => {},
  logout: () => {},
  user: null,
});

// Hook to use the authentication context
export const useAuth = () => useContext(AuthContext);

// Navigation provider component
export function NavigationProvider({ children }: { children: ReactNode }) {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState<{ username: string } | null>(null);
  const router = useRouter();
  const pathname = usePathname();

  // Check authentication status on mount and when pathname changes
  useEffect(() => {
    const checkAuth = () => {
      const authStatus = localStorage.getItem("isAuthenticated") === "true";
      setIsAuthenticated(authStatus);

      try {
        const userStr = localStorage.getItem("user");
        if (userStr) {
          const userData = JSON.parse(userStr);
          setUser(userData);
        } else {
          setUser(null);
        }
      } catch (error) {
        setUser(null);
      }

      // Handle route protection
      const isProtectedRoute = PROTECTED_ROUTES.some(route => 
        pathname?.startsWith(route)
      );

      if (isProtectedRoute && !authStatus) {
        // Redirect to login if trying to access protected route without auth
        router.push("/login");
      } else if (pathname === "/login" && authStatus) {
        // Redirect to dashboard if already authenticated and trying to access login
        router.push("/dashboard");
      }
    };

    checkAuth();
  }, [pathname, router]);

  // Login function
  const login = (username: string) => {
    localStorage.setItem("isAuthenticated", "true");
    localStorage.setItem("user", JSON.stringify({ username }));
    setIsAuthenticated(true);
    setUser({ username });
    router.push("/dashboard");
  };

  // Logout function
  const logout = () => {
    localStorage.removeItem("isAuthenticated");
    localStorage.removeItem("user");
    setIsAuthenticated(false);
    setUser(null);
    router.push("/login");
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, logout, user }}>
      {children}
    </AuthContext.Provider>
  );
}
