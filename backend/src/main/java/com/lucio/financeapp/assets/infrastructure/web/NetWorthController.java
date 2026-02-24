package com.lucio.financeapp.assets.infrastructure.web;

import com.lucio.financeapp.assets.api.NetWorthMonthlyView;
import com.lucio.financeapp.assets.application.ComputeNetWorthTimelineUseCase;
import org.springframework.web.bind.annotation.*;
import com.lucio.financeapp.assets.api.NetWorthReconciliationView;
import com.lucio.financeapp.assets.application.ComputeNetWorthReconciliationUseCase;
import com.lucio.financeapp.users.infrastructure.security.CurrentUser;

import java.util.List;

@RestController
@RequestMapping("/api/assets/networth")
public class NetWorthController {

    private final ComputeNetWorthTimelineUseCase useCase;
    private final ComputeNetWorthReconciliationUseCase reconciliationUseCase;
    private final CurrentUser currentUser;

    public NetWorthController(ComputeNetWorthTimelineUseCase useCase,
            ComputeNetWorthReconciliationUseCase reconciliationUseCase,
            CurrentUser currentUser) {
        this.useCase = useCase;
        this.reconciliationUseCase = reconciliationUseCase;
        this.currentUser = currentUser;
    }

    @GetMapping("/timeline")
    public List<NetWorthMonthlyView> timeline(@RequestParam("year") int year) {
        return useCase.handle(currentUser.requireUserId(), year);
    }

    @GetMapping("/reconciliation")
    public List<NetWorthReconciliationView> reconciliation(@RequestParam("year") int year) {
        return reconciliationUseCase.handle(currentUser.requireUserId(), year);
    }

}
