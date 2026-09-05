package com.btech_major_project.Personal_Cloud.billing;

import com.btech_major_project.Personal_Cloud.user.User;

import com.btech_major_project.Personal_Cloud.dto.BillingSummary;

public interface BillingService {
    BillingSummary calculateCurrent(User user);
}
