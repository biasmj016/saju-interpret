# Saju Interpret - Kubernetes 배포 설정

Spring Boot Java 25 기반의 사주 해석 애플리케이션을 위한 완전한 Kubernetes 배포 설정입니다.

## 📋 목차

- [개요](#개요)
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [사전 요구사항](#사전-요구사항)
- [설정 가이드](#설정-가이드)
- [배포 방법](#배포-방법)
- [모니터링](#모니터링)
- [Istio 설정](#istio-설정)
- [ArgoCD 통합](#argocd-통합)
- [GitHub Actions CI/CD](#github-actions-cicd)

## 개요

이 프로젝트는 다음을 포함하는 프로덕션 준비된 Kubernetes 배포 설정을 제공합니다:

- ✅ Spring Boot Java 25 애플리케이션
- ✅ Prometheus 메트릭 수집
- ✅ Grafana 대시보드
- ✅ Istio 서비스 메시 (`/api/saju/*` 와일드카드 경로)
- ✅ ArgoCD GitOps 배포
- ✅ GitHub Actions CI/CD 파이프라인

## 기술 스택

- **애플리케이션**: Spring Boot 3.2.1, Java 25
- **컨테이너**: Docker, Docker Buildx
- **오케스트레이션**: Kubernetes
- **서비스 메시**: Istio
- **모니터링**: Prometheus, Grafana
- **GitOps**: ArgoCD
- **CI/CD**: GitHub Actions
- **빌드 도구**: Gradle

## 프로젝트 구조

```
.
├── .github/
│   └── workflows/
│       ├── build-and-push.yaml    # Docker 이미지 빌드 및 푸시
│       └── deploy.yaml             # Kubernetes 배포
├── k8s/
│   ├── base/                       # 기본 Kubernetes 매니페스트
│   │   ├── deployment.yaml         # 애플리케이션 배포
│   │   ├── service.yaml            # 서비스 정의
│   │   ├── configmap.yaml          # 설정 맵
│   │   ├── servicemonitor.yaml     # Prometheus 모니터링
│   │   └── kustomization.yaml      # Kustomize 설정
│   ├── overlays/
│   │   ├── dev/                    # 개발 환경 오버레이
│   │   └── prod/                   # 프로덕션 환경 오버레이
│   └── argocd-application.yaml     # ArgoCD 애플리케이션
├── istio/
│   ├── gateway.yaml                # Istio 게이트웨이
│   ├── virtualservice.yaml         # 가상 서비스 (/api/saju/*)
│   └── destinationrule.yaml        # 트래픽 정책
├── grafana/
│   └── dashboard-configmap.yaml    # Grafana 대시보드
├── src/                            # Spring Boot 소스 코드
├── Dockerfile                      # 멀티 스테이지 Docker 빌드
└── build.gradle                    # Gradle 빌드 설정
```

## 사전 요구사항

### 필수 도구

- Kubernetes 클러스터 (v1.24+)
- kubectl CLI
- Helm 3
- Docker
- Git

### 선택 설치

- ArgoCD CLI
- Istio CLI (istioctl)

## 설정 가이드

### 1. Istio 설치

```bash
# Istio 다운로드 및 설치
curl -L https://istio.io/downloadIstio | sh -
cd istio-*
export PATH=$PWD/bin:$PATH

# Istio를 클러스터에 설치
istioctl install --set profile=default -y

# 네임스페이스에 Istio 사이드카 인젝션 활성화
kubectl label namespace default istio-injection=enabled
```

### 2. Prometheus 설치

```bash
# Prometheus Operator 설치
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set prometheus.prometheusSpec.serviceMonitorSelectorNilUsesHelmValues=false
```

### 3. Grafana 설정

Prometheus와 함께 설치된 Grafana에 접속:

```bash
# Grafana 비밀번호 확인
kubectl get secret --namespace monitoring prometheus-grafana -o jsonpath="{.data.admin-password}" | base64 --decode

# Grafana 포트 포워딩
kubectl port-forward --namespace monitoring svc/prometheus-grafana 3000:80
```

브라우저에서 `http://localhost:3000` 접속 (admin / 위에서 확인한 비밀번호)

대시보드 적용:
```bash
kubectl apply -f grafana/dashboard-configmap.yaml
```

### 4. ArgoCD 설치

```bash
# ArgoCD 설치
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# ArgoCD 초기 비밀번호 확인
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d

# ArgoCD 서버 포트 포워딩
kubectl port-forward svc/argocd-server -n argocd 8080:443
```

## 배포 방법

### 방법 1: kubectl 직접 배포

```bash
# 기본 리소스 배포
kubectl apply -k k8s/base/

# Istio 설정 배포
kubectl apply -f istio/

# ServiceMonitor 배포 (Prometheus)
kubectl apply -f k8s/base/servicemonitor.yaml
```

### 방법 2: ArgoCD GitOps 배포 (권장)

```bash
# ArgoCD 애플리케이션 생성
kubectl apply -f k8s/argocd-application.yaml

# 애플리케이션 동기화 확인
argocd app get saju-interpret

# 수동 동기화 (필요시)
argocd app sync saju-interpret
```

### 환경별 배포

**개발 환경:**
```bash
kubectl apply -k k8s/overlays/dev/
```

**프로덕션 환경:**
```bash
kubectl apply -k k8s/overlays/prod/
```

## GitHub Actions CI/CD

### 설정 방법

1. **GitHub Container Registry 사용 설정**
   - GitHub 저장소 → Settings → Actions → General
   - Workflow permissions → Read and write permissions 선택

2. **자동 빌드 및 배포**
   - `main` 브랜치에 푸시하면 자동으로 Docker 이미지 빌드
   - 이미지는 `ghcr.io/biasmj016/saju-interpret`로 푸시
   - ArgoCD가 자동으로 새 이미지 감지 및 배포

3. **수동 워크플로우 실행**
   ```bash
   # GitHub UI에서 Actions 탭 → Build and Push Docker Image → Run workflow
   ```

### 워크플로우

1. **build-and-push.yaml**: Docker 이미지 빌드 및 푸시
2. **deploy.yaml**: ArgoCD 동기화 트리거

## 모니터링

### Prometheus 메트릭 확인

```bash
# Prometheus UI 접속
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090
```

브라우저: `http://localhost:9090`

주요 메트릭:
- `http_server_requests_seconds_count`: HTTP 요청 수
- `jvm_memory_used_bytes`: JVM 메모리 사용량
- `system_cpu_usage`: CPU 사용률

### Grafana 대시보드

포함된 패널:
- HTTP 요청 비율
- HTTP 요청 지연시간 (p95, p99)
- JVM 메모리 사용량
- GC 일시정지 시간
- CPU 사용률
- 활성 스레드 수

## Istio 설정

### 게이트웨이 및 가상 서비스

애플리케이션은 Istio Gateway를 통해 노출됩니다:

```bash
# Istio Ingress Gateway IP 확인
kubectl get svc istio-ingressgateway -n istio-system
```

### 경로 설정

`/api/saju/*` 와일드카드 경로가 자동으로 `saju-interpret` 서비스로 라우팅됩니다.

**테스트:**
```bash
# Istio Gateway를 통한 요청
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

curl http://$INGRESS_HOST/api/saju/health
curl http://$INGRESS_HOST/api/saju/test123
curl http://$INGRESS_HOST/api/saju/interpret/19900101
```

### 트래픽 정책

DestinationRule에 포함된 설정:
- Connection pooling
- Load balancing (LEAST_REQUEST)
- Outlier detection
- Circuit breaking

## API 엔드포인트

- `GET /api/saju/health` - 헬스 체크
- `GET /api/saju/{id}` - ID로 사주 조회
- `GET /api/saju/interpret/{birthDate}` - 생년월일로 사주 해석
- `GET /actuator/health` - Spring Boot 헬스 체크
- `GET /actuator/prometheus` - Prometheus 메트릭

## 문제 해결

### Pod이 시작되지 않는 경우

```bash
# Pod 상태 확인
kubectl get pods -l app=saju-interpret

# Pod 로그 확인
kubectl logs -l app=saju-interpret --tail=100

# Pod 상세 정보
kubectl describe pod -l app=saju-interpret
```

### Istio 사이드카 문제

```bash
# 사이드카 주입 확인
kubectl get pod -l app=saju-interpret -o jsonpath='{.items[0].spec.containers[*].name}'

# 네임스페이스 레이블 확인
kubectl get namespace -L istio-injection
```

### Prometheus가 메트릭을 수집하지 않는 경우

```bash
# ServiceMonitor 확인
kubectl get servicemonitor

# Prometheus 타겟 확인 (Prometheus UI에서)
# Status → Targets → saju-interpret
```

## 보안 고려사항

1. **TLS 설정**: Istio Gateway에서 TLS 인증서 설정 필요
2. **이미지 스캔**: GitHub Actions에서 보안 취약점 스캔 추가 권장
3. **Secret 관리**: Kubernetes Secrets 또는 외부 Secret Manager 사용
4. **RBAC**: 적절한 역할 기반 접근 제어 설정

## 리소스 요구사항

### 개발 환경
- CPU: 250m (requests), 1000m (limits)
- Memory: 256Mi (requests), 512Mi (limits)
- Replicas: 1

### 프로덕션 환경
- CPU: 500m (requests), 2000m (limits)
- Memory: 1Gi (requests), 2Gi (limits)
- Replicas: 3

## 라이선스

MIT License

## 기여

이슈 및 PR을 환영합니다!

## 연락처

프로젝트 관리자: biasmj016
