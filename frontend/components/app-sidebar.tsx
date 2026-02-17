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

import { LayoutDashboard, ArrowRightLeft, Landmark, TrendingUp } from "lucide-react"

export function AppSidebar() {
    return (
        <Sidebar>
            <SidebarContent>

                <SidebarGroup>
                    <SidebarGroupContent>
                        <SidebarMenu>

                            <SidebarMenuItem>
                                <SidebarMenuButton>
                                    <Link href="/dashboard">
                                        <LayoutDashboard className="mr-2 h-4 w-4" />
                                        Dashboard
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton>
                                    <Link href="/transactions">
                                        <ArrowRightLeft className="mr-2 h-4 w-4" />
                                        Transactions
                                    </Link>
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton>
                                    <Landmark className="mr-2 h-4 w-4" />
                                    Accounts
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                            <SidebarMenuItem>
                                <SidebarMenuButton>
                                    <TrendingUp className="mr-2 h-4 w-4" />
                                    Net Worth
                                </SidebarMenuButton>
                            </SidebarMenuItem>

                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>

            </SidebarContent>
        </Sidebar>
    )
}
