package com.ddmo.app.controller;

import com.ddmo.app.dto.ApiResponse;
import com.ddmo.app.model.AuditLog;
import com.ddmo.app.service.BarbershopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final BarbershopService barbershopService;

    public AuditController(BarbershopService barbershopService) {
        this.barbershopService = barbershopService;
    }

    @GetMapping("/logs")
    public ApiResponse<List<AuditLog>> logs(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(barbershopService.listAuditLogs(keyword));
    }
}

