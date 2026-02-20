"use client"

import {
    Sidebar,
    SidebarContent,
    SidebarGroup,
    SidebarGroupContent,
    SidebarMenu,
    SidebarMenuItem,
    SidebarMenuButton,
} from "@/components/ui/sidebar"

import Link from "next/link"

import { LayoutDashboard, ArrowRightLeft, Landmark, TrendingUp, Droplets } from "lucide-react"

export function AppSidebar() {
    return (
        <Sidebar>
            <SidebarContent>

                <SidebarGroup>
                    <SidebarGroupContent>
                        <SidebarMenu>

                            <SidebarMenuItem>
                                <SidebarMenuButton asChild>
                                    <Link href="/dashboard" className="flex items-center">
                                        <LayoutDashboard className="mr-2 h-4 w-4" />
                                        Dashboard
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton asChild>
                                    <Link href="/transactions" className="flex items-center">
                                        <ArrowRightLeft className="mr-2 h-4 w-4" />
                                        Transazioni
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton asChild>
                                    <Link href="/accounts" className="flex items-center">
                                        <Landmark className="mr-2 h-4 w-4" />
                                        Conteggi Mensili
                                    </Link>
                                </SidebarMenuButton>

                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton asChild>
                                    <Link href="/liquidity" className="flex items-center">
                                        <Droplets className="mr-2 h-4 w-4" />
                                        Liquidità
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton asChild>
                                    <Link href="/investments" className="flex items-center">
                                        <TrendingUp className="mr-2 h-4 w-4" />
                                        Investimenti
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>

            </SidebarContent>
        </Sidebar>
    )
}
