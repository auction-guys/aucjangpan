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
<p>
	<img src="https://github.com/user-attachments/assets/ca97d5a5-266b-4f59-bdfd-b4b8b2ac379f" width="300px" />
	<img src="https://github.com/user-attachments/assets/a51a3b70-45d4-4ec0-ae6d-714def0cef99" width="300px" />	
</p>

<br/><br/>

**옥장판은 중고 거래에 경매의 짜릿함을 더한 새로운 방식의 중고 옥션입니다.**

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
<br/>
<table>
	<tr>
		<td width="50%"><img src="https://github.com/user-attachments/assets/06812d54-af6c-490e-a99c-f23806286cb2"/></td>
		<td width="50%"><img src="https://github.com/user-attachments/assets/e4d9a163-365f-4424-ba76-3868efbffe1a"/></td>
	</tr>
	<tr>
		<td>JWT를 이용한 자체 인증</td>
		<td>OAuth 2.0을 이용한 인증</td>
	</tr>
</table>
<br/>
</details>

<details>
    <summary><b>[💰 결제/정산]</b> 토스 페이먼츠를 이용한 결제 기능과 스케줄러를 이용한 자동 정산 기능</summary>
	<br/>
	<table>
		<tr>
			<td width="50%"><img src="https://github.com/user-attachments/assets/3ada77c3-fe42-48a9-9a92-0bf8b536f9ad"/></td>
			<td width="50%"><img src="https://github.com/user-attachments/assets/ba806099-3967-43a7-876f-e789861b1f54"/></td>
		</tr>
		<tr>
			<td>결제</td>
			<td>정산</td>
		</tr>
	</table>
	<br/>
</details>

<details>
    <summary><b>[💬 판매자 채팅]</b> Stomp + Redis를 이용한 실시간 채팅</summary>
	<br/>
	<table>
		<tr>
			<td width="33%"><img src="https://github.com/user-attachments/assets/ecd73cbc-3e8e-47d4-8238-a3ba63cb1f73"/></td>
			<td width="33%"><img src="https://github.com/user-attachments/assets/0cdba674-2980-403d-bd57-282a9376fba7"/></td>
			<td width="33%"><img src="https://github.com/user-attachments/assets/a0c6ccb6-8f0c-482f-a434-c57c0a585b9a"/></td>
		</tr>
		<tr>
			<td>채팅 요청</td>
			<td>메시지 전송 V1</td>
			<td>메시지 전송 V2</td>
		</tr>
	</table>
	<br/>
</details>

<details>
  <summary><b>[📦 상품 이미지 관리]</b> S3 + CloudFront를 이용한 최적화된 이미지 관리</summary>
	<br/>
	<table>
		<tr>
			<td width="50%"><img src="https://github.com/user-attachments/assets/1669e37c-6455-423c-89aa-21a4c21d9b70" /></td>
			<td width="50%"><img src="https://github.com/user-attachments/assets/c1cfc923-b389-42e2-9ef0-7b6fad2c078c" /></td>
		</tr>
		<tr>
			<td>이미지 업로드</td>
			<td>이미지 조회</td>
		</tr>
	</table>
	<br/>	
</details>

<details>
  <summary><b>[👍🏻 경매 추천]</b> Redis SortedSet을 통한 경매 Rule-based 추천 시스템</summary>
	<br/>
	<table>
		<tr>
			<td><img src="https://github.com/user-attachments/assets/f0280283-d991-49ee-b588-e679cf932443" width="400px"/></td>
		</tr>
		<tr>
			<td>경매 추천</td>
		</tr>
	</table>
	<br/>
</details>

<details>
    <summary><b>[📈 상품 가격 예측]</b> Chat GPT + Naver OpenAPI를 결합한 경매 상품 시세 예측</summary>
	<br/>
	<table>
		<tr>
			<td><img src="https://github.com/user-attachments/assets/e76af344-ccc9-4fbe-9a93-779a11ac0283" width="400px"/></td>
		</tr>
		<tr>
			<td>상품 가격 예측</td>
		</tr>
	</table>
	<br/>
