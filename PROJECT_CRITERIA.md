# Project Criteria and Implementation

## 1. CI/CD and Local Build

The application is fully buildable both in Continuous Integration/Continuous Deployment (CI/CD) pipelines and local development environments. The project uses Maven Wrapper (`mvnw`) to ensure consistent builds across different machines without requiring a pre-installed Maven version. Build automation is configured through `pom.xml` with all necessary dependencies, plugins, and build phases properly defined. This enables seamless integration with CI/CD platforms like GitHub Actions, Jenkins, or GitLab CI.

**Build Command:**
```bash
./mvnw clean install
```

## 2. Formal Specification with JML and OpenJML

Core methods of the application include formal specifications written in Java Modeling Language (JML). These specifications define preconditions, postconditions, and invariants that mathematically verify the correctness of critical code paths. OpenJML is integrated into the Docker build process as an optional verification step, ensuring that the implementation adheres to its formal contracts.

**Verification Status:** OpenJML verification can be enabled during Docker builds by setting `ENABLE_OPENJML=true` in docker-compose.yml.

## 3. Docker Image and Container Orchestration

A production-ready Docker image is available and configured for container orchestration. The application includes a multi-stage Dockerfile that builds the application and creates an optimized runtime image. Docker Compose configuration is provided for local development and testing, supporting multiple database backends (MySQL, PostgreSQL). The containerized application is deployable to Kubernetes or any container orchestration platform.

**Container Components:**
- Application container (Spring Boot on port 8081)
- Database containers (MySQL/PostgreSQL with health checks)
- Volume persistence for data

## 4. Comprehensive Test Suite

The application features a significant test suite covering multiple testing levels:
- **Unit Tests:** Testing individual components and methods in isolation
- **Integration Tests:** Validating interactions between components and database operations
- **Controller Tests:** Testing web endpoints using MockMvc
- **Service Layer Tests:** Verifying business logic correctness

**Test Statistics:**
- Total Tests: 64 passing tests
- Test Frameworks: JUnit 5, Mockito, Spring Test

## 5. Code Coverage Analysis with JaCoCo

Code coverage is measured using JaCoCo (Java Code Coverage), which analyzes how much of the codebase is executed during test runs. Coverage reports are generated in multiple formats (HTML, XML, CSV) and include both line coverage and branch coverage metrics. This ensures that the test suite adequately exercises the application code.

**Coverage Results:**
- Instruction Coverage: 79% (260 of 1,288 instructions)
- Branch Coverage: 78% (21 of 98 branches)
- Line Coverage: 84% (335 lines covered, 52 missed)
- Method Coverage: 99% (118 of 119 methods)
- Reports Location: `target/site/jacoco/index.html`

## 6. Mutation Testing with PiTest

A mutation testing campaign is conducted using PiTest to evaluate the quality and effectiveness of the test suite. PiTest introduces small changes (mutations) to the code and verifies whether existing tests detect these changes. This provides a deeper analysis of test effectiveness beyond simple code coverage.

**PiTest Configuration:**
- Mutation Engine: PIT 1.18.0
- Mutators: DEFAULTS (return values, conditionals, increments, etc.)
- Target Classes: `org.springframework.samples.petclinic.model.*`

**Mutation Testing Results:**
- Line Coverage: 100% (62/62 lines)
- Mutation Coverage: 85% (23/27 mutations killed)
- Test Strength: 85% (23/27 mutations detected)
- Number of Classes Tested: 4
- Total Mutations: 27 (23 killed, 4 survived)
- Report Location: `target/pit-reports/index.html`

## 7. Performance Testing with Apache JMeter

**Tool Selection: JMeter over JMH**

JMH (Java Microbenchmark Harness) is designed for nanosecond-level method benchmarking and is incompatible with Spring Boot's full application context initialization. Apache JMeter is used instead to perform HTTP-layer load testing, measuring real-world performance including concurrent requests, response times, throughput, and end-to-end system behavior.

**JMeter Configuration:**
- Test Plan Location: `src/test/jmeter/petclinic_test_plan.jmx`
- Test Scenarios: Owner search, vet listing, pet management workflows
- Execution: `jmeter -n -t src/test/jmeter/petclinic_test_plan.jmx -l target/jmeter-results.jtl -e -o target/jmeter-report`

**Performance Test Results:**
- Total Requests: 65,000 over 1m59s
- Throughput: 546.1 requests/second
- Error Rate: 0.04% (28/65,000)
- Average Response Time: 508ms
- Response Time Range: 3ms - 5,815ms
- Load: 500 concurrent users, 10s ramp-up, 10 loops
- Report: `target/jmeter-report/index.html`

The application demonstrates excellent stability under heavy load, maintaining a 99.96% success rate while handling over 500 requests per second. The average response time of 508ms is acceptable for a database-backed web application, though the maximum response time of ~6 seconds indicates occasional performance spikes under peak concurrent load that could benefit from caching or query optimization

## 8. Security Scanning with GitGuardian

The codebase has been scanned by GitGuardian for exposed secrets, API keys, credentials, and sensitive data. **No secret incidents were detected**, confirming that no hardcoded credentials or sensitive information is committed to the repository. This validates secure development practices and prevents potential security breaches from leaked secrets.

## 9. Dependency Vulnerability Scanning with Snyk

Snyk identified **3 vulnerabilities** in project dependencies:

**Critical Severity:**
- `org.apache.commons:commons-collections4@4.0` - Priority Score 919
  - 2 Deserialization of Untrusted Data vulnerabilities (CWE-502, CVSS 9.8)
  - Fix: Upgrade to version 4.1+

