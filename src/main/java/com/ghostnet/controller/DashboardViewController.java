package com.ghostnet.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ghostnet.model.Geisternetz;
import com.ghostnet.repository.GeisternetzRepository;

@Controller
public class DashboardViewController {

    private final GeisternetzRepository repo;

    public DashboardViewController(GeisternetzRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<Geisternetz> gemeldet = repo.findByStatus(Geisternetz.Status.GEMELDET);
        model.addAttribute("GEMELDET", gemeldet);
        return "dashboard";
    }
}
