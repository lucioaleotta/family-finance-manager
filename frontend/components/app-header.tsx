"use client"

import { useRouter } from "next/navigation"
import { SidebarTrigger } from "@/components/ui/sidebar"

export function AppHeader() {

    const router = useRouter()

    function logout() {
        localStorage.removeItem("auth_basic")
        router.replace("/login")
    }

    return (
        <header className="h-14 border-b bg-white flex items-center justify-between px-6">

            <div className="flex items-center gap-4">
                <SidebarTrigger />
                <div className="font-semibold">
                    Family Finance Manager
                </div>
            </div>

            <button
                onClick={logout}
                className="text-sm text-red-600 hover:underline"
            >
                Logout
            </button>

        </header>
    )
}
