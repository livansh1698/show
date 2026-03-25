package com.ddmo.app.controller;

import com.ddmo.app.dto.ApiResponse;
import com.ddmo.app.service.BarbershopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final BarbershopService barbershopService;

    public AccountController(BarbershopService barbershopService) {
        this.barbershopService = barbershopService;
    }

    @GetMapping("/{customerId}/balance")
    public ApiResponse<Map<String, BigDecimal>> getBalance(@PathVariable String customerId) {
        return ApiResponse.ok(Map.of("balance", barbershopService.getBalance(customerId)));
    }
}

