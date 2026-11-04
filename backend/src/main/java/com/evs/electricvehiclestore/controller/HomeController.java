package com.evs.electricvehiclestore.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping({"/", "/signin", "/signup", "/register"})
    public String home(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        return "forward:/index.html";
    }
}
