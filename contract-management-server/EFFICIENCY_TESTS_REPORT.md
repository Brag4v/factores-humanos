# Efficiency Test Report
## Contract Management System — Spring Boot Microservices

**Universidad Distrital Francisco José de Caldas**  
**Facultad de Ingeniería — Ingeniería de Sistemas**  
**Autores:** Jason Solarte · Javier Córdoba · Brayan Galindo

---

## 1. Objetivos

Las pruebas de eficiencia validan que cada caso de uso del sistema responde dentro de umbrales de tiempo aceptables bajo condiciones de carga simple y concurrente. Los objetivos específicos son:

- Medir la **latencia de respuesta** (ms) de cada endpoint por caso de uso.
- Verificar el **throughput** en operaciones masivas (inserción de 20–50 registros).
- Evaluar el comportamiento bajo **concurrencia** (5–10 hilos simultáneos).
- Detectar degradación de rendimiento en operaciones complejas (renovación de contratos, cálculo de elegibilidad).

---

## 2. Infraestructura de Pruebas

| Componente | Tecnología | Rol |
|---|---|---|
| Framework de pruebas | JUnit 5 (Jupiter) | Ejecución y aserción |
| Contexto Spring | `@SpringBootTest(RANDOM_PORT)` | Servidor HTTP real en puerto aleatorio |
| Base de datos | TestContainers + PostgreSQL 15-alpine | BD aislada por suite |
| Cliente HTTP | `TestRestTemplate` | Llamadas HTTP reales al servidor de prueba |
| Mocks externos | Mockito `@MockBean` | Aísla Feign clients y EmailService |
| Migración | Flyway | Aplica esquema real antes de cada suite |

### Umbrales de rendimiento definidos

| Tipo de operación | Umbral máximo |
|---|---|
| GET por ID (simple) | **500 ms** |
| POST crear registro | **1 000 ms** |
| Lista paginada | **1 500 ms** |
| Renovación de contrato (complejo) | **2 000 ms** |
| Wall-clock concurrente (10 hilos) | **5 000 ms** |

---

## 3. Estructura de Archivos

```
contract-management-server/
├── collaborator-service/
│   └── src/test/
│       ├── java/com/cms/collaborator/efficiency/
│       │   ├── CollaboratorEfficiencyTest.java       ← US-1.1, 1.2, 1.3
│       │   └── PerformanceReviewEfficiencyTest.java  ← US-3.1, 3.2, 3.3
│       └── resources/
│           └── application-test.yml
│
├── contract-service/
│   └── src/test/
│       ├── java/com/cms/contract/efficiency/
│       │   └── ContractEfficiencyTest.java           ← US-2.1, 2.2, 2.3, 2.4, 2.5
│       └── resources/
│           └── application-test.yml
│
└── notification-service/
    └── src/test/
        ├── java/com/cms/notification/efficiency/
        │   └── NotificationEfficiencyTest.java       ← US-4.2, 4.3
        └── resources/
            └── application-test.yml
```

---

## 4. Casos de Prueba por Historia de Usuario

---

### Epic 1 — Gestión de Colaboradores
**Archivo:** `collaborator-service/.../CollaboratorEfficiencyTest.java`  
**Endpoint base:** `POST/GET /api/v1/collaborators`

#### US-1.1: Registrar Nuevo Colaborador

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 1 | `us11_singleCreate` | Crear un colaborador individual | < 1 000 ms | HTTP 2xx, latencia dentro del umbral |
| 2 | `us11_bulkCreate` | Crear 50 colaboradores en secuencia | avg < 1 000 ms | ≥ 48 exitosos, promedio dentro del umbral |
| 10 | `us11_concurrentPosts` | 5 POSTs simultáneos sin conflictos de ID | wall < 5 000 ms | 5/5 exitosos, sin errores de concurrencia |

