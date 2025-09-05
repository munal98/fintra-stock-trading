import { AlertCircleIcon, CheckCircle2Icon, InfoIcon } from "lucide-react"
import { useState, useEffect } from "react"

import {
  Alert,
  AlertDescription,
  AlertTitle,
} from "@/components/ui/alert"

interface CustomAlertProps {
  message: string
  title?: string
  isOpen: boolean
  severity: string
  autoHideDuration?: number
  onClose?: () => void
}

export function CustomAlert({
  message,
  title,
  isOpen,
  severity,
  autoHideDuration = 3000,
  onClose
}: CustomAlertProps) {

    const [isAlertOpen, setIsAlertOpen] = useState(isOpen)

  useEffect(() => {
    setIsAlertOpen(isOpen)
  }, [isOpen])

  useEffect(() => {
    if (isAlertOpen && autoHideDuration) {
      const timer = setTimeout(() => {
        setIsAlertOpen(false)
        if (onClose) onClose()
      }, autoHideDuration)

      return () => clearTimeout(timer)
    }
  }, [isAlertOpen, autoHideDuration, onClose])

  if (!isAlertOpen) return null

  const getIcon = () => {
    switch (severity) {
      case 'success':
        return <CheckCircle2Icon />
      case 'error':
        return <AlertCircleIcon />
      case 'warning':
        return <InfoIcon />
      case 'info':
      default:
        return <InfoIcon />
    }
  }

  const variant = severity === 'error' ? 'destructive' : 'default'

  return (
    <div className="fixed bottom-4 right-4 z-50 max-w-md animate-in fade-in slide-in-from-bottom-5">
      <Alert variant={variant}>
        {getIcon()}
        {title && <AlertTitle>{title}</AlertTitle>}
        <AlertDescription>
          {message}
        </AlertDescription>
      </Alert>
    </div>
  )
}

