package com.btech_major_project.Personal_Cloud;

import com.btech_major_project.Personal_Cloud.BillingService.BillingSummary;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private static final AppLogger log = AppLogger.getLogger(BillingController.class);

    private final BillingService billingService;
    private final UserRepository userRepository;

    public BillingController(BillingService billingService, UserRepository userRepository) {
        this.billingService = billingService;
        this.userRepository = userRepository;
    }

    @GetMapping("/current")
    public ResponseEntity<BillingSummary> current(@AuthenticationPrincipal UserDetails principal) {
        log.info("GET /api/billing/current principal=" + (principal != null ? principal.getUsername() : "anonymous"));
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        BillingSummary summary = billingService.calculateCurrent(user);
        return ResponseEntity.ok(summary);
    }
}

