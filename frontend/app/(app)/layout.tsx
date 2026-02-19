import { SidebarProvider } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"
import { AuthGuard } from "@/components/auth-guard"
import { AppHeader } from "@/components/app-header"

export default function AppLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <SidebarProvider>
      <AuthGuard>
        <div className="flex h-screen">

          <AppSidebar />

          <div className="flex flex-col flex-1">

            <AppHeader />

            <main className="flex-1 overflow-auto p-8 bg-slate-50">
              {children}
            </main>

          </div>
        </div>
      </AuthGuard>
    </SidebarProvider>
  )
}
