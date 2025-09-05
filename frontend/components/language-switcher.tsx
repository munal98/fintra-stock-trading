'use client'

import { Button } from "@/components/ui/button"
import { Globe } from "lucide-react"
import { useTranslation } from 'react-i18next'
import i18next from 'i18next'
import { Locale } from "@/lib/i18n"
import '@/lib/i18n'
import { useState, useEffect } from 'react'

export function LanguageSwitcher() {
  const { i18n } = useTranslation()
  const currentLanguage = i18n.language as Locale
  
  // Use state for UI text to avoid hydration mismatch
  const [buttonTitle, setButtonTitle] = useState('')
  const [buttonLabel, setButtonLabel] = useState('')
  const [displayText, setDisplayText] = useState('')
  
  // Update UI text after initial render to avoid hydration mismatch
  useEffect(() => {
    const isEnglish = currentLanguage === 'en'
    setButtonTitle(isEnglish ? 'Switch to Turkish' : 'İngilizce\'ye geç')
    setButtonLabel(isEnglish ? 'Switch to Turkish' : 'İngilizce\'ye geç')
    setDisplayText(isEnglish ? 'TR' : 'EN')
  }, [currentLanguage])

  const toggleLocale = () => {
    try {
      const newLocale: Locale = currentLanguage === 'en' ? 'tr' : 'en'
      i18next.changeLanguage(newLocale)
      if (typeof window !== 'undefined') {
        localStorage.setItem('locale', newLocale)
      }
    } catch (error) {
      console.error('Error changing language:', error)
    }
  }

  return (
    <Button
      variant="default"
      size="sm"
      onClick={toggleLocale}
      className="flex items-center gap-1 px-2 py-1"
      title={buttonTitle}
    >
      <Globe className="h-4 w-4" />
      <span className="sr-only">
        {buttonLabel}
      </span>
      <span className="text-xs font-medium">
        {displayText}
      </span>
    </Button>
  )
}
