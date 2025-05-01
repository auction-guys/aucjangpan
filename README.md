<img src="https://github.com/user-attachments/assets/749d94e5-cbe6-4850-a6de-20bc5a9f0ca6" width="100%"/>

<br/>

## 목차

- [1. 프로젝트 소개](#1-프로젝트-소개)
- [2. 주요 기능](#2-주요-기능)
- [3. 기술 스택](#3-기술-스택)
- [4. 기술적 의사 결정 과정](#4-기술적-의사-결정-과정)
- [5. 트러블 슈팅 / 성능 개선](#5-트러블-슈팅-/-성능-개선)
- [6. 아키텍처](#6-인프라-아키텍처-/-적용-기술)
- [7. ERD / API 문서](#7-ERD-/-API-문서) 
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
    <a href="https://yeim.notion.site/INSERT-UPDATE-1ad16458a6bf80dbaefefe3bfadff7b2?pvs>https://yeim.notion.site/INSERT-UPDATE-1ad16458a6bf80dbaefefe3bfadff7b2?pvs=4">💡 입찰을 INSERT가 아닌, UPDATE로 한 이유</a>
  </summary>
</details>


<br/>


## 5. 트러블 슈팅 / 성능 개선

<details>
    <summary><a href="https://yeim.notion.site/AWS-EC2-19b16458a6bf808391f0c77b5f21fd22">[🎯 트러블슈팅] AWS EC2 서버 시간 불일치 이슈</a></summary>
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

