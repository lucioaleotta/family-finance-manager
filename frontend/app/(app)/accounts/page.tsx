"use client"

import * as React from "react"
import Link from "next/link"

import { apiGet } from "@/lib/api"
import type { AccountView } from "@/lib/types"
import { MonthPicker } from "@/components/month-picker"
import { formatAmount } from "@/lib/utils"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"

type MonthlyAccountSummaryView = {
    month: string
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

export default function AccountsPage() {
    const [month, setMonth] = React.useState(currentYM())
    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [summaries, setSummaries] = React.useState<MonthlyAccountSummaryView[]>([])
    const [loading, setLoading] = React.useState(true)

    const fetchData = React.useCallback(async () => {
        const [a, s] = await Promise.all([
            apiGet<AccountView[]>("/api/accounts?type=CHECKING"),
            apiGet<MonthlyAccountSummaryView[]>(`/api/reporting/monthly/accounts?month=${month}`),
        ])
        return { accounts: a, summaries: s }
    }, [month])

    React.useEffect(() => {
        let cancelled = false
            ; (async () => {
                try {
                    setLoading(true)
                    const { accounts: a, summaries: s } = await fetchData()
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
    }, [fetchData])

    const summaryByAccount = React.useMemo(() => {
        const map = new Map<string, MonthlyAccountSummaryView>()
        for (const s of summaries) map.set(s.accountId, s)
        return map
    }, [summaries])

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

    const totalBalance = React.useMemo(() => rows.reduce((sum, row) => sum + row.net, 0), [rows])

    return (
        <div className="space-y-6">
            <div className="flex flex-wrap items-end justify-between gap-4">
                <div className="space-y-1">
                    <h1 className="text-3xl font-semibold">Conteggi Mensili</h1>
                    <p className="text-slate-600">Mostra solo conti CHECKING e calcola i saldi automaticamente dal mese.</p>
                </div>

                <div className="flex items-center gap-3">
                    <MonthPicker value={month} onChange={setMonth} />
                    <Card className="shadow-sm flex-1 max-w-xs">
                        <CardContent className="px-6 py-2.5 flex items-center justify-between">
                            <span className="text-sm text-slate-600 font-medium">Saldo totale:</span>
                            <span className={"text-xl font-bold " + (totalBalance >= 0 ? "text-green-700" : "text-red-700")}>
                                {formatAmount(totalBalance)}
                            </span>
                        </CardContent>
                    </Card>
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
                                    <div className="text-xs text-slate-500">Entrate</div>
                                    <div className="mt-1 text-lg font-semibold">{formatAmount(income)}</div>
                                </div>

                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Uscite</div>
                                    <div className="mt-1 text-lg font-semibold">{formatAmount(expense)}</div>
                                </div>

                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Saldo</div>
                                    <div className={"mt-1 text-lg font-semibold " + (net >= 0 ? "text-green-700" : "text-red-700")}>
                                        {formatAmount(net)}
                                    </div>
                                </div>
                            </div>

                            <div className="flex justify-between">
                                <Button asChild variant="default" size="sm">
                                    <Link href={`/transactions/new?accountId=${account.id}`}>Aggiungi transazione</Link>
                                </Button>
                                <div className="flex gap-2">
                                    <Button asChild variant="ghost" size="sm">
                                        <Link href={`/transactions?month=${month}`}>Vedi movimenti →</Link>
                                    </Button>
                                    <Button asChild variant="secondary" size="sm">
                                        <Link href={`/accounts/${account.id}`}>12 mesi →</Link>
                                    </Button>
                                </div>
                            </div>

                            {loading && <div className="text-xs text-slate-500">Caricamento…</div>}
                        </CardContent>
                    </Card>
                ))}
            </div>

            {!loading && accounts.length === 0 && (
                <div className="text-slate-600">
                    Nessun conto CHECKING trovato. Crea un conto in Gestione Conti.
                </div>
            )}
        </div>
    )
}
