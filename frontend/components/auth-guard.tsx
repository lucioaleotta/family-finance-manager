"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"

export function AuthGuard({ children }: { children: React.ReactNode }) {
    const router = useRouter()
    const [mounted, setMounted] = useState(false)
    const [isAuthenticated, setIsAuthenticated] = useState(false)

    useEffect(() => {
        setMounted(true)
        const hasAuth = Boolean(localStorage.getItem("auth_basic"))
        setIsAuthenticated(hasAuth)
        
        if (!hasAuth) {
            router.replace("/login")
        }
    }, [router])

    // Durante SSR e primo render, non renderizzare nulla per evitare hydration mismatch
    if (!mounted) {
        return null
    }

    if (!isAuthenticated) {
        return null
    }

    return <>{children}</>
}