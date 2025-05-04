<img src="https://github.com/user-attachments/assets/749d94e5-cbe6-4850-a6de-20bc5a9f0ca6" width="100%"/>

<br/>

## 목차

- [1. 프로젝트 소개](#1-프로젝트-소개)
- [2. 주요 기능](#2-주요-기능)
- [3. 기술 스택](#3-기술-스택)
- [4. 기술적 의사 결정 과정](#4-기술적-의사-결정-과정)
- [5. 트러블 슈팅 / 성능 개선](#5-트러블-슈팅--성능-개선)
- [6. 아키텍처](#6-인프라-아키텍처--적용-기술)
- [7. ERD / API 문서](#7-ERD--API-문서) 
- [8. 성과 및 회고](#8-성과-및-회고)
- [9. 팀원 소개](#9-팀원-소개)

<br/>

## 1. 프로젝트 소개

**옥장판은 중고 거래에 경매의 짜릿함을 더한 새로운 방식의 중고 옥션입니다.**

<br/>

> 
> 원하는 물건을 단순히 **사는 것**에서 벗어나, 경쟁을 통해 더 합리적인 가격에 구매할 수 있습니다.
> 
> 또한, 상품을 찜하거나 둘러본 기록을 바탕으로 개인화된 추천 시스템을 제공합니다.
> 
> 사용자는 관심 있는 물건을 더 쉽고 빠르게 찾을 수 있습니다.
>
> 중고 거래에서도 야수의 심장을 경험할 수 있는 공간,
> 
> 공정하고 투명한 시스템에서 안심하고 중고 거래를 즐길 수 있는 공간.
> 
> **“ 옥장판 ” 으로 당신을 초대합니다**


<br/>


## 2. 주요 기능


<details>
    <summary><b>[🔒 회원가입/로그인]</b> JWT, Spring Security 및 OAuth2.0을 이용한 유저 인증/인가</summary>
</details>

<details>
    <summary><b>[💰 결제/정산]</b> 토스 페이먼츠를 이용한 결제 기능과 스케줄러를 이용한 자동 정산 기능</summary>
</details>

<details>
    <summary><b>[💬 판매자 채팅]</b> </summary>
</details>

<details>
  <summary><b>[📦 상품 이미지 관리]</b> S3 + CloudFront를 이용한 최적화된 이미지 관리</summary>
</details>

<details>
  <summary><b>[👍🏻 경매 추천]</b> Redis SortedSet을 통한 경매 Rule-based 추천 시스템</summary>
</details>

<details>
    <summary><b>[📈 상품 가격 예측]</b> Chat GPT + Naver OpenAPI를 결합한 경매 상품 시세 예측</summary>
</details>

<details>
    <summary><b>[🏁 입찰 / 즉시 구매]</b> JWT, Spring Security 및 OAuth2.0을 이용한 유저 인증/인가 기능</summary>
</details>

<details>
    <summary><b>[⏰ 경매 종료 알림]</b> </summary>
</details>

<br/>

## 3. 기술 스택

<img width="70%" src="https://github.com/user-attachments/assets/3aac0ff3-c603-4c0f-8f13-44465946bb34" />

<br/><br/>

## 4. 기술적 의사 결정 과정

<details>
  <summary>
    <a href="https://www.notion.so/1e70b71c146780bebf28e31cf5d182e9?pvs=4">🔑 큐 기반 웹훅 재시도 로직을 채택한 이유</a>
  </summary>

### 문제 상황

- 기존 웹훅은 동기 처리 방식으로 외부 장애나 지연 발생 시 데이터 유실 및 결제 상태 불일치 문제가 발생함.

### 원인

- 외부 결제사 호출 실패 시 재처리 불가
- 웹훅 처리 로직이 주요 서비스 흐름에 포함되어 전체 서비스에 영향

### 해결 전략

- 큐 기반 비동기 처리로 웹훅을 처리하여, 장애 발생 시에도 자동 재시도 가능
- 웹훅 로직을 별도 워커로 분리해 서비스 부하 분산
- 운영자 개입 없이 재처리 가능하여 운영 효율성 향상

### 도입 효과

- 결제 데이터의 정확성 및 신뢰성 확보
- 장애 대응력과 서비스 확장성 개선
- 운영 리스크와 수동 개입 최소화

<br/>

</details>


<details>
  <summary>
    <a href="https://www.notion.so/1e70b71c1467803cb906d03f6a3fc9f9?pvs=4">🔑 실시간 채팅 기능 개선을 위한 메시징 시스템 도입 결정 과정</a>
  </summary>

### 배경

- 채팅 메시지 전송 시 사용자 응답 속도가 느리고 실시간성이 떨어짐.
- 서버 확장 시 채팅 메시지가 일부 인스턴스에서만 처리되어 메시지 누락 문제가 발생

위의 문제들을 해결하기 위해 메시지 브로커 도입을 검토 하였습니다.

<br/>

### 기술 선택

**Kafka, RabbitMQ, Redis sub/sub중에서 Redis pub/sub 선택**

- Kafka나 RabbitMQ처럼 별도의 인프라를 구축하고 운영하는 부담 없이, 기존 Redis 설정에 약간의 코드만 추가하여 Pub/Sub 기능을 즉시 활용할 수 있었습니다. 이는 개발 시간과 비용을 절약하는 이점이었습니다.
- Redis는 In-Memory 기반으로 동작하므로 메시지 발행 속도가 매우 빠릅니다. 이는 기존의 동기 방식에서 발생했던 **클라이언트 메시지 전송 지연 문제를 직접적으로 해결**할 수 있었습니다.
- Kafka나 RabbitMQ에 비해 API가 훨씬 간단하여 학습 곡선이 낮고 빠르게 기능을 구현할 수 있었습니다.
- 향후 **독립적인 마이크로서비스로 분리하기 용이한 구조**였습니다.  추후 서비스 규모 확장 및 MSA 전환을 고려할 때 유연성을 제공하는 장점이었습니다

<br/>

### 결과

- 메시지 발행 즉시 클라이언트에 응답하여 체감**전송 지연 시간이  감소**
- 메시지 처리 부하가 분산, **수평 확장 기반**을 마련
- DB 장애가 발생하더라도 메시지 발행 및 클라이언트 응답은 정상적으로 이루어지도록 **장애 격리**

당면한 문제와 가용 리소스,확장 가능성을 고려하여 Redis Pub/Sub를 선택한 하였습니다. 복잡하고 강력한 기술만이 항상 정답은 아니며, **현재 상황에 가장 적합한 기술을 선택하는 것의 중요성**을 다시 한번 확인하는 계기가 되었습니다

<br/>

</details>

<details>
  <summary>
    <a href=https://www.notion.so/1e70b71c146780b3a839c0056b4f7ad6?pvs=4">🔑 유저 도메인 의사결정 과정 정리</a>
  </summary>

### 문제 상황

- 자체 로그인 구현 시 **비밀번호 관리, 보안 정책 수립 등 복잡성 증가.**
- 서버 저장소 기반 세션 인증은 **확장성 제한 및 Redis 의존도 증가.**

### 선택과 이유

- **Google OAuth**로 로그인 간편화 및 **자격 증명 보안 책임 분산.**
- **JWT + Spring Security** 방식으로 상태 비저장 인증 구현 → 서버 부하 감소, 확장성 확보.
- **Refresh Token Rotation(RTR)** 전략 도입 → 리프레시 토큰 유출 대비 및 보안 강화.

### 도입 효과

- 비밀번호 없는 간편하고 안전한 로그인 제공.
- JWT 기반의 인증 구조로 **확장성, 유지보수성, 성능 향상.**
- RTR 적용으로 **토큰 탈취 대응 및 보안 수준 향상.**

<br/>

</details>


<details>
  <summary>
    <a href="https://www.notion.so/SQS-EDA-1e70b71c146780c4b1e2f69ed8abe86a?pvs=4">🔑 빠른 입찰 응답을 위해 SQS를 활용한 EDA를 적용한 이유</a>
  </summary>

### **📌  요구사항**

1. 사용자가 **현재가를 신뢰**할 수 있어야 한다.
2. 입찰 요청 이후 **1초 이내 피드백**이 제공되어야 한다.
   - 단순 저장 외에도 경매 시간 연장 등 시스템 작업 포함됨.   
4. **Monolith 구조**로 인해 다른 기능에 영향을 주면 안 된다.
5. **서버 관리 인력이 부족**하므로, 관리 비용이 낮아야 한다.

</br>

### **🔍  결정 및 이유**

1. 요청 응답 구조: WebSocket 대신 EDA + HttpResponse

	- 실시간성이 필요한 시점은 경매 기간 중 약 **0.5% 수준**.
	- WebSocket은 상시 연결 유지 비용이 커서 **비효율적**.
	- **Redis 기반 현재가 API 제공**으로 실시간성 보완 가능.
	- **사용자 요청과 시스템 작업을 분리**해 빠른 응답 가능.

2. 이벤트 처리: Amazon SQS 기반 EDA

	- **이벤트 큐를 외부(SQS)에 위임**해 시스템 영향 최소화.
	- **별도 서버 없이 구성 가능**, 사용량 기반 과금으로 효율적.
	- AWS가 리소스 관리 → **낮은 운영/유지보수 비용**.

</br>

### **✅ 결론**

- **현재가 신뢰도 확보**: Redis + API 조합으로 실시간에 준하는 갱신.
- **빠른 피드백 제공**: 성공 여부만 빠르게 응답, 후속 작업은 비동기.
- **시스템 영향도 감소**: 요청 처리와 시스템 작업을 분리.
- **낮은 관리 비용**: AWS SQS를 통한 외부 시스템 활용으로 단순화.

</br>

### 🏃 한계 및 개선방안

- **SQS FIFO 제한**: 초당 API 요청 수 300개 제한 (Send/Receive/Delete).
- **현재 상황**: 메시지를 10개씩 읽는 방식으로 처리 중이며, TPS 1.29K에서도 스로틀링은 없음.
- **개선 방안**: 추후 스로틀링 발생 시 Batch API 활용 예정.
    - 예: SendMessageBatch, DeleteMessageBatch 적용
    - 단, **로직 복잡도 증가** 우려 있음.

</br>
 
</details>

<details>
  <summary>
    <a href="https://www.notion.so/EventBridge-Scheduler-1e70b71c146780a68acdf52069735353?pvs=4">🔑 경매 마감 처리와 알림을 위해 EventBridge Scheduler를 적용한 이유</a>
  </summary>

### **📌 요구사항**

1. **경매 마감 대상 식별**이 정확하게 이루어져야 한다.
    - 마감 시점에 서버 부하가 집중되지 않아야 함.
      
2. **마감 처리 및 알림 발송의 부하를 최소화**해야 한다.
    - 알림 대상자 식별이 효율적이어야 하며,
    - 다수의 알림 생성이 전체 시스템에 부담이 되면 안 됨.
    
3. **안정적인 관리 필요**
    - 서버 재시작/에러에도 영향 받지 않아야 함.
    - 알림 취소 등도 유연하게 처리할 수 있어야 함.
  
<br/>

### **🔍 결정 및 이유**

1. EventBridge Scheduler 활용

	- **경매 종료 시각에 맞춘 이벤트 예약**으로 부하 분산.
	- SQS를 통해 메시지를 Polling하며, **메시지 수 제한(10개)**으로 폭주 방지.
	- **서버 상태와 무관한 외부 스케줄러**이므로 안정성 확보.

2. 알림 처리 방식 개선
	
	- **입찰 이력은 Redis에서 조회**해 DB 접근 최소화.
	    - Bid 테이블을 직접 조회하지 않아 Insert 병목 방지.
	- **JdbcTemplate BulkInsert**를 사용해
	    - 알림 데이터(Inbox)를 **한 번에 DB에 효율적으로 적재**.
 
<br/>

### **✅ 결론**

1. **정확한 마감 대상 식별**
    - EventBridge를 통해 종료 시간 기준으로 이벤트 등록 성공.
    - **마감 처리 및 알림 부하 분산** 효과 확인.
      
2. **효율적인 알림 처리**
    - Redis + BulkInsert 조합으로 **DB I/O 및 지연 시간 최소화**.
      
3. **안정성 있는 관리 구조 확보**
    - API 서버 외부에서 마감 및 알림 처리 → **서버 상태와 분리된 안정성** 확보.
    - **알림 취소 이벤트도 EventBridge에 기억됨** → 메모리 부담 감소.

<br/>

### **🏃 한계 및 개선방안**

1. **동시 경매 수 증가 시 Redis 조회 부하 우려**
    - 장기적으로는:
        - Redis에는 **상위 입찰 기록만 유지**,
        - 조회 전용 **DB Replica 활용** 검토.
     
2. **알림 양 증가 시 서버 부담 증가**
    - **Batch 단위 처리** 도입 필요.
    - *서버리스 기술 (예: AWS Lambda)**를 통해
        - 알림 서버를 API 서버와 **완전히 분리**하는 방안 고려.

 <br/>
 
</details>


<details>
  <summary>
    <a href="https://www.notion.so/1e70b71c14678064a10fdd7a168ad1f1?pvs=4">🔑 시세 예측에 GPT, 네이버 쇼핑 API, Redis를 함께 활용한 이유</a>
  </summary>

### 문제점

- **GPT만으로 예측** 시 매 요청마다 API 호출 → **비용 과다**, **응답 속도 느림**
- 중복 상품 요청 시 매번 **GPT호출** 발생
- 시세에 대한 정확도 부족 (GPT 단독 예측 시 할루시네이션 위험)
- 입찰자는 판매자의 자의적인 시작가와 즉시구매가 설정으로 인해 **실제 시세를 판단하기 어렵다.**

<br/>

### 도입 내용

| **기능 목적** | **기술 선택** | **이유** |
| --- | --- | --- |
| **실시간 시세 수집** | Naver OpenAPI | 현재 네이버쇼핑에 등록된 상품의 가격을 수집 |
| **가격 예측 분석** | OpenAI GPT-3.5 Turbo | 수집된 데이터 기반으로 의미 있는 범위 추론 |
| **중복 호출 방지** | Redis 캐시 + @Cacheable | 동일 상품 재 요청시 불필요한 GPT 호출 생략, 응답 속도 향상 |
| **예측 데이터 저장** | MySQL | 오늘 시세 및 향후 3개월 시세를 저장 |

<br/>

### 전후 비교

| **항목** | **개선 전** | **개선 후** |
| --- | --- | --- |
||![image](https://github.com/user-attachments/assets/2f629235-d9bb-4aed-a0ff-980b01a3b41b)| ![image](https://github.com/user-attachments/assets/e1f59c53-93d2-4684-81d9-5fa6b57cb815) |
|| ![image](https://github.com/user-attachments/assets/57f6bea4-a098-487e-a60c-a50bbfe7afde) | ![image](https://github.com/user-attachments/assets/c566b077-3ba1-45c4-9c39-5c9bb6303440)|
| **TPS** | 121/s | **349/s** (2.88배 ↑) |
| **평균 응답 시간** | 9.43초 | **3.28초** (65% ↓) |
| **응답 성공률** | 8.25% | **100%** |
| **GPT 호출 수** | 모든 요청마다 | **99% 감소** (캐시 재사용) |

<br/>

### **핵심 포인트**

- 판매자가 아닌 **입찰자 중심 설계**: 실제 시세 흐름에 기반한 합리적 입찰 판단 가능
- 불필요한 API 호출 제거 → **비용 최적화 + 성능 향상**
- 오버엔지니어링 없이 실사용 기준에 맞춘 **적정 수준의 개선**

<br/>
  
</details>

<details>
  <summary>
    <a href="https://www.notion.so/Redis-SortedSet-Auction-1e70b71c1467800e8de0eca7bc300098?pvs=4">🔑 경매 추천에 Redis SortedSet을 활용한 이유</a>
  </summary>

  ### 1. 배경

- 기존 추천 시스템은 **Rule-Based + RDB 저장** 방식으로, 사용자 Group의 태그 사용 빈도를 기반으로 점수를 계산하고 이를 정렬해 추천 목록을 제공함.
- 그러나 **DB 중심 구조**는 **조회 지연**, **TPS 한계**, **정렬 부하**, **캐시 미활용** 등의 문제 발생.

<br/>

### 2. 문제점

| 문제 | 설명 |
| --- | --- |
| 높은 레이턴시 | DB 조인으로 응답 지연 (수 초) |
| TPS 한계 | TPS 300에서 성능 저하 |
| 캐시 미활용 | 매 요청마다 DB 접근 |
| 정렬 부하 | Java 정렬 사용으로 확장성 낮음 |

<br/>

### 3. 해결방안 선택

Redis Sorted Set이 빠르고 점수 정렬을 지원하는 부분에 있어 본 문제 해결에 적합한 것으로 판단

- **빠른 조회 속도**: ms 단위 응답 (`ZREVRANGE`)
- **높은 TPS**: 2000 이상 처리 가능
- **정렬 내장**: Java 정렬 불필요
- **TTL 적용**: 주기적 자동 갱신 가능

<br/>

### 4. 도입 효과

- 추천 조회 속도: **3초 → 500ms 이하**
- TPS: **300 → 1000 이상**
- 서버 부하 감소, **응답 일관성** 향상

<br/>

</details>


<details>
  <summary>
    <a href="https://www.notion.so/Rule-Based-1e70b71c1467801080c6d56100f8fc75?pvs=4">🔑 태그 기반 Rule-Based 추천 로직을 채택한 이유</a>
  </summary>

### 1. 배경

- 다양한 추천 방식(협업 필터링, 콘텐츠 기반, Elasticsearch 등)을 고려했으나,
- **도메인 특화 + 초기 개인화 효과**를 기대할 수 있는 **Rule-Based 방식** 채택

<br/>

### 2. 다른 추천 방식과의 비교

| 방식 | 미채택 이유 |
| --- | --- |
| 협업 필터링 | 사용자 수 부족, 콜드스타트 문제, 복잡한 연산 |
| 콘텐츠 기반 | 개인 위주라 그룹 기반 추천에 부적합 |
| Elasticsearch | 운영 복잡도 높고 오버엔지니어링 우려 |
| Rule-Based | 구조 단순, 점수 로직 명확, 그룹 기반에 적합 |

<br/>

### 3. Rule-Based 채택 이유

| 항목 | 설명 |
| --- | --- |
| 사용자 그룹화 | 성별/나이/지역으로 Group 설정 가능 |
| 직관적인 동작 | 어떤 태그를 선호하는지 파악 용이 |
| 간단한 점수 로직 | 찜 = 1점, 입찰 = 2점 등 명시적 가중치 부여 |
| 희소성 보완 | 개인 로그 부족 시 Group 단위로 보완 |
| 최적화 용이 | Redis 캐시 등 확장성 확보에 유리 4. 결론 |

<br/>

### 4. 결론

- **간단하지만 효과적인 도메인 특화 추천 시스템**으로 빠른 구현과 안정적 성능 확보 가능
- Redis Sorted Set과 궁합이 좋아 **TPS 1000~2000 수준에서도 병목 없이 운영 가능**
- 이후 로그가 누적되면 **협업 필터링 등 고도화 가능성**도 열려 있음

<br/>

</details>

<details>
  <summary>
    <a href="https://www.notion.so/S3-CloudFront-1e70b71c1467801a83baf6cf4e84debd?pvs=4">🔑 상품 이미지 업로드에 S3 + CloudFront를 도입한 이유</a>
  </summary>

### 1. 개요

중고 경매 플랫폼에서 상품 이미지는 UX에 직접적인 영향을 미치는 중요한 요소이며, 초기에는 로컬 저장, DB 저장, S3 단독 사용 등을 검토했으나, **확장성·성능·안정성·보안성**을 모두 만족하기 위해 **S3 + CloudFront 조합**을 최종 선택

<br/>

### 2. 장점

- **빠른 이미지 로딩 속도**: CloudFront의 CDN 캐시 덕분에 전 세계 어디서나 빠르게 로딩
- **높은 확장성과 안정성**: S3는 수천 개 이상의 파일도 문제없이 처리 가능하며, 내구성이 뛰어남
- **도메인 통합 및 HTTPS 지원**: 보안 및 브랜드 일관성 유지
- **비용 효율성**: CloudFront가 캐시 처리함으로써 S3 트래픽 비용 절감
- **서버 부하 감소**: WAS에서 정적 파일 요청을 분리하여 성능 최적화

<br/>

### 3. 결론

**S3 + CloudFront 조합은 확장성과 성능, 보안, 유지보수 측면에서 최적의 선택.** 이를 통해 정적 이미지 리소스를 효율적으로 처리하고, 서비스 초기부터 안정적인 사용자 경험을 제공 가능

<br/>

</details>

<br/>


## 5. 트러블 슈팅 / 성능 개선

<details>
    <summary>
	    <a href="https://www.notion.so/Redisson-1e70b71c146780859b00eac5e85712c9?pvs=4">🎯 Redisson 분산 락과 트랜잭션 분리를 통한 결제 안정성 확보</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/STOMP-Redis-pub-sub-1e70b71c1467806590f1f529a090cb63?pvs=4">🎯 STOMP + Redis pub/sub 를 통한 실시간 채팅 시스템 성능 개선</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Google-OAuth-URI-1e70b71c146780fc83f6e5fd3f949e44?pvs=4">🎯 Google OAuth 리디렉션 URI 오류</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Google-OAuth-HttpMessageConverter-1e70b71c14678072b631de42e41b51e1?pvs=4">🎯 Google OAuth 토큰 교환 시 HttpMessageConverter 오류</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/TPS-18-1-2K-1e70b71c146780a8a0bde177f9eab0af?pvs=4">🎯 입찰 최대 TPS 18 → 1.2K 까지의 개선 과정</a>
    </summary>

### **✅ 문제 상황**

- Spring의 ApplicationEventPublisher와 @EventListener를 사용한 EDA 구성에서 TPS 18, 평균 Latency 39초라는 매우 낮은 성능을 기록.
- 최대 2000명의 가상 유저 부하 테스트에서 Connection Pool 고갈로 SQLTransientConnectionException이 대량 발생.
- 요청 처리 중 커넥션을 오랫동안 점유하면서 풀 고갈 문제 발생.

<br/>

### **⏰ 개선 과정**

1. **아키텍처 리팩토링 (1차 개선)**
    - SQS FIFO Queue를 도입하여 요청과 처리 사이 시간 텀 확보.
    - 트랜잭션 커밋 후 이벤트 큐로 요청을 넘기고, SQS에서 10개씩 메시지를 가져와 동기 처리.
      
2. **DB 인덱스 추가 (2차 개선)**
    - auctionSeq (varchar) 및 status (enum) 필드에 인덱스를 추가하여 MySQL 조회 최적화.
      
3. **세부 로직 개선 (3차 개선)**
    - 낮은 입찰 요청 시 RuntimeException → DTO로 처리 방식 변경 → 예외로 인한 성능 저하 방지.
    - 트랜잭션 내부 Redis 접근 로직 분리 → 트랜잭션 커넥션 점유 시간 단축.
  
<br/>

### **📈 결과**

1. **아키텍처 리팩토링 & DB 인덱스 추가(1차 개선 + 2차 개선)**
    - **TPS**: 33 → **900** (4900%🔺)
    - **Latency**: 23s → **1s** (97%🔻)
      
2. **세부 로직 개선 (3차 개선)**
    - **TPS**: 900 → **1,290** (43%🔺)
    - **Latency**: 1s → **0.75s** (25%🔻)

 <br/>
 
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/1e70b71c1467805fb1aed63f21052f44?pvs=4">🎯 상품 시세 예측결과가 동일한 금액으로 고정되는 문제</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/500-1e70b71c146780329605f0f52259a301?pvs=4">🎯 시세 예측 실패시 500 에러 → 메시지 응답</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/S3-1e70b71c1467801e8229d45e326a473a?pvs=4">🎯 S3 이미지 업로드 실패 해결</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Redis-TTL-1e70b71c146780e99d58ff771dd97d57?pvs=4">🎯 Redis TTL 설정으로 조회수 관리</a>
    </summary>
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Auction-API-1e70b71c1467806eb9daf4c41554bd19?pvs=4">🎯 Auction 전체조회 + 추천목록 API 응답 지연 개선과정</a>
    </summary>
</details>

<br/>






## 6. 아키텍처

<img width="80%" src="https://github.com/user-attachments/assets/bd5971a1-a5b5-434b-9ea6-15fcf47ed6fd">

<br/>

## 7. ERD / API 문서

<br/>

## 8. 성과 및 회고


### 👍🏻 잘된 점
    
### 👀 아쉬운 점

### 📆 향후 계획

<br/>

## 9. 팀원 소개


<table>
    <tr>
        <!-- 프로필 -->
          <td>
            <a href="https://github.com/yeongbinim">
                <img src="https://github.com/user-attachments/assets/bd5971a1-a5b5-434b-9ea6-15fcf47ed6fd" width="100px;" alt="프로필 사진">
            </a>
          </td>
        <td align="center" style="width: 150px;">    
            <p>
                <b></b>
            </p>
        </td>
        <td>
	        <p>
                📌 실시간 입찰 동시성 제어 <br/>
                📌 입찰 시 최고가 조회 속도 향상 <br/>
                📌 인프라 구축, CI/CD, UI(React) <br/>
                📌 결제 API 연동(Toss Payments)
            </p>
        </td>
    </tr>
</table>

