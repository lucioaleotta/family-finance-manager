"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"

export default function RegisterPage() {

    const router = useRouter()

    const [username, setUsername] = useState("")
    const [password, setPassword] = useState("")
    const [currency, setCurrency] = useState("EUR")
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [submitError, setSubmitError] = useState("")

    const trimmedUsername = username.trim()

    function passwordValidationMessage(value: string) {
        if (!value) {
            return "Password obbligatoria"
        }
        if (value.length < 8) {
            return "Minimo 8 caratteri"
        }
        if (!/[a-z]/.test(value) || !/[A-Z]/.test(value) || !/\d/.test(value)) {
            return "Deve contenere almeno: una maiuscola, una minuscola e un numero"
        }
        return ""
    }

    const usernameError = trimmedUsername ? "" : "Username obbligatorio"
    const passwordError = passwordValidationMessage(password)
    const isFormValid = !usernameError && !passwordError

    async function register() {

        if (isSubmitting) {
            return
        }

        if (!isFormValid) {
            setSubmitError("Compila correttamente username e password")
            return
        }

        setSubmitError("")
        setIsSubmitting(true)

        try {
            const response = await fetch("http://localhost:8080/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    username: trimmedUsername,
                    password,
                    baseCurrency: currency
                })
            })

            if (!response.ok) {
                setSubmitError("Registrazione non riuscita. Verifica i dati inseriti.")
                return
            }

            // auto-login dopo registrazione
            const token = btoa(`${trimmedUsername}:${password}`)
            localStorage.setItem("auth_basic", token)

            router.push("/dashboard")
        } catch {
            setSubmitError("Errore di rete. Riprova tra poco.")
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="flex h-screen items-center justify-center">
            <div className="w-[320px] space-y-4">

                <h1 className="text-2xl font-semibold">Register</h1>

                <input
                    className="w-full border p-2"
                    placeholder="Username"
                    value={username}
                    onChange={e => setUsername(e.target.value)}
                />
                {usernameError && <p className="text-sm text-red-600">{usernameError}</p>}

                <input
                    type="password"
                    className="w-full border p-2"
                    placeholder="Password"
                    value={password}
                    onChange={e => setPassword(e.target.value)}
                />
                {passwordError && <p className="text-sm text-red-600">{passwordError}</p>}

                <select
                    className="w-full border p-2"
                    value={currency}
                    onChange={e => setCurrency(e.target.value)}
                >
                    <option value="EUR">EUR</option>
                    <option value="CHF">CHF</option>
                </select>

                {submitError && <p className="text-sm text-red-600">{submitError}</p>}

                <button
                    className="w-full bg-black text-white p-2"
                    disabled={!isFormValid || isSubmitting}
                    onClick={register}
                >
                    {isSubmitting ? "Creating..." : "Create Account"}
                </button>

                {isSubmitting && (
                    <p className="text-sm text-slate-600 text-center">Caricamento in corso...</p>
                )}

            </div>
        </div>
    )
}
