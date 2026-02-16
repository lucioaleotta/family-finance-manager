package com.lucio.financeapp.assets.infrastructure.web;

import com.lucio.financeapp.assets.api.NetWorthMonthlyView;
import com.lucio.financeapp.assets.application.ComputeNetWorthTimelineUseCase;
import com.lucio.financeapp.shared.domain.Currency;
import org.springframework.web.bind.annotation.*;
import com.lucio.financeapp.assets.api.NetWorthReconciliationView;
import com.lucio.financeapp.assets.application.ComputeNetWorthReconciliationUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/assets/networth")
public class NetWorthController {

    private final ComputeNetWorthTimelineUseCase useCase;
    private final ComputeNetWorthReconciliationUseCase reconciliationUseCase;

    public NetWorthController(ComputeNetWorthTimelineUseCase useCase,
            ComputeNetWorthReconciliationUseCase reconciliationUseCase) {
        this.useCase = useCase;
        this.reconciliationUseCase = reconciliationUseCase;
    }

    @GetMapping("/timeline")
    public List<NetWorthMonthlyView> timeline(@RequestParam("year") int year,
            @RequestParam("currency") Currency currency) {
        return useCase.handle(year, currency);
    }

    @GetMapping("/reconciliation")
    public List<NetWorthReconciliationView> reconciliation(@RequestParam("year") int year,
            @RequestParam("currency") Currency currency) {
        return reconciliationUseCase.handle(year, currency);
    }

}
