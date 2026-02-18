"use client"

import * as React from "react"
import { Button } from "@/components/ui/button"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

const MONTHS = [
    { value: "01", label: "Gennaio" },
    { value: "02", label: "Febbraio" },
    { value: "03", label: "Marzo" },
    { value: "04", label: "Aprile" },
    { value: "05", label: "Maggio" },
    { value: "06", label: "Giugno" },
    { value: "07", label: "Luglio" },
    { value: "08", label: "Agosto" },
    { value: "09", label: "Settembre" },
    { value: "10", label: "Ottobre" },
    { value: "11", label: "Novembre" },
    { value: "12", label: "Dicembre" },
] as const

function yearsAround(now: number, past = 5, future = 1) {
    const out: number[] = []
    for (let y = now - past; y <= now + future; y++) out.push(y)
    return out
}

export function MonthPicker({
    value, // "YYYY-MM"
    onChange,
    yearsPast = 5,
    yearsFuture = 1,
}: {
    value: string
    onChange: (v: string) => void
    yearsPast?: number
    yearsFuture?: number
}) {
    const [open, setOpen] = React.useState(false)

    const [year, month] = value.split("-")
    const years = React.useMemo(
        () => yearsAround(new Date().getFullYear(), yearsPast, yearsFuture),
        [yearsPast, yearsFuture]
    )

    const monthLabel = MONTHS.find((m) => m.value === month)?.label ?? month

    return (
        <Popover open={open} onOpenChange={setOpen}>
            <PopoverTrigger asChild>
                <Button variant="outline" className="w-[240px] justify-between">
                    {monthLabel} {year}
                </Button>
            </PopoverTrigger>

            <PopoverContent className="w-[260px] space-y-3" align="start">
                <div className="space-y-2">
                    <div className="text-sm font-medium">Anno</div>
                    <Select
                        value={year}
                        onValueChange={(y) => onChange(`${y}-${month}`)}
                    >
                        <SelectTrigger>
                            <SelectValue placeholder="Seleziona anno" />
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

                <div className="space-y-2">
                    <div className="text-sm font-medium">Mese</div>
                    <div className="grid grid-cols-2 gap-2">
                        {MONTHS.map((m) => (
                            <Button
                                key={m.value}
                                type="button"
                                variant={m.value === month ? "default" : "outline"}
                                className="justify-start"
                                onClick={() => {
                                    onChange(`${year}-${m.value}`)
                                    setOpen(false) // UX: chiudi appena scelto il mese
                                }}
                            >
                                {m.label}
                            </Button>
                        ))}
                    </div>
                </div>
            </PopoverContent>
        </Popover>
    )
}
