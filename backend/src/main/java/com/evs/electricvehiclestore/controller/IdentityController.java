package com.evs.electricvehiclestore.controller;

import com.evs.electricvehiclestore.service.IdentityService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/identity")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @PostMapping("/register")
    public String register() {
        return identityService.register();
    }

    @PostMapping("/login")
    public String login() {
        return identityService.login();
    }

    @PostMapping("/logout")
    public String logout() {
        return identityService.logout();
    }
}