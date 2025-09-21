package org.elmorshedy.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import org.bson.types.ObjectId;
import org.elmorshedy.lead.repo.LeadRepo;
import org.elmorshedy.meeting.repo.MeetingRepo;
import org.elmorshedy.product.repo.ProductRepo;
import org.elmorshedy.security.ObjectIdParam;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private LeadRepo leadRepo;
    @Autowired
    private MeetingRepo meetingRepo;
    @Autowired
    private ProductRepo productRepo;

    @GetMapping("/summary")
    public Map<String, Long> summary() {
        long leads = leadRepo.count();
        long meetings = meetingRepo.count();
        long products = productRepo.count();
        return Map.of(
                "leads", leads,
                "meetings", meetings,
                "products", products
        );
    }
    @Operation(summary = "Summary by assignedToId")

    @GetMapping("/summary/{id}")
    public Map<String, Long> summaryByAssignedToId(@ObjectIdParam @PathVariable ObjectId id) {
        long leads = leadRepo.countByAssignedToId(id);
        long meetings = meetingRepo.countByAssignedToId(id);
        long products = productRepo.count();
        return Map.of(
                "leads", leads,
                "meetings", meetings,
                "products", products
        );
    }
}