#### US-1.2: Ver Detalle de Colaborador

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 3 | `us12_getById` | GET por nationalId (con warm-up) | < 500 ms | HTTP 200, latencia dentro del umbral |
| 4 | `us12_getDetails` | GET endpoint `/details` con resumen de rendimiento | < 500 ms | HTTP 200, latencia dentro del umbral |
| 5 | `us12_notFound` | GET con ID inexistente | < 300 ms | HTTP 404, error rápido |
| 9 | `us12_concurrentGets` | 10 GETs simultáneos al mismo recurso | wall < 5 000 ms | 10/10 exitosos |

#### US-1.3: Listar Todos los Colaboradores

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 6 | `us13_list` | Lista paginada (page=0, size=20) | < 1 500 ms | HTTP 200, lista con datos |
| 7 | `us13_listFiltered` | Lista con filtro `status=ACTIVE` | < 1 500 ms | HTTP 200, resultados filtrados |
| 8 | `us13_listByDepartment` | Lista con filtro `department=Engineering` | < 1 500 ms | HTTP 200, resultados filtrados |

---

### Epic 3 — Evaluación de Desempeño
**Archivo:** `collaborator-service/.../PerformanceReviewEfficiencyTest.java`  
**Endpoint base:** `POST/GET /api/v1/performance-reviews`

#### US-3.1: Registrar Evaluación de Desempeño

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 1 | `us31_singleSubmit` | Enviar una evaluación individual | < 1 000 ms | HTTP 2xx, latencia dentro del umbral |
| 2 | `us31_bulkSubmit` | Enviar 20 evaluaciones con calificaciones mixtas | avg < 1 000 ms | ≥ 19 exitosos |
| 3 | `us31_lowRatingReviews` | Enviar evaluaciones con calificación baja (avg < 3.0) | < 1 000 ms | HTTP 2xx, guarda correctamente |

#### US-3.2: Ver Historial de Desempeño

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 4 | `us32_listReviews` | Historial paginado por colaborador | < 1 500 ms | HTTP 200, lista ordenada |
| 5 | `us32_latestReview` | Obtener la evaluación más reciente | < 500 ms | HTTP 200, un registro |
| 6 | `us32_averageRating` | Calcular calificación promedio (agregación) | < 500 ms | HTTP 200, promedio calculado |

#### US-3.3: Elegibilidad de Renovación por Desempeño

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 7 | `us33_eligibleCheck` | Verificar elegibilidad con avg ≥ 3.0 | < 500 ms | HTTP 200, `data: true` |
| 8 | `us33_ineligibleCheck` | Verificar no-elegibilidad con avg < 3.0 | < 500 ms | HTTP 200, `data: false` |
| 9 | `us33_repeatedChecks` | 10 verificaciones consecutivas (estabilidad) | avg < 500 ms, max < 1 000 ms | Tiempos estables, sin degradación |

---

### Epic 2 — Gestión de Contratos
**Archivo:** `contract-service/.../ContractEfficiencyTest.java`  
**Endpoint base:** `POST/GET/PUT /api/v1/contracts`  
**Mocks:** `CollaboratorClient` (existencia + elegibilidad), `NotificationClient` (notificaciones)

#### US-2.1: Registrar Nuevo Contrato

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 1 | `us21_singleCreate` | Crear un contrato (incluye validación vs. Feign mock) | < 1 500 ms | HTTP 201, ID en respuesta |
| 2 | `us21_bulkCreate` | Crear 20 contratos para distintos colaboradores | avg < 1 500 ms | ≥ 19 exitosos |

#### US-2.2: Ver Detalle de Contrato

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 3 | `us22_getById` | GET por UUID (con warm-up) | < 500 ms | HTTP 200, contrato completo |
| 4 | `us22_notFound` | GET con UUID inexistente | < 300 ms | HTTP 404, error rápido |

#### US-2.3: Listar Todos los Contratos

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 5 | `us23_list` | Lista paginada (size=20) | < 1 500 ms | HTTP 200, página con datos |
| 6 | `us23_listFiltered` | Lista con filtro `status=ACTIVE` | < 1 500 ms | HTTP 200 |
| 7 | `us23_listByCollaborator` | Lista por ID de colaborador | < 1 500 ms | HTTP 200 |
| 13 | `us23_concurrentList` | 10 GETs de lista simultáneos | wall < 5 000 ms | 10/10 exitosos |

