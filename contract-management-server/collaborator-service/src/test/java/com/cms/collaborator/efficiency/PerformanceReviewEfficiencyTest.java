package com.cms.collaborator.efficiency;

import com.cms.collaborator.dto.request.CollaboratorRequest;
import com.cms.collaborator.dto.request.PerformanceReviewRequest;
import com.cms.collaborator.entity.enums.CollaboratorStatus;
import com.cms.collaborator.entity.enums.PerformanceCategory;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Efficiency tests for Epic 3: Performance Evaluation
 *
 * Covers:
 *   US-3.1  Submit Performance Review         — POST /api/v1/performance-reviews
 *   US-3.2  View Performance History          — GET  /api/v1/performance-reviews/collaborator/{id}
 *   US-3.3  Performance-Based Renewal Check   — GET  /api/v1/performance-reviews/collaborator/{id}/renewal-eligibility
 *
 * Thresholds (ms):
 *   Single review create   < 1 000
 *   Single GET aggregation <   500
 *   Paginated list         < 1 500
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceReviewEfficiencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("collaborator_db")
            .withUsername("cms_user")
            .withPassword("cms_password");

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestRestTemplate http;

    private static final String COLLAB_URL  = "/api/v1/collaborators";
    private static final String REVIEW_URL  = "/api/v1/performance-reviews";
    private static final String HIGH_COLLAB = "PR-HIGH-001";
    private static final String LOW_COLLAB  = "PR-LOW-001";

    private static final long T_CREATE = 1_000;
    private static final long T_GET    =   500;
    private static final long T_LIST   = 1_500;

    // ─── Seed data ────────────────────────────────────────────────────────────

    @BeforeAll
    static void seed(@Autowired TestRestTemplate http) {
        // High-performer collaborator (avg >= 3.0 → eligible)
        http.postForEntity(COLLAB_URL, collab(HIGH_COLLAB, "EMP-PR-H01", "pr.high@test.com"), String.class);
        // Low-performer collaborator (avg < 3.0 → not eligible)
        http.postForEntity(COLLAB_URL, collab(LOW_COLLAB,  "EMP-PR-L01", "pr.low@test.com"),  String.class);
    }

    private static CollaboratorRequest collab(String id, String code, String email) {
        return CollaboratorRequest.builder()
                .nationalId(id).employeeCode(code)
                .firstName("Perf").lastName("Tester").email(email)
                .department("QA").position("Analyst")
                .status(CollaboratorStatus.ACTIVE)
                .hireDate(LocalDate.of(2021, 6, 1))
                .build();
    }

    private PerformanceReviewRequest review(String collaboratorId, double rating, PerformanceCategory cat) {
        return PerformanceReviewRequest.builder()
                .collaboratorId(collaboratorId)
                .reviewerName("Manager A")
                .reviewerEmail("manager@test.com")
                .reviewPeriodStart(LocalDate.of(2024, 1, 1))
                .reviewPeriodEnd(LocalDate.of(2024, 6, 30))
                .rating(BigDecimal.valueOf(rating))
                .performanceCategory(cat)
                .strengths("Technical skills")
                .areasForImprovement("Soft skills")
                .comments("Good overall")
                .build();
    }

    // ─── US-3.1: Submit Performance Review ───────────────────────────────────

    @Test @Order(1)
    @DisplayName("US-3.1 | Single review submit latency < " + T_CREATE + " ms")
    void us31_singleSubmit() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.postForEntity(REVIEW_URL,
                review(HIGH_COLLAB, 4.0, PerformanceCategory.MEETS_EXPECTATIONS), String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-3.1] submit review: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("submit latency").isLessThan(T_CREATE);
    }

    @Test @Order(2)
    @DisplayName("US-3.1 | Submit 20 reviews (mixed ratings) — avg latency < " + T_CREATE + " ms")
    void us31_bulkSubmit() {
        double[] ratings = {4.5, 3.8, 5.0, 4.0, 3.5, 4.8, 4.2, 3.7, 4.9, 4.6,
                            4.1, 3.9, 4.3, 4.7, 3.6, 4.4, 5.0, 3.8, 4.0, 4.5};
        PerformanceCategory[] cats = {
            PerformanceCategory.EXCEEDS_EXPECTATIONS, PerformanceCategory.MEETS_EXPECTATIONS,
            PerformanceCategory.EXCEEDS_EXPECTATIONS, PerformanceCategory.MEETS_EXPECTATIONS,
            PerformanceCategory.MEETS_EXPECTATIONS,   PerformanceCategory.EXCEEDS_EXPECTATIONS,
            PerformanceCategory.EXCEEDS_EXPECTATIONS, PerformanceCategory.MEETS_EXPECTATIONS,
            PerformanceCategory.EXCEEDS_EXPECTATIONS, PerformanceCategory.EXCEEDS_EXPECTATIONS,
            PerformanceCategory.EXCEEDS_EXPECTATIONS, PerformanceCategory.MEETS_EXPECTATIONS,
            PerformanceCategory.EXCEEDS_EXPECTATIONS, PerformanceCategory.EXCEEDS_EXPECTATIONS,
            PerformanceCategory.MEETS_EXPECTATIONS,   PerformanceCategory.EXCEEDS_EXPECTATIONS,
            PerformanceCategory.EXCEEDS_EXPECTATIONS, PerformanceCategory.MEETS_EXPECTATIONS,
            PerformanceCategory.MEETS_EXPECTATIONS,   PerformanceCategory.EXCEEDS_EXPECTATIONS
        };

        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            long t0 = System.currentTimeMillis();
            ResponseEntity<String> res = http.postForEntity(REVIEW_URL,
                    review(HIGH_COLLAB, ratings[i], cats[i]), String.class);
            if (res.getStatusCode().is2xxSuccessful()) times.add(System.currentTimeMillis() - t0);
        }

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long max   = times.stream().mapToLong(Long::longValue).max().orElse(0);
        System.out.printf("  [US-3.1] bulk 20 reviews: avg=%.0f ms, max=%d ms%n", avg, max);
        assertThat(times.size()).as("successful submits").isGreaterThanOrEqualTo(19);
        assertThat(avg).as("avg submit latency").isLessThan(T_CREATE);
    }

    @Test @Order(3)
    @DisplayName("US-3.1 | Low-rating reviews (avg < 3.0) — submit latency < " + T_CREATE + " ms")
    void us31_lowRatingReviews() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            long t0 = System.currentTimeMillis();
            ResponseEntity<String> res = http.postForEntity(REVIEW_URL,
                    review(LOW_COLLAB, 1.5 + i * 0.2, PerformanceCategory.NEEDS_IMPROVEMENT), String.class);
            if (res.getStatusCode().is2xxSuccessful()) times.add(System.currentTimeMillis() - t0);
        }

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("  [US-3.1] low-rating reviews: avg=%.0f ms%n", avg);
        assertThat(avg).as("low-rating submit latency").isLessThan(T_CREATE);
    }

    // ─── US-3.2: View Performance History ────────────────────────────────────

    @Test @Order(4)
    @DisplayName("US-3.2 | Paginated review history < " + T_LIST + " ms")
    void us32_listReviews() {
        http.getForEntity(REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "?page=0&size=10", String.class);

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(
                REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "?page=0&size=10", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-3.2] list review history: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("list latency").isLessThan(T_LIST);
    }

    @Test @Order(5)
    @DisplayName("US-3.2 | Latest review retrieval < " + T_GET + " ms")
    void us32_latestReview() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(
                REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "/latest", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-3.2] latest review: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("latest review latency").isLessThan(T_GET);
    }

    @Test @Order(6)
    @DisplayName("US-3.2 | Average rating aggregation < " + T_GET + " ms")
    void us32_averageRating() {
        http.getForEntity(REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "/average-rating", String.class);

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(
                REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "/average-rating", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-3.2] average rating: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("avg rating latency").isLessThan(T_GET);
    }

    // ─── US-3.3: Performance-Based Renewal Eligibility ───────────────────────

    @Test @Order(7)
    @DisplayName("US-3.3 | Eligible collaborator (avg >= 3.0) check < " + T_GET + " ms")
    void us33_eligibleCheck() {
        http.getForEntity(REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "/renewal-eligibility", String.class);

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(
                REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "/renewal-eligibility", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-3.3] eligible check: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("true");
        assertThat(ms).as("eligibility check latency").isLessThan(T_GET);
    }

    @Test @Order(8)
    @DisplayName("US-3.3 | Ineligible collaborator (avg < 3.0) check — correct result in < " + T_GET + " ms")
    void us33_ineligibleCheck() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(
                REVIEW_URL + "/collaborator/" + LOW_COLLAB + "/renewal-eligibility", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-3.3] ineligible check: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).contains("false");
        assertThat(ms).as("ineligibility check latency").isLessThan(T_GET);
    }

    @Test @Order(9)
    @DisplayName("US-3.3 | Repeated eligibility checks (10x) — stable response times")
    void us33_repeatedChecks() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long t0 = System.currentTimeMillis();
            http.getForEntity(REVIEW_URL + "/collaborator/" + HIGH_COLLAB + "/renewal-eligibility", String.class);
            times.add(System.currentTimeMillis() - t0);
        }

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long max   = times.stream().mapToLong(Long::longValue).max().orElse(0);
        System.out.printf("  [US-3.3] 10x eligibility checks: avg=%.0f ms, max=%d ms%n", avg, max);
        assertThat(avg).as("repeated check avg latency").isLessThan(T_GET);
        assertThat(max).as("repeated check max latency").isLessThan(T_GET * 2);
    }
}
