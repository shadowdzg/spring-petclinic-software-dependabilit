# GitHub Repository Setup Commands

## Build & Security Status

### CI/CD Pipelines
![CI/CD with Security Checks](https://github.com/shadowdzg/spring-petclinic-software-dependabilit/actions/workflows/ci-security.yml/badge.svg)
![CI/CD Pipeline](https://github.com/shadowdzg/spring-petclinic-software-dependabilit/actions/workflows/ci-cd.yml/badge.svg)
![Maven Build](https://github.com/shadowdzg/spring-petclinic-software-dependabilit/actions/workflows/maven-build.yml/badge.svg)

### Security Scans (with logos)
[![Snyk Security](https://snyk.io/test/github/shadowdzg/spring-petclinic-software-dependabilit/badge.svg)](https://snyk.io/test/github/shadowdzg/spring-petclinic-software-dependabilit)
[![SonarQube](https://sonarcloud.io/api/project_badges/measure?project=YOUR_SONAR_PROJECT_KEY&metric=alert_status)](https://sonarcloud.io/dashboard?id=YOUR_SONAR_PROJECT_KEY)
[![GitGuardian](https://api.gitguardian.com/v1/badges/YOUR_WORKSPACE_ID/public_repositories/YOUR_REPO_ID)](https://dashboard.gitguardian.com)

> **Note**: Replace `YOUR_SONAR_PROJECT_KEY`, `YOUR_WORKSPACE_ID`, and `YOUR_REPO_ID` with actual values from your SonarQube and GitGuardian dashboards.

---

After creating your new GitHub repository, run these commands:

## Replace YOUR_USERNAME with your actual GitHub username:

```bash
# Remove existing origin (if any)
git remote remove origin

# Add your new repository as origin  
git remote add origin https://github.com/YOUR_USERNAME/spring-petclinic-software-dependability.git

# Push to your new repository
git branch -M main
git push -u origin main
```

## Alternative with SSH (if you have SSH keys set up):

```bash
# Remove existing origin (if any)
git remote remove origin

# Add your new repository as origin (SSH)
git remote add origin git@github.com:YOUR_USERNAME/spring-petclinic-software-dependability.git

# Push to your new repository  
git branch -M main
git push -u origin main
```

## Verify the push worked:

```bash
git remote -v
git log --oneline -5
```

## Your application is now running on:
- **Docker URL:** http://localhost:8081
- **Health Check:** http://localhost:8081/actuator/health  
- **Database:** MySQL on localhost:3306

## What's included in this repository:
- ✅ **Step 1:** Buildability (CI/CD + Local builds)
- ✅ **Step 2:** JML Formal Specifications (15 methods, 73 annotations)  
- ✅ **Step 3:** Docker Containerization (Multi-stage, secure)
- ✅ **Step 4:** Testing & Coverage (67% coverage, PiTest mutation testing)
- 📁 **Comprehensive Documentation:** All reports and guides included