package com.btech_major_project.Personal_Cloud.common;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AppLoggerTest {

    @Test
    void testLogger() {
        AppLogger appLogger = AppLogger.getLogger(AppLoggerTest.class);
        assertNotNull(appLogger);

        Logger mockLogger = mock(Logger.class);
        ReflectionTestUtils.setField(appLogger, "logger", mockLogger);

        appLogger.info("info msg");
        verify(mockLogger).info("info msg");

        appLogger.warn("warn msg");
        verify(mockLogger).warn("warn msg");

        appLogger.debug("debug msg");
        verify(mockLogger).debug("debug msg");

        RuntimeException ex = new RuntimeException("err");
        appLogger.error("error msg", ex);
        verify(mockLogger).error("error msg", ex);
    }
}
