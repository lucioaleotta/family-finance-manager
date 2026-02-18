"use client"

import {
    LineChart,
    Line,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
    CartesianGrid,
} from "recharts"

type NetWorthMonthlyView = {
    month: string
    netWorth: number
}

export function NetWorthChart({
    data,
}: {
    data: NetWorthMonthlyView[]
}) {
    return (
        <div className="h-[320px] w-full">
            <ResponsiveContainer>
                <LineChart data={data}>
                    <CartesianGrid strokeDasharray="3 3" />

                    <XAxis
                        dataKey="month"
                        tickFormatter={(m: string) => m.slice(5)} // mostra solo MM
                    />

                    <YAxis />

                    <Tooltip />

                    <Line
                        type="monotone"
                        dataKey="netWorth"
                        strokeWidth={3}
                        dot={false}
                    />
                </LineChart>
            </ResponsiveContainer>
        </div>
    )
}