**Low Severity:**
- `org.springframework.boot:spring-boot-starter-actuator@4.0.0` - Priority Score 376
  - 1 External Initialization of Trusted Variables vulnerability (CWE-454, CVSS 1.8)
  - Fix: Upgrade to version 4.0.2+

All identified vulnerabilities have available fixes through dependency version upgrades.

**Remediation Actions:**
The critical vulnerabilities were successfully remediated by upgrading `org.apache.commons:commons-collections4` from version 4.0 to 4.1 and `spring-boot-starter-parent` from 4.0.0 to 4.0.2. After applying these upgrades, all 64 unit and integration tests passed successfully, confirming backward compatibility and maintaining application functionality. The upgrades were committed and pushed to the repository, reducing the project's security risk profile while preserving code integrity.

## 10. Static Code Analysis with SonarQube

SonarQube identified **4 security blocker issues** in the codebase, all of which have been remediated:

**1. SQL Injection Vulnerabilities (CWE-89 | OWASP A03:2021 - Injection) - FIXED:**
- `OwnerRepositoryImpl.java` Line 41 - SQL query construction from user-controlled data
  - **Vulnerability Type:** SQL Injection (CWE-89)
  - **OWASP Category:** A03:2021 - Injection
  - **Severity:** Blocker/Critical
  - **Remediation:** Replaced string concatenation with parameterized queries using `setParameter()`
  - **Code Change:** `SELECT * FROM owners WHERE last_name = :lastName` with `query.setParameter("lastName", lastName)`
  
- `OwnerRepositoryImpl.java` Line 55 - SQL query construction from user-controlled data
  - **Vulnerability Type:** SQL Injection (CWE-89)
  - **OWASP Category:** A03:2021 - Injection
  - **Severity:** Blocker/Critical
  - **Remediation:** Implemented parameterized LIKE queries with named parameters
  - **Code Change:** `SELECT * FROM owners WHERE first_name LIKE :searchPattern` with `query.setParameter("searchPattern", "%" + searchTerm + "%")`

**2. Open Redirect Vulnerability (CWE-601 | OWASP A01:2021 - Broken Access Control) - FIXED:**
- `CrashController.java` Line 54 - Unvalidated redirect based on user input
  - **Vulnerability Type:** Unvalidated Redirects and Forwards (CWE-601)
  - **OWASP Category:** A01:2021 - Broken Access Control
  - **Severity:** Blocker/High
  - **Remediation:** Added URL validation method `isAllowedRedirectUrl()` that restricts redirects to relative URLs or trusted localhost domains
  - **Code Change:** Implemented allowlist validation before creating RedirectView, defaulting to safe "/" location if URL is untrusted

**3. Hardcoded Database Password (CWE-798 | OWASP A02:2021 - Cryptographic Failures) - FIXED:**
- `CrashController.java` Line 71-80 - Database credentials exposed in error messages and model attributes
  - **Vulnerability Type:** Use of Hard-coded Password (CWE-798)
  - **OWASP Category:** A02:2021 - Cryptographic Failures / A04:2021 - Insecure Design
  - **Severity:** Blocker/Critical
  - **Remediation:** Removed all sensitive data from exception messages and model attributes
  - **Code Change:** Replaced detailed error messages containing database credentials with generic "An error occurred" message, removed `databasePassword`, `databaseUrl`, `databaseUser`, and stack trace exposure from model

**Security Improvements Applied:**
- All SQL queries now use parameterized statements to prevent injection attacks
- Redirect URLs are validated against an allowlist to prevent phishing
- Sensitive information is no longer exposed in error messages
- Generic error handling prevents information disclosure

**OWASP Top 10 Coverage:**
- ✅ A01:2021 - Broken Access Control (Open Redirect fixed)
- ✅ A02:2021 - Cryptographic Failures (Hardcoded credentials removed)
- ✅ A03:2021 - Injection (SQL Injection fixed)

All fixes maintain backward compatibility while eliminating critical security vulnerabilities identified by SonarQube.

## 11. CI/CD Security Integration with GitHub Actions

**For Your Report:**

A comprehensive CI/CD pipeline has been implemented using GitHub Actions (`.github/workflows/ci-security.yml`) that automatically enforces security checks on every code push and pull request. The pipeline consists of multiple security-focused jobs:

**Pipeline Stages:**
1. **Build & Test** - Compiles the application, runs unit/integration tests, and generates JaCoCo coverage reports
2. **Security Scanning** - Executes three parallel security scans:
   - **Snyk**: Scans dependencies for known vulnerabilities
   - **SonarQube**: Performs static code analysis for security issues and code quality
   - **GitGuardian**: Detects exposed secrets and credentials
3. **Mutation Testing** - Runs PiTest to validate test suite effectiveness
4. **Docker Build** - Creates containerized artifacts only after security validation passes

**Security Enforcement:**
- All security scans run automatically on push/PR events
- Security jobs require successful build/test completion before execution
- Docker image creation depends on passing security scans
- Failed security checks are visible in PR reviews, preventing vulnerable code merges
- Secret tokens (`SNYK_TOKEN`, `SONAR_TOKEN`, `GITGUARDIAN_API_KEY`) are managed via GitHub Secrets for secure access

**Continuous Security Benefits:**
- Early detection of vulnerabilities before production deployment
- Automated dependency monitoring and alerting
- Consistent security policy enforcement across all code changes
- Historical security scan results for compliance auditing
- Integration with GitHub Advanced Security (Dependabot) for automated dependency updates

This implementation establishes a "shift-left" security approach, identifying and addressing vulnerabilities during development rather than post-deployment.

---

## Summary

This project implements comprehensive software quality assurance practices covering build automation, formal verification, containerization, testing, coverage analysis, mutation testing, and performance benchmarking. Together, these practices ensure the application meets high standards for dependability, reliability, and performance.
