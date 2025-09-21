package org.elmorshedy.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // Route root and auth aliases to the auth page
    @GetMapping({"/", "/auth"})
    public String authPage() {
        return "forward:/auth/auth.html";
    }

    @GetMapping({"/dashboard"})
    public String dashboardPage() {
        return "redirect:/dashboard/dashboard.html";
    }

    @GetMapping({"/leads"})
    public String leadsPage() {
        return "redirect:/lead/leads.html";
    }

    @GetMapping({"/meetings"})
    public String meetingsPage() {
        return "redirect:/meetings/meetings.html";
    }
}
