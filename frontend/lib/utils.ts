import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"
import type { Currency } from "./types"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * Formatta un importo con separatore delle migliaia e decimali
 * @param amount - L'importo da formattare
 * @returns Stringa formattata es: "1.000,00"
 */
export function formatAmount(amount: number): string {
  const safeAmount = Number.isFinite(amount) ? amount : 0
  return new Intl.NumberFormat("it-IT", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
    useGrouping: true,
  }).format(safeAmount)
}

/**
 * Formatta un importo con valuta e separatore delle migliaia
 * @param amount - L'importo da formattare
 * @param currency - La valuta (EUR, CHF, ecc.)
 * @returns Stringa formattata es: "€ 1.000,00"
 */
export function formatCurrency(amount: number, currency: Currency | string): string {
  const safeAmount = Number.isFinite(amount) ? amount : 0
  const code = currency === "EUR" || currency === "CHF" ? currency : "EUR"

  return new Intl.NumberFormat("it-IT", {
    style: "currency",
    currency: code,
    useGrouping: true,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(safeAmount)
}
