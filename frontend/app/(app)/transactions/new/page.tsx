"use client"

import * as React from "react"
import { useRouter, useSearchParams } from "next/navigation"
import { z } from "zod"
import { useForm, type Resolver } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { format } from "date-fns"

import { apiGet, apiPost } from "@/lib/api"
import type { AccountView, CreateTransactionRequest, Currency, TransactionType } from "@/lib/types"
import { formatCurrency } from "@/lib/utils"

import { Card, CardContent, CardHeader, CardTitle, CardDescription } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { toast } from "sonner"


const schema = z.object({
    accountId: z.string().uuid().optional(),
    amount: z.coerce.number().positive(),
    currency: z.enum(["EUR", "CHF"]),
    date: z.date(),
    type: z.enum(["INCOME", "EXPENSE"]),
    category: z.string().min(1),
    description: z.string().optional(),
})

type FormValues = z.infer<typeof schema>



export default function NewTransactionPage() {
    return (
        <React.Suspense fallback={<div className="p-6 text-sm text-slate-600">Caricamento...</div>}>
            <NewTransactionPageContent />
        </React.Suspense>
    )
}

function NewTransactionPageContent() {
    const router = useRouter()
    const searchParams = useSearchParams()

    const [accounts, setAccounts] = React.useState<AccountView[]>([])
    const [loadingAccounts, setLoadingAccounts] = React.useState(true)
    const [submitting, setSubmitting] = React.useState(false)

    const form = useForm<FormValues>({
        resolver: zodResolver(schema) as Resolver<FormValues>,
        defaultValues: {
            amount: 0,
            currency: "EUR",
            date: new Date(),
            type: "EXPENSE",
            category: "",
            description: "",
        },
    })

    const watchedAccountId = form.watch("accountId")
    const watchedAmount = form.watch("amount")
    const watchedCurrency = form.watch("currency")
    const watchedDate = form.watch("date")

    React.useEffect(() => {
        let cancelled = false
            ; (async () => {
                try {
                    setLoadingAccounts(true)
                    const data = await apiGet<AccountView[]>("/api/accounts?type=CHECKING")
                    if (!cancelled) setAccounts(data)
                } catch {
                    toast.error("Impossibile caricare i conti. Verifica che il backend sia in esecuzione.")
                } finally {
                    if (!cancelled) setLoadingAccounts(false)
                }
            })()
        return () => {
            cancelled = true
        }
    }, [])

    // Preseleziona il conto se l'accountId è passato come query parameter
    React.useEffect(() => {
        const accountId = searchParams.get("accountId")
        if (accountId && accounts.length > 0) {
            const accountExists = accounts.find((a) => a.id === accountId)
            if (accountExists) {
                form.setValue("accountId", accountId)
            }
        }
    }, [searchParams, accounts, form])

    // Se selezioni un account, allinea la currency del form a quella del conto (scelta fintech UX)
    React.useEffect(() => {
        if (!watchedAccountId) return
        const acc = accounts.find((a) => a.id === watchedAccountId)
        if (!acc) return
        form.setValue("currency", acc.currency as Currency)
    }, [watchedAccountId, accounts, form])

    async function onSubmit(values: FormValues) {
        try {
            setSubmitting(true)

            const payload: CreateTransactionRequest = {
                accountId: values.accountId,
                amount: values.amount,
                currency: values.currency,
                date: format(values.date, "yyyy-MM-dd"),
                type: values.type as TransactionType,
                category: values.category,
                description: values.description || undefined,
            }

            await apiPost<CreateTransactionRequest, string>("/api/transactions", payload)

            toast.success("Transazione inserita correttamente.")

            router.push("/transactions")
        } catch (e: unknown) {
            const errorMessage = e instanceof Error ? e.message : "Errore durante il salvataggio."
            toast.error(errorMessage)
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <div className="space-y-6">
            <div className="space-y-1">
                <h1 className="text-3xl font-semibold">Add Transaction</h1>
                <p className="text-slate-600">Inserisci rapidamente entrate e uscite. Stile “fintech”, zero attrito.</p>
            </div>

            <Card className="shadow-sm">
                <CardHeader>
                    <CardTitle>Nuova transazione</CardTitle>
                    <CardDescription>
                        Suggerimento: scegli prima il conto per impostare automaticamente la valuta.
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    <form
                        className="grid gap-6"
                        onSubmit={form.handleSubmit(onSubmit)}
                    >
                        {/* Riga 1: Account + Type */}
                        <div className="grid gap-4 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Account</Label>
                                <Select
                                    value={form.getValues("accountId") ?? ""}
                                    onValueChange={(v) => form.setValue("accountId", v || undefined)}
                                    disabled={loadingAccounts}
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder={loadingAccounts ? "Caricamento..." : "Seleziona un conto"} />
                                    </SelectTrigger>
                                    <SelectContent>
                                        {accounts.map((a) => (
                                            <SelectItem key={a.id} value={a.id}>
                                                {a.name} · {a.currency}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                <p className="text-xs text-slate-500">
                                    Se non selezioni nulla, il backend userà l’account di default (“Unassigned”).
                                </p>
                            </div>

                            <div className="space-y-2">
                                <Label>Type</Label>
                                <Select
                                    value={form.getValues("type")}
                                    onValueChange={(v) => form.setValue("type", v as TransactionType)}
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

                        {/* Riga 2: Amount + Currency + Date */}
                        <div className="grid gap-4 md:grid-cols-3">
                            <div className="space-y-2">
                                <Label>Amount</Label>
                                <Input
                                    type="number"
                                    step="0.01"
                                    {...form.register("amount")}
                                />
                                <p className="text-xs text-slate-500">
                                    Valore formattato: {formatCurrency(Number(watchedAmount ?? 0), watchedCurrency)}
                                </p>
                            </div>

                            <div className="space-y-2">
                                <Label>Currency</Label>
                                <Select
                                    value={form.getValues("currency")}
                                    onValueChange={(v) => form.setValue("currency", v as Currency)}
                                    disabled={!!watchedAccountId} // se hai selezionato account, blocchiamo la valuta
                                >
                                    <SelectTrigger>
                                        <SelectValue placeholder="EUR / CHF" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="EUR">EUR</SelectItem>
                                        <SelectItem value="CHF">CHF</SelectItem>
                                    </SelectContent>
                                </Select>
                                {watchedAccountId && (
                                    <p className="text-xs text-slate-500">
                                        Valuta bloccata perché dipende dal conto selezionato.
                                    </p>
                                )}
                            </div>

                            <div className="space-y-2">
                                <Label>Date</Label>
                                <Popover>
                                    <PopoverTrigger asChild>
                                        <Button type="button" variant="outline" className="w-full justify-start">
                                            {format(watchedDate, "yyyy-MM-dd")}
                                        </Button>
                                    </PopoverTrigger>
                                    <PopoverContent className="w-auto p-0" align="start">
                                        <Calendar
                                            mode="single"
                                            selected={watchedDate}
                                            onSelect={(d) => d && form.setValue("date", d, { shouldDirty: true })}
                                            initialFocus
                                        />
                                    </PopoverContent>
                                </Popover>
                            </div>
                        </div>

                        {/* Riga 3: Category */}
                        <div className="space-y-2">
                            <Label>Category</Label>
                            <Input placeholder="Groceries, Salary, Fees..." {...form.register("category")} />
                        </div>

                        {/* Riga 4: Description */}
                        <div className="space-y-2">
                            <Label>Description (optional)</Label>
                            <Textarea placeholder="Note..." {...form.register("description")} />
                        </div>

                        <div className="flex items-center justify-end gap-3">
                            <Button
                                type="button"
                                variant="outline"
                                onClick={() => router.push("/transactions")}
                            >
                                Cancel
                            </Button>
                            <Button type="submit" disabled={submitting}>
                                {submitting ? "Saving..." : "Save Transaction"}
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    )
}
