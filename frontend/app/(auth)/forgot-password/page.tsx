"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"

export default function ForgotPasswordPage() {

    const router = useRouter()

    const [email, setEmail] = useState("")
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [isSent, setIsSent] = useState(false)
    const [error, setError] = useState("")

    const trimmedEmail = email.trim()
    const isEmailValid = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/.test(trimmedEmail)

    async function requestReset() {
        if (isSubmitting) {
            return
        }

        if (!isEmailValid) {
            setError("Inserisci un'email valida")
            return
        }

        setError("")
        setIsSubmitting(true)

        try {
            const response = await fetch("http://localhost:8080/api/auth/forgot-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email: trimmedEmail })
            })

            if (!response.ok) {
                setError("Impossibile inviare la richiesta. Riprova.")
                return
            }

            setIsSent(true)
        } catch {
            setError("Errore di rete. Riprova tra poco.")
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="flex h-screen items-center justify-center">
            <div className="w-[360px] space-y-4">
                <h1 className="text-2xl font-semibold">Forgot Password</h1>

                <p className="text-sm text-slate-600">
                    Inserisci la tua email e ti invieremo un link per reimpostare la password.
                </p>

                <input
                    className="w-full border p-2"
                    placeholder="Email"
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                />

                {error && <p className="text-sm text-red-600">{error}</p>}

                {isSent && (
                    <p className="text-sm text-green-700">
                        Se l&apos;indirizzo email è registrato, riceverai a breve le istruzioni per reimpostare la password.
                    </p>
                )}

                <button
                    className="w-full bg-black p-2 text-white"
                    disabled={isSubmitting}
                    onClick={requestReset}
                >
                    {isSubmitting ? "Invio..." : "Invia link reset"}
                </button>

                <button
                    className="w-full border p-2"
                    onClick={() => router.push("/login")}
                >
                    Torna al login
                </button>
            </div>
        </div>
    )
}
