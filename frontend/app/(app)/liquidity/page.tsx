"use client"

import * as React from "react"
import Link from "next/link"
import { MonthPicker } from "@/components/month-picker"
import { apiGet, apiPut } from "@/lib/api"
import { formatAmount } from "@/lib/utils"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { ArrowDownRight, ArrowUpRight, Minus } from "lucide-react"

type AccountView = {
    id: string
    name: string
    currency: "EUR" | "CHF"
}

type LiquiditySnapshotView = {
    month: string
    accountId: string
    accountName: string
    liquidity: number
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

function previousYM(ym: string) {
    const [yearStr, monthStr] = ym.split("-")
    const year = Number(yearStr)
    const month = Number(monthStr)

    if (!Number.isFinite(year) || !Number.isFinite(month) || month < 1 || month > 12) {
        return ym
    }

    if (month === 1) {
        return `${year - 1}-12`
    }

    return `${year}-${String(month - 1).padStart(2, "0")}`
}

function formatDeltaPercent(value: number | null) {
    if (value === null || !Number.isFinite(value)) {
        return "n/d"
    }

    const sign = value > 0 ? "+" : ""
    return `${sign}${value.toFixed(2).replace(".", ",")}%`
}

function formatDeltaAmount(value: number) {
    const sign = value > 0 ? "+" : value < 0 ? "-" : ""
    return `${sign}${formatAmount(Math.abs(value))}`
}

export default function LiquidityPage() {
    const [month, setMonth] = React.useState(currentYM())
    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [totals, setTotals] = React.useState<CategoryMonthlyTotalView[]>([])
    const [previousTotals, setPreviousTotals] = React.useState<CategoryMonthlyTotalView[]>([])
    const [accountId, setAccountId] = React.useState("")
    const [amount, setAmount] = React.useState("")
    const [note, setNote] = React.useState("")
    const [snapshots, setSnapshots] = React.useState<LiquiditySnapshotView[]>([])
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
                    const data = await apiGet<AccountView[]>("/api/accounts?type=LIQUIDITY")
                    if (!cancelled) {
                        setAccounts(data)
                        if (data.length > 0) {
                            setAccountId((current) => current || data[0].id)
                        }
                    }
                } catch {
                    if (!cancelled) {
                        setError("Impossibile caricare i conti LIQUIDITY.")
                    }
                }
            })()

        return () => {
            cancelled = true
        }
    }, [])

    const loadTotals = React.useCallback(async () => {
        const previousMonth = previousYM(month)
        const [currentData, previousData] = await Promise.all([
            apiGet<CategoryMonthlyTotalView[]>(`/api/assets/liquidity/totals?month=${month}`),
            apiGet<CategoryMonthlyTotalView[]>(`/api/assets/liquidity/totals?month=${previousMonth}`),
        ])

        setTotals(currentData)
        setPreviousTotals(previousData)
    }, [month])

    const loadSnapshots = React.useCallback(async () => {
        if (!accountId) {
            setSnapshots([])
            setLoading(false)
            return
        }

        setLoading(true)
        try {
            const data = await apiGet<LiquiditySnapshotView[]>(
                `/api/assets/liquidity/snapshots/last-12?month=${month}&accountId=${accountId}`
            )
            setSnapshots(data)

            const prefillKey = `${accountId}:${month}`
            const shouldPrefill = lastPrefillKeyRef.current === null || lastPrefillKeyRef.current !== prefillKey
            if (shouldPrefill) {
                const current = data.find((snapshot) => snapshot.month === month)
                if (current) {
                    setAmount(fmtInputAmount(current.liquidity))
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
    const previousTotalsByCurrency = React.useMemo(() => {
        return new Map(previousTotals.map((total) => [total.currency, total.total]))
    }, [previousTotals])

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
            await apiPut("/api/assets/liquidity/snapshots", {
                month,
                accountId,
                liquidity: parsedAmount,
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
            <h1 className="text-3xl font-semibold">Liquidità</h1>

            <Card className="w-full">
                <CardHeader>
                    <CardTitle>Totale automatico del mese</CardTitle>
                </CardHeader>
                <CardContent>
                    {totals.length === 0 ? (
                        <p className="text-sm text-slate-600">Nessuno snapshot LIQUIDITY presente per {month}.</p>
                    ) : (
                        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                            {totals.map((total) => {
                                const previousValue = previousTotalsByCurrency.get(total.currency)
                                const hasDelta = typeof previousValue === "number"
                                const deltaAmount = hasDelta ? total.total - previousValue : 0
                                const deltaPercent =
                                    hasDelta && previousValue !== 0
                                        ? (deltaAmount / previousValue) * 100
                                        : hasDelta
                                            ? 0
                                            : null
                                const isPositive = deltaAmount > 0
                                const isNegative = deltaAmount < 0

                                return (
                                    <div key={total.currency} className="rounded-xl border bg-white px-6 py-5">
                                        <p className="text-base text-slate-500">{total.currency}</p>
                                        <p className="mt-1 text-3xl font-semibold leading-none">{formatAmount(total.total)}</p>

                                        {hasDelta ? (
                                            <div
                                                className={`mt-3 flex items-center gap-1 text-sm ${isPositive
                                                        ? "text-emerald-600"
                                                        : isNegative
                                                            ? "text-red-600"
                                                            : "text-muted-foreground"
                                                    }`}
                                            >
                                                {isPositive ? (
                                                    <ArrowUpRight className="h-3.5 w-3.5" />
                                                ) : isNegative ? (
                                                    <ArrowDownRight className="h-3.5 w-3.5" />
                                                ) : (
                                                    <Minus className="h-3.5 w-3.5" />
                                                )}
                                                <span>{formatDeltaAmount(deltaAmount)}</span>
                                                <span>({formatDeltaPercent(deltaPercent)})</span>
                                            </div>
                                        ) : null}
                                    </div>
                                )
                            })}
                        </div>
                    )}
                </CardContent>
            </Card>

            <Card className="w-full">
                <CardHeader>
                    <CardTitle>Snapshot mensile per conto</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                    {selectedAccount ? (
                        <p className="text-sm text-slate-500">
                            Stai aggiornando gli snapshot di <strong>{selectedAccount.name}</strong> ({selectedAccount.currency}).
                        </p>
                    ) : null}

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
                                placeholder="Liquidità"
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
                            Nessun conto LIQUIDITY trovato. Crealo da <Link className="underline" href="/accounts/manage">Gestione Conti</Link>.
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
                                            {snapshot.currency} {formatAmount(snapshot.liquidity)}
                                            {snapshot.note ? <span className="block text-xs text-slate-500">{snapshot.note}</span> : null}
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
