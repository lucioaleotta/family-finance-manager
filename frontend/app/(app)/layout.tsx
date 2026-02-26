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
        <div className="flex h-screen w-screen">

          <AppSidebar />

          <div className="flex min-w-0 flex-1 flex-col">

            <AppHeader />

            <main className="flex-1 w-full overflow-auto bg-slate-50">
              <div className="h-full w-full px-6 py-6">
                {children}
              </div>
            </main>

          </div>
        </div>
      </AuthGuard>
    </SidebarProvider>
  )
}
