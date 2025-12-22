# Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         GitHub Repository                        │
│                     github.com/biasmj016/saju-interpret         │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ git push
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                        GitHub Actions                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  1. Build with Gradle (Java 25)                          │  │
│  │  2. Run tests                                            │  │
│  │  3. Build Docker image (multi-stage)                     │  │
│  │  4. Push to ghcr.io/biasmj016/saju-interpret            │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ sync
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                           ArgoCD                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  - Watch k8s/ directory in Git                           │  │
│  │  - Auto-sync to Kubernetes cluster                       │  │
│  │  - Self-heal on drift                                    │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────┬────────────────────────────────────────────────────┘
             │
             │ apply manifests
             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                            │
│                                                                  │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                    Istio Service Mesh                      │ │
│  │                                                            │ │
│  │  ┌──────────────┐                                         │ │
│  │  │   Gateway    │ HTTP/HTTPS Entry Point                  │ │
│  │  └──────┬───────┘                                         │ │
│  │         │                                                  │ │
│  │         ▼                                                  │ │
│  │  ┌──────────────────┐                                     │ │
│  │  │ VirtualService   │ Route /api/saju/* → Service         │ │
│  │  └──────┬───────────┘                                     │ │
│  │         │                                                  │ │
│  │         ▼                                                  │ │
│  │  ┌──────────────────┐                                     │ │
│  │  │ DestinationRule  │ Load Balancing, Circuit Breaker    │ │
│  │  └──────┬───────────┘                                     │ │
│  └─────────┼────────────────────────────────────────────────┘ │
│            │                                                    │
│            ▼                                                    │
│  ┌─────────────────────────────────────────────────────────┐  │
│  │              saju-interpret Service                      │  │
│  │                  (ClusterIP:8080)                        │  │
│  └────────┬────────────────────────────────────────────────┘  │
│           │                                                     │
│           ▼                                                     │
│  ┌────────────────────────────────────────────────────────┐   │
│  │           saju-interpret Deployment                     │   │
│  │                                                         │   │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐             │   │
│  │  │  Pod 1   │  │  Pod 2   │  │  Pod 3   │  (replicas) │   │
│  │  │          │  │          │  │          │             │   │
│  │  │ ┌──────┐ │  │ ┌──────┐ │  │ ┌──────┐ │             │   │
│  │  │ │ App  │ │  │ │ App  │ │  │ │ App  │ │ Spring Boot │   │
│  │  │ │Java25│ │  │ │Java25│ │  │ │Java25│ │ Java 25     │   │
│  │  │ └──────┘ │  │ └──────┘ │  │ └──────┘ │             │   │
│  │  │ ┌──────┐ │  │ ┌──────┐ │  │ ┌──────┐ │             │   │
│  │  │ │Istio │ │  │ │Istio │ │  │ │Istio │ │ Sidecar     │   │
│  │  │ │Proxy │ │  │ │Proxy │ │  │ │Proxy │ │ (Envoy)     │   │
│  │  │ └──────┘ │  │ └──────┘ │  │ └──────┘ │             │   │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘             │   │
│  │       │             │             │                    │   │
│  └───────┼─────────────┼─────────────┼────────────────────┘   │
│          │             │             │                         │
│          │             │             │ /actuator/prometheus    │
│          └─────────────┴─────────────┘                         │
│                        │                                        │
│                        ▼                                        │
│          ┌──────────────────────────┐                          │
│          │    ServiceMonitor        │ Prometheus CRD           │
│          └────────┬─────────────────┘                          │
│                   │                                             │
└───────────────────┼─────────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Monitoring Stack                            │
│                                                                  │
│  ┌──────────────────┐        ┌─────────────────────┐           │
│  │   Prometheus     │───────▶│      Grafana        │           │
│  │                  │        │                     │           │
│  │  Scrape metrics  │        │  ┌───────────────┐  │           │
│  │  every 30s       │        │  │ HTTP Metrics  │  │           │
│  │                  │        │  │ JVM Memory    │  │           │
│  │                  │        │  │ GC Metrics    │  │           │
│  │                  │        │  │ CPU Usage     │  │           │
│  └──────────────────┘        │  │ Threads       │  │           │
│                              │  └───────────────┘  │           │
│                              └─────────────────────┘           │
└─────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                      API Endpoints                               │
│                                                                  │
│  External Access (via Istio Gateway):                           │
│  ├─ http://<INGRESS_IP>/api/saju/health                         │
│  ├─ http://<INGRESS_IP>/api/saju/{id}                           │
│  └─ http://<INGRESS_IP>/api/saju/interpret/{birthDate}          │
│                                                                  │
│  Internal Actuator (via Port Forward):                          │
│  ├─ http://localhost:8080/actuator/health                       │
│  ├─ http://localhost:8080/actuator/prometheus                   │
│  └─ http://localhost:8080/actuator/metrics                      │
└─────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────┐
│                    Resource Flow                                 │
│                                                                  │
│  Request Flow:                                                   │
│  User → Gateway → VirtualService → Service → Pod → App          │
│                                                                  │
│  Metrics Flow:                                                   │
│  App → /actuator/prometheus → ServiceMonitor → Prometheus       │
│                                                                  │
│  Visualization:                                                  │
│  Prometheus → Grafana Dashboard → User                          │
│                                                                  │
│  Deployment Flow:                                                │
│  Git Push → GitHub Actions → GHCR → ArgoCD → K8s Deploy         │
└─────────────────────────────────────────────────────────────────┘
```

## Key Components Explained

### 1. **Istio Gateway**
   - Entry point for external traffic (HTTP/HTTPS)
   - Configured for ports 80 and 443
   - TLS termination support

### 2. **VirtualService**
   - Routes `/api/saju/*` wildcard paths to the service
   - Implements retry logic (3 attempts)
   - 30s timeout per request

### 3. **DestinationRule**
   - LEAST_REQUEST load balancing algorithm
   - Connection pooling (max 100 connections)
   - Outlier detection for circuit breaking

### 4. **Service & Deployment**
   - ClusterIP service on port 8080
   - 2-3 replicas for high availability
   - Health checks (liveness, readiness, startup)

### 5. **Monitoring**
   - ServiceMonitor scrapes metrics every 30s
   - Prometheus stores time-series data
   - Grafana visualizes 6 dashboard panels

### 6. **CI/CD Pipeline**
   - GitHub Actions builds Docker image
   - Pushes to GitHub Container Registry
   - ArgoCD auto-syncs from Git repository
