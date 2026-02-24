"use client"

import * as React from "react"
import Link from "next/link"
import { format } from "date-fns"

import { apiDelete, apiGet, apiPut } from "@/lib/api"
import type { AccountView, Currency, TransactionType } from "@/lib/types"
import { formatCurrency } from "@/lib/utils"

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { MonthPicker } from "@/components/month-picker"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { toast } from "sonner"


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

type EditTransactionForm = {
    accountId: string
    amount: string
    currency: Currency
    date: string
    type: TransactionType
    category: string
    description: string
}



function toDate(value: string) {
    const [y, m, d] = value.split("-").map((part) => Number(part))
    if (!y || !m || !d) return new Date()
    return new Date(y, m - 1, d)
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
    const [editingId, setEditingId] = React.useState<string | null>(null)
    const [editForm, setEditForm] = React.useState<EditTransactionForm | null>(null)
    const [savingId, setSavingId] = React.useState<string | null>(null)
    const [deletingId, setDeletingId] = React.useState<string | null>(null)

    const loadData = React.useCallback(async () => {
        const [a, t] = await Promise.all([
            apiGet<AccountView[]>("/api/accounts?type=CHECKING"),
            apiGet<TransactionView[]>(`/api/transactions?month=${month}`),
        ])
        setAccounts(a)
        setTxs(t)
    }, [month])

    React.useEffect(() => {
        let cancelled = false
            ; (async () => {
                try {
                    setLoading(true)
                    await loadData()
                } finally {
                    if (!cancelled) setLoading(false)
                }
            })()
        return () => {
            cancelled = true
        }
    }, [loadData])

    const accountName = React.useMemo(() => {
        const map = new Map(accounts.map((a) => [a.id, a.name]))
        return (id: string | null) => (id ? map.get(id) ?? id : "Unassigned")
    }, [accounts])

    const accountCurrency = React.useMemo(() => {
        const map = new Map(accounts.map((a) => [a.id, a.currency]))
        return (id: string | null) => (id ? map.get(id) ?? "EUR" : "EUR")
    }, [accounts])

    const unassignedValue = "UNASSIGNED"

    function startEdit(tx: TransactionView) {
        setEditingId(tx.id)
        setEditForm({
            accountId: tx.accountId ?? unassignedValue,
            amount: String(tx.amount.amount ?? ""),
            currency: (tx.amount.currency as Currency) ?? "EUR",
            date: tx.date,
            type: tx.type,
            category: tx.category,
            description: tx.description ?? "",
        })
    }

    function cancelEdit() {
        setEditingId(null)
        setEditForm(null)
    }

    async function handleSave(id: string) {
        if (!editForm) return
        const amountValue = Number(editForm.amount)
        if (!Number.isFinite(amountValue) || amountValue <= 0) {
            toast.error("Inserisci un importo valido.")
            return
        }
        if (!editForm.category.trim()) {
            toast.error("La categoria e obbligatoria.")
            return
        }

        try {
            setSavingId(id)
            await apiPut(`/api/transactions/${id}`, {
                accountId: editForm.accountId === unassignedValue ? null : editForm.accountId,
                amount: amountValue,
                currency: editForm.currency,
                date: editForm.date,
                type: editForm.type,
                category: editForm.category.trim(),
                description: editForm.description.trim() || null,
            })
            await loadData()
            toast.success("Transazione aggiornata.")
            cancelEdit()
        } catch (error) {
            toast.error(error instanceof Error ? error.message : "Errore durante il salvataggio.")
        } finally {
            setSavingId(null)
        }
    }

    async function handleDelete(id: string, kind: TransactionView["kind"]) {
        if (kind !== "STANDARD") {
            toast.error("Le transazioni transfer non sono modificabili o cancellabili qui.")
            return
        }
        if (!confirm("Eliminare questa transazione?")) return
        try {
            setDeletingId(id)
            await apiDelete(`/api/transactions/${id}`)
            await loadData()
            toast.success("Transazione eliminata.")
        } catch (error) {
            toast.error(error instanceof Error ? error.message : "Errore durante la cancellazione.")
        } finally {
            setDeletingId(null)
        }
    }

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

                                {editingId === t.id && editForm ? (
                                    <div className="mt-4 grid gap-4">
                                        <div className="grid gap-4 md:grid-cols-2">
                                            <div className="space-y-2">
                                                <Label>Account</Label>
                                                <Select
                                                    value={editForm.accountId}
                                                    onValueChange={(value) => {
                                                        const resolvedId = value === unassignedValue ? null : value
                                                        const currency = accountCurrency(resolvedId)
                                                        setEditForm({
                                                            ...editForm,
                                                            accountId: value,
                                                            currency: currency as Currency,
                                                        })
                                                    }}
                                                >
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="Seleziona un conto" />
                                                    </SelectTrigger>
                                                    <SelectContent>
                                                        <SelectItem value={unassignedValue}>Unassigned</SelectItem>
                                                        {accounts.map((a) => (
                                                            <SelectItem key={a.id} value={a.id}>
                                                                {a.name} · {a.currency}
                                                            </SelectItem>
                                                        ))}
                                                    </SelectContent>
                                                </Select>
                                            </div>

                                            <div className="space-y-2">
                                                <Label>Tipo</Label>
                                                <Select
                                                    value={editForm.type}
                                                    onValueChange={(value) =>
                                                        setEditForm({ ...editForm, type: value as TransactionType })
                                                    }
                                                >
                                                    <SelectTrigger>
                                                        <SelectValue placeholder="INCOME / EXPENSE" />
                                                    </SelectTrigger>
                                                    <SelectContent>
                                                        <SelectItem value="EXPENSE">EXPENSE</SelectItem>
                                                        <SelectItem value="INCOME">INCOME</SelectItem>
                                                    </SelectContent>
                                                </Select>
                                            </div>
                                        </div>

                                        <div className="grid gap-4 md:grid-cols-3">
                                            <div className="space-y-2">
                                                <Label>Importo</Label>
                                                <Input
                                                    type="number"
                                                    step="0.01"
                                                    value={editForm.amount}
                                                    onChange={(event) =>
                                                        setEditForm({ ...editForm, amount: event.target.value })
                                                    }
                                                />
                                                <p className="text-xs text-slate-500">
                                                    {formatCurrency(Number(editForm.amount), editForm.currency)}
                                                </p>
                                            </div>
                                            <div className="space-y-2">
                                                <Label>Valuta</Label>
                                                <Input value={editForm.currency} disabled />
                                            </div>
                                            <div className="space-y-2">
                                                <Label>Data</Label>
                                                <Popover>
                                                    <PopoverTrigger asChild>
                                                        <Button type="button" variant="outline" className="w-full justify-start">
                                                            {format(toDate(editForm.date), "yyyy-MM-dd")}
                                                        </Button>
                                                    </PopoverTrigger>
                                                    <PopoverContent className="w-auto p-0" align="start">
                                                        <Calendar
                                                            mode="single"
                                                            selected={toDate(editForm.date)}
                                                            onSelect={(date) => {
                                                                if (!date) return
                                                                setEditForm({
                                                                    ...editForm,
                                                                    date: format(date, "yyyy-MM-dd"),
                                                                })
                                                            }}
                                                            initialFocus
                                                        />
                                                    </PopoverContent>
                                                </Popover>
                                            </div>
                                        </div>

                                        <div className="space-y-2">
                                            <Label>Categoria</Label>
                                            <Input
                                                value={editForm.category}
                                                onChange={(event) =>
                                                    setEditForm({ ...editForm, category: event.target.value })
                                                }
                                            />
                                        </div>

                                        <div className="space-y-2">
                                            <Label>Descrizione</Label>
                                            <Textarea
                                                value={editForm.description}
                                                onChange={(event) =>
                                                    setEditForm({ ...editForm, description: event.target.value })
                                                }
                                            />
                                        </div>

                                        <div className="flex items-center justify-end gap-2">
                                            <Button type="button" variant="ghost" onClick={cancelEdit}>
                                                Annulla
                                            </Button>
                                            <Button type="button" onClick={() => handleSave(t.id)} disabled={savingId === t.id}>
                                                {savingId === t.id ? "Salvataggio..." : "Salva"}
                                            </Button>
                                        </div>
                                    </div>
                                ) : (
                                    <div className="mt-2 space-y-1">
                                        <div className="flex items-center justify-between">
                                            <div className="font-medium text-slate-700">
                                                {t.category}
                                            </div>
                                            <div className="text-lg font-semibold">
                                                {formatCurrency(Number(t.amount.amount), t.amount.currency)}
                                            </div>
                                        </div>
                                        {t.description && (
                                            <div className="text-sm text-slate-600">
                                                {t.description}
                                            </div>
                                        )}
                                    </div>
                                )}

                                <div className="mt-3 flex flex-wrap items-center justify-between gap-2">
                                    {t.transferId ? (
                                        <div className="text-xs text-slate-500">transferId: {t.transferId}</div>
                                    ) : (
                                        <div className="text-xs text-slate-500">&nbsp;</div>
                                    )}

                                    <div className="flex items-center gap-2">
                                        <Button
                                            type="button"
                                            variant="ghost"
                                            onClick={() => startEdit(t)}
                                            disabled={t.kind !== "STANDARD" || editingId === t.id}
                                        >
                                            Modifica
                                        </Button>
                                        <Button
                                            type="button"
                                            variant="ghost"
                                            onClick={() => handleDelete(t.id, t.kind)}
                                            disabled={t.kind !== "STANDARD" || deletingId === t.id}
                                        >
                                            {deletingId === t.id ? "Eliminazione..." : "Elimina"}
                                        </Button>
                                    </div>
                                </div>
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
