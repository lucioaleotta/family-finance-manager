"use client"

import * as React from "react"
import Link from "next/link"
import { useParams } from "next/navigation"

import { apiGet } from "@/lib/api"
import type { AccountView } from "@/lib/types"
import { formatAmount } from "@/lib/utils"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

type MonthlyAccountTimelineView = {
  month: string // "YYYY-MM"
  accountId: string
  income: number
  expense: number
  net: number
}

function yearsAround(now: number, past = 5, future = 1) {
  const out: number[] = []
  for (let y = now - past; y <= now + future; y++) out.push(y)
  return out
}

function fmt(n: number, currency: string) {
  const sign = n < 0 ? "-" : ""
  return `${sign}${currency} ${formatAmount(Math.abs(n))}`
}

const MONTH_LABEL: Record<string, string> = {
  "01": "Gennaio",
  "02": "Febbraio",
  "03": "Marzo",
  "04": "Aprile",
  "05": "Maggio",
  "06": "Giugno",
  "07": "Luglio",
  "08": "Agosto",
  "09": "Settembre",
  "10": "Ottobre",
  "11": "Novembre",
  "12": "Dicembre",
}

export default function AccountYearPage() {
  const params = useParams<{ id: string }>()
  const accountId = params.id

  const [year, setYear] = React.useState<number>(new Date().getFullYear())
  const [account, setAccount] = React.useState<AccountView | null>(null)
  const [rows, setRows] = React.useState<MonthlyAccountTimelineView[]>([])
  const [loading, setLoading] = React.useState(true)

  const years = React.useMemo(() => yearsAround(new Date().getFullYear(), 5, 1), [])

  React.useEffect(() => {
    let cancelled = false
      ; (async () => {
        try {
          setLoading(true)

          const [accounts, timeline] = await Promise.all([
            apiGet<AccountView[]>("/api/accounts"),
            apiGet<MonthlyAccountTimelineView[]>(
              `/api/reporting/annual/timeline/accounts?year=${year}`
            ),
          ])

          if (cancelled) return

          const a = accounts.find((x) => x.id === accountId) ?? null
          setAccount(a)

          // filtra solo questo account e ordina per mese asc (01..12)
          const mine = timeline
            .filter((t) => t.accountId === accountId)
            .sort((a, b) => a.month.localeCompare(b.month))

          // se per qualche mese non ci sono dati, vogliamo comunque 12 card con 0
          const filled: MonthlyAccountTimelineView[] = []
          for (let m = 1; m <= 12; m++) {
            const mm = String(m).padStart(2, "0")
            const key = `${year}-${mm}`
            const found = mine.find((x) => x.month === key)
            filled.push(
              found ?? { month: key, accountId, income: 0, expense: 0, net: 0 }
            )
          }

          setRows(filled)
        } finally {
          if (!cancelled) setLoading(false)
        }
      })()

    return () => {
      cancelled = true
    }
  }, [accountId, year])

  const currency = account?.currency ?? "EUR"
  const name = account?.name ?? accountId

  // totali annuali (utile)
  const totals = React.useMemo(() => {
    const income = rows.reduce((s, r) => s + (r.income ?? 0), 0)
    const expense = rows.reduce((s, r) => s + (r.expense ?? 0), 0)
    const net = rows.reduce((s, r) => s + (r.net ?? 0), 0)
    return { income, expense, net }
  }, [rows])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-4">
        <div className="space-y-1">
          <h1 className="text-3xl font-semibold">{name}</h1>
          <p className="text-slate-600">
            Risultato mese per mese (12 mesi). Perfetto per “colpo d’occhio”.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="space-y-1">
            <div className="text-sm font-medium">Anno</div>
            <Select value={String(year)} onValueChange={(v) => setYear(Number(v))}>
              <SelectTrigger className="w-[140px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {years.map((y) => (
                  <SelectItem key={y} value={String(y)}>
                    {y}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <Button asChild variant="outline">
            <Link href="/accounts">← Back</Link>
          </Button>
        </div>
      </div>

      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle>Totale anno {year}</CardTitle>
          <CardDescription>Somma dei 12 mesi per questo account</CardDescription>
        </CardHeader>
        <CardContent className="grid gap-3 md:grid-cols-3">
          <div className="rounded-xl border bg-white p-3">
            <div className="text-xs text-slate-500">Income</div>
            <div className="mt-1 text-lg font-semibold">{fmt(totals.income, currency)}</div>
          </div>
          <div className="rounded-xl border bg-white p-3">
            <div className="text-xs text-slate-500">Expense</div>
            <div className="mt-1 text-lg font-semibold">{fmt(totals.expense, currency)}</div>
          </div>
          <div className="rounded-xl border bg-white p-3">
            <div className="text-xs text-slate-500">Net</div>
            <div
              className={
                "mt-1 text-lg font-semibold " + (totals.net >= 0 ? "text-green-700" : "text-red-700")
              }
            >
              {fmt(totals.net, currency)}
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
        {rows.map((r) => {
          const mm = r.month.split("-")[1]
          const label = MONTH_LABEL[mm] ?? r.month
          return (
            <Card key={r.month} className="shadow-sm">
              <CardHeader>
                <CardTitle className="flex items-center justify-between">
                  <span>{label}</span>
                  <span className="text-sm font-medium text-slate-600">{r.month}</span>
                </CardTitle>
                <CardDescription>{loading ? "Loading…" : " "}</CardDescription>
              </CardHeader>

              <CardContent className="space-y-3">
                <div className="grid grid-cols-3 gap-3">
                  <div className="rounded-xl border bg-white p-3">
                    <div className="text-xs text-slate-500">Income</div>
                    <div className="mt-1 font-semibold">{fmt(r.income, currency)}</div>
                  </div>
                  <div className="rounded-xl border bg-white p-3">
                    <div className="text-xs text-slate-500">Expense</div>
                    <div className="mt-1 font-semibold">{fmt(r.expense, currency)}</div>
                  </div>
                  <div className="rounded-xl border bg-white p-3">
                    <div className="text-xs text-slate-500">Net</div>
                    <div
                      className={
                        "mt-1 font-semibold " + (r.net >= 0 ? "text-green-700" : "text-red-700")
                      }
                    >
                      {fmt(r.net, currency)}
                    </div>
                  </div>
                </div>

                <div className="flex justify-end">
                  <Button asChild variant="ghost">
                    <Link href={`/transactions?month=${r.month}`}>View month →</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>
    </div>
  )
}
