package com.lucio.financeapp.assets.infrastructure.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lucio.financeapp.assets.api.AssetsOverviewView;
import com.lucio.financeapp.assets.application.ComputeAssetsOverviewUseCase;
import com.lucio.financeapp.users.infrastructure.security.CurrentUser;

@RestController
@RequestMapping("/api/assets/overview")
public class AssetsOverviewController {

    private final ComputeAssetsOverviewUseCase useCase;
    private final CurrentUser currentUser;

    public AssetsOverviewController(ComputeAssetsOverviewUseCase useCase, CurrentUser currentUser) {
        this.useCase = useCase;
        this.currentUser = currentUser;
    }

    @GetMapping
    public AssetsOverviewView overview(@RequestParam("year") int year) {
        return useCase.handle(currentUser.requireUserId(), year);
    }
}
