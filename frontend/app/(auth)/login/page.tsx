"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"

export default function LoginPage() {

    const router = useRouter()

    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [loginError, setLoginError] = useState(false)

    function login() {

        if (isSubmitting) {
            return
        }

        setIsSubmitting(true)
        setLoginError(false)

            ; (async () => {
                try {
                    const response = await fetch("http://localhost:8080/api/auth/login", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ username: username.trim(), password })
                    })

                    if (!response.ok) {
                        setLoginError(true)
                        setIsSubmitting(false)
                        return
                    }

                    const data: { accessToken: string; username: string; userId: string } = await response.json()
                    localStorage.setItem("auth_token", data.accessToken)
                    localStorage.setItem("auth_username", data.username)
                    localStorage.setItem("auth_user_id", data.userId)

                    router.push("/dashboard")
                } catch {
                    setLoginError(true)
                    setIsSubmitting(false)
                }
            })()
    }

    return (
        <div className="flex h-screen items-center justify-center">
            <div className="w-[320px] space-y-4">

                <h1 className="text-2xl font-semibold">Login</h1>

                <input
                    className="w-full border p-2"
                    placeholder="Username"
                    onChange={e => {
                        setUsername(e.target.value)
                        if (loginError) {
                            setLoginError(false)
                        }
                    }}
                />

                <input
                    type="password"
                    className="w-full border p-2"
                    placeholder="Password"
                    onChange={e => {
                        setPassword(e.target.value)
                        if (loginError) {
                            setLoginError(false)
                        }
                    }}
                />

                {loginError && (
                    <p className="text-sm text-red-600">Le credenziali non sono corrette</p>
                )}

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

                <button
                    className="w-full p-2 text-sm text-slate-600 underline"
                    disabled={isSubmitting}
                    onClick={() => router.push("/forgot-password")}
                >
                    Forgot password?
                </button>

                {isSubmitting && (
                    <p className="text-sm text-slate-600 text-center">Caricamento in corso...</p>
                )}

            </div>
        </div>
    )
}
