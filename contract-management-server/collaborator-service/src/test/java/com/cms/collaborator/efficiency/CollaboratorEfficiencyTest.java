package com.cms.collaborator.efficiency;

import com.cms.collaborator.dto.request.CollaboratorRequest;
import com.cms.collaborator.entity.enums.CollaboratorStatus;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Efficiency tests for Epic 1: Collaborator Management
 *
 * Covers:
 *   US-1.1  Register New Collaborator  — POST /api/v1/collaborators
 *   US-1.2  View Collaborator Details  — GET  /api/v1/collaborators/{nationalId}
 *   US-1.3  List All Collaborators     — GET  /api/v1/collaborators
 *
 * Thresholds (ms):
 *   Single create  < 1 000
 *   Single GET     <   500
 *   Paginated list < 1 500
 *   Concurrent wall< 5 000
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CollaboratorEfficiencyTest {

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

    private static final String URL = "/api/v1/collaborators";

    private static final long T_CREATE = 1_000;
    private static final long T_GET    =   500;
    private static final long T_LIST   = 1_500;
    private static final long T_WALL   = 5_000;

    private CollaboratorRequest req(String id, String code, String email) {
        return CollaboratorRequest.builder()
                .nationalId(id)
                .employeeCode(code)
                .firstName("Test")
                .lastName("Efficiency")
                .email(email)
                .phone("3001234567")
                .department("Engineering")
                .position("Developer")
                .status(CollaboratorStatus.ACTIVE)
                .hireDate(LocalDate.of(2022, 1, 15))
                .build();
    }

    // ─── US-1.1: Register New Collaborator ────────────────────────────────────

    @Test @Order(1)
    @DisplayName("US-1.1 | Single create latency < " + T_CREATE + " ms")
    void us11_singleCreate() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.postForEntity(URL, req("EFF-001", "EMP-001", "eff001@test.com"), String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-1.1] create: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("create latency").isLessThan(T_CREATE);
    }

    @Test @Order(2)
    @DisplayName("US-1.1 | Bulk create 50 records — avg latency < " + T_CREATE + " ms")
    void us11_bulkCreate() {
        List<Long> times = new ArrayList<>();
        for (int i = 2; i <= 51; i++) {
            long t0 = System.currentTimeMillis();
            ResponseEntity<String> res = http.postForEntity(URL,
                    req("EFF-" + String.format("%03d", i), "EMP-" + i, "eff" + i + "@test.com"), String.class);
            if (res.getStatusCode().is2xxSuccessful()) times.add(System.currentTimeMillis() - t0);
        }

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long  max = times.stream().mapToLong(Long::longValue).max().orElse(0);
        System.out.printf("  [US-1.1] bulk 50: avg=%.0f ms, max=%d ms%n", avg, max);
        assertThat(times).as("successful inserts").hasSizeGreaterThanOrEqualTo(48);
        assertThat(avg).as("avg create latency").isLessThan(T_CREATE);
    }

    // ─── US-1.2: View Collaborator Details ────────────────────────────────────

    @Test @Order(3)
    @DisplayName("US-1.2 | Get by ID latency < " + T_GET + " ms")
    void us12_getById() {
        http.getForEntity(URL + "/EFF-001", String.class);           // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/EFF-001", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-1.2] get by ID: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("GET latency").isLessThan(T_GET);
    }

    @Test @Order(4)
    @DisplayName("US-1.2 | Get details endpoint latency < " + T_GET + " ms")
    void us12_getDetails() {
        http.getForEntity(URL + "/EFF-001/details", String.class);  // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/EFF-001/details", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-1.2] get details: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("details latency").isLessThan(T_GET);
    }

    @Test @Order(5)
    @DisplayName("US-1.2 | 404 for unknown ID returned in < 300 ms")
    void us12_notFound() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/NONEXISTENT-999", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-1.2] 404 response: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ms).as("404 latency").isLessThan(300);
    }

    // ─── US-1.3: List All Collaborators ──────────────────────────────────────

    @Test @Order(6)
    @DisplayName("US-1.3 | Paginated list (page=0, size=20) latency < " + T_LIST + " ms")
    void us13_list() {
        http.getForEntity(URL + "?page=0&size=20", String.class);   // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?page=0&size=20", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-1.3] list (size 20): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("list latency").isLessThan(T_LIST);
    }

    @Test @Order(7)
    @DisplayName("US-1.3 | List with status=ACTIVE filter < " + T_LIST + " ms")
    void us13_listFiltered() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?page=0&size=10&status=ACTIVE", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-1.3] list (ACTIVE filter): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("filtered list latency").isLessThan(T_LIST);
    }

    @Test @Order(8)
    @DisplayName("US-1.3 | List with department filter < " + T_LIST + " ms")
    void us13_listByDepartment() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?page=0&size=10&department=Engineering", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-1.3] list (dept filter): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("dept filter latency").isLessThan(T_LIST);
    }

    // ─── Concurrency ──────────────────────────────────────────────────────────

    @Test @Order(9)
    @DisplayName("US-1.2 | 10 concurrent GETs — all succeed, wall-clock < " + T_WALL + " ms")
    void us12_concurrentGets() throws InterruptedException {
        int n = 10;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        AtomicInteger ok = new AtomicInteger();
        List<Long> times = new CopyOnWriteArrayList<>();

        long wall0 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                long t0 = System.currentTimeMillis();
                ResponseEntity<String> r = http.getForEntity(URL + "/EFF-001", String.class);
                times.add(System.currentTimeMillis() - t0);
                if (r.getStatusCode().is2xxSuccessful()) ok.incrementAndGet();
            });
        }
        pool.shutdown();
        pool.awaitTermination(T_WALL, TimeUnit.MILLISECONDS);
        long wall = System.currentTimeMillis() - wall0;

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("  [US-1.2] concurrent GET x%d: wall=%d ms, avg=%.0f ms, %d/%d ok%n",
                n, wall, avg, ok.get(), n);
        assertThat(ok.get()).as("all concurrent GETs succeed").isEqualTo(n);
        assertThat(wall).as("concurrent wall-clock").isLessThan(T_WALL);
    }

    @Test @Order(10)
    @DisplayName("US-1.1 | 5 concurrent POSTs — all succeed without conflicts")
    void us11_concurrentPosts() throws InterruptedException {
        int n = 5;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        AtomicInteger ok = new AtomicInteger();
        List<Long> times = new CopyOnWriteArrayList<>();

        long wall0 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int idx = 200 + i;
            pool.submit(() -> {
                long t0 = System.currentTimeMillis();
                ResponseEntity<String> r = http.postForEntity(URL,
                        req("CONC-" + idx, "EMP-CONC-" + idx, "conc" + idx + "@test.com"), String.class);
                times.add(System.currentTimeMillis() - t0);
                if (r.getStatusCode().is2xxSuccessful()) ok.incrementAndGet();
            });
        }
        pool.shutdown();
        pool.awaitTermination(T_WALL, TimeUnit.MILLISECONDS);
        long wall = System.currentTimeMillis() - wall0;

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("  [US-1.1] concurrent POST x%d: wall=%d ms, avg=%.0f ms, %d/%d ok%n",
                n, wall, avg, ok.get(), n);
        assertThat(ok.get()).as("all concurrent POSTs succeed").isEqualTo(n);
    }
}
