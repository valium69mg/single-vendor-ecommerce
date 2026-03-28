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

- Docker & Docker Pipeline

- SSH Agent

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

# 8 For docker hub repo

## 8.1 Create access token for user

- Account settings > personal access tokens > Generate new token

- Add it to credentials as username and password with same name as the Jenkinsfile

# 9 QualityGate Webhook

You need to set up a webhook in order for Sonar to notify that the quality gate is passed,
it needs to be registered in jenkins and in sonar, with the following url:

- http://jenkins:8080/sonarqube-webhook/

# 10 SSH Deploy

## 10.1 Generar clave SSH dentro del contenedor de Jenkins
```bash
docker exec jenkins mkdir -p /var/jenkins_home/.ssh
docker exec jenkins ssh-keygen -t ed25519 -C "jenkins" -f /var/jenkins_home/.ssh/id_ed25519 -N ""
```

## 10.2 Obtener clave publica de Jenkins y agregarla al servidor Ubuntu
```bash
# Obtener la clave publica
docker exec jenkins cat /var/jenkins_home/.ssh/id_ed25519.pub

# Agregarla al servidor Ubuntu
echo "paste-jenkins-public-key-here" >> ~/.ssh/authorized_keys

# Fijar permisos
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

## 10.3 Copiar clave al directorio /root/.ssh dentro del contenedor
```bash
docker exec jenkins mkdir -p /root/.ssh
docker exec jenkins cp /var/jenkins_home/.ssh/id_ed25519 /root/.ssh/id_ed25519
docker exec jenkins cp /var/jenkins_home/.ssh/id_ed25519.pub /root/.ssh/id_ed25519.pub
docker exec jenkins chmod 700 /root/.ssh
docker exec jenkins chmod 600 /root/.ssh/id_ed25519
```

## 10.4 Agregar clave privada de Jenkins a Jenkins Credentials (UI)
```
Manage Jenkins → Credentials → Global → Add Credentials

Kind:        SSH Username with private key
ID:          ubuntu-server-ssh
Username:    carlostr
Private Key: Enter directly → paste output of:
             docker exec jenkins cat /var/jenkins_home/.ssh/id_ed25519
```

## 10.5 Habilitar PubkeyAuthentication en el servidor Ubuntu
```bash
sudo nano /etc/ssh/sshd_config
```

Descomentar estas lineas:
```
PubkeyAuthentication yes
AuthorizedKeysFile .ssh/authorized_keys
```

Reiniciar SSH:
```bash
sudo systemctl restart ssh
```

## 10.6 Probar conexion
```bash
docker exec jenkins ssh -o StrictHostKeyChecking=no carlostr@192.168.100.50
# debe conectar sin pedir password
```