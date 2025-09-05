"use client"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import Link from "next/link"
import { useState } from "react"
import authConfig from "@/configs/authConfig"
import { useRouter } from "next/navigation"
import axios from "axios"
import { LoadingDialog } from "./ui/loading-dialog"
import { useTranslation } from 'react-i18next'
import '@/lib/i18n'

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<"form">) {
  const router = useRouter()
  const [loading, setLoading] = useState(false)
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const { t } = useTranslation()

  const handleSubmit = async (e: React.MouseEvent<HTMLButtonElement>) => {
    setLoading(true)
    e.preventDefault()
    setError("")
    if (email.length < 8 || password.length < 8) {
      setError("Email and password must be at least 8 characters.")
      setLoading(false)
      return
    } else {
      try {
        const response = await axios.post(authConfig.loginEndpoint, {
          email,
          password
        })
        const { token, email: userEmail, role, firstName, lastName } = response.data
        localStorage.setItem(authConfig.storageTokenKeyName, token)
        localStorage.setItem('role', role)
        localStorage.setItem('email', userEmail)
        localStorage.setItem('firstName', firstName)
        localStorage.setItem('lastName', lastName)

        router.push('/home-page')
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
      } catch (err: any) {
        setLoading(false)
        if (err.response?.data?.status == 400) {
          setError('Login failed. Please check your credentials.')
        } else if (err.response?.data?.status == 401) {
          setError(err.response?.data?.message || 'Login failed. Please check your credentials.')
        } else {
          setError(err.response?.data?.message || 'Login failed. Please check your credentials.')
          console.error(err)
        }

      } finally {
        // setLoading(false)
      }
    }
  }

  return (
    <form className={cn("flex flex-col gap-6", className)} {...props}>
      <div className="flex flex-col items-center gap-2 text-center">
        <h1 className="text-2xl font-bold">{t("Login to your account")}</h1>
        <p className="text-muted-foreground text-sm text-balance">
          {t("Enter your details below to log in to your account.")}
        </p>
      </div>
      <div className="grid gap-6">
        <div className="grid gap-3">
          <Label htmlFor="email">{t("Email")}</Label>
          <Input
            id="email"
            type="email"
            placeholder={t("Enter Email")}
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <div className="grid gap-3">
          <div className="flex items-center">
            <Label htmlFor="password">{t('Password')}</Label>
            <Link
              href="/forgot-password"
              className="ml-auto text-sm underline-offset-4 hover:underline"
            >
              {t('Forgot your password?')}
            </Link>
          </div>
          <Input
            id="password"
            type="password"
            placeholder={t("Enter Password")}
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>
        {error && (
          <div className="text-red-500 text-sm">{error}</div>
        )}
        <Button
          onClick={handleSubmit}
          type="submit"
          className="w-full"
          disabled={loading}
        >
          {loading && t("Logging in...") || t("Login")}
        </Button>
      </div>
      {loading && <LoadingDialog isOpen={loading} />}
    </form>
  )
}
