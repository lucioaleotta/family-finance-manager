"use client"

import * as React from "react"
import Link from "next/link"

import { apiGet } from "@/lib/api"
import type { AccountView } from "@/lib/types"

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { MonthPicker } from "@/components/month-picker"


type TransactionView = {
    id: string
    accountId: string | null
    amount: { amount: number; currency: string } // Money
    date: string
    type: "INCOME" | "EXPENSE"
    category: string
    description?: string
    kind: "STANDARD" | "TRANSFER"
    transferId?: string | null
}

function ymNow() {
    const d = new Date()
    const y = d.getFullYear()
    const m = String(d.getMonth() + 1).padStart(2, "0")
    return `${y}-${m}`
}

export default function TransactionsPage() {
    const [month, setMonth] = React.useState(ymNow())
    const [txs, setTxs] = React.useState<TransactionView[]>([])
    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [loading, setLoading] = React.useState(true)

    React.useEffect(() => {
        let cancelled = false
            ; (async () => {
                try {
                    setLoading(true)
                    const [a, t] = await Promise.all([
                        apiGet<AccountView[]>("/api/accounts"),
                        apiGet<TransactionView[]>(`/api/transactions?month=${month}`),
                    ])
                    if (cancelled) return
                    setAccounts(a)
                    setTxs(t)
                } finally {
                    if (!cancelled) setLoading(false)
                }
            })()
        return () => {
            cancelled = true
        }
    }, [month])

    const accountName = React.useMemo(() => {
        const map = new Map(accounts.map((a) => [a.id, a.name]))
        return (id: string | null) => (id ? map.get(id) ?? id : "Unassigned")
    }, [accounts])

    return (
        <div className="space-y-6">
            <div className="flex items-end justify-between gap-4">
                <div className="space-y-1">
                    <h1 className="text-3xl font-semibold">Transactions</h1>
                    <p className="text-slate-600">Lista per mese. (Transfer inclusi: li vedi ma puoi filtrarli dopo.)</p>
                </div>
                <Button asChild>
                    <Link href="/transactions/new">Add Transaction</Link>
                </Button>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Month</CardTitle>
                    <CardDescription>Select a month and year</CardDescription>
                </CardHeader>
                <CardContent className="flex items-center gap-3">
                    <MonthPicker value={month} onChange={setMonth} />
                </CardContent>
            </Card>

            <Card className="shadow-sm">
                <CardHeader>
                    <CardTitle>Movimenti</CardTitle>
                    <CardDescription>{loading ? "Caricamento..." : `${txs.length} righe`}</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="space-y-3">
                        {txs.map((t) => (
                            <div key={t.id} className="rounded-xl border bg-white p-4">
                                <div className="flex flex-wrap items-center justify-between gap-2">
                                    <div className="font-medium">
                                        {t.type} · {t.kind}
                                    </div>
                                    <div className="text-sm text-slate-600">
                                        {t.date} · {accountName(t.accountId)}
                                    </div>
                                </div>

                                <div className="mt-2 flex items-center justify-between">
                                    <div className="text-slate-700">
                                        {t.category} {t.description ? `— ${t.description}` : ""}
                                    </div>
                                    <div className="text-lg font-semibold">
                                        {t.amount.currency} {Number(t.amount.amount).toFixed(2)}
                                    </div>
                                </div>

                                {t.transferId ? (
                                    <div className="mt-2 text-xs text-slate-500">
                                        transferId: {t.transferId}
                                    </div>
                                ) : null}
                            </div>
                        ))}

                        {!loading && txs.length === 0 && (
                            <div className="text-slate-600">Nessuna transazione per questo mese.</div>
                        )}
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}
