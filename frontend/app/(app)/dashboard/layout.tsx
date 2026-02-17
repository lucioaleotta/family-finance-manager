import { SidebarProvider, SidebarTrigger } from "@/components/ui/sidebar"
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

                <main className="flex-1 p-8 bg-slate-50">
                    <SidebarTrigger />
                    {children}
                </main>
            </div>
        </SidebarProvider>
    )
}
