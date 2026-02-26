"use client"

import { useEffect, useSyncExternalStore } from "react"
import { useRouter } from "next/navigation"

export function AuthGuard({ children }: { children: React.ReactNode }) {
    const router = useRouter()

    const mounted = useSyncExternalStore(
        () => () => { },
        () => true,
        () => false
    )

    const isAuthenticated = useSyncExternalStore(
        () => () => { },
        () => Boolean(localStorage.getItem("auth_token")),
        () => false
    )

    useEffect(() => {
        if (mounted && !isAuthenticated) {
            router.replace("/login")
        }
    }, [mounted, isAuthenticated, router])

    // Durante SSR e primo render, non renderizzare nulla per evitare hydration mismatch
    if (!mounted) {
        return null
    }

    if (!isAuthenticated) {
        return null
    }

    return <>{children}</>
}