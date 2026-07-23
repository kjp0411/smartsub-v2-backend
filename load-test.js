import http from 'k6/http';

export const options = {
  vus: 20,          // 동시 사용자 20명
  duration: '2m',   // 2분간 실행 — 순간 튀는 케이스(p95/p99)까지 잡을 수 있도록 샘플 수 확보
};

export default function () {
  const payload = JSON.stringify({
    storeId: 'a626ba11-86d7-4bea-b7b0-d7d84c898bc8',
    tableNumber: '3',
    question: '대표 메뉴가 무엇인가요?',
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  http.post('http://localhost:8080/api/v1/guide/chat', payload, params);
  // sleep 제거 — 각 VU가 쉬지 않고 바로 다음 요청을 보내 실제 순간 부하에 가깝게 재현
}