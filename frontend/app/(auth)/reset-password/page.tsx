"use client"

import { Suspense, useEffect, useState } from "react"
import { useRouter, useSearchParams } from "next/navigation"

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

function ResetPasswordPageContent() {

    const router = useRouter()
    const searchParams = useSearchParams()

    const token = (searchParams.get("token") ?? "").trim()

    const [checkingToken, setCheckingToken] = useState(true)
    const [isTokenValid, setIsTokenValid] = useState(false)
    const [password, setPassword] = useState("")
    const [confirmPassword, setConfirmPassword] = useState("")
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [error, setError] = useState("")
    const [done, setDone] = useState(false)

    useEffect(() => {
        let cancelled = false

        void (async () => {
            if (!token) {
                if (!cancelled) {
                    setIsTokenValid(false)
                    setCheckingToken(false)
                }
                return
            }

            try {
                const response = await fetch(`http://localhost:8080/api/auth/reset-password/validate?token=${encodeURIComponent(token)}`)
                if (!response.ok) {
                    if (!cancelled) {
                        setIsTokenValid(false)
                        setCheckingToken(false)
                    }
                    return
                }

                const data: { valid: boolean } = await response.json()
                if (!cancelled) {
                    setIsTokenValid(data.valid)
                    setCheckingToken(false)
                }
            } catch {
                if (!cancelled) {
                    setIsTokenValid(false)
                    setCheckingToken(false)
                }
            }
        })()

        return () => {
            cancelled = true
        }
    }, [token])

    const passwordError = passwordValidationMessage(password)
    const confirmError = confirmPassword && password !== confirmPassword ? "Le password non coincidono" : ""
    const canSubmit = !passwordError && !confirmError && password.length > 0 && confirmPassword.length > 0

    async function resetPassword() {
        if (isSubmitting) {
            return
        }

        if (!canSubmit) {
            setError("Compila correttamente i campi password")
            return
        }

        setError("")
        setIsSubmitting(true)

        try {
            const response = await fetch("http://localhost:8080/api/auth/reset-password", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    token,
                    newPassword: password,
                    confirmPassword
                })
            })

            if (!response.ok) {
                setError("Token non valido/scaduto o password non conforme")
                return
            }

            setDone(true)
        } catch {
            setError("Errore di rete. Riprova tra poco.")
        } finally {
            setIsSubmitting(false)
        }
    }

    if (checkingToken) {
        return (
            <div className="flex h-screen items-center justify-center">
                <p className="text-sm text-slate-600">Verifica token in corso...</p>
            </div>
        )
    }

    return (
        <div className="flex h-screen items-center justify-center">
            <div className="w-[360px] space-y-4">
                <h1 className="text-2xl font-semibold">Reset Password</h1>

                {!isTokenValid ? (
                    <p className="text-sm text-red-600">
                        Il link di reset non è valido o è scaduto.
                    </p>
                ) : done ? (
                    <p className="text-sm text-green-700">
                        Password aggiornata con successo. Ora puoi accedere con la nuova password.
                    </p>
                ) : (
                    <>
                        <input
                            type="password"
                            className="w-full border p-2"
                            placeholder="Nuova password"
                            value={password}
                            onChange={e => setPassword(e.target.value)}
                        />
                        {passwordError && <p className="text-sm text-red-600">{passwordError}</p>}

                        <input
                            type="password"
                            className="w-full border p-2"
                            placeholder="Conferma nuova password"
                            value={confirmPassword}
                            onChange={e => setConfirmPassword(e.target.value)}
                        />
                        {confirmError && <p className="text-sm text-red-600">{confirmError}</p>}

                        {error && <p className="text-sm text-red-600">{error}</p>}

                        <button
                            className="w-full bg-black p-2 text-white"
                            disabled={isSubmitting}
                            onClick={resetPassword}
                        >
                            {isSubmitting ? "Salvataggio..." : "Aggiorna password"}
                        </button>
                    </>
                )}

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

export default function ResetPasswordPage() {
    return (
        <Suspense
            fallback={
                <div className="flex h-screen items-center justify-center">
                    <p className="text-sm text-slate-600">Caricamento...</p>
                </div>
            }
        >
            <ResetPasswordPageContent />
        </Suspense>
    )
}