#### US-2.4: Ver Contratos por Vencer

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 8 | `us24_expiringSoon30` | Contratos que vencen en 30 días | < 1 500 ms | HTTP 200, lista ordenada |
| 9 | `us24_expiringSoon7` | Contratos que vencen en 7 días | < 1 500 ms | HTTP 200 |
| 10 | `us24_repeatedExpiringSoon` | 10 consultas consecutivas (estabilidad de caché) | avg < 1 500 ms | Sin degradación |
| 14 | `us24_concurrentExpiryQueries` | 5 consultas expiry simultáneas | wall < 5 000 ms | 5/5 exitosos |

#### US-2.5: Renovar Contrato

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 11 | `us25_renew` | Renovar el contrato creado en el test 1 | < 2 000 ms | HTTP 200, nuevo ID, estado RENEWED→ACTIVE |
| 12 | `us25_chainedRenewals` | 3 renovaciones encadenadas (generaciones sucesivas) | < 2 000 ms c/u | Cada generación exitosa, IDs distintos, renewal_count incrementado |

---

### Epic 4 — Notificaciones de Vencimiento
**Archivo:** `notification-service/.../NotificationEfficiencyTest.java`  
**Endpoint base:** `POST/GET /api/v1/notifications`  
**Mock:** `EmailService` (retorna `true` sin conectar a SMTP)

#### US-4.2: Enviar Notificación de Vencimiento

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 1 | `us42_scheduleNotification` | Programar notificación (POST /) | < 1 000 ms | HTTP 201, estado PENDING |
| 2 | `us42_sendImmediately` | Crear y enviar inmediatamente (POST /send) | < 1 000 ms | HTTP 200, estado SENT |
| 3 | `us42_sendExpiredNotice` | Enviar notificación tipo EXPIRED_NOTICE | < 1 000 ms | HTTP 200, estado SENT |
| 4 | `us42_bulkSchedule` | Programar 20 notificaciones mixtas | avg < 1 000 ms | ≥ 19 exitosos |
| 5 | `us42_sendExistingNotification` | Disparar envío de notificación existente (POST /{id}/send) | < 1 000 ms | HTTP 200 |
| 13 | `us42_concurrentSend` | 5 POST /send simultáneos | wall < 5 000 ms | 5/5 exitosos |

#### US-4.3: Ver Notificaciones Pendientes

| # | Nombre del Test | Descripción | Umbral | Resultado Esperado |
|---|---|---|---|---|
| 6 | `us43_getPending` | GET /pending (con warm-up) | < 500 ms | HTTP 200, lista de pendientes |
| 7 | `us43_listAll` | Lista paginada de todas las notificaciones | < 1 500 ms | HTTP 200 |
| 8 | `us43_filterByStatus` | Filtro por `status=PENDING` | < 1 500 ms | HTTP 200 |
| 9 | `us43_filterBySent` | Filtro por `status=SENT` | < 1 500 ms | HTTP 200 |
| 10 | `us43_filterByContract` | Filtro por `contractId` | < 1 500 ms | HTTP 200 |
| 11 | `us43_byContract` | GET /contract/{id} (lista directa) | < 500 ms | HTTP 200 |
| 12 | `us43_concurrentPending` | 10 GET /pending simultáneos | wall < 5 000 ms | 10/10 exitosos |

---

## 5. Resumen de Cobertura

