"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"

export default function LoginPage() {

    const router = useRouter()

    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [isSubmitting, setIsSubmitting] = useState(false)

    function login() {

        if (isSubmitting) {
            return
        }

        setIsSubmitting(true)

        const token = btoa(`${username}:${password}`)
        localStorage.setItem("auth_basic", token)

        router.push("/dashboard")
    }

    return (
        <div className="flex h-screen items-center justify-center">
            <div className="w-[320px] space-y-4">

                <h1 className="text-2xl font-semibold">Login</h1>

                <input
                    className="w-full border p-2"
                    placeholder="Username"
                    onChange={e => setUsername(e.target.value)}
                />

                <input
                    type="password"
                    className="w-full border p-2"
                    placeholder="Password"
                    onChange={e => setPassword(e.target.value)}
                />

                <button
                    className="w-full bg-black text-white p-2"
                    disabled={isSubmitting}
                    onClick={login}
                >
                    {isSubmitting ? "Signing in..." : "Sign In"}
                </button>

                <button
                    className="w-full border p-2"
                    disabled={isSubmitting}
                    onClick={() => router.push("/register")}
                >
                    Register
                </button>

                {isSubmitting && (
                    <p className="text-sm text-slate-600 text-center">Caricamento in corso...</p>
                )}

            </div>
        </div>
    )
}
