"use client"

import * as React from "react"
import Link from "next/link"

import { apiGet } from "@/lib/api"
import type { AccountView } from "@/lib/types"
import { MonthPicker } from "@/components/month-picker"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"

type MonthlyAccountSummaryView = {
    month: string // "YYYY-MM"
    accountId: string
    income: number
    expense: number
    net: number
}

function currentYM() {
    const d = new Date()
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, "0")
    return `${y}-${m}`
}

function fmt(n: number) {
    const sign = n < 0 ? "-" : ""
    const abs = Math.abs(n)
    return `${sign}${abs.toFixed(2)}`
}

export default function AccountsPage() {
    const [month, setMonth] = React.useState(currentYM())
    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [summaries, setSummaries] = React.useState<MonthlyAccountSummaryView[]>([])
    const [loading, setLoading] = React.useState(true)

    React.useEffect(() => {
        let cancelled = false
            ; (async () => {
                try {
                    setLoading(true)
                    const [a, s] = await Promise.all([
                        apiGet<AccountView[]>("/api/accounts"),
                        apiGet<MonthlyAccountSummaryView[]>(`/api/reporting/monthly/accounts?month=${month}`),
                    ])
                    if (cancelled) return
                    setAccounts(a)
                    setSummaries(s)
                } finally {
                    if (!cancelled) setLoading(false)
                }
            })()
        return () => {
            cancelled = true
        }
    }, [month])

    const summaryByAccount = React.useMemo(() => {
        const map = new Map<string, MonthlyAccountSummaryView>()
        for (const s of summaries) map.set(s.accountId, s)
        return map
    }, [summaries])

    // Mostra anche conti senza movimenti nel mese
    const rows = React.useMemo(() => {
        return accounts.map((a) => {
            const s = summaryByAccount.get(a.id)
            return {
                account: a,
                income: s?.income ?? 0,
                expense: s?.expense ?? 0,
                net: s?.net ?? 0,
            }
        })
    }, [accounts, summaryByAccount])

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-end justify-between gap-4">
                <div className="space-y-1">
                    <h1 className="text-3xl font-semibold">Accounts</h1>
                    <p className="text-slate-600">
                        Saldo del mese per conto (include anche i transfer). Cashflow “esterno” lo vedi in Dashboard.
                    </p>
                </div>

                <div className="flex items-center gap-3">
                    <MonthPicker value={month} onChange={setMonth} />
                    <Button asChild variant="outline">
                        <Link href="/transactions/new">Add Transaction</Link>
                    </Button>
                </div>
            </div>

            <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
                {rows.map(({ account, income, expense, net }) => (
                    <Card key={account.id} className="shadow-sm">
                        <CardHeader>
                            <CardTitle className="flex items-center justify-between gap-3">
                                <span className="truncate">{account.name}</span>
                                <span className="text-sm font-medium text-slate-600">{account.currency}</span>
                            </CardTitle>
                            <CardDescription>{account.type}</CardDescription>
                        </CardHeader>

                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-3 gap-3">
                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Income</div>
                                    <div className="mt-1 text-lg font-semibold">{fmt(income)}</div>
                                </div>

                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Expense</div>
                                    <div className="mt-1 text-lg font-semibold">{fmt(expense)}</div>
                                </div>

                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Net</div>
                                    <div
                                        className={
                                            "mt-1 text-lg font-semibold " + (net >= 0 ? "text-green-700" : "text-red-700")
                                        }
                                    >
                                        {fmt(net)}
                                    </div>
                                </div>
                            </div>

                            {/* Prossimo step: link ai movimenti filtrati per account */}
                            <div className="flex justify-end">
                                <Button asChild variant="ghost">
                                    <Link href={`/transactions?month=${month}`}>
                                        View transactions →
                                    </Link>
                                </Button>
                                <Button asChild variant="secondary">
                                    <Link href={`/accounts/${account.id}`}>12 mesi →</Link>
                                </Button>
                            </div>

                            {loading && <div className="text-xs text-slate-500">Loading…</div>}
                        </CardContent>
                    </Card>
                ))}
            </div>

            {!loading && accounts.length === 0 && (
                <div className="text-slate-600">
                    Nessun account trovato. Crea un account dal backend (per ora).
                </div>
            )}
        </div>
    )
}
