# Saju Interpret

Spring Boot Java 25 기반 사주 해석 애플리케이션

## 🚀 빠른 시작

### 로컬 개발

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트
./gradlew test
```

### Kubernetes 배포

전체 K8s 배포 가이드는 [K8S_SETUP.md](K8S_SETUP.md)를 참조하세요.

```bash
# ArgoCD로 배포
kubectl apply -f k8s/argocd-application.yaml

# 또는 직접 배포
kubectl apply -k k8s/base/
kubectl apply -f istio/
```

## 📊 포함된 기능

- ✅ Spring Boot 3.2.1 + Java 25
- ✅ Kubernetes 배포 설정 (Deployment, Service, ConfigMap)
- ✅ Prometheus 메트릭 수집
- ✅ Grafana 대시보드
- ✅ Istio 서비스 메시 (/api/saju/* 와일드카드)
- ✅ ArgoCD GitOps
- ✅ GitHub Actions CI/CD
- ✅ 멀티 스테이지 Docker 빌드
- ✅ Health checks & Probes

## 📁 주요 파일

- `k8s/` - Kubernetes 매니페스트
- `istio/` - Istio 설정 (Gateway, VirtualService, DestinationRule)
- `grafana/` - Grafana 대시보드
- `.github/workflows/` - CI/CD 파이프라인
- `Dockerfile` - 컨테이너 이미지 빌드

## 🔗 참고 문서

- [Kubernetes 설정 가이드](K8S_SETUP.md)
- [API 문서](#api-엔드포인트)

## API 엔드포인트

- `GET /api/saju/health` - 서비스 헬스 체크
- `GET /api/saju/{id}` - 사주 조회
- `GET /api/saju/interpret/{birthDate}` - 사주 해석
- `GET /actuator/health` - Spring Boot Actuator 헬스
- `GET /actuator/prometheus` - Prometheus 메트릭
