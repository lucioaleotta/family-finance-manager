import "./globals.css"
import { Toaster } from "sonner"

export const metadata = {
  title: "Family Finance Manager",
  description: "Personal finance dashboard",
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="it">
      <body className="min-h-screen bg-slate-50 antialiased">
        {children}
        <Toaster richColors position="top-right" />
      </body>
    </html>
  )
}
