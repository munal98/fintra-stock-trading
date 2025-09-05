/* eslint-disable @typescript-eslint/no-explicit-any */
'use client'

import React from 'react'
import i18next from 'i18next'
import { useTranslation } from 'react-i18next'
import { Locale, defaultLocale } from '@/lib/i18n'

// Define the context type
type LanguageContextType = {
  locale: Locale
  changeLocale: (locale: Locale) => void
  t: (key: string) => string
}

// Create a default context value
const defaultContextValue: LanguageContextType = {
  locale: defaultLocale,
  changeLocale: () => {},
  t: (key: string) => key
}

// Create the context with a default value
const LanguageContext = React.createContext<LanguageContextType>(defaultContextValue)

export function LanguageProvider({ children }: { children: React.ReactNode }) {
  const [locale, setLocale] = React.useState<Locale>(defaultLocale)
  const { t } = useTranslation()

  // Safe access to localStorage
  const getStoredLocale = React.useCallback(() => {
    if (typeof window === 'undefined') return null
    return localStorage.getItem('locale') as Locale | null
  }, [])

  // Safe storage to localStorage
  const storeLocale = React.useCallback((locale: Locale) => {
    if (typeof window === 'undefined') return
    localStorage.setItem('locale', locale)
  }, [])

  React.useEffect(() => {
    const savedLocale = getStoredLocale()
    if (savedLocale && (savedLocale === 'en' || savedLocale === 'tr')) {
      setLocale(savedLocale)
      i18next.changeLanguage(savedLocale)
    }
  }, [getStoredLocale])

  const changeLocale = React.useCallback((newLocale: Locale) => {
    setLocale(newLocale)
    i18next.changeLanguage(newLocale)
    storeLocale(newLocale)
  }, [storeLocale])

  // Memoize the context value to prevent unnecessary re-renders
  const contextValue = React.useMemo(() => ({
    locale,
    changeLocale,
    t
  }), [locale, changeLocale, t])

  return (
    <LanguageContext.Provider value={contextValue}>
      {children}
    </LanguageContext.Provider>
  )
}

export function useLanguage() {
  const context = React.useContext(LanguageContext)
  return context
}
