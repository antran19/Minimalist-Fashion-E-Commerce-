package com.uminimalist.store.controller;

import com.uminimalist.store.service.LandingPageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final LandingPageService landingPageService;

    public HomeController(LandingPageService landingPageService) {
        this.landingPageService = landingPageService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", landingPageService.getFeaturedCategories());
        model.addAttribute("newArrivals", landingPageService.getNewArrivals());
        model.addAttribute("essentials", landingPageService.getEssentials());
        return "home";
    }
}
