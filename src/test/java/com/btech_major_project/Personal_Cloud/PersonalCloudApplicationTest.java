package com.btech_major_project.Personal_Cloud;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class PersonalCloudApplicationTest {

    @Test
    void testMain() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(eq(PersonalCloudApplication.class), any(String[].class)))
                    .thenReturn(null);

            PersonalCloudApplication.main(new String[]{});

            mocked.verify(() -> SpringApplication.run(eq(PersonalCloudApplication.class), any(String[].class)));
        }
    }

    @Test
    void testConstructor() {
        PersonalCloudApplication app = new PersonalCloudApplication();
        org.junit.jupiter.api.Assertions.assertNotNull(app);
    }
}