| Epic | Historia | Tests | Tipos de medición |
|---|---|:---:|---|
| Epic 1 | US-1.1 Registrar Colaborador | 3 | Latencia · Bulk · Concurrencia |
| Epic 1 | US-1.2 Ver Detalle Colaborador | 4 | Latencia · 404 · Concurrencia |
| Epic 1 | US-1.3 Listar Colaboradores | 3 | Latencia · Filtros |
| Epic 2 | US-2.1 Registrar Contrato | 2 | Latencia · Bulk |
| Epic 2 | US-2.2 Ver Detalle Contrato | 2 | Latencia · 404 |
| Epic 2 | US-2.3 Listar Contratos | 4 | Latencia · Filtros · Concurrencia |
| Epic 2 | US-2.4 Contratos por Vencer | 4 | Latencia · Estabilidad · Concurrencia |
| Epic 2 | US-2.5 Renovar Contrato | 2 | Latencia · Encadenamiento |
| Epic 3 | US-3.1 Registrar Evaluación | 3 | Latencia · Bulk |
| Epic 3 | US-3.2 Historial Desempeño | 3 | Latencia · Agregación |
| Epic 3 | US-3.3 Elegibilidad Renovación | 3 | Latencia · Corrección · Estabilidad |
| Epic 4 | US-4.2 Enviar Notificación | 6 | Latencia · Bulk · Concurrencia |
| Epic 4 | US-4.3 Ver Notificaciones | 7 | Latencia · Filtros · Concurrencia |
| **Total** | **13 historias** | **46** | |

---

## 6. Cómo Ejecutar las Pruebas

> **Requisito:** Docker en ejecución (TestContainers lo usa para levantar PostgreSQL automáticamente).

### Ejecutar todos los módulos
```bash
cd contract-management-server
mvn test -pl collaborator-service,contract-service,notification-service
```

### Ejecutar un servicio específico
```bash
# Solo colaboradores (US-1.x y US-3.x)
mvn test -pl collaborator-service

# Solo contratos (US-2.x)
mvn test -pl contract-service

# Solo notificaciones (US-4.x)
mvn test -pl notification-service
```

### Ejecutar un test específico
```bash
mvn test -pl contract-service -Dtest=ContractEfficiencyTest#us25_renew
```

### Ver salida detallada de tiempos
Cada test imprime una línea con el formato:
```
[US-X.Y] descripción: NNN ms
```
Ejemplo de salida esperada:
```
[US-1.1] create: 87 ms
[US-1.1] bulk 50: avg=64 ms, max=143 ms
[US-1.2] get by ID: 22 ms
[US-1.3] list (size 20): 45 ms
[US-2.5] renew contract: 134 ms
[US-2.5] renewal gen 1: 98 ms (new ID: ...)
[US-3.3] eligible check: 18 ms
[US-4.2] send immediately: 76 ms
[US-4.3] concurrent GET /pending x10: wall=312 ms, avg=89 ms, 10/10 ok
```

---

## 7. Decisiones de Diseño

| Decisión | Justificación |
|---|---|
| `@SpringBootTest(RANDOM_PORT)` sobre MockMvc | Mide tiempos de red HTTP reales, no solo la capa de servicio |
| TestContainers con PostgreSQL real | Evita diferencias de comportamiento entre H2 y PostgreSQL (índices, constraints) |
| `@MockBean` para Feign clients | Aísla el servicio de contratos sin requerir que collaborator-service y notification-service estén activos |
| `@MockBean EmailService` | Evita conexión SMTP real; mide solo la lógica de persistencia de notificaciones |
| Warm-up antes de medir | La primera llamada inicializa caches de Hibernate y JIT; el warm-up asegura mediciones representativas |
| `@TestMethodOrder(OrderAnnotation)` | Permite que tests posteriores reusen datos creados por tests anteriores (ej. `firstContractId`) |
| Scheduler deshabilitado en `application-test.yml` | Evita que jobs programados interfieran con las mediciones durante la prueba |

---

## 8. Limitaciones

- Las pruebas miden latencia en entorno local con una sola instancia; los tiempos en producción variarán según carga real, red y hardware del servidor.
- TestContainers agrega ~5–10 s de overhead al levantar PostgreSQL; este tiempo **no** se incluye en las mediciones de los tests.
- La concurrencia se prueba hasta 10 hilos; para pruebas de carga con cientos de usuarios simultáneos se recomienda complementar con k6 o Gatling.
- US-4.1 (detección automática de vencimiento por scheduler) no se incluye en este reporte porque el job está deshabilitado en el perfil de prueba.
