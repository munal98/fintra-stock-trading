"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import defaultConfig from "@/configs/defaultConfig";
import authConfig from "@/configs/authConfig";
import '../lib/i18n'


export default function Home() {
  const router = useRouter();

  useEffect(() => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      router.replace("/home-page");
    } else {
      router.replace("/login");
    }
  }, [router]);

  useEffect(() => {
    if (defaultConfig.mode !== 'development') {
      defaultConfig.baseUrl = `${window.location.protocol}//${window.location.hostname}/api`
      authConfig.loginEndpoint = `${window.location.protocol}//${window.location.hostname}/api/v1/auth/login`
    }
  }, [])

  return <div></div>;
}
