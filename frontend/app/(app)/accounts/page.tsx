"use client"

import * as React from "react"
import Link from "next/link"

import { apiGet, apiPost, apiPut } from "@/lib/api"
import type { AccountType, AccountView, Currency } from "@/lib/types"
import { MonthPicker } from "@/components/month-picker"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

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

const accountTypes: { value: AccountType; label: string }[] = [
    { value: "CHECKING", label: "Conto corrente" },
    { value: "CARD", label: "Carta" },
]
const accountCurrencies: Currency[] = ["EUR", "CHF"]

export default function AccountsPage() {
    const [month, setMonth] = React.useState(currentYM())
    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [summaries, setSummaries] = React.useState<MonthlyAccountSummaryView[]>([])
    const [loading, setLoading] = React.useState(true)
    const [createName, setCreateName] = React.useState("")
    const [createType, setCreateType] = React.useState<AccountType>("CHECKING")
    const [createCurrency, setCreateCurrency] = React.useState<Currency>("EUR")
    const [createError, setCreateError] = React.useState<string | null>(null)
    const [creating, setCreating] = React.useState(false)
    const [editingId, setEditingId] = React.useState<string | null>(null)

    const fetchData = React.useCallback(async () => {
        const [a, s] = await Promise.all([
            apiGet<AccountView[]>("/api/accounts"),
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

    async function handleCreateAccount(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const name = createName.trim()
        if (!name) {
            setCreateError("Inserisci un nome per il conto.")
            return
        }

        const normalized = name.toLowerCase()
        const duplicate = accounts.find((account) => {
            if (editingId && account.id === editingId) return false
            return account.name.trim().toLowerCase() === normalized
        })
        if (duplicate) {
            setCreateError("Esiste gia un account con questo nome.")
            return
        }

        try {
            setCreating(true)
            setCreateError(null)
            if (editingId) {
                await apiPut(`/api/accounts/${editingId}`, {
                    name,
                    type: createType,
                    currency: createCurrency,
                })
            } else {
                await apiPost("/api/accounts", {
                    name,
                    type: createType,
                    currency: createCurrency,
                })
            }
            setCreateName("")
            setCreateType("CHECKING")
            setCreateCurrency("EUR")
            setEditingId(null)
            const { accounts: a, summaries: s } = await fetchData()
            setAccounts(a)
            setSummaries(s)
        } catch (error) {
            setCreateError(error instanceof Error ? error.message : "Errore durante la creazione.")
        } finally {
            setCreating(false)
        }
    }

    function startEdit(account: AccountView) {
        setEditingId(account.id)
        setCreateName(account.name)
        setCreateType(account.type)
        setCreateCurrency(account.currency)
        setCreateError(null)
    }

    function cancelEdit() {
        setEditingId(null)
        setCreateName("")
        setCreateType("CHECKING")
        setCreateCurrency("EUR")
        setCreateError(null)
    }

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
                    <h1 className="text-3xl font-semibold">Conti</h1>
                    <p className="text-slate-600">
                        Saldo del mese per conto (include anche i transfer). Cashflow “esterno” lo vedi in Dashboard.
                    </p>
                </div>

                <div className="flex items-center gap-3">
                    <MonthPicker value={month} onChange={setMonth} />
                    <Button asChild variant="outline">
                        <Link href="/transactions/new">Aggiungi transazione</Link>
                    </Button>
                </div>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>{editingId ? "Modifica account" : "Nuovo account"}</CardTitle>
                    <CardDescription>
                        {editingId
                            ? "Aggiorna nome, tipo o valuta del conto."
                            : "Aggiungi un conto da utilizzare nelle transazioni."}
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form className="grid gap-4 md:grid-cols-[2fr_1fr_1fr_auto]" onSubmit={handleCreateAccount}>
                        <div className="space-y-2">
                            <Label htmlFor="account-name">Nome</Label>
                            <Input
                                id="account-name"
                                value={createName}
                                onChange={(event) => setCreateName(event.target.value)}
                                placeholder="Es. Conto corrente"
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="account-type">Tipo</Label>
                            <select
                                id="account-type"
                                className="h-10 rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                                value={createType}
                                onChange={(event) => setCreateType(event.target.value as AccountType)}
                            >
                                {accountTypes.map((type) => (
                                    <option key={type.value} value={type.value}>
                                        {type.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="account-currency">Valuta</Label>
                            <select
                                id="account-currency"
                                className="h-10 rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                                value={createCurrency}
                                onChange={(event) => setCreateCurrency(event.target.value as Currency)}
                            >
                                {accountCurrencies.map((currency) => (
                                    <option key={currency} value={currency}>
                                        {currency}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="flex items-end gap-2">
                            <Button type="submit" disabled={creating}>
                                {creating ? "Salvataggio..." : editingId ? "Salva" : "Crea account"}
                            </Button>
                            {editingId && (
                                <Button type="button" variant="ghost" onClick={cancelEdit}>
                                    Annulla
                                </Button>
                            )}
                        </div>
                    </form>

                    {createError && <p className="mt-3 text-sm text-red-600">{createError}</p>}
                </CardContent>
            </Card>

            <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
                {rows.map(({ account, income, expense, net }) => (
                    <Card key={account.id} className="shadow-sm">
                        <CardHeader>
                            <CardTitle className="flex items-center justify-between gap-3">
                                <span className="truncate">{account.name}</span>
                                <div className="flex items-center gap-2">
                                    <span className="text-sm font-medium text-slate-600">{account.currency}</span>
                                    <Button variant="ghost" size="sm" onClick={() => startEdit(account)}>
                                        Modifica
                                    </Button>
                                </div>
                            </CardTitle>
                            <CardDescription>{account.type}</CardDescription>
                        </CardHeader>

                        <CardContent className="space-y-4">
                            <div className="grid grid-cols-3 gap-3">
                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Entrate</div>
                                    <div className="mt-1 text-lg font-semibold">{fmt(income)}</div>
                                </div>

                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Uscite</div>
                                    <div className="mt-1 text-lg font-semibold">{fmt(expense)}</div>
                                </div>

                                <div className="rounded-xl border bg-white p-3">
                                    <div className="text-xs text-slate-500">Saldo</div>
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
                                        Vedi movimenti →
                                    </Link>
                                </Button>
                                <Button asChild variant="secondary">
                                    <Link href={`/accounts/${account.id}`}>12 mesi →</Link>
                                </Button>
                            </div>

                            {loading && <div className="text-xs text-slate-500">Caricamento…</div>}
                        </CardContent>
                    </Card>
                ))}
            </div>

            {!loading && accounts.length === 0 && (
                <div className="text-slate-600">
                    Nessun account trovato. Crea un account dal modulo qui sopra.
                </div>
            )}
        </div>
    )
}
