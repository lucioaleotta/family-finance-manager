"use client"

import * as React from "react"
import { apiGet } from "@/lib/api"
import { formatAmount } from "@/lib/utils"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { AssetsAnnualChart } from "@/components/assets-annual-chart"
import { ArrowDownRight, ArrowUpRight, Droplets, Minus, TrendingUp, Wallet } from "lucide-react"

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

function currentYM() {
    const d = new Date()
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`
}

function previousYM(ym: string) {
    const [yearPart, monthPart] = ym.split("-")
    const year = Number(yearPart)
    const month = Number(monthPart)

    if (month <= 1) return `${year - 1}-12`
    return `${year}-${String(month - 1).padStart(2, "0")}`
}

function ym(year: number, month: number) {
    return `${year}-${String(month).padStart(2, "0")}`
}

function buildDelta(currentValue: number, previousValue: number | null) {
    if (previousValue === null) {
        return {
            amount: 0,
            percent: 0,
        }
    }

    const amount = currentValue - previousValue
    const percent = previousValue !== 0 ? (amount / previousValue) * 100 : 0

    return { amount, percent }
}

function formatDeltaPercent(value: number) {
    const sign = value > 0 ? "+" : ""
    return `${sign}${value.toFixed(2).replace(".", ",")}%`
}

function formatDeltaAmount(value: number) {
    const sign = value > 0 ? "+" : value < 0 ? "-" : ""
    return `${sign}${formatAmount(Math.abs(value))}`
}



export default function DashboardPage() {
    const [overview, setOverview] = React.useState<AssetsOverviewView | null>(null)
    const [previousYearOverview, setPreviousYearOverview] = React.useState<AssetsOverviewView | null>(null)
    const [loading, setLoading] = React.useState(true)
    const [year, setYear] = React.useState(currentYear())

    React.useEffect(() => {
        let cancelled = false

            ; (async () => {
                try {
                    setLoading(true)

                    const [overviewResponse, previousYearResponse] = await Promise.all([
                        apiGet<AssetsOverviewView>(`/api/assets/overview?year=${year}`),
                        apiGet<AssetsOverviewView>(`/api/assets/overview?year=${year - 1}`),
                    ])

                    if (cancelled) return

                    setOverview(overviewResponse)
                    setPreviousYearOverview(previousYearResponse)
                } finally {
                    if (!cancelled) setLoading(false)
                }
            })()

        return () => {
            cancelled = true
        }
    }, [year])

    const monthly = overview?.monthly ?? []
    const previousYearMonthly = previousYearOverview?.monthly ?? []
    const annual = overview?.annual
    const currency = overview?.currency ?? "EUR"
    const sortedMonthly = [...monthly].sort((a, b) => a.month.localeCompare(b.month))
    const sortedAllMonthly = [...previousYearMonthly, ...monthly].sort((a, b) => a.month.localeCompare(b.month))
    const latestMonthSnapshotInYear = sortedMonthly.length > 0 ? sortedMonthly[sortedMonthly.length - 1] : null

    const t1Month = year === currentYear() ? previousYM(currentYM()) : ym(year, 12)
    const t2Month = previousYM(t1Month)

    const t1Snapshot = sortedAllMonthly.find((entry) => entry.month === t1Month) ?? null
    const t2Snapshot = sortedAllMonthly.find((entry) => entry.month === t2Month) ?? null

    const referenceSnapshot = t1Snapshot ?? latestMonthSnapshotInYear
    const previousSnapshot = t1Snapshot ? t2Snapshot : null

    const annualLiquidity = referenceSnapshot?.liquidity ?? annual?.liquidity ?? 0
    const annualInvestments = referenceSnapshot?.investments ?? annual?.investments ?? 0
    const annualNetWorth = referenceSnapshot?.netWorth ?? annual?.netWorth ?? (annualLiquidity + annualInvestments)
    const netWorthDelta = buildDelta(
        referenceSnapshot?.netWorth ?? 0,
        previousSnapshot?.netWorth ?? null
    )
    const liquidityDelta = buildDelta(
        referenceSnapshot?.liquidity ?? 0,
        previousSnapshot?.liquidity ?? null
    )
    const investmentsDelta = buildDelta(
        referenceSnapshot?.investments ?? 0,
        previousSnapshot?.investments ?? null
    )

    return (
        <div className="w-full max-w-none space-y-6">

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

            <div className="grid gap-6 md:grid-cols-3">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0">
                        <CardTitle>Patrimonio</CardTitle>
                        <Wallet className="h-5 w-5 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold">
                            {currency} {formatAmount(annualNetWorth)}
                        </p>
                        <div
                            className={`mt-1 flex items-center gap-1 text-xs ${netWorthDelta.amount > 0
                                ? "text-emerald-600"
                                : netWorthDelta.amount < 0
                                    ? "text-red-600"
                                    : "text-muted-foreground"
                                }`}
                        >
                            {netWorthDelta.amount > 0 ? (
                                <ArrowUpRight className="h-3.5 w-3.5" />
                            ) : netWorthDelta.amount < 0 ? (
                                <ArrowDownRight className="h-3.5 w-3.5" />
                            ) : (
                                <Minus className="h-3.5 w-3.5" />
                            )}
                            <span>{formatDeltaAmount(netWorthDelta.amount)}</span>
                            <span>({formatDeltaPercent(netWorthDelta.percent)})</span>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0">
                        <CardTitle>Liquidità</CardTitle>
                        <Droplets className="h-5 w-5 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold">
                            {currency} {formatAmount(annualLiquidity)}
                        </p>
                        <div
                            className={`mt-1 flex items-center gap-1 text-xs ${liquidityDelta.amount > 0
                                ? "text-emerald-600"
                                : liquidityDelta.amount < 0
                                    ? "text-red-600"
                                    : "text-muted-foreground"
                                }`}
                        >
                            {liquidityDelta.amount > 0 ? (
                                <ArrowUpRight className="h-3.5 w-3.5" />
                            ) : liquidityDelta.amount < 0 ? (
                                <ArrowDownRight className="h-3.5 w-3.5" />
                            ) : (
                                <Minus className="h-3.5 w-3.5" />
                            )}
                            <span>{formatDeltaAmount(liquidityDelta.amount)}</span>
                            <span>({formatDeltaPercent(liquidityDelta.percent)})</span>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0">
                        <CardTitle>Investimenti</CardTitle>
                        <TrendingUp className="h-5 w-5 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold">
                            {currency} {formatAmount(annualInvestments)}
                        </p>
                        <div
                            className={`mt-1 flex items-center gap-1 text-xs ${investmentsDelta.amount > 0
                                ? "text-emerald-600"
                                : investmentsDelta.amount < 0
                                    ? "text-red-600"
                                    : "text-muted-foreground"
                                }`}
                        >
                            {investmentsDelta.amount > 0 ? (
                                <ArrowUpRight className="h-3.5 w-3.5" />
                            ) : investmentsDelta.amount < 0 ? (
                                <ArrowDownRight className="h-3.5 w-3.5" />
                            ) : (
                                <Minus className="h-3.5 w-3.5" />
                            )}
                            <span>{formatDeltaAmount(investmentsDelta.amount)}</span>
                            <span>({formatDeltaPercent(investmentsDelta.percent)})</span>
                        </div>
                    </CardContent>
                </Card>
            </div>

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
