package com.cms.contract.efficiency;

import com.cms.common.dto.ApiResponse;
import com.cms.contract.client.CollaboratorClient;
import com.cms.contract.client.NotificationClient;
import com.cms.contract.client.dto.AverageRatingResponse;
import com.cms.contract.client.dto.CollaboratorResponse;
import com.cms.contract.dto.request.ContractRequest;
import com.cms.contract.dto.request.RenewalRequest;
import com.cms.contract.dto.request.TerminationRequest;
import com.cms.contract.entity.enums.ContractType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Efficiency tests for Epic 2: Contract Management
 *
 * Covers:
 *   US-2.1  Register New Contract       — POST /api/v1/contracts
 *   US-2.2  View Contract Details       — GET  /api/v1/contracts/{id}
 *   US-2.3  List All Contracts          — GET  /api/v1/contracts
 *   US-2.4  View Expiring Contracts     — GET  /api/v1/contracts/expiring-soon
 *   US-2.5  Renew Contract              — PUT  /api/v1/contracts/{id}/renew
 *
 * Feign clients (CollaboratorClient, NotificationClient) are mocked so tests
 * run in isolation without depending on other services.
 *
 * Thresholds (ms):
 *   Create contract   < 1 500
 *   Simple GET        <   500
 *   Paginated list    < 1 500
 *   Renewal (complex) < 2 000
 *   Concurrent wall   < 5 000
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ContractEfficiencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("contract_db")
            .withUsername("cms_user")
            .withPassword("cms_password");

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired TestRestTemplate http;
    @MockBean  CollaboratorClient collaboratorClient;
    @MockBean  NotificationClient notificationClient;

    private static final String URL = "/api/v1/contracts";

    private static final long T_CREATE    = 1_500;
    private static final long T_GET       =   500;
    private static final long T_LIST      = 1_500;
    private static final long T_RENEWAL   = 2_000;
    private static final long T_WALL      = 5_000;

    // ID of a contract created in test 1 — reused by later tests
    private static String firstContractId;

    @BeforeEach
    void setupMocks() {
        // collaborator exists
        when(collaboratorClient.checkCollaboratorExists(anyString()))
                .thenReturn(ApiResponse.success(true));

        // collaborator details
        CollaboratorResponse collab = new CollaboratorResponse();
        collab.setNationalId("CC-TEST-001");
        collab.setFirstName("Test");
        collab.setLastName("Collaborator");
        collab.setFullName("Test Collaborator");
        collab.setEmail("test.collab@test.com");
        when(collaboratorClient.getCollaboratorByNationalId(anyString()))
                .thenReturn(ApiResponse.success(collab));

        // average rating (eligible)
        AverageRatingResponse rating = new AverageRatingResponse();
        rating.setAverageRating(new BigDecimal("4.0"));
        rating.setTotalReviews(5L);
        rating.setIsEligibleForRenewal(true);
        when(collaboratorClient.getAverageRating(anyString()))
                .thenReturn(ApiResponse.success(rating));

        // renewal eligibility
        when(collaboratorClient.isEligibleForRenewal(anyString()))
                .thenReturn(ApiResponse.success(true));

        // notification client — lenient mock (returns null by default, which is fine)
        when(notificationClient.sendNotification(any()))
                .thenReturn(ApiResponse.success(null));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String extractId(ResponseEntity<Map> res) {
        Map<?, ?> body = res.getBody();
        if (body == null) return null;
        Map<?, ?> data = (Map<?, ?>) body.get("data");
        return data == null ? null : String.valueOf(data.get("id"));
    }

    private ContractRequest contractReq(String collaboratorId, int futureMonths) {
        return ContractRequest.builder()
                .collaboratorId(collaboratorId)
                .contractType(ContractType.FULL_TIME)
                .startDate(LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now().plusMonths(futureMonths))
                .salary(new BigDecimal("5000.00"))
                .currency("USD")
                .autoRenewal(false)
                .noticePeriodDays(30)
                .build();
    }

    // ─── US-2.1: Register New Contract ───────────────────────────────────────

    @Test @Order(1)
    @DisplayName("US-2.1 | Single create latency < " + T_CREATE + " ms")
    void us21_singleCreate() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<Map> res = http.postForEntity(URL, contractReq("CC-TEST-001", 12), Map.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.1] create contract: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("create latency").isLessThan(T_CREATE);

        firstContractId = extractId(res);
        assertThat(firstContractId).as("contract ID in response").isNotNull();
    }

    @Test @Order(2)
    @DisplayName("US-2.1 | Bulk create 20 contracts (distinct collaborators) — avg < " + T_CREATE + " ms")
    void us21_bulkCreate() {
        List<Long> times = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            long t0 = System.currentTimeMillis();
            ResponseEntity<Map> res = http.postForEntity(URL, contractReq("CC-BULK-" + String.format("%03d", i), 12 + i), Map.class);
            if (res.getStatusCode().is2xxSuccessful()) times.add(System.currentTimeMillis() - t0);
        }

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long   max = times.stream().mapToLong(Long::longValue).max().orElse(0);
        System.out.printf("  [US-2.1] bulk 20: avg=%.0f ms, max=%d ms%n", avg, max);
        assertThat(times).as("successful creates").hasSizeGreaterThanOrEqualTo(19);
        assertThat(avg).as("avg create latency").isLessThan(T_CREATE);
    }

    // ─── US-2.2: View Contract Details ───────────────────────────────────────

    @Test @Order(3)
    @DisplayName("US-2.2 | Get contract by ID latency < " + T_GET + " ms")
    void us22_getById() {
        assertThat(firstContractId).as("contract ID available").isNotNull();

        http.getForEntity(URL + "/" + firstContractId, String.class); // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/" + firstContractId, String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.2] get by ID: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("GET latency").isLessThan(T_GET);
    }

    @Test @Order(4)
    @DisplayName("US-2.2 | 404 for unknown contract ID returned in < 300 ms")
    void us22_notFound() {
        String fakeId = UUID.randomUUID().toString();
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/" + fakeId, String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.2] 404 response: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ms).as("404 latency").isLessThan(300);
    }

    // ─── US-2.3: List All Contracts ───────────────────────────────────────────

    @Test @Order(5)
    @DisplayName("US-2.3 | Paginated list (size=20) latency < " + T_LIST + " ms")
    void us23_list() {
        http.getForEntity(URL + "?page=0&size=20", String.class); // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?page=0&size=20", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.3] list (size 20): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("list latency").isLessThan(T_LIST);
    }

    @Test @Order(6)
    @DisplayName("US-2.3 | List with status=ACTIVE filter < " + T_LIST + " ms")
    void us23_listFiltered() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?page=0&size=10&status=ACTIVE", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.3] list (ACTIVE filter): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("filtered list latency").isLessThan(T_LIST);
    }

    @Test @Order(7)
    @DisplayName("US-2.3 | List by collaborator < " + T_LIST + " ms")
    void us23_listByCollaborator() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/collaborator/CC-TEST-001", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.3] list by collaborator: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("by-collaborator latency").isLessThan(T_LIST);
    }

    // ─── US-2.4: View Expiring Contracts ─────────────────────────────────────

    @Test @Order(8)
    @DisplayName("US-2.4 | Expiring-soon (30 days) query < " + T_LIST + " ms")
    void us24_expiringSoon30() {
        http.getForEntity(URL + "/expiring-soon?days=30", String.class); // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/expiring-soon?days=30", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.4] expiring soon (30d): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("expiring-soon latency").isLessThan(T_LIST);
    }

    @Test @Order(9)
    @DisplayName("US-2.4 | Expiring-soon (7 days) query < " + T_LIST + " ms")
    void us24_expiringSoon7() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/expiring-soon?days=7", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.4] expiring soon (7d): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("7-day expiring-soon latency").isLessThan(T_LIST);
    }

    @Test @Order(10)
    @DisplayName("US-2.4 | 10 repeated expiring-soon calls — avg < " + T_LIST + " ms")
    void us24_repeatedExpiringSoon() {
        List<Long> times = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long t0 = System.currentTimeMillis();
            http.getForEntity(URL + "/expiring-soon?days=30", String.class);
            times.add(System.currentTimeMillis() - t0);
        }
        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long   max = times.stream().mapToLong(Long::longValue).max().orElse(0);
        System.out.printf("  [US-2.4] 10x expiring-soon: avg=%.0f ms, max=%d ms%n", avg, max);
        assertThat(avg).as("repeated avg latency").isLessThan(T_LIST);
    }

    // ─── US-2.5: Renew Contract ───────────────────────────────────────────────

    @Test @Order(11)
    @DisplayName("US-2.5 | Contract renewal latency < " + T_RENEWAL + " ms")
    void us25_renew() {
        assertThat(firstContractId).as("contract ID available for renewal").isNotNull();

        RenewalRequest renewal = RenewalRequest.builder()
                .newEndDate(LocalDate.now().plusYears(2))
                .newSalary(new BigDecimal("5500.00"))
                .renewalNotes("Performance-based renewal")
                .autoRenewal(false)
                .noticePeriodDays(30)
                .build();

        HttpEntity<RenewalRequest> entity = new HttpEntity<>(renewal);

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.exchange(
                URL + "/" + firstContractId + "/renew", HttpMethod.PUT, entity, String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-2.5] renew contract: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("renewal latency").isLessThan(T_RENEWAL);
    }

    @Test @Order(12)
    @DisplayName("US-2.5 | Sequential renewals (3 generations) — each < " + T_RENEWAL + " ms")
    void us25_chainedRenewals() {
        // Create a fresh contract for the chain
        ResponseEntity<Map> createRes = http.postForEntity(URL, contractReq("CC-CHAIN-001", 6), Map.class);
        assertThat(createRes.getStatusCode().is2xxSuccessful()).isTrue();
        String chainId = extractId(createRes);
        assertThat(chainId).isNotNull();

        List<Long> times = new ArrayList<>();
        for (int gen = 1; gen <= 3; gen++) {
            RenewalRequest req = RenewalRequest.builder()
                    .newEndDate(LocalDate.now().plusYears(gen + 1))
                    .newSalary(new BigDecimal("5000.00").add(BigDecimal.valueOf(gen * 100)))
                    .renewalNotes("Gen " + gen + " renewal")
                    .autoRenewal(false)
                    .noticePeriodDays(30)
                    .build();

            HttpEntity<RenewalRequest> entity = new HttpEntity<>(req);
            long t0 = System.currentTimeMillis();
            ResponseEntity<Map> res = http.exchange(
                    URL + "/" + chainId + "/renew", HttpMethod.PUT, entity, Map.class);
            long ms = System.currentTimeMillis() - t0;
            times.add(ms);

            assertThat(res.getStatusCode().is2xxSuccessful())
                    .as("generation " + gen + " renewal succeeds").isTrue();

            // The new contract becomes the one to renew next
            chainId = extractId(res);
            assertThat(chainId).isNotNull();

            System.out.printf("  [US-2.5] renewal gen %d: %d ms (new ID: %s)%n", gen, ms, chainId);
        }

        times.forEach(ms -> assertThat(ms).as("each renewal latency").isLessThan(T_RENEWAL));
    }

    // ─── Concurrency ──────────────────────────────────────────────────────────

    @Test @Order(13)
    @DisplayName("US-2.3 | 10 concurrent list GETs — all succeed in < " + T_WALL + " ms")
    void us23_concurrentList() throws InterruptedException {
        int n = 10;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        AtomicInteger ok = new AtomicInteger();
        List<Long> times = new CopyOnWriteArrayList<>();

        long wall0 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                long t0 = System.currentTimeMillis();
                ResponseEntity<String> r = http.getForEntity(URL + "?page=0&size=10", String.class);
                times.add(System.currentTimeMillis() - t0);
                if (r.getStatusCode().is2xxSuccessful()) ok.incrementAndGet();
            });
        }
        pool.shutdown();
        pool.awaitTermination(T_WALL, TimeUnit.MILLISECONDS);
        long wall = System.currentTimeMillis() - wall0;

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("  [US-2.3] concurrent LIST x%d: wall=%d ms, avg=%.0f ms, %d/%d ok%n",
                n, wall, avg, ok.get(), n);
        assertThat(ok.get()).as("all concurrent GETs succeed").isEqualTo(n);
        assertThat(wall).as("concurrent wall-clock").isLessThan(T_WALL);
    }

    @Test @Order(14)
    @DisplayName("US-2.4 | 5 concurrent expiring-soon queries — all succeed")
    void us24_concurrentExpiryQueries() throws InterruptedException {
        int n = 5;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        AtomicInteger ok = new AtomicInteger();
        List<Long> times = new CopyOnWriteArrayList<>();

        long wall0 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                long t0 = System.currentTimeMillis();
                ResponseEntity<String> r = http.getForEntity(URL + "/expiring-soon?days=30", String.class);
                times.add(System.currentTimeMillis() - t0);
                if (r.getStatusCode().is2xxSuccessful()) ok.incrementAndGet();
            });
        }
        pool.shutdown();
        pool.awaitTermination(T_WALL, TimeUnit.MILLISECONDS);
        long wall = System.currentTimeMillis() - wall0;

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("  [US-2.4] concurrent expiry x%d: wall=%d ms, avg=%.0f ms, %d/%d ok%n",
                n, wall, avg, ok.get(), n);
        assertThat(ok.get()).as("all concurrent expiry queries succeed").isEqualTo(n);
        assertThat(wall).as("concurrent wall-clock").isLessThan(T_WALL);
    }
}
