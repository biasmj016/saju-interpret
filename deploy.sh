#!/bin/bash

# Saju Interpret - Quick Start Script
# Kubernetes 클러스터에 애플리케이션을 배포하는 스크립트

set -e

echo "🚀 Saju Interpret Kubernetes 배포 시작..."

# 색상 정의
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. Namespace 확인
echo -e "${BLUE}📦 Namespace 확인 중...${NC}"
kubectl get namespace default || kubectl create namespace default

# 2. Istio 설정 배포
echo -e "${BLUE}🌐 Istio 설정 배포 중...${NC}"
kubectl apply -f istio/gateway.yaml
kubectl apply -f istio/virtualservice.yaml
kubectl apply -f istio/destinationrule.yaml
echo -e "${GREEN}✅ Istio 설정 완료${NC}"

# 3. 애플리케이션 배포
echo -e "${BLUE}🎯 애플리케이션 배포 중...${NC}"
kubectl apply -k k8s/base/
echo -e "${GREEN}✅ 애플리케이션 배포 완료${NC}"

# 4. ServiceMonitor 배포 (Prometheus)
echo -e "${BLUE}📊 Prometheus ServiceMonitor 배포 중...${NC}"
kubectl apply -f k8s/base/servicemonitor.yaml || echo -e "${YELLOW}⚠️  ServiceMonitor는 Prometheus Operator가 필요합니다${NC}"

# 5. Grafana Dashboard 배포
echo -e "${BLUE}📈 Grafana Dashboard 배포 중...${NC}"
kubectl apply -f grafana/dashboard-configmap.yaml -n monitoring || echo -e "${YELLOW}⚠️  monitoring namespace가 없으면 건너뜁니다${NC}"

# 6. 배포 상태 확인
echo -e "${BLUE}🔍 배포 상태 확인 중...${NC}"
sleep 5
kubectl rollout status deployment/saju-interpret --timeout=300s

# 7. Pod 상태 출력
echo -e "${BLUE}📋 Pod 목록:${NC}"
kubectl get pods -l app=saju-interpret

# 8. Service 정보 출력
echo -e "${BLUE}🔗 Service 정보:${NC}"
kubectl get svc saju-interpret

# 9. Istio Gateway 정보
echo -e "${BLUE}🌐 Istio Gateway 정보:${NC}"
kubectl get svc istio-ingressgateway -n istio-system || echo -e "${YELLOW}⚠️  Istio가 설치되지 않았습니다${NC}"

echo ""
echo -e "${GREEN}✨ 배포 완료!${NC}"
echo ""
echo "다음 명령어로 애플리케이션을 테스트할 수 있습니다:"
echo ""
echo "# 포트 포워딩으로 로컬 테스트:"
echo "kubectl port-forward svc/saju-interpret 8080:8080"
echo "curl http://localhost:8080/api/saju/health"
echo ""
echo "# Istio Gateway를 통한 테스트:"
echo "export INGRESS_HOST=\$(kubectl -n istio-system get service istio-ingressgateway -o jsonpath='{.status.loadBalancer.ingress[0].ip}')"
echo "curl http://\$INGRESS_HOST/api/saju/health"
echo ""
echo "자세한 내용은 K8S_SETUP.md를 참조하세요."
