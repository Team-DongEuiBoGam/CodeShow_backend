# 🎬 CodeShow Backend
**AI 기반 코드 애니메이션 플랫폼 'CodeShow'의 백엔드 서버입니다.**
사용자가 입력한 코드를 OpenAI API로 분석하여 시각화를 위한 JSON 데이터를 생성하고, 이를 관리하는 기능을 제공합니다.

<br>

## 🔗 Live Demo
**Web**
- https://codeshow-ai-app.onrender.com

**Backend**
- https://codeshow-backend.onrender.com/swagger-ui/index.html

<br>

## 🛠 Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.3.5
- **Database**: MySQL, Spring Data JPA
- **Security**: Spring Security, JWT (JSON Web Token)
- **API Docs**: Swagger (Springdoc OpenAPI)
- **External API**: OpenAI API (gpt-4o-mini)
- **Deployment**: Docker, Render/Vercel (추가 가능)

<br>

## 🏗 Architecture & Flow
1. **Client Request**: 클라이언트(React 등)에서 코드 분석 및 애니메이션 생성 요청
2. **Authentication**: `JwtAuthenticationFilter`를 통해 회원은 DB 기반 인증, 비회원은 Guest 권한으로 API 접근 제어
3. **AI Processing**: `AiService`에서 프롬프트 엔지니어링을 통해 코드를 분석하고, 애니메이션 시각화를 위한 정형화된 JSON 데이터 추출
4. **Data Persistence**: 생성된 메타데이터 및 JSON 구조를 MySQL에 저장하여 이후 재조회 및 공유 지원

<br>

## 💡 Key Technical Decisions & Features

**1. OpenAI API 응답의 정형화 (JSON 강제)**
- AI 모델이 일관성 없는 텍스트를 반환하는 것을 방지하기 위해 `response_format: { "type": "json_object" }` 옵션을 적용했습니다.
- 프롬프트에 `variables` 및 `operations` 배열이 포함된 구체적인 JSON 스키마 예시를 제공하여 프론트엔드에서 즉시 렌더링 가능한 데이터를 추출합니다.

**2. JWT 기반 상태 무저장(Stateless) 인증 및 Guest 로그인**
- 서버의 확장성을 고려하여 세션 대신 JWT를 사용한 인증 아키텍처를 구성했습니다.
- 서비스를 체험해보고 싶은 사용자를 위해 DB 저장 없이 임시 토큰을 발급하는 `Guest Login` 기능을 구현하여 사용자 접근성을 높였습니다.

**3. 전역 예외 처리 (Global Exception Handling)**
- `@RestControllerAdvice`를 활용해 일관된 에러 응답 형식(`ErrorResponse`)을 제공합니다.
- `ApiException`, `ImageNotSupportedException` 등 커스텀 예외를 정의하여 비즈니스 로직과 예외 처리 로직을 분리하고 유지보수성을 향상시켰습니다.

<br>

## 📁 Directory Structure
```text
src/main/java/org/example/
├── ai/          # OpenAI API 통신 및 프롬프트 처리
├── animation/   # 애니메이션 메타데이터 CRUD 및 DTO
├── auth/        # JWT 토큰 발급, Spring Security 인증 필터 및 유저 관리
├── common/      # 전역 예외 처리(GlobalExceptionHandler) 및 커스텀 예외
├── config/      # Security, Swagger, JWT, CORS 설정
└── health/      # 서버 및 DB 상태 확인용 Health Check
```

<br>

## 🚀 Getting Started
**Prerequisites**
- JDK 21
- MySQL 8.0+

<br>

**Environment Variables (.env 또는 application.yml)**
- 프로젝트 실행을 위해 아래의 환경 변수 설정이 필요합니다.

```YAML
PORT: 8080
DB_HOST: localhost
DB_PORT: 3306
DB_NAME: codeshow
DB_USERNAME: your_db_username
DB_PASSWORD: your_db_password
JWT_SECRET: your_jwt_secret_key_string
OPENAI_API_KEY: your_openai_api_key
```

<br>

**Build & Run**
```Bash
# 1. Repository Clone
$ git clone https://github.com/Team-DongEuiBoGam/CodeShow_backend.git
$ cd CodeShow_backend

# 2. Build
$ ./gradlew clean build

# 3. Run
$ java -jar build/libs/codeshow_backend-0.0.1-SNAPSHOT.jar
```

<br>

## 📖 API 명세서

### [🔗 애니메이션 API 명세서](https://github.com/Team-DongEuiBoGam/CodeShow_backend/blob/feature/animation-save/README.md)
### [🔗 인증 시스템 API 명세서](https://github.com/Team-DongEuiBoGam/CodeShow_backend/blob/feature/auth/README.md)
### [🔗 AI 시스템 API 명세서](https://github.com/Team-DongEuiBoGam/CodeShow_backend/blob/feature/ai-processing/README.md)

<br>

## 🗄 DB 구조

### ERD
<img width="800" height="252" alt="Image" src="https://github.com/user-attachments/assets/5933515d-f7f3-4761-a2e2-f6c5e8fad97f" />

### 릴레이션 구조
<img width="600" height="486" alt="Image" src="https://github.com/user-attachments/assets/fa1c132e-cca0-407d-8723-28085a62b019" />
