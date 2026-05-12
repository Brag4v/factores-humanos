package com.cms.notification.efficiency;

import com.cms.notification.dto.request.NotificationRequest;
import com.cms.notification.dto.request.SendNotificationRequest;
import com.cms.notification.entity.enums.NotificationType;
import com.cms.notification.service.EmailService;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Efficiency tests for Epic 4: Expiry Notifications
 *
 * Covers:
 *   US-4.2  Send Expiry Notification    — POST /api/v1/notifications/send
 *                                         POST /api/v1/notifications  (schedule)
 *   US-4.3  View Pending Notifications  — GET  /api/v1/notifications/pending
 *                                         GET  /api/v1/notifications?status=PENDING
 *
 * EmailService is mocked so no real SMTP connection is made.
 *
 * Thresholds (ms):
 *   Single POST (create / send) < 1 000
 *   Single GET                  <   500
 *   Paginated list              < 1 500
 *   Concurrent wall             < 5 000
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationEfficiencyTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("notification_db")
            .withUsername("cms_user")
            .withPassword("cms_password");

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired TestRestTemplate http;

    // Mock EmailService so no real SMTP calls are made
    @MockBean EmailService emailService;

    private static final String URL = "/api/v1/notifications";

    private static final long T_CREATE = 1_000;
    private static final long T_GET    =   500;
    private static final long T_LIST   = 1_500;
    private static final long T_WALL   = 5_000;

    // Shared contract ID — real UUID used across tests
    private static final UUID CONTRACT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // ID of first scheduled notification, reused in later tests
    private static String pendingNotificationId;

    @BeforeEach
    void setupMocks() {
        // Email sending always succeeds
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private NotificationRequest scheduleReq(String collaboratorId, NotificationType type, int days) {
        return NotificationRequest.builder()
                .contractId(CONTRACT_ID)
                .collaboratorId(collaboratorId)
                .notificationType(type)
                .recipientEmail(collaboratorId.toLowerCase() + "@test.com")
                .recipientName("Test User " + collaboratorId)
                .subject("Contract Expiry Notice — " + type)
                .messageBody("Your contract expires in " + days + " days.")
                .daysUntilExpiry(days)
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    private SendNotificationRequest sendReq(String collaboratorId, NotificationType type, int days) {
        return SendNotificationRequest.builder()
                .contractId(CONTRACT_ID)
                .collaboratorId(collaboratorId)
                .notificationType(type)
                .recipientEmail(collaboratorId.toLowerCase() + "@test.com")
                .recipientName("Test User " + collaboratorId)
                .subject("Contract Expiry — " + type)
                .messageBody("Your contract expires in " + days + " days.")
                .daysUntilExpiry(days)
                .build();
    }

    // ─── US-4.2: Send Expiry Notification ────────────────────────────────────

    @Test @Order(1)
    @DisplayName("US-4.2 | Schedule notification (POST) latency < " + T_CREATE + " ms")
    void us42_scheduleNotification() {
        NotificationRequest req = scheduleReq("CC-NOTIF-001", NotificationType.RENEWAL_REMINDER, 28);

        long t0 = System.currentTimeMillis();
        ResponseEntity<Map> res = http.postForEntity(URL, req, Map.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.2] schedule notification: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("schedule latency").isLessThan(T_CREATE);

        // Store ID for later tests
        Map<?, ?> data = (Map<?, ?>) res.getBody().get("data");
        pendingNotificationId = String.valueOf(data.get("id"));
    }

    @Test @Order(2)
    @DisplayName("US-4.2 | Send notification immediately (POST /send) latency < " + T_CREATE + " ms")
    void us42_sendImmediately() {
        SendNotificationRequest req = sendReq("CC-NOTIF-002", NotificationType.EXPIRY_WARNING, 6);

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.postForEntity(URL + "/send", req, String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.2] send immediately: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("send latency").isLessThan(T_CREATE);
    }

    @Test @Order(3)
    @DisplayName("US-4.2 | Send EXPIRED_NOTICE type < " + T_CREATE + " ms")
    void us42_sendExpiredNotice() {
        SendNotificationRequest req = sendReq("CC-NOTIF-003", NotificationType.EXPIRED_NOTICE, 0);

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.postForEntity(URL + "/send", req, String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.2] send expired notice: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("expired notice latency").isLessThan(T_CREATE);
    }

    @Test @Order(4)
    @DisplayName("US-4.2 | Bulk create 20 scheduled notifications — avg < " + T_CREATE + " ms")
    void us42_bulkSchedule() {
        List<Long> times = new ArrayList<>();
        NotificationType[] types = {
            NotificationType.RENEWAL_REMINDER, NotificationType.EXPIRY_WARNING, NotificationType.EXPIRED_NOTICE
        };

        for (int i = 1; i <= 20; i++) {
            NotificationType type = types[i % 3];
            NotificationRequest req = scheduleReq("CC-BULK-" + String.format("%02d", i), type, 30 - i);

            long t0 = System.currentTimeMillis();
            ResponseEntity<String> res = http.postForEntity(URL, req, String.class);
            if (res.getStatusCode().is2xxSuccessful()) times.add(System.currentTimeMillis() - t0);
        }

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        long   max = times.stream().mapToLong(Long::longValue).max().orElse(0);
        System.out.printf("  [US-4.2] bulk schedule 20: avg=%.0f ms, max=%d ms%n", avg, max);
        assertThat(times).as("successful schedules").hasSizeGreaterThanOrEqualTo(19);
        assertThat(avg).as("avg schedule latency").isLessThan(T_CREATE);
    }

    @Test @Order(5)
    @DisplayName("US-4.2 | Trigger send on existing notification (POST /{id}/send) < " + T_CREATE + " ms")
    void us42_sendExistingNotification() {
        assertThat(pendingNotificationId).as("pending notification ID available").isNotNull();

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.postForEntity(URL + "/" + pendingNotificationId + "/send", null, String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.2] send existing: %d ms%n", ms);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(ms).as("trigger-send latency").isLessThan(T_CREATE);
    }

    // ─── US-4.3: View Pending Notifications ──────────────────────────────────

    @Test @Order(6)
    @DisplayName("US-4.3 | Get pending notifications list < " + T_GET + " ms")
    void us43_getPending() {
        http.getForEntity(URL + "/pending", String.class); // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/pending", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.3] pending list: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("pending list latency").isLessThan(T_GET);
    }

    @Test @Order(7)
    @DisplayName("US-4.3 | Paginated list all notifications < " + T_LIST + " ms")
    void us43_listAll() {
        http.getForEntity(URL + "?page=0&size=20", String.class); // warm-up

        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?page=0&size=20", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.3] list all (size 20): %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("list-all latency").isLessThan(T_LIST);
    }

    @Test @Order(8)
    @DisplayName("US-4.3 | Filter by status=PENDING < " + T_LIST + " ms")
    void us43_filterByStatus() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?status=PENDING&page=0&size=10", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.3] filter PENDING: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("status filter latency").isLessThan(T_LIST);
    }

    @Test @Order(9)
    @DisplayName("US-4.3 | Filter by status=SENT < " + T_LIST + " ms")
    void us43_filterBySent() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "?status=SENT&page=0&size=10", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.3] filter SENT: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("SENT filter latency").isLessThan(T_LIST);
    }

    @Test @Order(10)
    @DisplayName("US-4.3 | Filter by contract ID < " + T_LIST + " ms")
    void us43_filterByContract() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(
                URL + "?contractId=" + CONTRACT_ID + "&page=0&size=10", String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.3] filter by contract: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("contract filter latency").isLessThan(T_LIST);
    }

    @Test @Order(11)
    @DisplayName("US-4.3 | Get notifications by contract < " + T_GET + " ms")
    void us43_byContract() {
        long t0 = System.currentTimeMillis();
        ResponseEntity<String> res = http.getForEntity(URL + "/contract/" + CONTRACT_ID, String.class);
        long ms = System.currentTimeMillis() - t0;

        System.out.printf("  [US-4.3] by contract: %d ms%n", ms);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ms).as("by-contract latency").isLessThan(T_GET);
    }

    // ─── Concurrency ──────────────────────────────────────────────────────────

    @Test @Order(12)
    @DisplayName("US-4.3 | 10 concurrent GET /pending — all succeed in < " + T_WALL + " ms")
    void us43_concurrentPending() throws InterruptedException {
        int n = 10;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        AtomicInteger ok = new AtomicInteger();
        List<Long> times = new CopyOnWriteArrayList<>();

        long wall0 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                long t0 = System.currentTimeMillis();
                ResponseEntity<String> r = http.getForEntity(URL + "/pending", String.class);
                times.add(System.currentTimeMillis() - t0);
                if (r.getStatusCode().is2xxSuccessful()) ok.incrementAndGet();
            });
        }
        pool.shutdown();
        pool.awaitTermination(T_WALL, TimeUnit.MILLISECONDS);
        long wall = System.currentTimeMillis() - wall0;

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("  [US-4.3] concurrent GET /pending x%d: wall=%d ms, avg=%.0f ms, %d/%d ok%n",
                n, wall, avg, ok.get(), n);
        assertThat(ok.get()).as("all concurrent GETs succeed").isEqualTo(n);
        assertThat(wall).as("concurrent wall-clock").isLessThan(T_WALL);
    }

    @Test @Order(13)
    @DisplayName("US-4.2 | 5 concurrent POST /send — all succeed")
    void us42_concurrentSend() throws InterruptedException {
        int n = 5;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        AtomicInteger ok = new AtomicInteger();
        List<Long> times = new CopyOnWriteArrayList<>();

        long wall0 = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            int idx = 300 + i;
            pool.submit(() -> {
                SendNotificationRequest req = sendReq("CC-CONC-" + idx, NotificationType.EXPIRY_WARNING, 14);
                long t0 = System.currentTimeMillis();
                ResponseEntity<String> r = http.postForEntity(URL + "/send", req, String.class);
                times.add(System.currentTimeMillis() - t0);
                if (r.getStatusCode().is2xxSuccessful()) ok.incrementAndGet();
            });
        }
        pool.shutdown();
        pool.awaitTermination(T_WALL, TimeUnit.MILLISECONDS);
        long wall = System.currentTimeMillis() - wall0;

        double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
        System.out.printf("  [US-4.2] concurrent POST /send x%d: wall=%d ms, avg=%.0f ms, %d/%d ok%n",
                n, wall, avg, ok.get(), n);
        assertThat(ok.get()).as("all concurrent POSTs succeed").isEqualTo(n);
        assertThat(wall).as("concurrent wall-clock").isLessThan(T_WALL);
    }
}
