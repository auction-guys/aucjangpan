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
</details>

<details>
  <summary>
    <a href="https://www.notion.so/EventBridge-Scheduler-1e70b71c146780a68acdf52069735353?pvs=4">🔑 경매 마감 처리와 알림을 위해 EventBridge Scheduler를 적용한 이유</a>
  </summary>
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
</details>

<details>
    <summary>
	    <a href="https://www.notion.so/Redis-1e70b71c1467804e88dff440c0b4b3e1?pvs=4">🎯 Redis의 병목지점을 찾고 해결한 과정</a>
    </summary>
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

