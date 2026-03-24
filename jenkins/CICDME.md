# Setup the CI CD Pipieline

Document on how to setup the CI CD pipeline with Jenkins and SonarQube
of the Single-Vendor Ecommerce backend.

## 1. Get Jenkins and SonarQube up and running

```bash
docker compose -f docker-compose-cicd.yml up -d
```

## 2. Unlock Jenkins if needed

### 2.1 Open http://localhost:8080

### 2.2 Jenkins asks for the initial admin password

```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 2.3 Paste the password → continue setup

### 2.4 Install suggested plugins

## 3. Install Required Plugins

### 3.1 Go to Manage Jenkins → Manage Plugins → Available and Install:

- Git plugin (for GitHub integration)

- Pipeline (Declarative pipelines)

- Pipeline: JUnit (for junit step)

- SonarQube Scanner (for code analysis)

## 4. Configure GitHub Access

### 4.1 Manage Jenkins → Credentials → System → Global credentials → Add Credentials:

- Kind: Username with password or Personal Access Token (PAT)

- Scope: Global

- ID: e.g., github-cred

## 5. Configure SonarQube

### 5.1 Go to SonarQube → My Account → Security → Tokens → Generate Token → copy it.

### 5.2 Jenkins → Manage Jenkins → Configure System → SonarQube servers → Add SonarQube:

- Name: SonarQube

- Server URL: http://sonarqube:9000

- Server authentication token: add new credential → Secret text → paste token

## 6. Create Pipeline Job

### 6.1 Jenkins → New Item → Pipeline → OK

### 6.2 Name: CICDME.mc

### 6.3 Pipeline Definition: Pipeline script from SCM

- SCM: Git

- Repository URL: https://github.com/valium69mg/single-vendor-ecommerce

- Branch: main or dev

- Script Path: Jenkinsfile

## 7. Run the pipeline

### 7.1 Open the pipeline job → click Build Now

### 7.2 Watch the console output → Jenkins will:

- Checkout code

- Build project

- Run tests

- Run SonarQube analysis
