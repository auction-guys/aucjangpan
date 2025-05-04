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
</details>


<details>
  <summary>
    <a href="https://www.notion.so/1e70b71c1467803cb906d03f6a3fc9f9?pvs=4">🔑 실시간 채팅 기능 개선을 위한 메시징 시스템 도입 결정 과정</a>
  </summary>
</details>

<details>
  <summary>
    <a href=https://www.notion.so/1e70b71c146780b3a839c0056b4f7ad6?pvs=4">🔑 유저 도메인 의사결정 과정 정리</a>
  </summary>
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
    <a href="https://www.notion.so/1e70b71c14678064a10fdd7a168ad1f1?pvs=4">🔑 경매 단건 조회에 시세 예측 기능을 도입한 이유</a>
  </summary>
</details>

<details>
  <summary>
    <a href="https://www.notion.so/Redis-SortedSet-Auction-1e70b71c1467800e8de0eca7bc300098?pvs=4">🔑 Redis SortedSet을 통한 Auction 추천을 도입한 이유</a>
  </summary>
</details>


<details>
  <summary>
    <a href="https://www.notion.so/Rule-Based-1e70b71c1467801080c6d56100f8fc75?pvs=4">🔑 태그 기반 Rule-Based 추천 로직을 채택한 이유</a>
  </summary>
</details>

<details>
  <summary>
    <a href="https://www.notion.so/S3-CloudFront-1e70b71c1467801a83baf6cf4e84debd?pvs=4">🔑 상품 이미지 업로드에 S3 + CloudFront를 도입한 이유</a>
  </summary>
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

