package com.btech_major_project.Personal_Cloud;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class BillingService {

    private static final AppLogger log = AppLogger.getLogger(BillingService.class);

    // Rates (USD)
    private static final BigDecimal STORAGE_PER_GB_MONTH = new BigDecimal("0.025");
    private static final BigDecimal WRITE_PER_1000 = new BigDecimal("0.0005"); // PUT, COPY, POST, LIST
    private static final BigDecimal READ_PER_1000 = new BigDecimal("0.0004");  // GET, SELECT

    private final UserUsageRepository usageRepo;

    public BillingService(UserUsageRepository usageRepo) {
        this.usageRepo = usageRepo;
    }

    public BillingSummary calculateCurrent(User user) {
        UserUsage u = usageRepo.findByUserId(user.getId()).orElse(null);
        if (u == null) {
            return BillingSummary.empty();
        }

        long storageBytes = Math.max(0, u.getTotalBytesStored());
        BigDecimal storageGb = bytesToGb(storageBytes);
        BigDecimal storageCost = storageGb.multiply(STORAGE_PER_GB_MONTH);

        long writeRequests = safeAdd(safeAdd(u.getPutCount(), u.getCopyCount()), safeAdd(u.getPostCount(), u.getListCount()));
        long readRequests = safeAdd(u.getGetCount(), u.getSelectCount());

        BigDecimal writeCost = perThousand(writeRequests).multiply(WRITE_PER_1000);
        BigDecimal readCost = perThousand(readRequests).multiply(READ_PER_1000);

        BigDecimal total = storageCost.add(writeCost).add(readCost);

        BillingSummary summary = new BillingSummary();
        summary.setStorageBytes(storageBytes);
        summary.setStorageGb(scale(storageGb));
        summary.setStorageCost(scale(storageCost));
        summary.setWriteRequests(writeRequests);
        summary.setWriteCost(scale(writeCost));
        summary.setReadRequests(readRequests);
        summary.setReadCost(scale(readCost));
        summary.setTotal(scale(total));
        summary.setCurrency("USD");

        log.info("Calculated bill for userId=" + user.getId() + ": total=" + summary.getTotal() + " " + summary.getCurrency());
        return summary;
    }

    private static BigDecimal bytesToGb(long bytes) {
        // 1 GB = 1024^3 bytes
        return new BigDecimal(bytes).divide(new BigDecimal(1024L * 1024L * 1024L), 12, RoundingMode.HALF_UP);
    }

    private static BigDecimal perThousand(long count) {
        return new BigDecimal(count).divide(new BigDecimal(1000), 12, RoundingMode.HALF_UP);
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(6, RoundingMode.HALF_UP);
    }

    private static long safeAdd(long a, long b) {
        long r = a + b;
        if (((a ^ r) & (b ^ r)) < 0) {
            return Long.MAX_VALUE; // overflow guard
        }
        return r;
    }

    // Summary DTO for internal use and controller response mapping
    public static class BillingSummary {
        private long storageBytes;
        private BigDecimal storageGb;
        private BigDecimal storageCost;
        private long writeRequests;
        private BigDecimal writeCost;
        private long readRequests;
        private BigDecimal readCost;
        private BigDecimal total;
        private String currency;

        public static BillingSummary empty() {
            BillingSummary s = new BillingSummary();
            s.storageBytes = 0L;
            s.storageGb = BigDecimal.ZERO.setScale(6);
            s.storageCost = BigDecimal.ZERO.setScale(6);
            s.writeRequests = 0L;
            s.writeCost = BigDecimal.ZERO.setScale(6);
            s.readRequests = 0L;
            s.readCost = BigDecimal.ZERO.setScale(6);
            s.total = BigDecimal.ZERO.setScale(6);
            s.currency = "USD";
            return s;
        }

        public long getStorageBytes() { return storageBytes; }
        public void setStorageBytes(long storageBytes) { this.storageBytes = storageBytes; }
        public BigDecimal getStorageGb() { return storageGb; }
        public void setStorageGb(BigDecimal storageGb) { this.storageGb = storageGb; }
        public BigDecimal getStorageCost() { return storageCost; }
        public void setStorageCost(BigDecimal storageCost) { this.storageCost = storageCost; }
        public long getWriteRequests() { return writeRequests; }
        public void setWriteRequests(long writeRequests) { this.writeRequests = writeRequests; }
        public BigDecimal getWriteCost() { return writeCost; }
        public void setWriteCost(BigDecimal writeCost) { this.writeCost = writeCost; }
        public long getReadRequests() { return readRequests; }
        public void setReadRequests(long readRequests) { this.readRequests = readRequests; }
        public BigDecimal getReadCost() { return readCost; }
        public void setReadCost(BigDecimal readCost) { this.readCost = readCost; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}

