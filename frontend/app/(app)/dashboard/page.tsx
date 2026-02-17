import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"

export default function DashboardPage() {
    return (
        <div className="space-y-6">

            <h1 className="text-3xl font-semibold">
                Financial Overview
            </h1>

            <div className="grid gap-6 md:grid-cols-3">

                <Card>
                    <CardHeader>
                        <CardTitle>Net Worth</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold">€ 124,000</p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Cashflow (Month)</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold text-green-600">
                            + € 2,450
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Liquidity</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <p className="text-2xl font-bold">
                            € 18,200
                        </p>
                    </CardContent>
                </Card>

            </div>

        </div>
    )
}
