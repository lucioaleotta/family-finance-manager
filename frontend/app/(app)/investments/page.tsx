"use client"

import * as React from "react"
import Link from "next/link"
import { MonthPicker } from "@/components/month-picker"
import { apiGet, apiPut } from "@/lib/api"
import { formatAmount } from "@/lib/utils"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

type AccountView = {
    id: string
    name: string
    currency: "EUR" | "CHF"
}

type InvestmentSnapshotView = {
    month: string
    accountId: string
    accountName: string
    totalInvested: number
    currency: "EUR" | "CHF"
    note?: string | null
}

type CategoryMonthlyTotalView = {
    currency: "EUR" | "CHF"
    total: number
}

function currentYM() {
    const d = new Date()
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`
}

function fmtInputAmount(value: number) {
    return value.toFixed(2)
}

export default function InvestmentsPage() {
    const [month, setMonth] = React.useState(currentYM())
    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [totals, setTotals] = React.useState<CategoryMonthlyTotalView[]>([])
    const [accountId, setAccountId] = React.useState("")
    const [amount, setAmount] = React.useState("")
    const [note, setNote] = React.useState("")
    const [snapshots, setSnapshots] = React.useState<InvestmentSnapshotView[]>([])
    const [loading, setLoading] = React.useState(true)
    const [saving, setSaving] = React.useState(false)
    const [error, setError] = React.useState("")
    const lastPrefillKeyRef = React.useRef<string | null>(null)

    const selectedAccount = React.useMemo(
        () => accounts.find((account) => account.id === accountId),
        [accounts, accountId]
    )

    React.useEffect(() => {
        let cancelled = false

            ; (async () => {
                try {
                    const data = await apiGet<AccountView[]>("/api/accounts?type=INVESTMENT")
                    if (!cancelled) {
                        setAccounts(data)
                        if (data.length > 0) {
                            setAccountId((current) => current || data[0].id)
                        }
                    }
                } catch {
                    if (!cancelled) {
                        setError("Impossibile caricare i conti INVESTMENT.")
                    }
                }
            })()

        return () => {
            cancelled = true
        }
    }, [])

    const loadTotals = React.useCallback(async () => {
        const data = await apiGet<CategoryMonthlyTotalView[]>(`/api/assets/investments/totals?month=${month}`)
        setTotals(data)
    }, [month])

    const loadSnapshots = React.useCallback(async () => {
        if (!accountId) {
            setSnapshots([])
            setLoading(false)
            return
        }

        setLoading(true)
        try {
            const data = await apiGet<InvestmentSnapshotView[]>(
                `/api/assets/investments/snapshots/last-12?month=${month}&accountId=${accountId}`
            )
            setSnapshots(data)

            const prefillKey = `${accountId}:${month}`
            const shouldPrefill = lastPrefillKeyRef.current === null || lastPrefillKeyRef.current !== prefillKey
            if (shouldPrefill) {
                const current = data.find((snapshot) => snapshot.month === month)
                if (current) {
                    setAmount(fmtInputAmount(current.totalInvested))
                    setNote(current.note ?? "")
                } else {
                    setAmount("")
                    setNote("")
                }
            }
            lastPrefillKeyRef.current = prefillKey
        } catch {
            setError("Caricamento snapshot non riuscito.")
        } finally {
            setLoading(false)
        }
    }, [accountId, month])

    React.useEffect(() => {
        void loadTotals()
    }, [loadTotals])

    React.useEffect(() => {
        void loadSnapshots()
    }, [loadSnapshots])

    const parsedAmount = Number(amount.replace(",", "."))
    const isAmountValid = Number.isFinite(parsedAmount) && parsedAmount >= 0

    async function saveSnapshot() {
        if (!accountId) {
            setError("Seleziona un conto")
            return
        }

        if (!isAmountValid || saving) {
            setError("Inserisci un importo valido")
            return
        }

        setError("")
        setSaving(true)

        try {
            await apiPut("/api/assets/investments/snapshots", {
                month,
                accountId,
                totalInvested: parsedAmount,
                note: note.trim() || null,
            })

            await Promise.all([loadSnapshots(), loadTotals()])
        } catch {
            setError("Salvataggio non riuscito. Riprova.")
        } finally {
            setSaving(false)
        }
    }

    return (
        <div className="w-full max-w-none space-y-6">
            <h1 className="text-3xl font-semibold">Investimenti</h1>

            <Card className="w-full">
                <CardHeader>
                    <CardTitle>Totale automatico del mese</CardTitle>
                </CardHeader>
                <CardContent>
                    {totals.length === 0 ? (
                        <p className="text-sm text-slate-600">Nessuno snapshot INVESTMENT presente per {month}.</p>
                    ) : (
                        <div className="flex flex-wrap gap-3">
                            {totals.map((total) => (
                                <div key={total.currency} className="rounded-xl border bg-white px-4 py-2 text-sm">
                                    <span className="text-slate-500">{total.currency}: </span>
                                    <span className="font-semibold">{formatAmount(total.total)}</span>
                                </div>
                            ))}
                        </div>
                    )}
                </CardContent>
            </Card>

            <Card className="w-full">
                <CardHeader>
                    <CardTitle>Snapshot mensile per conto</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="flex flex-wrap items-center gap-4">
                        <MonthPicker value={month} onChange={setMonth} />

                        <select
                            className="h-10 min-w-[220px] rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                            value={accountId}
                            onChange={(event) => setAccountId(event.target.value)}
                            disabled={accounts.length === 0}
                        >
                            {accounts.map((account) => (
                                <option key={account.id} value={account.id}>
                                    {account.name} ({account.currency})
                                </option>
                            ))}
                        </select>

                        <div className="w-[220px]">
                            <Input
                                type="number"
                                step="0.01"
                                min="0"
                                placeholder="Investimento"
                                value={amount}
                                onChange={(event) => setAmount(event.target.value)}
                            />
                        </div>

                        <div className="min-w-[220px] flex-1">
                            <Input
                                placeholder="Nota (opzionale)"
                                value={note}
                                onChange={(event) => setNote(event.target.value)}
                            />
                        </div>

                        <Button onClick={saveSnapshot} disabled={!isAmountValid || saving || !accountId}>
                            {saving ? "Salvataggio..." : "Salva snapshot"}
                        </Button>
                    </div>

                    {error && <p className="text-sm text-red-600">{error}</p>}

                    {accounts.length === 0 && (
                        <p className="text-sm text-slate-600">
                            Nessun conto INVESTMENT trovato. Crealo da <Link className="underline" href="/accounts/manage">Gestione Conti</Link>.
                        </p>
                    )}
                </CardContent>
            </Card>

            <Card className="w-full">
                <CardHeader>
                    <CardTitle>Ultimi 12 mesi</CardTitle>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <p>Caricamento...</p>
                    ) : (
                        <div className="space-y-2">
                            {[...snapshots]
                                .sort((a, b) => b.month.localeCompare(a.month))
                                .map((snapshot) => (
                                    <div
                                        key={`${snapshot.accountId}-${snapshot.month}`}
                                        className="flex flex-col gap-1 border-b py-2 text-sm sm:flex-row sm:items-center sm:justify-between"
                                    >
                                        <span className="font-medium">{snapshot.month}</span>
                                        <span className="sm:text-right">
                                            {snapshot.currency} {formatAmount(snapshot.totalInvested)}
                                            {snapshot.note ? <span className="block text-xs text-slate-500">{snapshot.note}</span> : null}
                                        </span>
                                    </div>
                                ))}
                        </div>
                    )}
                </CardContent>
            </Card>

            {selectedAccount ? (
                <p className="text-sm text-slate-500">
                    Stai aggiornando gli snapshot di <strong>{selectedAccount.name}</strong> ({selectedAccount.currency}).
                </p>
            ) : null}
        </div>
    )
}
