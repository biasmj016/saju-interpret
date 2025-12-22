# Saju Interpret

Spring Boot Java 25 기반 사주 해석 애플리케이션

Kubernetes, Istio, Prometheus, Grafana, ArgoCD를 활용한 완전한 프로덕션 배포 환경

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

```bash
# 원클릭 배포 스크립트
./deploy.sh

# 또는 ArgoCD로 배포
kubectl apply -f k8s/argocd-application.yaml

# 또는 수동 배포
kubectl apply -f istio/
kubectl apply -k k8s/base/
```

## 📚 문서 가이드

프로젝트를 처음 접하시나요? 다음 순서로 문서를 읽어보세요:

1. **[README.md](README.md)** (현재 문서) - 프로젝트 개요
2. **[SUMMARY.md](SUMMARY.md)** - 전체 프로젝트 요약 및 생성된 파일 목록
3. **[ARCHITECTURE.md](ARCHITECTURE.md)** - 시스템 아키텍처 및 컴포넌트 다이어그램
4. **[CHECKLIST.md](CHECKLIST.md)** - 배포 전 필수 체크리스트
5. **[K8S_SETUP.md](K8S_SETUP.md)** - 상세 Kubernetes 설정 가이드
6. **[QUICKREF.md](QUICKREF.md)** - 빠른 명령어 참조

## 📊 포함된 기능

### 애플리케이션
- ✅ Spring Boot 3.2.1 + Java 25
- ✅ REST API 엔드포인트 (/api/saju/*)
- ✅ Spring Boot Actuator (Health, Metrics)

### Kubernetes
- ✅ Production-ready Deployment (replicas, health checks)
- ✅ Service (ClusterIP)
- ✅ ConfigMap (애플리케이션 설정)
- ✅ Kustomize (dev/prod 환경 분리)

### Istio 서비스 메시
- ✅ Gateway (HTTP/HTTPS)
- ✅ VirtualService (/api/saju/* 와일드카드 라우팅)
- ✅ DestinationRule (로드밸런싱, 재시도, 서킷브레이커)

### 모니터링
- ✅ Prometheus ServiceMonitor (메트릭 수집)
- ✅ Grafana Dashboard (6개 패널)
- ✅ HTTP, JVM, CPU, GC 메트릭

### CI/CD
- ✅ GitHub Actions (Docker 빌드 및 푸시)
- ✅ ArgoCD (GitOps 자동 배포)
- ✅ 멀티 플랫폼 이미지 (amd64, arm64)

## 📁 프로젝트 구조

```
.
├── k8s/                          # Kubernetes 매니페스트
│   ├── base/                     # 기본 설정
│   ├── overlays/dev/             # 개발 환경
│   ├── overlays/prod/            # 프로덕션 환경
│   └── argocd-application.yaml   # ArgoCD 앱
├── istio/                        # Istio 설정
│   ├── gateway.yaml
│   ├── virtualservice.yaml
│   └── destinationrule.yaml
├── grafana/                      # Grafana 대시보드
├── .github/workflows/            # CI/CD 파이프라인
├── src/                          # Spring Boot 소스 코드
├── Dockerfile                    # 컨테이너 이미지
└── deploy.sh                     # 배포 스크립트
```

## 🔍 API 엔드포인트

### 애플리케이션 API (Istio Gateway를 통해 접근)
- `GET /api/saju/health` - 서비스 헬스 체크
- `GET /api/saju/{id}` - 사주 조회
- `GET /api/saju/interpret/{birthDate}` - 생년월일로 사주 해석

### Actuator API (내부 접근)
- `GET /actuator/health` - Spring Boot Actuator 헬스
- `GET /actuator/prometheus` - Prometheus 메트릭
- `GET /actuator/metrics` - 메트릭 목록

## 📋 배포 요구사항

- Kubernetes 클러스터 (v1.24+)
- Istio 설치 완료
- Prometheus Operator 설치 완료
- ArgoCD 설치 완료 (GitOps 사용시)

## 🎯 다음 단계

1. [CHECKLIST.md](CHECKLIST.md)에서 배포 전 필수 설정 확인
2. Docker 이미지 레지스트리 주소 수정 (k8s/base/deployment.yaml)
3. 배포 실행 (`./deploy.sh`)
4. 모니터링 대시보드 확인

## 💡 빠른 테스트

```bash
# 포트 포워딩으로 로컬 테스트
kubectl port-forward svc/saju-interpret 8080:8080
curl http://localhost:8080/api/saju/health

# Istio Gateway를 통한 테스트
export INGRESS_HOST=$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl http://$INGRESS_HOST/api/saju/health
```

## 📖 상세 문서

| 문서 | 설명 |
|------|------|
| [SUMMARY.md](SUMMARY.md) | 프로젝트 전체 요약 |
| [ARCHITECTURE.md](ARCHITECTURE.md) | 시스템 아키텍처 다이어그램 |
| [K8S_SETUP.md](K8S_SETUP.md) | 상세 Kubernetes 설정 가이드 |
| [QUICKREF.md](QUICKREF.md) | 빠른 명령어 참조 |
| [CHECKLIST.md](CHECKLIST.md) | 배포 전 체크리스트 |

## 🤝 기여

이슈 및 Pull Request를 환영합니다!

## 📄 라이선스

MIT License
