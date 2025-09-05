import Backend from 'i18next-http-backend'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import i18next from 'i18next'

export type Locale = 'en' | 'tr'
export const defaultLocale: Locale = 'en'

i18next
  // Enables the i18next backend
  .use(Backend)
  // Enable automatic language detection
  .use(LanguageDetector)
  // Enables the hook initialization module
  .use(initReactI18next)
  .init({
    lng: defaultLocale,
    backend: {
      /* translation file path */
      loadPath: '/locales/{{lng}}.json'
    },
    fallbackLng: defaultLocale,
    debug: false,
    keySeparator: false,
    react: {
      useSuspense: false
    },
    interpolation: {
      escapeValue: false,
      formatSeparator: ','
    }
  })

// Helper function to get locale from path
export function getLocaleFromPath(pathname: string): Locale {
  // Extract locale from the pathname
  const segments = pathname.split('/')
  const localeSegment = segments[1]
  
  if (localeSegment === 'tr') {
    return 'tr'
  }
  
  return defaultLocale
}

export default i18next
