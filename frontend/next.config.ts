import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  // Enable standalone output for Docker
  output: 'standalone',
  
  // Commenting out i18n configuration to prevent automatic locale redirection
  // i18n: {
  //   // List of locales supported by your application
  //   locales: ['en', 'tr'],
  //   // Default locale when visiting a non-locale prefixed path
  //   defaultLocale: 'en',
  //   // Domains configuration for language-specific domains (optional)
  //   // domains: [
  //   //   {
  //   //     domain: 'example.com',
  //   //     defaultLocale: 'en',
  //   //   },
  //   //   {
  //   //     domain: 'example.tr',
  //   //     defaultLocale: 'tr',
  //   //   },
  //   // ],
  // }
};

export default nextConfig;
