# Containerfile pour beeboard-api (Podman/Buildah compatible)
# Multi-stage build pour optimiser la taille de l'image finale

# ============================================================
# Stage 1: Build stage - Compilation Maven
# ============================================================
FROM registry.access.redhat.com/ubi9/openjdk-21:1.20 AS builder

# Métadonnées
LABEL maintainer="beeboard-team"
LABEL description="Build stage for BeeBoard API"

# Définir le répertoire de travail
WORKDIR /build

# Copier les fichiers de configuration Maven
COPY --chown=default:root pom.xml ./
COPY --chown=default:root .mvn .mvn
COPY --chown=default:root mvnw ./

# Télécharger les dépendances (layer séparé pour cache)
RUN ./mvnw dependency:go-offline -B

# Copier le code source
COPY --chown=default:root src ./src

# Compiler l'application
# Note: Quarkus package crée un runner JAR optimisé
RUN ./mvnw package -DskipTests -Dquarkus.package.type=fast-jar

# ============================================================
# Stage 2: Runtime stage - Image minimale pour exécution
# ============================================================
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:1.20

# Métadonnées de l'image finale
LABEL maintainer="beeboard-team" \
      description="BeeBoard API - Identity & Access Management + Note Management" \
      version="1.0.0" \
      vendor="BeeBoard" \
      io.openshift.tags="java,quarkus,rest-api,ddd" \
      io.k8s.description="Backend API for BeeBoard with DDD architecture" \
      io.openshift.expose-services="8080:http"

# Variables d'environnement pour Quarkus
ENV QUARKUS_HTTP_HOST=0.0.0.0 \
    QUARKUS_HTTP_PORT=8080 \
    JAVA_OPTS="-Djava.util.logging.manager=org.jboss.logmanager.LogManager" \
    JAVA_MAX_MEM_RATIO=80.0 \
    GC_MAX_METASPACE_SIZE=256

# Créer répertoire applicatif
WORKDIR /deployments

# Copier l'application compilée depuis le stage builder
COPY --from=builder --chown=185:0 /build/target/quarkus-app/lib/ ./lib/
COPY --from=builder --chown=185:0 /build/target/quarkus-app/*.jar ./
COPY --from=builder --chown=185:0 /build/target/quarkus-app/app/ ./app/
COPY --from=builder --chown=185:0 /build/target/quarkus-app/quarkus/ ./quarkus/

# Permissions pour OpenShift (arbitrary user ID)
RUN chmod -R g=u /deployments && \
    chown -R 185:0 /deployments

# Exposer le port HTTP
EXPOSE 8080

# Health check (optionnel, OpenShift utilise readiness/liveness probes)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/q/health/ready || exit 1

# Utiliser l'utilisateur non-root (obligatoire OpenShift)
USER 185

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
