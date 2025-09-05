"use client"

import * as React from "react"
import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  InputOTP,
  InputOTPGroup,
  InputOTPSeparator,
  InputOTPSlot,
} from "@/components/ui/input-otp"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useState, useEffect } from "react"
import axios from "axios"
import { CustomAlert } from "./custom-alert"
import defaultConfig from "@/configs/defaultConfig"
import { useRouter } from "next/navigation"

export function ForgotForm({
  className,
}: React.HTMLAttributes<HTMLDivElement>) {
  const router = useRouter()
  const [email, setEmail] = useState("")
  const [loading, setLoading] = useState(false)
  const [severityAlert, setSeverityAlert] = useState('')
  const [textAlert, setTextAlert] = useState('')
  const [codeVerified, setCodeVerified] = useState(false)
  const [code, setCode] = useState('')
  const [countdown, setCountdown] = useState(0)
  const [codeSent, setCodeSent] = useState(false)
  const [emailError, setEmailError] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  // Update alert function
  const updateAlert = (severity: string, text: string) => {
    setSeverityAlert(severity)
    setTextAlert(text)
  }

  // Email validation
  const validateEmail = (email: string) => {
    if (email.length < 8) {
      setEmailError('Email must be at least 8 characters')
      return false
    }
    setEmailError('')
    return true
  }

  // Handle countdown timer
  useEffect(() => {
    let timer: NodeJS.Timeout | null = null;

    if (countdown > 0) {
      timer = setTimeout(() => {
        setCountdown(countdown - 1);
      }, 1000);
    } else if (countdown === 0 && codeSent) {
      // Auto resend code if timer expires and code hasn't been confirmed
      if (code.length !== 6) {
        handleSendCode();
      }
    }

    return () => {
      if (timer) clearTimeout(timer);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [countdown, codeSent, code]);

  // Handle code send
  const handleSendCode = async () => {
    // Validate email
    if (!validateEmail(email)) {
      return;
    }

    setLoading(true);
    try {
      const response = await axios.post(defaultConfig.baseUrl + '/password/reset-request', {
        email,
      });

      if (response.status === 200) {
        setLoading(false)
        updateAlert('success', 'Code sent successfully');
        setCodeSent(true);
        setCountdown(180);
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      setLoading(false);
      updateAlert('error', 'Invalid email');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // Handle code confirmation
  const handleConfirmCode = async () => {
    if (code.length !== 6) {
      updateAlert('error', 'Please enter the complete 6-digit code');
      return;
    }

    try {
      const response = await axios.post(defaultConfig.baseUrl + '/password/verify-token', {
        email,
        token: code
      });

      if (response.status === 200) {
        setLoading(false)
        updateAlert('success', 'Code verified successfully');
        setCodeVerified(true)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      setLoading(false)
      updateAlert('error', 'Invalid code');
      console.error(err);
    }
  };

  const handleChangePassword = async () => {
    if (password !== confirmPassword) {
      updateAlert('error', 'Passwords do not match');
      return;
    }

    try {
      const response = await axios.post(defaultConfig.baseUrl + '/password/reset-complete', {
        email,
        token: code,
        newPassword: password
      });

      if (response.status === 200) {
        setLoading(false)
        updateAlert('success', 'Password changed successfully');
        router.push('/login');
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      setLoading(false)
      updateAlert('error', 'Failed to change password');
      console.error(err);
    }
  };

  if (!codeVerified) {
    return (
      <div className={cn("grid gap-6", className)}>
        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl">Forgot password</CardTitle>
            <CardDescription>
              Enter your email address and we&apos;ll send you a verification code
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={(e) => e.preventDefault()}>
              <div className="flex flex-col gap-6">
                <div className="grid gap-3">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="m@example.com"
                    required
                    disabled={codeSent}
                    value={email}
                    onChange={(e) => {
                      setEmail(e.target.value);
                      validateEmail(e.target.value);
                    }}
                    className={emailError ? "border-red-500" : ""}
                  />
                  {emailError && (
                    <p className="text-sm text-red-500">{emailError}</p>
                  )}
                </div>

                {codeSent && (
                  <div className="flex flex-col gap-3">
                    <div className="flex justify-between items-center">
                      <Label htmlFor="code">Verification Code</Label>
                      <span className="text-sm text-muted-foreground">
                        {Math.floor(countdown / 60)}:{(countdown % 60).toString().padStart(2, '0')}
                      </span>
                    </div>
                    <div className="flex justify-center">
                      <InputOTP
                        maxLength={6}
                        value={code}
                        onChange={(value) => {
                          if (value.length <= 6) {
                            setCode(value);
                          }
                        }}
                      >
                        <InputOTPGroup>
                          <InputOTPSlot index={0} />
                          <InputOTPSlot index={1} />
                          <InputOTPSlot index={2} />
                        </InputOTPGroup>
                        <InputOTPSeparator />
                        <InputOTPGroup>
                          <InputOTPSlot index={3} />
                          <InputOTPSlot index={4} />
                          <InputOTPSlot index={5} />
                        </InputOTPGroup>
                      </InputOTP>
                    </div>
                  </div>
                )}

                <div className="flex flex-col gap-3">
                  {!codeSent ? (
                    <Button
                      disabled={loading || email.length < 8}
                      onClick={handleSendCode}
                      type="button"
                      className="w-full"
                    >
                      {loading ? "Sending..." : "Send Code"}
                    </Button>
                  ) : (
                    <div className="flex flex-col gap-3">
                      <Button
                        disabled={loading || countdown > 0}
                        onClick={handleSendCode}
                        type="button"
                        className="w-full"
                      >
                        {loading ? "Sending..." : countdown > 0 ? `Resend Code (${Math.floor(countdown / 60)}:${(countdown % 60).toString().padStart(2, '0')})` : "Resend Code"}
                      </Button>

                      <Button
                        disabled={code.length !== 6}
                        onClick={handleConfirmCode}
                        type="button"
                        className="w-full"
                      >
                        Confirm Code
                      </Button>
                    </div>
                  )}
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        {severityAlert && textAlert &&
          <CustomAlert
            message={textAlert}
            isOpen={!!(textAlert && severityAlert)}
            severity={severityAlert}
            autoHideDuration={3000}
          />}
      </div>
    )
  } else {
    return (
      <div className={cn("grid gap-6", className)}>
        <Card>
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl">Forgot password</CardTitle>
            <CardDescription>
              Enter your new password
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={(e) => e.preventDefault()}>
              <div className="flex flex-col gap-6">
                <div className="grid gap-3">
                  <Label htmlFor="email">Email</Label>
                  <Input
                    id="email"
                    type="email"
                    placeholder="m@example.com"
                    required
                    disabled={codeSent}
                    value={email}
                    onChange={(e) => {
                      setEmail(e.target.value);
                      validateEmail(e.target.value);
                    }}
                    className={emailError ? "border-red-500" : ""}
                  />
                  {emailError && (
                    <p className="text-sm text-red-500">{emailError}</p>
                  )}
                  <Label htmlFor="password">New Password</Label>
                  <Input
                    id="password"
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                  />
                  <Label htmlFor="confirmPassword">Confirm Password</Label>
                  <Input
                    id="confirmPassword"
                    type="password"
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                  />
                </div>


                <div className="flex flex-col gap-3">
                  <Button
                    disabled={loading || email.length < 8}
                    onClick={handleChangePassword}
                    type="button"
                    className="w-full"
                  >
                    Change Password
                  </Button>
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        {severityAlert && textAlert &&
          <CustomAlert
            message={textAlert}
            isOpen={!!(textAlert && severityAlert)}
            severity={severityAlert}
            autoHideDuration={3000}
          />}
      </div>
    )
  }
}
