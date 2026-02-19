"use client"

import * as React from "react"
import { apiGet } from "@/lib/api"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs"
import { AssetsAnnualChart } from "@/components/assets-annual-chart"

type AssetsMonthlyView = {
    month: string
    currency: "EUR" | "CHF"
    liquidity: number
    investments: number
    netWorth: number
}

type AssetsAnnualView = {
    year: number
    currency: "EUR" | "CHF"
    liquidity: number
    investments: number
    netWorth: number
}

type AssetsOverviewView = {
    currency: "EUR" | "CHF"
    annual: AssetsAnnualView
    monthly: AssetsMonthlyView[]
}

function currentYear() {
    return new Date().getFullYear()
}

function yearsAround(now: number, past = 4) {
    const out: number[] = []
    for (let y = now - past; y <= now; y++) out.push(y)
    return out
}

function formatMoney(n: number) {
    return new Intl.NumberFormat("it-IT", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
        useGrouping: true
    }).format(n)
}

export default function DashboardPage() {
    const [overview, setOverview] = React.useState<AssetsOverviewView | null>(null)
    const [loading, setLoading] = React.useState(true)
    const [year, setYear] = React.useState(currentYear())

    React.useEffect(() => {
        let cancelled = false

            ; (async () => {
                try {
                    setLoading(true)

                    const overviewResponse = await apiGet<AssetsOverviewView>(
                        `/api/assets/overview?year=${year}`
                    )

                    if (cancelled) return

                    setOverview(overviewResponse)
                } finally {
                    if (!cancelled) setLoading(false)
                }
            })()

        return () => {
            cancelled = true
        }
    }, [year])

    const monthly = overview?.monthly ?? []
    const annual = overview?.annual
    const currency = overview?.currency ?? "EUR"
    const latestMonth = monthly.length > 0 ? monthly[monthly.length - 1] : null

    return (
        <div className="space-y-6">

            <div className="flex flex-wrap items-center justify-between gap-4">
                <h1 className="text-3xl font-semibold">Financial Overview</h1>
                <select
                    className="h-10 rounded-md border border-input bg-transparent px-3 py-2 text-sm"
                    value={String(year)}
                    onChange={(e) => setYear(Number(e.target.value))}
                >
                    {yearsAround(currentYear()).map((y) => (
                        <option key={y} value={y}>
                            {y}
                        </option>
                    ))}
                </select>
            </div>

            {/* CARDS */}

            <Tabs defaultValue="annual" className="w-full">
                <TabsList className="grid w-full grid-cols-2">
                    <TabsTrigger value="annual">Annuale</TabsTrigger>
                    <TabsTrigger value="monthly">Ultimo Mese</TabsTrigger>
                </TabsList>

                <TabsContent value="annual">
                    <div className="grid gap-6 md:grid-cols-3">
                        <Card>
                            <CardHeader>
                                <CardTitle>Patrimonio</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold">
                                    {currency} {formatMoney(annual?.netWorth ?? 0)}
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Liquidità</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold">
                                    {currency} {formatMoney(annual?.liquidity ?? 0)}
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Investimenti</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold">
                                    {currency} {formatMoney(annual?.investments ?? 0)}
                                </p>
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>

                <TabsContent value="monthly">
                    <div className="grid gap-6 md:grid-cols-3">
                        <Card>
                            <CardHeader>
                                <CardTitle>Patrimonio</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold">
                                    {currency} {formatMoney(latestMonth?.netWorth ?? 0)}
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Liquidità</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold">
                                    {currency} {formatMoney(latestMonth?.liquidity ?? 0)}
                                </p>
                            </CardContent>
                        </Card>

                        <Card>
                            <CardHeader>
                                <CardTitle>Investimenti</CardTitle>
                            </CardHeader>
                            <CardContent>
                                <p className="text-2xl font-bold">
                                    {currency} {formatMoney(latestMonth?.investments ?? 0)}
                                </p>
                            </CardContent>
                        </Card>
                    </div>
                </TabsContent>
            </Tabs>

            {/* CHART */}

            <Card>
                <CardHeader>
                    <CardTitle>Trends</CardTitle>
                </CardHeader>

                <CardContent>
                    {loading ? (
                        <p>Loading...</p>
                    ) : (
                        <AssetsAnnualChart data={monthly} currency={currency} />
                    )}
                </CardContent>
            </Card>

        </div>
    )
}
