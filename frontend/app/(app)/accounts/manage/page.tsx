"use client"

import * as React from "react"

import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api"
import type { AccountType, AccountView, Currency } from "@/lib/types"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

const accountTypes: { value: AccountType; label: string }[] = [
    { value: "CHECKING", label: "Conteggi mensili" },
    { value: "LIQUIDITY", label: "Liquidità" },
    { value: "INVESTMENT", label: "Investimenti" },
]
const accountCurrencies: Currency[] = ["EUR", "CHF"]

export default function ManageAccountsPage() {
    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [loading, setLoading] = React.useState(true)
    const [createName, setCreateName] = React.useState("")
    const [createType, setCreateType] = React.useState<AccountType>("CHECKING")
    const [createCurrency, setCreateCurrency] = React.useState<Currency>("EUR")
    const [error, setError] = React.useState<string | null>(null)
    const [creating, setCreating] = React.useState(false)
    const [editingId, setEditingId] = React.useState<string | null>(null)
    const [deletingId, setDeletingId] = React.useState<string | null>(null)

    const loadAccounts = React.useCallback(async () => {
        const data = await apiGet<AccountView[]>("/api/accounts")
        setAccounts(data)
    }, [])

    React.useEffect(() => {
        let cancelled = false
            ; (async () => {
                try {
                    setLoading(true)
                    const data = await apiGet<AccountView[]>("/api/accounts")
                    if (!cancelled) setAccounts(data)
                } finally {
                    if (!cancelled) setLoading(false)
                }
            })()
        return () => {
            cancelled = true
        }
    }, [])

    async function handleSave(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault()
        const name = createName.trim()
        if (!name) {
            setError("Inserisci un nome per il conto.")
            return
        }

        const normalized = name.toLowerCase()
        const duplicate = accounts.find((account) => {
            if (editingId && account.id === editingId) return false
            return account.name.trim().toLowerCase() === normalized
        })
        if (duplicate) {
            setError("Esiste già un account con questo nome.")
            return
        }

        try {
            setCreating(true)
            setError(null)
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
            await loadAccounts()
        } catch (e) {
            setError(e instanceof Error ? e.message : "Errore durante il salvataggio.")
        } finally {
            setCreating(false)
        }
    }

    async function handleDelete(accountId: string) {
        if (!confirm("Confermi la cancellazione del conto?")) return
        try {
            setDeletingId(accountId)
            setError(null)
            await apiDelete(`/api/accounts/${accountId}`)
            if (editingId === accountId) {
                cancelEdit()
            }
            await loadAccounts()
        } catch (e) {
            setError(e instanceof Error ? e.message : "Errore durante la cancellazione.")
        } finally {
            setDeletingId(null)
        }
    }

    function startEdit(account: AccountView) {
        setEditingId(account.id)
        setCreateName(account.name)
        setCreateType(account.type)
        setCreateCurrency(account.currency)
        setError(null)
    }

    function cancelEdit() {
        setEditingId(null)
        setCreateName("")
        setCreateType("CHECKING")
        setCreateCurrency("EUR")
        setError(null)
    }

    return (
        <div className="space-y-6">
            <div className="space-y-1">
                <h1 className="text-3xl font-semibold">Gestione Conti</h1>
                <p className="text-slate-600">Crea, modifica o elimina conti definendo la tipologia funzionale.</p>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>{editingId ? "Modifica conto" : "Nuovo conto"}</CardTitle>
                    <CardDescription>
                        {editingId
                            ? "Aggiorna nome, tipologia o valuta del conto."
                            : "Crea un conto CHECKING, LIQUIDITY o INVESTMENT."}
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form className="grid gap-4 md:grid-cols-[2fr_1fr_1fr_auto]" onSubmit={handleSave}>
                        <div className="space-y-2">
                            <Label htmlFor="account-name">Nome</Label>
                            <Input
                                id="account-name"
                                value={createName}
                                onChange={(event) => setCreateName(event.target.value)}
                                placeholder="Es. Conto principale"
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
                                {creating ? "Salvataggio..." : editingId ? "Salva" : "Crea conto"}
                            </Button>
                            {editingId && (
                                <Button type="button" variant="ghost" onClick={cancelEdit}>
                                    Annulla
                                </Button>
                            )}
                        </div>
                    </form>

                    {error && <p className="mt-3 text-sm text-red-600">{error}</p>}
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Conti esistenti</CardTitle>
                    <CardDescription>{loading ? "Caricamento..." : `${accounts.length} conti`}</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="space-y-3">
                        {accounts.map((account) => (
                            <div key={account.id} className="rounded-xl border bg-white p-4">
                                <div className="flex flex-wrap items-center justify-between gap-3">
                                    <div>
                                        <div className="font-medium">{account.name}</div>
                                        <div className="text-sm text-slate-600">
                                            {account.type} · {account.currency}
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <Button variant="ghost" size="sm" onClick={() => startEdit(account)}>
                                            Modifica
                                        </Button>
                                        <Button
                                            variant="ghost"
                                            size="sm"
                                            onClick={() => handleDelete(account.id)}
                                            disabled={deletingId === account.id}
                                        >
                                            {deletingId === account.id ? "Eliminazione..." : "Elimina"}
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        ))}

                        {!loading && accounts.length === 0 && <div className="text-slate-600">Nessun conto disponibile.</div>}
                    </div>
                </CardContent>
            </Card>
        </div>
    )
}
