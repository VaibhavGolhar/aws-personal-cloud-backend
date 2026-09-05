package com.btech_major_project.Personal_Cloud.billing;

import com.btech_major_project.Personal_Cloud.dto.BillingSummary;
import com.btech_major_project.Personal_Cloud.usage.UserUsage;
import com.btech_major_project.Personal_Cloud.usage.UserUsageRepository;
import com.btech_major_project.Personal_Cloud.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StandardBillingServiceTest {

    @Mock
    private UserUsageRepository usageRepo;

    @InjectMocks
    private StandardBillingService billingService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void calculateCurrent_EmptyWhenNoUsage() {
        when(usageRepo.findByUserId(1L)).thenReturn(Optional.empty());

        BillingSummary summary = billingService.calculateCurrent(user);

        assertEquals(0L, summary.getStorageBytes());
        assertEquals(BigDecimal.ZERO.setScale(6), summary.getTotal());
    }

    @Test
    void calculateCurrent_CalculatesCorrectly() {
        UserUsage usage = new UserUsage();
        // Set exactly 1 GB
        usage.setTotalBytesStored(1024L * 1024L * 1024L);
        // 2000 write requests
        usage.setPutCount(1000L);
        usage.setCopyCount(500L);
        usage.setPostCount(500L);
        usage.setListCount(0L);
        // 1000 read requests
        usage.setGetCount(800L);
        usage.setSelectCount(200L);

        when(usageRepo.findByUserId(1L)).thenReturn(Optional.of(usage));

        BillingSummary summary = billingService.calculateCurrent(user);

        // 1 GB * 0.025 = 0.025
        // 2000 writes * 0.0005 per 1000 = 0.001
        // 1000 reads * 0.0004 per 1000 = 0.0004
        // Total = 0.025 + 0.001 + 0.0004 = 0.0264

        assertEquals(1024L * 1024L * 1024L, summary.getStorageBytes());
        assertEquals(new BigDecimal("1.000000"), summary.getStorageGb());
        assertEquals(new BigDecimal("0.025000"), summary.getStorageCost());

        assertEquals(2000L, summary.getWriteRequests());
        assertEquals(new BigDecimal("0.001000"), summary.getWriteCost());

        assertEquals(1000L, summary.getReadRequests());
        assertEquals(new BigDecimal("0.000400"), summary.getReadCost());

        assertEquals(new BigDecimal("0.026400"), summary.getTotal());
    }

    @Test
    void calculateCurrent_NullUser() {
        assertThrows(NullPointerException.class, () -> billingService.calculateCurrent(null));
    }

    @Test
    void calculateCurrent_WithNegativeUsage() {
        UserUsage usage = new UserUsage();
        usage.setTotalBytesStored(-1024L); // Negative bytes
        usage.setPutCount(-10L); // Negative requests
        
        when(usageRepo.findByUserId(1L)).thenReturn(Optional.of(usage));

        BillingSummary summary = billingService.calculateCurrent(user);

        // Storage should be clamped to 0
        assertEquals(0L, summary.getStorageBytes());
        assertEquals(BigDecimal.ZERO.setScale(6), summary.getStorageGb());
        assertEquals(BigDecimal.ZERO.setScale(6), summary.getStorageCost());
        
        // Requests are not clamped currently, so it might be negative
        assertEquals(-10L, summary.getWriteRequests());
        assertTrue(summary.getWriteCost().compareTo(BigDecimal.ZERO) < 0);
    }
}
