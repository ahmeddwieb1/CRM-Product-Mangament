package org.elmorshedy.web;

import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.repo.meetingRepo;
import org.elmorshedy.product.repo.ProductRepo;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private LeadRepo leadRepo;
    @Autowired
    private meetingRepo meetingRepo;
    @Autowired
    private ProductRepo productRepo;

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        long users = userRepo.count();
        long leads = leadRepo.count();
        long meetings = meetingRepo.count();
        long products = productRepo.count();
        return Map.of(
                "users", users,
                "leads", leads,
                "meetings", meetings,
                "products", products
        );
    }
}
