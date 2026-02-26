export type Currency = "EUR" | "CHF"
export type AccountType = "CHECKING" | "LIQUIDITY" | "INVESTMENT" | "CARD" | "SAVINGS" | "CASH"

export type AccountView = {
    id: string
    name: string
    type: AccountType
    currency: Currency
}

export type TransactionType = "INCOME" | "EXPENSE"

export type CreateTransactionRequest = {
    accountId?: string
    amount: number
    currency: Currency
    date: string // YYYY-MM-DD
    type: TransactionType
    category: string
    description?: string
}
