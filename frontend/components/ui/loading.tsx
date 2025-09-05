"use client"

import React from "react"
import { cn } from "@/lib/utils"

interface LoadingProps {
  size?: "sm" | "md" | "lg"
  variant?: "primary" | "secondary" | "accent"
  fullScreen?: boolean
  text?: string
  className?: string
}

export function Loading({
  size = "md",
  variant = "primary",
  fullScreen = false,
  text,
  className,
}: LoadingProps) {
  const sizeClasses = {
    sm: "w-6 h-6",
    md: "w-10 h-10",
    lg: "w-16 h-16",
  }

  const variantClasses = {
    primary: "border-primary border-b-transparent",
    secondary: "border-secondary border-b-transparent",
    accent: "border-accent border-b-transparent",
  }

  const containerClasses = fullScreen
    ? "fixed inset-0 flex flex-col items-center justify-center bg-background/80 backdrop-blur-sm z-50"
    : "flex flex-col items-center justify-center"

  return (
    <div className={cn(containerClasses, className)}>
      <div
        className={cn(
          "animate-spin rounded-full border-4",
          sizeClasses[size],
          variantClasses[variant]
        )}
      />
      {text && (
        <p className="mt-4 text-sm font-medium text-foreground">{text}</p>
      )}
    </div>
  )
}

export function LoadingOverlay() {
  return (
    <Loading
      fullScreen
      size="lg"
      variant="primary"
      text="Loading..."
    />
  )
}

export function LoadingButton({
  loading,
  children,
  className,
  ...props
}: React.ButtonHTMLAttributes<HTMLButtonElement> & { loading: boolean }) {
  return (
    <button
      className={cn(
        "relative inline-flex items-center justify-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50",
        className
      )}
      disabled={loading}
      {...props}
    >
      {loading && (
        <span className="absolute inset-0 flex items-center justify-center">
          <Loading size="sm" variant="primary" className="border-primary-foreground border-b-transparent" />
        </span>
      )}
      <span className={cn("flex items-center gap-2", loading ? "invisible" : "")}>
        {children}
      </span>
    </button>
  )
}

export function LoadingDots({
  className,
  variant = "primary",
}: {
  className?: string
  variant?: "primary" | "secondary" | "accent"
}) {
  const dotVariantClasses = {
    primary: "bg-primary",
    secondary: "bg-secondary",
    accent: "bg-accent",
  }

  return (
    <div className={cn("flex items-center space-x-1", className)}>
      <div className={cn("h-2 w-2 rounded-full animate-bounce", dotVariantClasses[variant])} 
            />
      <div className={cn("h-2 w-2 rounded-full animate-bounce", dotVariantClasses[variant])} 
            />
      <div className={cn("h-2 w-2 rounded-full animate-bounce", dotVariantClasses[variant])} 
            />
    </div>
  )
}
