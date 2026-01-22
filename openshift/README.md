# Guide de déploiement OpenShift - BeeBoard API

Guide complet pour déployer beeboard-api sur OpenShift.

## Table des matières

1. [Architecture](#architecture)
2. [Prérequis](#prérequis)
3. [Build de l'image](#build-de-limage)
4. [Préparation des secrets](#préparation-des-secrets)
5. [Déploiement](#déploiement)
6. [Vérification](#vérification)
7. [Accès à l'application](#accès-à-lapplication)
8. [Mise à jour](#mise-à-jour)
9. [Troubleshooting](#troubleshooting)
10. [Nettoyage](#nettoyage)

---

## Architecture

Application monolithique Quarkus 3.30.3 avec :
- **Backend** : Java 21, Quarkus fast-jar
- **Base de données** : PostgreSQL 16
- **Communication** : CDI Events (événements en mémoire)
- **Authentication** : JWT (RSA 2048)
- **Email** : Resend SMTP

### Bounded Contexts

- **Access BC** : Gestion des utilisateurs, rôles et permissions
- **Note BC** : Gestion des notes et commentaires

Communication via CDI Events synchronisée par author_view (CQRS read model).

---

## Prérequis

### Outils requis

- **OpenShift CLI** (`oc`) version 4.x+
  ```bash
  # Installation
  https://docs.openshift.com/container-platform/latest/cli_reference/openshift_cli/getting-started-cli.html

  # Vérifier la version
  oc version
  ```

- **Podman** version 4.x+
  ```bash
  # Installation Fedora/RHEL
  sudo dnf install podman

  # Vérifier la version
  podman --version
  ```

- **OpenSSL** (pour générer les clés JWT)
  ```bash
  openssl version
  ```

### Accès requis

- Accès à un cluster OpenShift avec permissions de créer des namespaces
- Accès à un container registry (Quay.io, Docker Hub, registry interne OpenShift, etc.)
- Compte Resend pour l'envoi d'emails (API key)

---

## Build de l'image

### 1. Compiler le code localement (optionnel)

```bash
# À la racine du projet
./mvnw clean package -DskipTests
```

### 2. Build de l'image avec Podman

```bash
# Build l'image
podman build -t beeboard-api:latest -f Containerfile .

# Vérifier l'image
podman images | grep beeboard-api
```

### 3. Tag et Push vers le registry

#### Option A : Quay.io

```bash
# Login
podman login quay.io

# Tag
podman tag beeboard-api:latest quay.io/<YOUR_ORG>/beeboard-api:1.0.0

# Push
podman push quay.io/<YOUR_ORG>/beeboard-api:1.0.0
```

#### Option B : OpenShift Internal Registry

```bash
# Login au cluster OpenShift
oc login <CLUSTER_URL>

# Exposer le registry (si nécessaire)
oc patch configs.imageregistry.operator.openshift.io/cluster --patch '{"spec":{"defaultRoute":true}}' --type=merge

# Obtenir l'URL du registry
REGISTRY=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')

# Login au registry
podman login -u $(oc whoami) -p $(oc whoami -t) $REGISTRY --tls-verify=false

# Tag
podman tag beeboard-api:latest $REGISTRY/beeboard/beeboard-api:1.0.0

# Push
podman push $REGISTRY/beeboard/beeboard-api:1.0.0 --tls-verify=false
```

#### Option C : Docker Hub

```bash
# Login
podman login docker.io

# Tag
podman tag beeboard-api:latest docker.io/<YOUR_USERNAME>/beeboard-api:1.0.0

# Push
podman push docker.io/<YOUR_USERNAME>/beeboard-api:1.0.0
```

---

## Préparation des secrets

### 1. Générer les clés JWT (RSA 2048)

```bash
# Créer un répertoire temporaire
mkdir -p /tmp/beeboard-keys
cd /tmp/beeboard-keys

# Générer la clé privée
openssl genrsa -out private.pem 2048

# Extraire la clé publique
openssl rsa -in private.pem -pubout -out public.pem

# Encoder en base64 pour Kubernetes (une seule ligne)
PRIVATE_KEY_B64=$(cat private.pem | base64 -w 0)
PUBLIC_KEY_B64=$(cat public.pem | base64 -w 0)

# Afficher les valeurs (copier pour plus tard)
echo "Private Key (base64):"
echo $PRIVATE_KEY_B64
echo ""
echo "Public Key (base64):"
echo $PUBLIC_KEY_B64
```

### 2. Générer le mot de passe PostgreSQL

```bash
# Générer un mot de passe sécurisé
PG_PASSWORD=$(openssl rand -base64 32)

# Encoder en base64
PG_PASSWORD_B64=$(echo -n "$PG_PASSWORD" | base64 -w 0)

echo "PostgreSQL Password (base64):"
echo $PG_PASSWORD_B64
```

### 3. Encoder les autres valeurs

```bash
# PostgreSQL User
PG_USER_B64=$(echo -n "beeboard_user" | base64 -w 0)
echo "PostgreSQL User (base64): $PG_USER_B64"

# PostgreSQL Database
PG_DB_B64=$(echo -n "beeboard_db" | base64 -w 0)
echo "PostgreSQL Database (base64): $PG_DB_B64"

# Database URL
DB_URL_B64=$(echo -n "jdbc:postgresql://postgresql:5432/beeboard_db" | base64 -w 0)
echo "Database URL (base64): $DB_URL_B64"

# Resend API Key (remplacer re_xxx par votre vraie clé)
RESEND_KEY_B64=$(echo -n "re_your_api_key_here" | base64 -w 0)
echo "Resend API Key (base64): $RESEND_KEY_B64"
```

### 4. Modifier le fichier secret.yaml

Ouvrir `openshift/secret.yaml` et remplacer tous les `<BASE64_ENCODED_VALUE>` :

```yaml
data:
  POSTGRES_USER: <remplacer par $PG_USER_B64>
  POSTGRES_PASSWORD: <remplacer par $PG_PASSWORD_B64>
  POSTGRES_DB: <remplacer par $PG_DB_B64>
  DB_URL: <remplacer par $DB_URL_B64>
  DB_USERNAME: <remplacer par $PG_USER_B64>
  DB_PASSWORD: <remplacer par $PG_PASSWORD_B64>
  MP_JWT_VERIFY_PRIVATEKEY: <remplacer par $PRIVATE_KEY_B64>
  MP_JWT_VERIFY_PUBLICKEY: <remplacer par $PUBLIC_KEY_B64>
  SMALLRYE_JWT_SIGN_KEY_LOCATION: <remplacer par $PRIVATE_KEY_B64>
  RESEND_API_KEY: <remplacer par $RESEND_KEY_B64>
  QUARKUS_MAILER_PASSWORD: <remplacer par $RESEND_KEY_B64>
```

---

## Déploiement

### 1. Login au cluster OpenShift

```bash
# Login
oc login <CLUSTER_URL> -u <USERNAME>

# Vérifier la connexion
oc whoami
oc cluster-info
```

### 2. Créer le namespace

```bash
oc apply -f openshift/namespace.yaml

# Vérifier
oc get namespace beeboard
```

### 3. Modifier deployment.yaml

Éditer `openshift/deployment.yaml` et remplacer l'image :

```yaml
# Ligne 51 environ
containers:
  - name: beeboard-api
    # Remplacer par votre image complète
    image: quay.io/<YOUR_ORG>/beeboard-api:1.0.0
    # OU
    # image: default-route-openshift-image-registry.apps.<cluster>/beeboard/beeboard-api:1.0.0
```

### 4. Modifier configmap.yaml (optionnel)

Éditer `openshift/configmap.yaml` pour ajuster :

```yaml
# Frontend URL (pour password reset)
FRONTEND_BASE_URL: "https://beeboard.example.com"

# CORS origins
QUARKUS_HTTP_CORS_ORIGINS: "https://beeboard.example.com,https://admin.beeboard.example.com"
```

### 5. Déployer toutes les ressources

```bash
# ConfigMap
oc apply -f openshift/configmap.yaml

# Secrets (assurez-vous d'avoir modifié les valeurs !)
oc apply -f openshift/secret.yaml

# PostgreSQL (PVC + Deployment + Service)
oc apply -f openshift/postgresql.yaml

# Application
oc apply -f openshift/deployment.yaml
oc apply -f openshift/service.yaml
oc apply -f openshift/route.yaml
```

### 6. Vérifier le déploiement

```bash
# Vérifier toutes les ressources
oc get all -n beeboard

# Surveiller les pods
oc get pods -n beeboard -w
```

---

## Vérification

### 1. Vérifier les pods

```bash
# Status des pods
oc get pods -n beeboard

# Résultat attendu :
# NAME                            READY   STATUS    RESTARTS   AGE
# beeboard-api-xxxxxxxxxx-xxxxx   1/1     Running   0          2m
# postgresql-xxxxxxxxxx-xxxxx     1/1     Running   0          3m
```

### 2. Consulter les logs

```bash
# Logs PostgreSQL
oc logs -f deployment/postgresql -n beeboard

# Logs de l'application
oc logs -f deployment/beeboard-api -n beeboard

# Logs de l'init container (si problème de démarrage)
oc logs deployment/beeboard-api -n beeboard -c wait-for-db
```

### 3. Vérifier les health checks

```bash
# Obtenir l'URL de la route
ROUTE_URL=$(oc get route beeboard-api -n beeboard -o jsonpath='{.spec.host}')
echo "Application URL: https://$ROUTE_URL"

# Health check (live)
curl https://$ROUTE_URL/q/health/live

# Health check (ready)
curl https://$ROUTE_URL/q/health/ready

# Health check (started)
curl https://$ROUTE_URL/q/health/started
```

### 4. Tester l'API

```bash
# Swagger UI
echo "Swagger UI: https://$ROUTE_URL/q/swagger-ui"

# Ouvrir dans le navigateur ou tester avec curl
curl https://$ROUTE_URL/q/swagger-ui
```

---

## Accès à l'application

### Endpoints principaux

```bash
ROUTE_URL=$(oc get route beeboard-api -n beeboard -o jsonpath='{.spec.host}')

# Swagger UI (documentation API)
https://$ROUTE_URL/q/swagger-ui

# Health endpoints
https://$ROUTE_URL/q/health
https://$ROUTE_URL/q/health/live
https://$ROUTE_URL/q/health/ready

# Metrics (si activé)
https://$ROUTE_URL/q/metrics

# API endpoints
https://$ROUTE_URL/api/v1/auth/login
https://$ROUTE_URL/api/v1/users
https://$ROUTE_URL/api/v1/notes
```

### Test de login

```bash
# Login (remplacer par vos vraies données de test)
curl -X POST https://$ROUTE_URL/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

---

## Mise à jour

### 1. Build nouvelle version

```bash
# Build
podman build -t beeboard-api:1.1.0 -f Containerfile .

# Tag
podman tag beeboard-api:1.1.0 quay.io/<YOUR_ORG>/beeboard-api:1.1.0

# Push
podman push quay.io/<YOUR_ORG>/beeboard-api:1.1.0
```

### 2. Mise à jour du deployment

```bash
# Méthode 1 : Via oc set image
oc set image deployment/beeboard-api \
  beeboard-api=quay.io/<YOUR_ORG>/beeboard-api:1.1.0 \
  -n beeboard

# Méthode 2 : Éditer le fichier et réappliquer
# Éditer openshift/deployment.yaml (changer la version)
oc apply -f openshift/deployment.yaml
```

### 3. Surveiller le rollout

```bash
# Surveiller le déploiement
oc rollout status deployment/beeboard-api -n beeboard

# Historique des rollouts
oc rollout history deployment/beeboard-api -n beeboard
```

### 4. Rollback si nécessaire

```bash
# Rollback vers la version précédente
oc rollout undo deployment/beeboard-api -n beeboard

# Rollback vers une révision spécifique
oc rollout undo deployment/beeboard-api -n beeboard --to-revision=2
```

---

## Troubleshooting

### Pod ne démarre pas

```bash
# Décrire le pod
oc describe pod <POD_NAME> -n beeboard

# Vérifier les events
oc get events -n beeboard --sort-by='.lastTimestamp'

# Logs du pod
oc logs <POD_NAME> -n beeboard

# Logs de l'init container
oc logs <POD_NAME> -n beeboard -c wait-for-db
```

### Problème de connexion à la base de données

```bash
# Vérifier que PostgreSQL est prêt
oc get pods -n beeboard | grep postgresql

# Tester la connexion depuis le pod de l'application
oc exec -it deployment/beeboard-api -n beeboard -- \
  nc -zv postgresql 5432

# Vérifier les secrets
oc get secret beeboard-api-secrets -n beeboard -o yaml
```

### Problème d'image

```bash
# Vérifier les pull secrets
oc get secrets -n beeboard

# Créer un pull secret si nécessaire (pour registry privé)
oc create secret docker-registry quay-secret \
  --docker-server=quay.io \
  --docker-username=<USERNAME> \
  --docker-password=<PASSWORD> \
  -n beeboard

# Lier le secret au service account
oc secrets link default quay-secret --for=pull -n beeboard
```

### CrashLoopBackOff

```bash
# Vérifier les logs
oc logs -f deployment/beeboard-api -n beeboard --previous

# Vérifier les health probes
oc describe pod <POD_NAME> -n beeboard | grep -A 10 "Liveness\|Readiness"

# Augmenter temporairement les timeouts dans deployment.yaml
# failureThreshold: 10
# initialDelaySeconds: 30
```

### Accès shell au conteneur

```bash
# Bash dans le pod de l'application
oc exec -it deployment/beeboard-api -n beeboard -- /bin/bash

# Bash dans PostgreSQL
oc exec -it deployment/postgresql -n beeboard -- /bin/bash

# Une fois dans PostgreSQL, se connecter à la DB
psql -U beeboard_user -d beeboard_db
```

### Vérifier les permissions OpenShift

```bash
# Vérifier le SCC (Security Context Constraints)
oc get pod <POD_NAME> -n beeboard -o yaml | grep scc

# Si erreur de permissions, vérifier que l'image utilise UID 185
podman inspect beeboard-api:latest | grep -i user
```

---

## Nettoyage

### Supprimer l'application (conserver PostgreSQL)

```bash
oc delete deployment beeboard-api -n beeboard
oc delete service beeboard-api -n beeboard
oc delete route beeboard-api -n beeboard
```

### Supprimer tout le namespace

```bash
# ATTENTION : Supprime TOUTES les ressources, y compris les données PostgreSQL
oc delete namespace beeboard
```

### Supprimer uniquement les données PostgreSQL

```bash
# Supprimer le PVC (les données seront perdues)
oc delete pvc postgresql-pvc -n beeboard

# Redémarrer PostgreSQL pour recréer la DB
oc rollout restart deployment/postgresql -n beeboard
```

---

## Ressources

### Documentation

- [Quarkus on OpenShift](https://quarkus.io/guides/deploying-to-openshift)
- [OpenShift CLI Reference](https://docs.openshift.com/container-platform/latest/cli_reference/openshift_cli/getting-started-cli.html)
- [PostgreSQL on OpenShift](https://docs.openshift.com/container-platform/latest/using_images/db_images/postgresql.html)

### Support

- Issues GitHub : (à compléter)
- Documentation projet : `documentation/`

---

## Checklist de déploiement

- [ ] Cluster OpenShift accessible
- [ ] `oc` CLI installé et configuré
- [ ] Podman installé
- [ ] Image buildée et pushée au registry
- [ ] Clés JWT générées
- [ ] Mot de passe PostgreSQL généré
- [ ] API key Resend obtenue
- [ ] `secret.yaml` modifié avec les vraies valeurs
- [ ] `deployment.yaml` modifié avec la bonne image
- [ ] `configmap.yaml` modifié (frontend URL, CORS)
- [ ] Namespace créé
- [ ] ConfigMap déployé
- [ ] Secret déployé
- [ ] PostgreSQL déployé et prêt
- [ ] Application déployée
- [ ] Service créé
- [ ] Route créée
- [ ] Health checks OK
- [ ] Swagger UI accessible
- [ ] Login API testé

---

**Version** : 1.0.0
**Date** : 2026-01-21
**Auteur** : BeeBoard Team
