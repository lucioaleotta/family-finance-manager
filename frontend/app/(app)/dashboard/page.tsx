"use client"

import * as React from "react"
import { apiGet } from "@/lib/api"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { NetWorthChart } from "@/components/networth-chart"

type NetWorthMonthlyView = {
    month: string
    netWorth: number
}

type MonthlyBalanceView = {
    income: number
    expense: number
    net: number
}

function currentYear() {
    return new Date().getFullYear()
}

function currentYM() {
    const d = new Date()
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}`
}

function fmt(n: number) {
    return n.toFixed(2)
}

export default function DashboardPage() {
    const [timeline, setTimeline] = React.useState<NetWorthMonthlyView[]>([])
    const [balance, setBalance] = React.useState<MonthlyBalanceView | null>(null)
    const [loading, setLoading] = React.useState(true)

    React.useEffect(() => {
        let cancelled = false

            ; (async () => {
                try {
                    setLoading(true)

                    const year = currentYear()
                    const month = currentYM()

                    const [networth, monthly] = await Promise.all([
                        apiGet<NetWorthMonthlyView[]>(
                            `/api/assets/networth/timeline?year=${year}`
                        ),
                        apiGet<MonthlyBalanceView>(
                            `/api/reporting/monthly?month=${month}`
                        ),
                    ])

                    if (cancelled) return

                    setTimeline(networth)
                    setBalance(monthly)
                } finally {
                    if (!cancelled) setLoading(false)
                }
            })()

        return () => {
            cancelled = true
        }
    }, [])

    const netWorthNow =
        timeline.length > 0
            ? timeline[timeline.length - 1].netWorth
            : 0

    return (
        <div className="space-y-6">

            <h1 className="text-3xl font-semibold">
                Financial Overview
            </h1>

            {/* TOP CARDS */}

            <div className="grid gap-6 md:grid-cols-3">

                <Card>
                    <CardHeader>
                        <CardTitle>Net Worth</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold">
                            € {fmt(netWorthNow)}
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Cashflow (Month)</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p
                            className={
                                "text-2xl font-bold " +
                                ((balance?.net ?? 0) >= 0
                                    ? "text-green-600"
                                    : "text-red-600")
                            }
                        >
                            € {fmt(balance?.net ?? 0)}
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Liquidity</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold">
                            € {fmt(netWorthNow)} {/* placeholder */}
                        </p>
                    </CardContent>
                </Card>

            </div>

            {/* CHART */}

            <Card>
                <CardHeader>
                    <CardTitle>Net Worth Trend</CardTitle>
                </CardHeader>

                <CardContent>
                    {loading ? (
                        <p>Loading...</p>
                    ) : (
                        <NetWorthChart data={timeline} />
                    )}
                </CardContent>
            </Card>

        </div>
    )
}
