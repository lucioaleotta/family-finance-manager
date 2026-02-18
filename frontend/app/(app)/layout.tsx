import { SidebarProvider } from "@/components/ui/sidebar"
import { AppSidebar } from "@/components/app-sidebar"

export default function AppLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <SidebarProvider>
      <div className="flex h-screen">
        <AppSidebar />

        <main className="flex-1 overflow-auto p-8 bg-slate-50">
          {children}
        </main>
      </div>
    </SidebarProvider>
  )
}
