"use client"

import * as React from "react"

import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    CartesianGrid,
    Legend,
} from "recharts"
import { formatAmount } from "@/lib/utils"

type AssetsMonthlyView = {
    month: string
    liquidity: number
    investments: number
    netWorth: number
}

export function AssetsOverviewChart({
    data,
    currency,
}: {
    data: AssetsMonthlyView[]
    currency: string
}) {
    return (
        <div className="h-[320px] w-full">
            <ResponsiveContainer>
                <LineChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />

                    <XAxis
                        dataKey="month"
                        tickFormatter={(m: string) => m.slice(5)}
                    />

                    <YAxis />

                    <Tooltip
                        formatter={(value?: number) => {
                            const safe = typeof value === "number" ? value : 0
                            return `${currency} ${formatAmount(safe)}`
                        }}
                    />
                    <Legend />

                    <Line type="monotone" dataKey="liquidity" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="investments" strokeWidth={2} dot={false} />
                    <Line type="monotone" dataKey="netWorth" strokeWidth={3} dot={false} />
                </LineChart>
            </ResponsiveContainer>
        </div>
    )
}
