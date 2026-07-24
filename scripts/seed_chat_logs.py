import json
import time
import sys
import requests

BASE_URL = "http://localhost:8080/api/v1/guide/chat"

QUESTIONS = [
  "대표 메뉴가 무엇인가요?",
  "가격이 어떻게 되나요?",
  "메뉴 좀 추천해주세요",
  "What's on the menu?",
  "주차 가능한가요?",
  "주차장 어디예요?",
  "Where can I park?",
  "화장실 어디에 있나요?",
  "와이파이 비밀번호 알려주세요",
  "Wifi password please",
  "영업시간이 어떻게 되나요?",
  "몇 시까지 하나요?",
  "오늘 몇 시에 열어요?",
  "오늘 할인 이벤트 있나요?",
  "프로모션 진행중인가요?",
  "적립카드 있나요?",
  "사장님 오늘 기분 어떠세요?",
  "화장실 청소는 언제 하나요?",
  "여기 몇 년 됐어요?",
]

def send_question(store_id: str, question: str):
  import random
  payload = {
    "storeId": store_id,
    "tableNumber": str(random.randint(1, 10)),
    "question": question,
  }
  res = requests.post(BASE_URL, json=payload)
  print(f"[store={store_id}] Q: {question}")
  print(f"  -> {res.status_code} {res.text}")
  time.sleep(0.3)

def main():
  if len(sys.argv) < 3:
    print("사용법: python seed_chat_logs.py <storeId1> <storeId2>")
    sys.exit(1)

  store_ids = sys.argv[1:3]
  for store_id in store_ids:
    print(f"=== 매장 ({store_id}) 시드 시작 ===")
    for q in QUESTIONS:
      send_question(store_id, q)

  print("=== 시드 완료 ===")

if __name__ == "__main__":
  main()