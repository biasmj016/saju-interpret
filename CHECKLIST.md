# 배포 전 체크리스트

이 파일은 실제 배포하기 전에 수정해야 할 설정들을 안내합니다.

## ✅ 필수 수정 사항

### 1. Docker 이미지 레지스트리 설정

**파일: `k8s/base/deployment.yaml`**
```yaml
# 현재 (수정 필요)
image: your-registry/saju-interpret:latest

# 변경 예시
image: ghcr.io/biasmj016/saju-interpret:latest
```

**파일: `.github/workflows/build-and-push.yaml`**
- 현재 설정은 GitHub Container Registry (GHCR) 사용
- 다른 레지스트리 사용시 수정 필요:
  - Docker Hub: `docker.io/biasmj016/saju-interpret`
  - AWS ECR: `{account-id}.dkr.ecr.{region}.amazonaws.com/saju-interpret`
  - GCR: `gcr.io/{project-id}/saju-interpret`

### 2. Istio Gateway 호스트 설정 (선택사항)

**파일: `istio/gateway.yaml`**
```yaml
# 현재 (모든 호스트 허용)
hosts:
- "*"

# 프로덕션 권장 (도메인 지정)
hosts:
- "saju.yourdomain.com"
```

**파일: `istio/virtualservice.yaml`**
```yaml
# 현재
hosts:
- "*"

# 도메인 사용시
hosts:
- "saju.yourdomain.com"
```

### 3. TLS 인증서 설정 (HTTPS 사용시)

**파일: `istio/gateway.yaml`**
```yaml
# TLS 인증서 Secret 생성 필요
credentialName: saju-interpret-tls
```

인증서 생성 방법:
```bash
# Self-signed 인증서 (테스트용)
kubectl create secret tls saju-interpret-tls \
  --key=tls.key \
  --cert=tls.crt

# Let's Encrypt 사용시 cert-manager 설치 권장
```

### 4. ArgoCD Repository URL 확인

**파일: `k8s/argocd-application.yaml`**
```yaml
# 현재 설정 확인
repoURL: https://github.com/biasmj016/saju-interpret.git

# Private 저장소인 경우 ArgoCD에 SSH 키 또는 토큰 등록 필요
```

### 5. 네임스페이스 설정

현재 기본 설정:
- 애플리케이션: `default` 네임스페이스
- 모니터링: `monitoring` 네임스페이스
- ArgoCD: `argocd` 네임스페이스
- Istio: `istio-system` 네임스페이스

다른 네임스페이스 사용시 다음 파일들 수정:
- `k8s/overlays/dev/kustomization.yaml`
- `k8s/overlays/prod/kustomization.yaml`
- `k8s/argocd-application.yaml`

## 🔧 선택 수정 사항

### 1. 리소스 제한 조정

**파일: `k8s/base/deployment.yaml`**

현재 설정:
```yaml
resources:
  requests:
    memory: "512Mi"
    cpu: "250m"
  limits:
    memory: "1Gi"
    cpu: "1000m"
```

클러스터 리소스에 따라 조정하세요.

### 2. Replica 수 조정

**개발 환경: `k8s/overlays/dev/kustomization.yaml`**
```yaml
replicas:
  - name: saju-interpret
    count: 1  # 필요시 조정
```

**프로덕션 환경: `k8s/overlays/prod/kustomization.yaml`**
```yaml
replicas:
  - name: saju-interpret
    count: 3  # 필요시 조정 (고가용성을 위해 최소 2개 권장)
```

### 3. 애플리케이션 설정

**파일: `k8s/base/configmap.yaml`**

Spring Boot 설정 커스터마이징:
- 로그 레벨
- 데이터베이스 연결
- 외부 서비스 URL
- 기타 애플리케이션 설정

### 4. Istio 트래픽 정책

**파일: `istio/destinationrule.yaml`**

필요시 조정:
- Connection Pool 크기
- Timeout 값
- Retry 정책
- Circuit Breaker 설정

## 📋 배포 전 체크리스트

- [ ] Docker 이미지 레지스트리 주소 수정
- [ ] GitHub Actions에서 GHCR 접근 권한 확인
- [ ] Kubernetes 클러스터 연결 확인
- [ ] Istio 설치 완료
- [ ] Prometheus Operator 설치 완료
- [ ] Grafana 설치 완료
- [ ] ArgoCD 설치 완료 (GitOps 사용시)
- [ ] 네임스페이스 생성
- [ ] TLS 인증서 준비 (HTTPS 사용시)
- [ ] 도메인 DNS 설정 (도메인 사용시)
- [ ] 리소스 제한 값 확인
- [ ] 환경 변수 및 Secret 설정

## 🚀 배포 순서

1. **사전 준비**
   ```bash
   # Istio 설치
   istioctl install --set profile=default -y
   kubectl label namespace default istio-injection=enabled
   
   # Prometheus 설치
   helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
   
   # ArgoCD 설치 (GitOps 사용시)
   kubectl create namespace argocd
   kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
   ```

2. **설정 파일 수정**
   - 위의 체크리스트에 따라 필요한 파일들 수정

3. **배포 실행**
   ```bash
   # 방법 1: 스크립트 사용
   ./deploy.sh
   
   # 방법 2: ArgoCD 사용
   kubectl apply -f k8s/argocd-application.yaml
   
   # 방법 3: 수동 배포
   kubectl apply -f istio/
   kubectl apply -k k8s/base/
   ```

4. **배포 확인**
   ```bash
   kubectl get pods -l app=saju-interpret
   kubectl get svc saju-interpret
   kubectl get virtualservice
   ```

5. **테스트**
   ```bash
   # 포트 포워딩으로 로컬 테스트
   kubectl port-forward svc/saju-interpret 8080:8080
   curl http://localhost:8080/api/saju/health
   ```

## 💡 팁

1. **로컬 개발 환경**
   - Minikube 또는 Kind로 로컬 클러스터 생성
   - `k8s/overlays/dev` 사용

2. **프로덕션 배포**
   - 항상 `k8s/overlays/prod` 사용
   - TLS 필수 설정
   - 리소스 모니터링 활성화

3. **GitOps 워크플로우**
   - 모든 변경사항은 Git에 커밋
   - ArgoCD가 자동으로 동기화
   - 수동 배포 최소화

4. **보안**
   - Secret은 절대 Git에 커밋하지 않기
   - Kubernetes Secrets 또는 외부 Secret Manager 사용
   - RBAC 적용

## ❓ 도움이 필요하신가요?

- [K8S_SETUP.md](K8S_SETUP.md) - 상세 설정 가이드
- [QUICKREF.md](QUICKREF.md) - 빠른 참조 가이드
- [README.md](README.md) - 프로젝트 개요
