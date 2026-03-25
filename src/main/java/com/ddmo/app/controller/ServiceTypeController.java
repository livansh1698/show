package com.ddmo.app.controller;

import com.ddmo.app.dto.ApiResponse;
import com.ddmo.app.dto.ServiceTypeRequest;
import com.ddmo.app.model.ServiceType;
import com.ddmo.app.service.BarbershopService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/config/services")
public class ServiceTypeController {

    private final BarbershopService barbershopService;

    public ServiceTypeController(BarbershopService barbershopService) {
        this.barbershopService = barbershopService;
    }

    @GetMapping
    public ApiResponse<List<ServiceType>> list() {
        return ApiResponse.ok(barbershopService.listServiceTypes());
    }

    @PostMapping
    public ApiResponse<ServiceType> create(@RequestBody ServiceTypeRequest request) {
        return ApiResponse.ok("创建成功", barbershopService.createServiceType(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ServiceType> update(@PathVariable String id, @RequestBody ServiceTypeRequest request) {
        return ApiResponse.ok("更新成功", barbershopService.updateServiceType(id, request));
    }

    @PatchMapping("/{id}/toggle-status")
    public ApiResponse<ServiceType> toggleStatus(@PathVariable String id) {
        return ApiResponse.ok("状态已更新", barbershopService.toggleServiceTypeStatus(id));
    }
}

