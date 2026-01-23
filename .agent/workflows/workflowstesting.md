---

description:
------------

--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

# Testing Workflow – Spring Boot

## 1. Purpose

Dokumen ini menjelaskan workflow dan standar pengujian (testing) untuk memastikan kualitas, stabilitas, dan keamanan aplikasi Spring Boot sebelum dirilis ke production.

Testing mengikuti prinsip **Testing Pyramid** untuk mendapatkan feedback cepat dengan biaya rendah.

---

## 2. Testing Pyramid

lua
Salin kode
E2E / Contract Tests
--------------------

Integration Tests
-----------------

Unit Tests
markdown
Salin kode

### Principles

- Unit test adalah prioritas utama
- Integration test untuk critical flow
- E2E test hanya untuk smoke test
- Testing harus **fast, isolated, and repeatable**

---

## 3. Test Types & Scope

### 3.1 Unit Test

#### Purpose

- Menguji business logic secara terisolasi
- Tanpa Spring Context
- Tanpa database atau external service

#### Scope

- Service
- UseCase
- Mapper
- Utility class

#### Tools

- JUnit 5
- Mockito
- AssertJ

#### Rules

- ❌ Jangan gunakan `@SpringBootTest`
- ❌ Jangan akses database
- ✅ Gunakan mock dependency

#### Example

```java
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @InjectMocks
    private LoanService loanService;

    @Test
    void shouldApproveLoan() {
        when(loanRepository.save(any())).thenReturn(new Loan());

        Loan result = loanService.approve(new Loan());

        assertThat(result).isNotNull();
    }
}
3.2 Integration Test
Purpose
Menguji interaksi antar komponen

Memastikan konfigurasi Spring berjalan dengan benar

Scope
Repository

Service + Repository

Transaction & JPA behavior

Tools
@SpringBootTest

@DataJpaTest

Testcontainers (recommended)

Rules
Gunakan database asli (PostgreSQL/MySQL)

Hindari H2 untuk behavior production

Example
java
Salin kode
@DataJpaTest
class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    @Test
    void shouldPersistLoan() {
        Loan loan = new Loan();
        Loan saved = loanRepository.save(loan);

        assertThat(saved.getId()).isNotNull();
    }
}
3.3 API / Controller Test
Purpose
Menguji REST API

Validasi request, response, dan HTTP status

Scope
Controller

Request validation

Error handling

Tools
@WebMvcTest

MockMvc

Rules
Mock Service layer

Tidak load full context

Example
java
Salin kode
@WebMvcTest(LoanController.class)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @Test
    void shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/loans"))
            .andExpect(status().isOk());
    }
}
3.4 Contract Test (Optional but Recommended)
Purpose
Mencegah breaking change antar service / frontend

Backend dan consumer memiliki kontrak yang sama

Tools
Spring Cloud Contract

Pact

Scope
API request/response schema

HTTP status dan headers

3.5 End-to-End (E2E) Test
Purpose
Menguji alur aplikasi dari API sampai database

Smoke test sebelum release

Tools
SpringBootTest (RANDOM_PORT)

RestAssured

Testcontainers

Rules
Minimal test

Fokus ke critical user flow

Example
java
Salin kode
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoanE2ETest {

    @Test
    void shouldGetLoansSuccessfully() {
        given()
            .when().get("/api/loans")
            .then().statusCode(200);
    }
}
4. Test Data Strategy
Rules
Gunakan Test Factory / Mother Object

Hindari hardcoded JSON

Data harus reusable

Example
java
Salin kode
Loan loan = LoanMother.approved();
5. Folder Structure
bash
Salin kode
src/test/java
 ├── unit
 │    └── service
 ├── integration
 │    └── repository
 ├── web
 │    └── controller
 └── e2e
6. Coverage Policy
Layer	Target
Unit Test	80–90%
Controller	60–70%
Integration	Critical paths only

Coverage tinggi bukan tujuan utama, kualitas test adalah prioritas.

7. CI Testing Workflow
Local Development
bash
Salin kode
mvn test
CI Pipeline
bash
Salin kode
mvn clean verify
mvn jacoco:report
Fail Strategy
Unit test failure → stop pipeline

Integration test failure → stop pipeline

E2E test → blocking before release

8. Best Practices
One assertion per test case

Test behavior, not implementation

Gunakan descriptive test method name

Hindari flaky tests

Isolasi external dependency

9. Definition of Done (DoD)
Feature dianggap selesai jika:

Unit test ditulis dan lulus

Integration test untuk critical flow tersedia

API contract tidak breaking

CI pipeline lulus

10. Summary
Unit Test → Fast feedback

Integration Test → Stability

Contract Test → Safety

E2E Test → Confidence

Testing adalah bagian dari development, bukan aktivitas terpisah.
```

