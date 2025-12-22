# 빠른 참조 가이드 (Quick Reference)

## 📋 설정 파일 요약

### Kubernetes 리소스
- `k8s/base/deployment.yaml` - 애플리케이션 배포 정의 (2 replicas)
- `k8s/base/service.yaml` - ClusterIP 서비스 (포트 8080)
- `k8s/base/configmap.yaml` - Spring Boot 설정
- `k8s/base/servicemonitor.yaml` - Prometheus 메트릭 수집
- `k8s/base/kustomization.yaml` - Kustomize 기본 설정

### Istio 설정
- `istio/gateway.yaml` - HTTP/HTTPS 게이트웨이
- `istio/virtualservice.yaml` - `/api/saju/*` 경로 라우팅 (와일드카드 지원)
- `istio/destinationrule.yaml` - 트래픽 정책 및 로드밸런싱

### 모니터링
- `grafana/dashboard-configmap.yaml` - Grafana 대시보드 (HTTP, JVM, CPU 메트릭)
- Prometheus 자동 스크래핑: `/actuator/prometheus`

### CI/CD
- `.github/workflows/build-and-push.yaml` - Docker 이미지 빌드 및 GHCR 푸시
- `.github/workflows/deploy.yaml` - ArgoCD 동기화 트리거

### ArgoCD
- `k8s/argocd-application.yaml` - GitOps 자동 배포 설정

## 🚀 배포 명령어

### 1. 전체 배포 (스크립트 사용)
```bash
./deploy.sh
```

### 2. ArgoCD로 배포
```bash
kubectl apply -f k8s/argocd-application.yaml
argocd app sync saju-interpret
```

### 3. 수동 배포
```bash
# Istio 설정
kubectl apply -f istio/

# 애플리케이션
kubectl apply -k k8s/base/

# 모니터링
kubectl apply -f k8s/base/servicemonitor.yaml
kubectl apply -f grafana/dashboard-configmap.yaml -n monitoring
```

### 4. 환경별 배포
```bash
# 개발 환경 (1 replica, 256Mi 메모리)
kubectl apply -k k8s/overlays/dev/

# 프로덕션 환경 (3 replicas, 1Gi 메모리)
kubectl apply -k k8s/overlays/prod/
```

## 🔍 확인 명령어

### Pod 상태
```bash
kubectl get pods -l app=saju-interpret
kubectl logs -l app=saju-interpret -f
kubectl describe pod -l app=saju-interpret
```

### 서비스 상태
```bash
kubectl get svc saju-interpret
kubectl get virtualservice
kubectl get gateway
```

### Istio Gateway
```bash
# Gateway IP 확인
kubectl get svc istio-ingressgateway -n istio-system

# 또는
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
echo $INGRESS_HOST
```

### 메트릭 확인
```bash
# Prometheus
kubectl port-forward -n monitoring svc/prometheus-kube-prometheus-prometheus 9090:9090

# Grafana
kubectl port-forward -n monitoring svc/prometheus-grafana 3000:80
```

## 🧪 테스트 명령어

### 로컬 포트 포워딩
```bash
kubectl port-forward svc/saju-interpret 8080:8080
curl http://localhost:8080/api/saju/health
curl http://localhost:8080/api/saju/test123
curl http://localhost:8080/api/saju/interpret/19900101
```

### Istio Gateway 통한 테스트
```bash
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')

curl http://$INGRESS_HOST/api/saju/health
curl http://$INGRESS_HOST/api/saju/test123
curl http://$INGRESS_HOST/api/saju/interpret/19900101
```

### Actuator 엔드포인트
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/prometheus
curl http://localhost:8080/actuator/metrics
```

## 🛠 문제 해결

### Pod이 Pending 상태
```bash
kubectl describe pod -l app=saju-interpret
# 리소스 부족 확인
kubectl top nodes
```

### ImagePullBackOff 오류
```bash
# deployment.yaml의 이미지 주소 수정 필요
# your-registry/saju-interpret:latest → ghcr.io/biasmj016/saju-interpret:latest
```

### Istio 사이드카 미주입
```bash
# 네임스페이스에 레이블 추가
kubectl label namespace default istio-injection=enabled

# Pod 재시작
kubectl rollout restart deployment saju-interpret
```

### Prometheus가 메트릭 수집 안함
```bash
# ServiceMonitor 확인
kubectl get servicemonitor

# Prometheus 타겟 확인 (Prometheus UI에서)
# http://localhost:9090/targets
```

## 📝 주요 설정 값

### 리소스 제한
**개발 환경:**
- CPU: 250m (요청), 1000m (제한)
- Memory: 256Mi (요청), 512Mi (제한)
- Replicas: 1

**프로덕션 환경:**
- CPU: 500m (요청), 2000m (제한)
- Memory: 1Gi (요청), 2Gi (제한)
- Replicas: 3

### 헬스체크
- Liveness Probe: `/actuator/health/liveness` (60초 지연)
- Readiness Probe: `/actuator/health/readiness` (30초 지연)
- Startup Probe: `/actuator/health/liveness` (최대 300초)

### Istio 트래픽 정책
- Load Balancer: LEAST_REQUEST
- Connection Pool: 100 connections
- Outlier Detection: 활성화
- Retry: 3회 (5xx, reset, connect-failure)
- Timeout: 30초

## 🔄 업데이트 흐름

1. 코드 변경 후 `main` 브랜치에 푸시
2. GitHub Actions가 자동으로 Docker 이미지 빌드
3. 이미지가 GHCR에 푸시됨
4. ArgoCD가 변경사항 감지
5. 자동으로 Kubernetes에 배포

## 📊 모니터링 대시보드

### Grafana 패널
1. HTTP Request Rate - 초당 요청 수
2. HTTP Request Duration - p95, p99 레이턴시
3. JVM Memory Usage - 힙/논힙 메모리
4. JVM GC Pause Time - GC 일시정지 시간
5. CPU Usage - 시스템/프로세스 CPU
6. Active Threads - 활성 스레드 수

### 주요 메트릭 쿼리
```promql
# HTTP 요청률
rate(http_server_requests_seconds_count{application="saju-interpret"}[5m])

# p95 레이턴시
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket{application="saju-interpret"}[5m]))

# JVM 메모리
jvm_memory_used_bytes{application="saju-interpret"}

# CPU 사용률
system_cpu_usage{application="saju-interpret"}
```

## 🔐 보안 체크리스트

- [ ] TLS 인증서 설정 (Istio Gateway)
- [ ] RBAC 구성
- [ ] Network Policies 적용
- [ ] Secret 관리 (Kubernetes Secrets 또는 Vault)
- [ ] 이미지 보안 스캔 (Trivy 등)
- [ ] Pod Security Standards 적용

## 📞 유용한 링크

- [Kubernetes 문서](https://kubernetes.io/docs/)
- [Istio 문서](https://istio.io/latest/docs/)
- [Prometheus 문서](https://prometheus.io/docs/)
- [ArgoCD 문서](https://argo-cd.readthedocs.io/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
