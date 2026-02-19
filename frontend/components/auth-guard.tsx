"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"

export function AuthGuard({ children }: { children: React.ReactNode }) {
    const router = useRouter()
    const [isAuthenticated] = useState(() => {
        if (typeof window === "undefined") {
            return false
        }
        return Boolean(localStorage.getItem("auth_basic"))
    })

    useEffect(() => {
        if (!isAuthenticated) {
            router.replace("/login")
        }
    }, [isAuthenticated, router])

    if (!isAuthenticated) {
        return null
    }

    return <>{children}</>
}