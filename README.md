# 한화시스템 Beyond SW 21기 최종 프로젝트 - Rhight

---
# 😄 팀소개 Re:verse
|[김세현](https://github.com/sekong11)|[양승재](https://github.com/DMXZC)|[이상준](https://github.com/Ongsaem0)|[정규원](https://github.com/Gyuwon-Jung)|
|:-:|:-:|:-:|:-:|
| CoreHR | 성과 관리 시스템 | 근태·급여 | 전자결재 |
| <img width="300" alt="image" src="https://github.com/user-attachments/assets/43127b72-a3e6-40fc-9bbe-db60160c1bf5" /> | <img width="300" alt="image" src="https://github.com/user-attachments/assets/9555e12a-6aef-4370-84ed-d414ee7e4987" /> | <img width="300" alt="image" src="https://github.com/user-attachments/assets/2533ae0d-629f-4337-822d-7c138a297369" />| <img width="300" alt="image" src="https://github.com/user-attachments/assets/a740436c-5a48-4943-bfd4-493baa530e0a" /> |


---
# 🛠️ 기술 스택
<!-- Tech Stack Badges -->
🖥 Backend  

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge&logoColor=white)
![Spring Modulith](https://img.shields.io/badge/Spring_Modulith-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

🌐 Frontend  

![Vue.js](https://img.shields.io/badge/Vue.js-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![Pinia](https://img.shields.io/badge/Pinia-FFD859?style=for-the-badge&logoColor=black)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white)
![FullCalendar](https://img.shields.io/badge/FullCalendar-1A252F?style=for-the-badge&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)

🗄 Database  

![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

🛜 DevOps / Infra

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Docker Hub](https://img.shields.io/badge/Docker_Hub-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Amazon EC2](https://img.shields.io/badge/Amazon_EC2-FF9900?style=for-the-badge&logo=amazonec2&logoColor=white)
![Amazon ECR](https://img.shields.io/badge/Amazon_ECR-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![Amazon EKS](https://img.shields.io/badge/Amazon_EKS-FF9900?style=for-the-badge&logo=amazoneks&logoColor=white)
![Amazon S3](https://img.shields.io/badge/Amazon_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![Amazon CloudFront](https://img.shields.io/badge/Amazon_CloudFront-8C4FFF?style=for-the-badge&logo=amazonaws&logoColor=white)

🛠 CI / CD  

![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white)

🔧 Collaboration & Tools  

![Git](https://img.shields.io/badge/Git-F05032?style=flat-square&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=flat-square&logo=github&logoColor=white)
![Jira](https://img.shields.io/badge/Jira-0052CC?style=flat-square&logo=jira&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=discord&logoColor=white)

---
# ℹ️ 프로젝트 소개

## 1. 프로젝트 개요

**RHight**는 근태, 급여, 전자결재, 공지사항, 조직/인사 정보 조회 등 사내 인사 운영 전반을 통합 관리할 수 있도록 설계한 **HR 관리 시스템**입니다.

단순한 기능 나열형 시스템이 아니라, 사용자가 자주 확인하는 핵심 정보를 **대시보드 중심으로 빠르게 접근**할 수 있도록 구성했으며,  
**역할 기반 접근 제어**를 통해 사용자별로 필요한 기능만 직관적으로 사용할 수 있도록 설계했습니다.

또한 근태 관리, 급여 조회, 전자결재, 조직도, 마이페이지 등 실무에서 자주 사용하는 기능들을 하나의 흐름 안에서 유기적으로 연결하여,  
복잡한 탐색 없이 업무를 처리할 수 있는 **효율적인 통합 인사 관리 플랫폼**을 목표로 구현했습니다.

특히 RHight는 단순한 관리 기능 제공을 넘어, **성과를 보다 객관적으로 확인하고 반영할 수 있는 성과 관리 시스템**을 핵심 축으로 삼고 있습니다.

---

## 2. 프로젝트 배경

기업이 지속적으로 성장하기 위해서는 핵심 인재의 이탈을 줄이는 것이 중요합니다.  
초기에는 이탈 가능성이 높은 직원을 예측하는 방향도 고려했지만, 단순한 예측만으로는 실질적인 해결에 한계가 있다고 판단했습니다.

저희는 문제의 본질을 “구성원이 자신의 기여를 충분히 인정받고 있는가”에 두었습니다.  
직원이 조직을 떠나는 이유 중 하나는 단순한 보상이 아니라, 자신의 노력과 성과가 공정하게 평가되지 않는다고 느끼는 데 있다고 보았습니다.

이에 RHight는 주관적인 인상이나 감정에 의존하는 평가가 아니라,  
보다 명확한 기준을 바탕으로 성과를 확인하고 반영할 수 있는 **성과 기반 점수 시스템**을 핵심 방향으로 설계했습니다.

이를 통해 조직은 구성원의 기여를 보다 객관적으로 파악할 수 있고,  
구성원은 자신의 성과가 어떤 기준으로 반영되는지 투명하게 확인할 수 있도록 하는 것을 목표로 했습니다.

---

# 💡 주요 기능

## 성과 관리 시스템
- 성과를 점수 기반으로 관리하여 보다 객관적인 평가가 가능하도록 구성했습니다.
- 개인 및 팀 단위의 성과를 확인할 수 있으며, 평가 반영 비율 관리 등 성과 중심 인사 운영을 지원합니다.

## 역할 기반 접근 제어
- 사용자 역할에 따라 접근 가능한 메뉴와 기능을 구분하여 필요한 화면만 노출되도록 구현했습니다.
- 이를 통해 보안성과 사용 편의성을 함께 확보했습니다.

## 대시보드 중심 정보 제공
- 사용자가 자주 확인하는 근태, 결재, 공지사항, 급여, 일정 등의 핵심 정보를 메인 화면과 각 도메인 대시보드에서 한눈에 확인할 수 있도록 구성했습니다.
- 카드형 UI를 통해 주요 현황을 빠르게 파악하고, 상세 페이지로 자연스럽게 이동할 수 있도록 설계했습니다.

## 근태 관리
- 출퇴근 기록, 주간/월간 근무 시간, 근태 신청 내역, 팀 근태 현황 등을 직관적으로 확인할 수 있습니다.
- 개인 근태와 팀 근태를 함께 관리할 수 있도록 구성하여 실무 활용도를 높였습니다.

## 급여 관리
- 급여 정보는 비밀번호 재확인 후에만 조회 가능하도록 하여 민감 정보에 대한 보안을 강화했습니다.
- 실수령액, 공제 내역, 지급 내역을 대시보드에서 확인할 수 있으며, 급여 명세서 다운로드 기능도 제공합니다.

## 전자결재
- 결재 대기, 진행, 완료 문서 현황을 한눈에 확인할 수 있습니다.
- 결재선 지정, 문서 진행 상태 추적, 문서 상세 확인 기능을 통해 결재 흐름을 효율적으로 관리할 수 있습니다.

## 조직 / 인사 정보 관리
- 마이페이지, 내 조직 조회, 조직도 기능을 통해 개인 정보와 조직 정보를 쉽게 확인할 수 있도록 구성했습니다.
- 조직 구성원 탐색과 인사 정보 확인이 용이하도록 시각적 구조를 단순화했습니다.

## 직관적인 UI/UX
- 헤더는 대분류, 사이드바는 세부 메뉴 중심으로 구성하여 메뉴 탐색의 depth를 줄였습니다.
- 메인 화면의 바로가기 기능을 통해 사용자가 자주 사용하는 메뉴에 더욱 빠르게 접근할 수 있도록 설계했습니다.


---


---
# 파이프 라인 구조도


---
# 요구사항 명세서
<details>
<summary>요구사항 명세서  </summary>
<div markdown="1">



</div>
</details>

---
