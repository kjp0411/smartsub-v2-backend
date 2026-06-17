import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 20,          // 동시 사용자 20명
  duration: '30s',  // 30초간 실행
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
  sleep(1);
}