</details>

<details>
    <summary><b>[🏁 입찰 / 즉시 구매]</b> Redis + SQS를 활용한 이벤트 기반의 입찰 / 즉시 구매 처리</summary>
	<br/>
	<table>
		<tr>
			<td width="50%"><img src="https://github.com/user-attachments/assets/0443bb2c-57c8-4af4-b837-dd761dd06989" /></td>
			<td width="50%"><img src="https://github.com/user-attachments/assets/cb9a443a-faf2-417d-a81e-ead68c52e7b0" /></td>
		</tr>
		<tr>
			<td>입찰 V1</td>
			<td>입찰 V2</td>
		</tr>
	</table>
	<br/>
</details>

<details>
    <summary><b>[⏰ 경매 종료 알림]</b> Eventbridge + SQS를 활용한 경매 종료 알림</summary>
 	<br/>
	<table>
		<tr>
			<td width="50%"><img src="https://github.com/user-attachments/assets/410b793d-561e-4c32-a1fe-6eb4bea862cf" /></td>
			<td width="50%"><img src="https://github.com/user-attachments/assets/e6d72305-55d7-4ca1-929c-d7edc6b435d3" /></td>
		</tr>
		<tr>
			<td>경매 종료 알림 V1</td>
			<td>경매 종료 알림 V2</td>
		</tr>
	</table>
	<br/>


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

 ### 문제 상황

- 결제 승인/취소 시 분산 락(Redisson)과 DB 트랜잭션이 충돌하여 `rollback-only`, `Transaction synchronization error` 등의 예외가 발생함.
    

### 원인 분석

- 락 획득 후 트랜잭션 처리 로직이 길고 복잡해 충돌 발생.
- 분산 락과 트랜잭션의 범위가 중첩되어 트랜잭션 롤백 현상 발생.

### 문제 해결

- **분산 락 범위 최소화**: 락 내부에서 서비스 메서드만 호출.
- **트랜잭션 분리**: 실제 DB 작업은 `@Transactional`로 명확히 분리.
- **Redis 멱등성 처리**: 중복 결제 방지를 위해 Redis 캐시 활용.

### 적용 결과

- 트랜잭션 충돌 발생이 완전히 사라짐.
- 결제 안정성과 응답 성능이 모두 개선됨.

<br/>

</details>

<details>
    <summary>
	    <a href="https://www.notion.so/STOMP-Redis-pub-sub-1e70b71c1467806590f1f529a090cb63?pvs=4">🎯 STOMP + Redis pub/sub 를 통한 실시간 채팅 시스템 성능 개선</a>
    </summary>
	
### 1. 문제

- 채팅 메시지 전송 시 사용자 응답 속도가 느리고 실시간성이 떨어짐.
- 서버 확장 시 채팅 메시지가 일부 인스턴스에서만 처리되어 메시지 누락 문제가 발생
- 서버 장애나 과부하 시 전체 채팅 서비스의 안정성과 가용성이 크게 저하

### 2. 요구사항

- 체팅 메시지가 사용자에게 즉시 전달되어야 합니다.
- 메시지를 DB에 저장하는 과정에서 에러가 발생해도 사용자에게 메시지가 전달되어야 합니다.
- Scale-out 시에도 안정적으로 동작되어야 합니다.

### 3. 원인

- 메시지가 DB에 동기적으로 저장되므로 메시지 처리 속도가 느려지고, 메시지를 DB에 저장하는 과정에서 에러가 발생시 사용자에게 메시지가 전달되지 않을 수 있음
- STOMP 기반 메시지 전달 방식이 **단일 인스턴스 중심**으로 작동해, 서버 확장 시 인스턴스 간 메시지 공유가 불가능함.
- 메시지 브로커나 분산 처리가 없기 때문에, 서버 장애나 부하 발생 시 대체 인스턴스가 역할을 수행하지 못함

