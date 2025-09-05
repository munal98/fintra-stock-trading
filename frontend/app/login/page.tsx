/* eslint-disable @next/next/no-img-element */
"use client"
import { ModeToggle } from "@/components/mode-toggle"
import { LoginForm } from "@/components/login-form"
import { useTheme } from "next-themes"
import { LanguageSwitcher } from "@/components/language-switcher"
import '@/lib/i18n'
import { useState, useEffect } from "react"

export default function LoginPage() {
  const { theme } = useTheme()
  const [logoSrc, setLogoSrc] = useState("logo.png")

  useEffect(() => {
    setLogoSrc(theme === "dark" ? "logo.png" : "logo-light.png")
  }, [theme])

  return (
    <div className="grid min-h-svh lg:grid-cols-2">
      <div className="flex flex-col gap-4 p-6 md:p-10">
        <div className="flex justify-between items-center">
          <a href="#" className="flex items-center gap-2 font-medium">
            <div className="bg-primary text-primary-foreground flex size-10 items-center justify-center rounded-md">
              <img
                src={logoSrc}
                alt="Logo"
                className="size-8"
              />
            </div>
            Fintra
          </a>
          <div className="flex items-center gap-2">
            <LanguageSwitcher />
            <ModeToggle />
          </div>
        </div>
        <div className="flex flex-1 items-center justify-center">
          <div className="w-full max-w-xs">
            <LoginForm />
          </div>
        </div>
      </div>
      <div className="bg-muted relative hidden lg:block">
        <img
          src="/login.jpg"
          alt="Image"
          className="absolute inset-0 h-full w-full object-cover"
        />
      </div>
    </div >
  )
}
