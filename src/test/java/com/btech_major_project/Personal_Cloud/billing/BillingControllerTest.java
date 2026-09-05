package com.btech_major_project.Personal_Cloud.billing;

import com.btech_major_project.Personal_Cloud.dto.BillingSummary;
import com.btech_major_project.Personal_Cloud.user.User;
import com.btech_major_project.Personal_Cloud.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingControllerTest {

    @Mock
    private BillingService billingService;
    @Mock
    private UserService userService;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private BillingController billingController;

    @Test
    void current() {
        when(userDetails.getUsername()).thenReturn("testuser");
        
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("testuser");
        
        when(userService.findByUsername("testuser")).thenReturn(user);

        BillingSummary summary = BillingSummary.empty();
        when(billingService.calculateCurrent(user)).thenReturn(summary);

        ResponseEntity<BillingSummary> response = billingController.current(userDetails);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(summary, response.getBody());
    }

    @Test
    void current_NullPrincipal() {
        assertThrows(NullPointerException.class, () -> billingController.current(null));
    }
}