### 4. 해결

**Redis pub/sub 적용**: Redis를 사용하여 메시지가 발행되면, 모든 서버 인스턴스가 이를 구독하고 메시지를 즉시 사용자에게 전달하도록 설정.

- 메시지는 Redis로 즉시 전달되고, DB에 대한 저장은 비동기로 처리하여 서버 성능에 부담을 주지 않도록 구성.
- 서버 인스턴스를 늘려도 메시지 동기화가 이루어지므로 scale-out 가능

<br/>

### 5. 결과

부하 테스트는 로컬에서 150명의 가상 사용자(VUS)를 대상으로 5분간 **v1**과 **v2**의 성능을 비교

**RPS (Requests Per Second)**

- **v1**: 약 112.84 RPS (초당 처리되는 요청 수)
![image](https://github.com/user-attachments/assets/7aca3632-a165-4fe9-b080-b4fc1924912c)


- **v2**: 약 153.32 RPS (초당 처리되는 요청 수)
![image](https://github.com/user-attachments/assets/ed0289e8-2045-4b0f-9865-b55cac90df6e)


v2에서는 Redis의 pub/sub 방식과 비동기 DB 저장을 통해 요청 처리 속도가 약 35.87**%** 향상되었습니다.

<br/>

**Latency (응답 시간)**

- **v1**: 평균 응답 시간 **30.1ms**
  
![image](https://github.com/user-attachments/assets/3f125c61-455b-4f18-9abc-f82df981cfcd)

- **v2**: 평균 응답 시간 **22.5ms**

![image](https://github.com/user-attachments/assets/6f7e7107-3190-47a0-af88-e49f0a4491bd)


v2에서는 Redis로 메시지를 즉시 전달하고, DB 저장을 비동기 처리하면서 메시지 처리 시간이 약 **25.6%** 감소하였습니다.

<br/>

### 6. 한계점 및 보완 방안

**Redis pub/sub의 메시지 유실 가능성**

서버가 일시적으로 다운되어 구독하지 못하면 해당 메시지를 수신하지 못하고 **유실될 수 있습니다.**

- 보완 방안
    - Kafka 등의 메시지 브로커를 도입하여 메시지를 저장 기반으로 처리하도록 변경
    - 메시지를 Redis에 저장 후 메시지 ID를 기준으로 ack 처리 방식 도입 가능.

<br/>

**메시지 순서 보장 어려움**

Redis pub/sub 및 비동기 처리 구조에서는 메시지 순서가 **보장되지 않을 수 있습니다.** 

- 보완 방안
    - 메시지에 **timestamp** 또는 **순번 필드**를 부여하여 클라이언트에서 정렬 처리.
    - Kafka 등 메시지 순서를 보장하는 브로커로 구조 변경 고려.

<br/>

**Redis 단일 장애 지점(SPOF)**

Redis가 단일 인스턴스일 경우 장애 시 전체 채팅 기능이 마비될 수 있습니다.

- 보완 방안
    - Redis Cluster 구성으로 고가용성 확보

<br/>
  
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Google-OAuth-URI-1e70b71c146780fc83f6e5fd3f949e44?pvs=4">🎯 Google OAuth 리디렉션 URI 오류</a>
    </summary>

### 문제 상황

Google OAuth 로그인 시 `redirect_uri_mismatch` 오류로 **로그인 실패** 발생

### 원인

- Google Cloud Console에 등록된 리디렉션 URI와 **실제 콜백 URL 불일치**
- OAuth는 **정확히 일치하는 URI만 허용**하여 토큰 교환 실패

### 해결 전략

- Google Cloud Console의 OAuth 설정에서 **정확한 콜백 URI 등록**
- Postman을 활용한 **OAuth 흐름 수동 테스트**로 설정 검증

### 도입 효과

- Google OAuth 로그인이 **정상 작동**
- 액세스 토큰 발급 및 사용자 정보 조회까지 **전 과정 성공적으로 확인**

<br/>
 
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Google-OAuth-HttpMessageConverter-1e70b71c14678072b631de42e41b51e1?pvs=4">🎯 Google OAuth 토큰 교환 시 HttpMessageConverter 오류</a>
    </summary>

### 문제 상황

`OAuthService.loginWithGoogle()` 메서드에서 Google OAuth 토큰 교환 시, `No suitable HttpMessageConverter` 예외로 **JSON 응답 파싱 실패**

### 원인

- `RestTemplate`에 JSON 응답을 처리할 **MappingJackson2HttpMessageConverter 누락**
- 요청 본문 형식인 **application/x-www-form-urlencoded**도 처리 불가

### 해결 전략

- `FormHttpMessageConverter`, `MappingJackson2HttpMessageConverter`를 명시적으로 추가
- `setMessageConverters()`로 RestTemplate 동작을 **예측 가능하게 구성**

### 결과

- Google OAuth API의 **JSON 응답 파싱 및 토큰 수신 정상화**
- 이후 사용자 정보 조회 및 로그인 흐름까지 **안정적으로 동작**

<br/>

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

### 문제점

- 상품명과 설명을 기준으로 시세 예측을 요청했지만,
    
    **모든 상품에 대해 항상 동일한 결과** (`min: 30000`, `max: 50000`)만 반환됨
    

<br/>

### 원인 분석

- 프롬프트 내에 아래와 같은 **고정 예시 값**이 포함되어 있었음:

```java

String.format("""
    제품명: %s
    설명: %s

    해당 중고 상품의 예상 거래 가격 범위를 알려주세요.
    아래 형식처럼 JSON 형식으로만 응답해 주세요.

    {
      "min": 30000,
      "max": 50000
    }
""", productName, productDescription)

```

ChatGPT가 해당 숫자들을 **예시가 아닌 정답으로 인식**하여 반복 출력함

<br/>

### 해결 방법

- 프롬프트에서 **구체적인 예시 숫자 제거**

```java
String.format("""
    제품명: %s
    설명: %s

    해당 중고 상품의 예상 거래 가격 범위를 알려주세요.
    아래 형식처럼 JSON 형식으로만 응답해 주세요.

    {
      "min": [예상 최소 가격 숫자만 입력],
      "max": [예상 최대 가격 숫자만 입력]
    }
""", productName, productDescription)

```

<br/>

### 적용 결과

```json
{
  "productName": "아이폰 13",
  "todayPrice": {
    "min": 420000,
    "max": 520000,
    "priceType": "TODAY",
    "priceDate": "2025-04-30"
  },
  "message": "시세 예측 완료"
}

```

정상적으로 예측값 반환됨

<br/>

</details>

<details>
    <summary>
	    <a href="https://www.notion.so/500-1e70b71c146780329605f0f52259a301?pvs=4">🎯 시세 예측 실패시 500 에러 → 메시지 응답</a>
    </summary>

### 문제점

- GPT 호출 중 예외가 발생하면 단순한 시세 예측 실패에도 상품 등록/경매 단건조회 기능 전체가 500

오류로 실패

- 사용자 입장에서는 단순 시세 조회 실패가 전체 기능 장애처럼 보이게 됨

<br/>

### 원인 분석

- `ChatGPTClient`에서 예측이 실패하면서 500 에러 발생

<br/>

### 해결 방법

**ChatGPTClient**

```java

try {
    return chatGPTClient.callGptForHistoricalPrices(query);
} catch (Exception e) {
    log.warn("GPT 예측 실패: {}", e.getMessage());
    return Collections.emptyList();
}

```

**MarketPriceService**

```java

if (predictedPrices.isEmpty()) {
    return MarketPriceResponse.error("시세 정보가 존재하지 않습니다.");
}

```

<br/>

### 적용 결과

```json

{
  "message": "시세 정보가 존재하지 않습니다."
}

```

GPT 예측 실패 시에도 **200 OK + 안내 메시지 응답**

<br/>

</details>

<details>
    <summary>
	    <a href="https://www.notion.so/S3-1e70b71c1467801e8229d45e326a473a?pvs=4">🎯 S3 이미지 업로드 실패 해결</a>
    </summary>

 ### 1. 배경

중고 경매 플랫폼에서는 사용자가 상품을 등록할 때 이미지를 함께 업로드합니다. 이때 이미지 업로드가 정상적으로 동작하지 않는 문제가 발견되었습니다. 클라이언트 측에서 **HTTP 403 오류** 또는 **`AccessDenied` 응답**이 발생했으며, 특히 프론트엔드에서 `multipart/form-data` 형식으로 전송한 요청에서 문제가 집중적으로 발생했습니다. 반면 Postman에서는 정상적으로 업로드가 가능했고, 직접 S3 객체 URL에 접근하면 AccessDenied 화면이 출력되는 상황이었습니다.

</br>

### 2. 원인

문제를 분석한 결과, 여러 요소가 복합적으로 작용한 것으로 나타났습니다.

- **IAM 권한 부족**: S3 버킷에 파일을 업로드하거나 조회할 수 있는 권한이 계정 또는 IAM Role에 누락되어 있었습니다.
- **CORS 미설정**: 프론트엔드가 브라우저 환경에서 직접 S3로 요청을 보낼 경우 CORS 설정이 되어 있지 않으면 브라우저가 요청을 차단합니다.
- **Content-Type 누락**: 업로드 요청 시 `Content-Type` 헤더가 누락되어, S3가 파일을 제대로 처리하지 못하고 거부했습니다.
- **버킷의 퍼블릭 접근 제한**: S3는 기본적으로 모든 요청을 차단하며, 공개 접근 허용이 없으면 외부에서 이미지를 열람할 수 없습니다.

</br>

### 3. 문제 해결

문제를 해결하기 위해 아래와 같은 조치를 단계적으로 적용했습니다:

- **IAM 정책 수정**: `s3:PutObject`, `s3:GetObject` 권한을 명시적으로 포함한 정책을 EC2 인스턴스 혹은 사용 계정에 적용하여 업로드 및 조회 권한을 확보했습니다.
- **CORS 설정 추가**: S3 버킷에 `AllowedMethods`, `AllowedOrigins`, `AllowedHeaders` 설정을 추가하여 브라우저 환경에서도 CORS 오류 없이 요청이 가능하도록 구성했습니다.
- **버킷 정책 수정**: 이미지 열람이 필요한 최소 범위에서 `public-read` 접근을 허용하도록 설정해, CloudFront 및 직접 URL 접근이 가능해졌습니다.
- **Content-Type 명시**: 파일 업로드 시 `ObjectMetadata`에 `Content-Type`을 명확히 설정하여 S3가 파일 형식을 정확히 인식하도록 보완했습니다.

</br>

### 4. 결론

이러한 설정을 통해 이미지 업로드와 접근이 모두 정상화되었습니다. 프론트엔드에서 발생하던 403 및 AccessDenied 오류가 해결되었고, 사용자 입장에서도 이미지 미리보기 및 접근이 원활하게 작동하게 되었습니다.

이번 경험을 통해, **S3를 활용한 이미지 업로드에서는 IAM, CORS, Content-Type, 버킷 정책이 서로 맞물려 동작하므로 종합적인 접근이 필수**라는 점을 명확히 인식할 수 있었습니다.

</br>

</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Redis-TTL-1e70b71c146780e99d58ff771dd97d57?pvs=4">🎯 Redis TTL 설정으로 조회수 관리</a>
    </summary>

### 1. 배경

경매 상세 조회 API 호출 시마다 DB의 `view` 필드가 무조건 증가하는 구조로 되어 있어 문제가 발생
동일 사용자가 새로고침을 반복해도 조회수가 계속 증가했고, 특히 JMeter를 활용한 부하 테스트에서는 **DB에 과도한 update 쿼리**가 발생해 CPU 사용률이 급격히 상승함. 현재는 Redis 캐시를 활용 중이었지만, **중복 조회 방지 로직이 없는 상태**였으며 문제 해결이 필요

<br/>

### 2. 원인

- **중복 조회 제한 미비**: 동일 사용자에 의한 반복 접근도 모두 조회수에 반영됨
- **TTL 설정 누락**: Redis에 캐시된 조회 여부가 영구 저장되거나 아예 저장되지 않음
- **Redis 키 설계 부재**: 사용자 단위를 식별할 수 있는 키 구조(`userId`, `IP`)가 없었음
- **실시간 통계 반영 실패**: 단순 카운터 증가 방식이 의도한 통계 구조와 부합하지 않음

<br/>

### 3. 문제 해결

- **사용자 기반 조회 캐시**: Redis에 사용자의 조회 이력을 남겨 중복 조회 차단
- **TTL 설정**: 일정 시간(예: 1시간) 동안 동일 사용자의 조회는 무시
- **Redis 키 구조 개선**: `view:auction:{auctionId}:user:{userKey}` 형태로 키 설계
- **DB 조회수 증가 조건화**: Redis에서 중복 확인 후에만 `view` 필드를 증가시키도록 변경

<br/>

### 4. 적용

- Redis에 사용자의 조회 이력을 캐시하며, TTL은 60분으로 설정
- 조회 요청이 들어오면 Redis에서 최근 조회 여부를 확인한 뒤, 없을 경우에만 DB 조회수 증가 수행
- 조회 후에는 Redis에 해당 키를 저장하여 중복 방지

```java
java
복사편집
String redisKey = "view:auction:" + auctionId + ":user:" + userKey;
Duration ttl = Duration.ofMinutes(60);

if (!redisService.isViewedRecently(redisKey)) {
    auction.increaseViews();
    redisService.markViewed(redisKey, ttl);
}

```

<br/>

### 5. 결론

이 개선을 통해 **의미 없는 중복 조회수 증가를 방지하고, DB 부하를 크게 줄였고,**

DB 업데이트는 실질적인 사용자 접근에만 반응하게 되었으며, TPS는 약 300 → 1000 이상으로 증가했으며, 평균 응답 속도도 300ms ~ 500ms 수준으로 개선

Redis의 메모리 사용량은 소폭 증가했지만, TTL을 기반으로 한 키 관리로 효율적 운영이 가능

이로써 조회수 통계의 정확도와 시스템의 성능이 동시에 향상

<br/>

</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Auction-API-1e70b71c1467806eb9daf4c41554bd19?pvs=4">🎯 Auction 전체조회 + 추천목록 API 응답 지연 개선과정</a>
    </summary>

### 1. 배경

`/api/v2/auctions/overview` API는 **추천 경매 + 일반 경매 목록을 함께 반환**하는 통합 API였습니다.

그러나 부하 테스트 결과, **응답 지연(2~6초)**과 **TPS 300 수준에서 CPU 부하/DB 병목**이 발생해 사용자 경험이 크게 저하되었습니다.

<br/>

### 2. 원인

- **추천 ID는 Redis에 있지만 상세 정보는 RDB에서 별도 조회**
- **`currentPrice`, `bidCount`를 매 경매마다 Redis에서 개별 조회(N+1 문제)**
- **추천과 일반 경매 구분 및 정렬/페이징 로직이 복잡**
- **전체 응답에 대한 캐싱이 없어 모든 요청이 풀로직 수행**

<br/>

### 3. 해결 방안 및 적용

- **`currentPrice`, `bidCount` Redis 요청을 Bulk 처리** → N+1 제거, pipeline 활용
- **추천 ID 목록만 Redis TTL 캐싱** → 반복 요청에 대응
- **DB 조회 최적화** → IN 조건 + Projection 기반 조회
- **전체 응답 캐싱은 보류**, 추천 대상만 캐싱

<br/>

### 4. 결과

- 평균 응답 시간: 약 **200ms로 대폭 단축**
- TPS 처리: **300 → 1000~2000 수준으로 개선**
- Redis 요청 수 감소 및 DB 쿼리 최적화로 전반적인 시스템 성능 향상
- 사용자 입장에서 **실시간처럼 빠른 응답 체감**

<br/>

### 5. 후속 과제

- **추천 목록 전체를 Redis에 JSON으로 캐싱**하는 전략 검토
- **공통 조회 필드(예: 가격, 입찰수 등)에 대한 Embedded DTO 캐싱** 도입 예정

<br/>

</details>

<br/>

## 6. 아키텍처

<img width="80%" src="https://github.com/user-attachments/assets/9575c050-2e4a-44ff-becd-ab3344b5eb1d">

<br/>

## 7. ERD / API 문서

### 📗 ERD

<img src="https://github.com/user-attachments/assets/4e7bb9c2-358c-4dce-96a5-99a6aa584498" width="900px"/>

<br/>

### 📕 API 문서

<details>
    <summary>회원</summary>
<img src="https://github.com/user-attachments/assets/cea19207-0cac-4f47-a803-e958f58c9e37" width="900px"/>
</details>

<details>
    <summary>경매</summary>

#### V1
<img src="https://github.com/user-attachments/assets/a37cbc28-d59a-4d94-9373-2562007c204a" width="900px"/>
<img src="https://github.com/user-attachments/assets/c965375a-6b45-4c33-b502-98588f190469" width="900px"/>
<img src="https://github.com/user-attachments/assets/7bf9d8d6-48b6-45f9-9415-e82b9a6cc747" width="900px"/>


#### V2, V3

<img src="https://github.com/user-attachments/assets/45fb123d-d714-4dfb-a713-4a6093baf1cf" width="900px"/>
</details>

<details>
    <summary>알림함</summary>
<img src="https://github.com/user-attachments/assets/cc97b2a6-e089-4951-b93d-83406ae2ab13" width="900px"/>
</details>

<details>
    <summary>주문/결제</summary>
	
#### 주문
<img src="https://github.com/user-attachments/assets/7af110f3-5bf1-4df6-98af-d50285c857a9" width="900px"/>

#### 결제
<img src="https://github.com/user-attachments/assets/7c97ea37-8d1e-4ff5-b2ad-5f315a07f432" width="900px"/>

</details>

<details>
    <summary>상품/찜하기</summary>

#### 상품
<img src="https://github.com/user-attachments/assets/b32d1505-e058-4297-b1f1-2b15ab7d2712" width="900px"/>
<img src="https://github.com/user-attachments/assets/e40472d9-6e5f-41c3-a69c-caa2e0201736" width="900px"/>

#### 찜하기
<img src="https://github.com/user-attachments/assets/adbe8bb6-7dc4-4038-adbd-d793d3dfee25" width="900px"/>
</details>

<details>
    <summary>정산</summary>
<img src="https://github.com/user-attachments/assets/6c087dc3-af76-4688-9bf8-68d48c3d6990" width="900px"/>
<img src="https://github.com/user-attachments/assets/1aec81f6-2c72-4ac6-860c-30adf07487bc" width="900px"/>
</details>

<details>
    <summary>채팅</summary>
<img src="https://github.com/user-attachments/assets/98a92feb-872c-4391-b37b-d6d1de6058cc" width="900px"/>
<img src="https://github.com/user-attachments/assets/c24e453b-e3f3-481a-8b5f-31760dc8b091" width="900px"/>
</details>
<br/>


## 8. 성과 및 회고

### ✅ 잘된 점
- 팀원 간 **화목한 분위기를 유지하며 소통**하고, 갈등 없이 프로젝트를 마무리한 것
- 개발 과정에 있어 **토의를 통해 함께  깊이 있는 고민**을 하고 문제 해결 방안을 찾아 적용한 것
- 기획 당시 생각했던 **도전 기능을 모두 반영**한 것
- 부하 테스트를 통해 **성능 저하에 대한 가설**을 세우고 **개선**해 가는 과정을 경험한 것
- 활발하게 **PR을 검토하고 리뷰**하는 문화를 시도한 것

### 🏃 아쉬운 점
- 프로젝트 상황상 로컬 환경에서만 부하 테스트를 진행, **프로덕션 환경과 동일하지 못했던 점**
- 개선의 방향성을 설계했으나 **모든 개선 사항을 반영하지는 못한 것**
- Use Case에 대한 **테스트를 전부 커버하지 못한 것**
- MSA에서 **일관성, 가용성, 분할 허용성 간의 관계**를 고려하며 설계하는 경험을 하지 못한 것

### 📆 향후 계획
- 상품명 정규화 또는 **ElasticSearch** 도입
- 모놀리식 → 마이크로 서비스 아키텍처(**MSA**) 전환
- 분산 환경에서의 **Tx 관리, Eventual Consistency** 보장
- 정산 등에 **Spring Batch 적용 및 비동기 처리** 이벤트 큐 구조로 전환


<br/>

## 9. 팀원 소개

<table>
	<tr align="center">
		<td width="240px"><code>팀장</code> 김종연</td>
		<td width="240px"><code>부팀장</code> 박경민</td>
		<td width="240px">김종훈</td>
	</tr>
	<tr align="center">
		<td width="240px"><img src="https://github.com/user-attachments/assets/7867dd29-f9e1-4614-8b96-03d842a2aafe"></td>
		<td width="240px"><img src="https://github.com/user-attachments/assets/86e73c0c-e01f-4ea6-9866-8f00ee50425e"></td>
		<td width="240px"><img src="https://github.com/user-attachments/assets/14e71d7c-72f5-4e36-845a-f0306c6979a2"></td>
	</tr>
	<tr>
		<td width="240px"> 
			 <p>
				 	📌 경매 CRUD<br/>
					📌 입찰 서비스(SQS & Redis)<br/>
				 	📌 마감 처리 예약(EventBridge)<br/>
					📌 마감 알림 발송(BulkInsert)<br/>
			</p>
		</td>
		<td width="240px">
			 <p>
					📌 상품 CRUD <br/>
					📌 찜하기, Views 기능<br/>
					📌 추천 기능 개발 <br/>
					📌 상품 이미지 관리 기능
			</p>
		</td>
		<td width="240px">
			 <p>
					📌 상품 시세예측 <br/>
					📌 ChatGPT & Naver쇼핑 API <br/>
					📌 API 호출 최적화(Redis 캐싱) <br/>
			</p>
		</td>
	</tr>
	<tr align="center">
		<td width="240px">김대정</td>
		<td width="240px">김수환</td>
		<td width="240px">김성찬</td>
	</tr>
	<tr align="center">
		<td width="240px"><img src="https://github.com/user-attachments/assets/bb030eda-d9c0-47b9-86ab-97e874c01b24"></td>
		<td width="240px"><img src="https://github.com/user-attachments/assets/f7c68aad-4fb0-468b-8d7b-ac386a8f6559"></td>
		<td width="240px"><img src="https://github.com/user-attachments/assets/1274a8ac-ddf5-4d5e-a11b-78d9aa6d4d93"></td>
	</tr>
	<tr>
		<td width="240px">
			 <p>
					📌 주문 CRUD<br/>
					📌 결제 API 연동(TossPayments) <br/>
					📌 정산 자동화 시스템 구현
			</p>
		</td>
		<td width="240px">
			 <p>
					📌 유저 인증/인가 CRUD</br>
					📌 JWT & Spring Security</br>
					📌 OAuth 적용</br>
					📌 RTR 적용
			</p>
		</td>
		<td width="240px">
			 <p>
				📌 AWS 인프라 & CI/CD<br/>
				📌 Prometheus & Grafana<br/>
				📌 실시간 채팅 구현(STOMP)
			</p>
		</td>
	</tr>
</table>

