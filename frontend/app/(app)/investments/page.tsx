"use client"

import * as React from "react"
import { MonthPicker } from "@/components/month-picker"
import { apiGet, apiPut } from "@/lib/api"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"

type InvestmentSnapshotView = {
    month: string
    totalInvested: number
    currency: "EUR" | "CHF"
    note?: string | null
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
    const [amount, setAmount] = React.useState("")
    const [currency, setCurrency] = React.useState<"EUR" | "CHF">("EUR")
    const [note, setNote] = React.useState("")
    const [snapshots, setSnapshots] = React.useState<InvestmentSnapshotView[]>([])
    const [loading, setLoading] = React.useState(true)
    const [saving, setSaving] = React.useState(false)
    const [error, setError] = React.useState("")
    const lastPrefillMonthRef = React.useRef<string | null>(null)

    const loadSnapshots = React.useCallback(async () => {
        setLoading(true)
        try {
            const data = await apiGet<InvestmentSnapshotView[]>(
                `/api/assets/investments/snapshots/last-12?month=${month}&currency=${currency}`
            )
            setSnapshots(data)

            const shouldPrefill = lastPrefillMonthRef.current === null || lastPrefillMonthRef.current !== month
            if (shouldPrefill) {
                const current = data.find((s) => s.month === month)
                if (current) {
                    setAmount(fmtInputAmount(current.totalInvested))
                    setNote(current.note ?? "")
                } else {
                    setAmount("")
                    setNote("")
                }
            }
            lastPrefillMonthRef.current = month
        } finally {
            setLoading(false)
        }
    }, [currency, month])

    React.useEffect(() => {
        void loadSnapshots()
    }, [loadSnapshots])

    const parsedAmount = Number(amount.replace(",", "."))
    const isAmountValid = Number.isFinite(parsedAmount) && parsedAmount >= 0

    async function saveSnapshot() {
        if (!isAmountValid || saving) {
            setError("Inserisci un importo valido")
            return
        }

        setError("")
        setSaving(true)
        try {
            await apiPut("/api/assets/investments/snapshots", {
                month,
                totalInvested: parsedAmount,
                currency,
                note: note.trim() || null,
            })

            await loadSnapshots()
        } catch {
            setError("Salvataggio non riuscito. Riprova.")
        } finally {
            setSaving(false)
        }
    }

    return (
        <div className="space-y-6">

            <h1 className="text-3xl font-semibold">Investments</h1>

            <Card>
                <CardHeader>
                    <CardTitle>Snapshot mensile</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="flex flex-wrap items-center gap-4">
                        <MonthPicker value={month} onChange={setMonth} />
                        <select
                            className="h-10 w-[120px] rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                            value={currency}
                            onChange={(e) => setCurrency(e.target.value as "EUR" | "CHF")}
                        >
                            <option value="EUR">EUR</option>
                            <option value="CHF">CHF</option>
                        </select>
                        <div className="w-[220px]">
                            <Input
                                type="number"
                                step="0.01"
                                min="0"
                                placeholder="Totale investito"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                            />
                        </div>
                        <div className="min-w-[220px] flex-1">
                            <Input
                                placeholder="Nota (opzionale)"
                                value={note}
                                onChange={(e) => setNote(e.target.value)}
                            />
                        </div>
                        <Button onClick={saveSnapshot} disabled={!isAmountValid || saving}>
                            {saving ? "Saving..." : "Save snapshot"}
                        </Button>
                    </div>

                    {error && <p className="text-sm text-red-600">{error}</p>}
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Ultimi 12 mesi</CardTitle>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <p>Loading...</p>
                    ) : (
                        <div className="space-y-2">
                            {[...snapshots]
                                .sort((a, b) => b.month.localeCompare(a.month))
                                .map((s) => (
                                    <div
                                        key={s.month}
                                        className="flex flex-col gap-1 border-b py-2 text-sm sm:flex-row sm:items-center sm:justify-between"
                                    >
                                        <span className="font-medium">{s.month}</span>
                                        <span className="sm:text-right">
                                            {s.currency} {formatAmount(s.totalInvested)}
                                            {s.note ? (
                                                <span className="block text-xs text-slate-500">{s.note}</span>
                                            ) : null}
                                        </span>
                                    </div>
                                ))}
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    )
}
