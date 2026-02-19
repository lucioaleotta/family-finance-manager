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

type MonthlyPoint = {
    month: string
    currency: string
    liquidity: number
    investments: number
    netWorth: number
}

export function AssetsAnnualChart({
    data,
    currency,
}: {
    data: MonthlyPoint[]
    currency: string
}) {
    const formatValue = (value?: number) => {
        const safe = typeof value === "number" ? value : 0
        return new Intl.NumberFormat("it-IT", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2,
            useGrouping: true
        }).format(safe)
    }

    return (
        <div className="h-[260px] w-full">
            <ResponsiveContainer>
                <LineChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis
                        dataKey="month"
                        tickFormatter={(val: string) => val.slice(5)}
                    />
                    <YAxis />
                    <Tooltip
                        formatter={(value?: number) => {
                            return `${currency} ${formatValue(value)}`
                        }}
                    />
                    <Legend />
                    <Line
                        type="monotone"
                        dataKey="liquidity"
                        stroke="#8884d8"
                        name="Liquidità"
                    />
                    <Line
                        type="monotone"
                        dataKey="investments"
                        stroke="#82ca9d"
                        name="Investimenti"
                    />
                    <Line
                        type="monotone"
                        dataKey="netWorth"
                        stroke="#ffc658"
                        name="Patrimonio"
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    )
}
