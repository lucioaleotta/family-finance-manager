"use client"

import * as React from "react"
import { useRouter } from "next/navigation"
import { User, LogOut } from "lucide-react"
import { SidebarTrigger } from "@/components/ui/sidebar"
import { Button } from "@/components/ui/button"

export function AppHeader() {

    const router = useRouter()
    const [username, setUsername] = React.useState<string | null>(null)

    React.useEffect(() => {
        const user = localStorage.getItem("auth_username")
        setUsername(user)
    }, [])

    function logout() {
        localStorage.removeItem("auth_token")
        localStorage.removeItem("auth_username")
        localStorage.removeItem("auth_user_id")
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

            <div className="flex items-center gap-3">
                <div className="flex items-center gap-2 text-sm font-medium text-slate-700">
                    <User className="h-4 w-4" />
                    <span>Ciao {username || "Leot"}</span>
                </div>

                <Button
                    onClick={logout}
                    variant="outline"
                    size="sm"
                    className="gap-2 border-red-200 text-red-600 hover:bg-red-50 hover:text-red-700"
                >
                    <LogOut className="h-4 w-4" />
                    <span>logout</span>
                </Button>
            </div>

        </header>
    )
}
