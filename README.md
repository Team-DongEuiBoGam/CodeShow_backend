# 인증 시스템
---
<br>

<details>
<summary>

## 회원가입 API

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | 회원가입 (Signup) |
| **설명** | 신규 사용자의 계정을 생성하고 즉시 로그인 처리(JWT 발급)를 수행함 |
| **HTTP Method** | `POST` |
| **Endpoint** | `/api/auth/signup` |
| **요청 형식** | JSON (Request Body) |
| **응답 형식** | JSON |
| **인증 필요 여부** | 불필요 |

<br>

### Request Body

- **클라이언트에서 전송하는 JSON 파라미터:**
    
    ```json
    {
      "loginId": "developer_kim",
      "password": "securePassword123!",
      "username": "개발자킴"
    }
    ```
    

- **Request Body 설명**
    
    
    | **파라미터명** | **위치** | **타입** | **설명** | **필수** |
    | --- | --- | --- | --- | --- |
    | `loginId` | Body | String | 사용할 로그인 아이디 (4~25자, 영문/숫자/밑줄만 허용) | Ο |
    | `password` | Body | String | 비밀번호 (최소 8자 이상) | Ο |
    | `username` | Body | String | 사용자 닉네임 (최대 10자) | Ο |

<br>

### Validations (백엔드 검증 규칙)

- **중복 아이디 확인**
    - `loginId`가 DB(`user_mst`)에 이미 존재하는지 확인
    - 존재하면 400 Bad Request 처리 ("이미 사용 중인 아이디입니다.")
- **입력값 유효성 검사 (Validation)**
    - `loginId`, `password`, `username` 누락 여부 확인
    - 길이 제한 및 정규식 위반 시 400 Bad Request 처리

<br>

### 성공 Response

- **201 Created**

```json
{
  "userId": 5,
  "loginId": "developer_kim",
  "username": "개발자킴",
  "role": "USER",
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1Iiwi..."
}
```

<br>

### 실패 Response

- **400 Bad Request - 중복 아이디 / 형식 오류**

```json
{
  "status": 400,
  "message": "이미 사용 중인 아이디입니다.",
  "timestamp": "2026-04-11T19:05:22"
}
```

<br>

### 생성 시 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **비밀번호 암호화** | `BCryptPasswordEncoder`를 사용하여 평문 비밀번호 단방향 해싱 처리 |
| **데이터 삽입** | `user_mst` 테이블에 신규 회원 정보 Insert (생성일자 `createDate` 자동 기록) |
| **JWT 발급** | 가입 완료 후 즉시 사용 가능한 인증 토큰(Access Token) 생성 후 반환 |

<br>

### 실행되는 SQL 예시

```sql
-- 1. 아이디 중복 확인을 위한 조회
SELECT COUNT(*) FROM user_mst WHERE login_id = 'developer_kim';

-- 2. 사용자 생성 (비밀번호는 암호화된 상태)
INSERT INTO user_mst (login_id, password, user_name, create_date)
VALUES ('developer_kim', '$2a$10$w...암호화된문자열...', '개발자킴', '2026-04-11');
```
</summary>
</details>
  

## 로그인 API

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | 로그인 (Login) |
| **설명** | 기존 사용자의 아이디와 비밀번호를 검증하고 JWT 토큰을 발급함 |
| **HTTP Method** | `POST` |
| **Endpoint** | `/api/auth/login` |
| **요청 형식** | JSON (Request Body) |
| **응답 형식** | JSON |
| **인증 필요 여부** | 불필요 |

<br>

### Request Body

- 클라이언트에서 전송하는 JSON 파라미터:

```json
{
  "loginId": "developer_kim",
  "password": "securePassword123!"
}
```

- **Request Body 설명**
    
    
    | **파라미터명** | **위치** | **타입** | **설명** | **필수** |
    | --- | --- | --- | --- | --- |
    | `loginId` | Body | String | 사용자의 로그인 아이디 | Ο |
    | `password` | Body | String | 사용자의 비밀번호 | Ο |

<br>

### Validations (백엔드 검증 규칙)

- **입력값 유효성 검사 (Validation)**
    - `loginId`, `password` 누락 여부 확인
- **로그인 및 권한 확인**
    - `AuthenticationManager`를 통해 아이디와 비밀번호 일치 여부 검증
    - 검증 실패 시 401 Unauthorized 처리 ("아이디 또는 비밀번호가 올바르지 않습니다.")
- **존재 여부 확인**
    - 인증 성공 후 해당 사용자가 DB에 존재하는지 확인 (없으면 404 Not Found)

<br>

### 성공 Response

- **200 OK**
    
    ```json
    {
      "userId": 5,
      "loginId": "developer_kim",
      "username": "개발자킴",
      "role": "USER",
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1Iiwi..."
    }
    ```
    

<br>

### 실패 Response

- **401 Unauthorized - 아이디 또는 비밀번호 불일치**
    
    ```json
    {
      "status": 401,
      "message": "아이디 또는 비밀번호가 올바르지 않습니다.",
      "timestamp": "2026-04-11T19:10:00"
    }
    ```
    

<br>

### 생성 시 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **인증 정보 검증** | 입력된 비밀번호를 암호화된 DB 비밀번호와 대조하여 시큐리티 컨텍스트에서 검증 |
| **회원 정보 조회** | 검증 완료 후 `user_mst` 테이블에서 사용자 기본 정보 조회 |
| **JWT 발급** | 인증 성공 시 사용자의 ID, 이름, 권한(Role)을 담은 Access Token 생성 후 반환 |

<br>

### 실행되는 SQL 예시

```sql
-- 1. 사용자 인증 및 토큰 발급을 위한 정보 조회
SELECT * FROM user_mst WHERE login_id = 'developer_kim';
```

---
<br>

## 비회원 로그인 API

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | 비회원 로그인 (Guest Login) |
| **설명** | 비회원 사용자를 위한 임시 게스트 토큰을 발급함 |
| **HTTP Method** | `POST` |
| **Endpoint** | `/api/auth/guest-login` |
| **요청 형식** | 없음 |
| **응답 형식** | JSON |
| **인증 필요 여부** | 불필요 |

<br>

### Request Body

- 별도의 Request Body를 전송하지 않습니다.

<br>

### Validations (백엔드 검증 규칙)

- 별도의 검증 로직 없이 즉시 임시 닉네임과 토큰을 생성합니다.

<br>

## 성공 Response

- **200 OK**
    
    ```json
    {
      "userId": null,
      "loginId": null,
      "username": "guest-1712838392012",
      "role": "GUEST",
      "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJndWVzdCIs..."
    }
    ```
    

<br>

### 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **데이터 미저장** | 비회원은 `user_mst` DB에 데이터를 저장하지 않습니다. |
| **임시 이름 생성** | `guest-` 접두사와 현재 시간(System.currentTimeMillis)을 조합하여 임시 닉네임을 생성합니다. |
| **JWT 발급** | GUEST 권한을 가진 임시 Access Token을 즉시 생성하여 응답으로 반환합니다. |
---
<br>

## 내 정보 조회 API

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | 내 정보 조회 (Get Current User) |
| **설명** | 현재 로그인한 사용자(회원 또는 게스트)의 정보를 조회함 |
| **HTTP Method** | `GET` |
| **Endpoint** | `/api/auth/me` |
| **요청 형식** | Header (JWT 토큰) |
| **응답 형식** | JSON |
| **인증 필요 여부** | 필요 (회원 및 비회원 게스트 모두 조회 가능) |

<br>

### Request Header

- 헤더에 인증 토큰을 담아 전송해야 합니다.
    
    ```html
    Authorization: Bearer {accessToken}
    ```
    
<br>

### Validations (백엔드 검증 규칙)

- **로그인 및 인증 확인**
    - 헤더로 전달된 JWT 토큰의 유효성을 검증합니다.
    - 토큰이 없거나 만료된 경우 401 Unauthorized 처리됩니다.
- **회원 존재 여부 확인 (회원의 경우)**
    - 회원(`ROLE_USER`) 토큰인 경우 DB에서 해당 회원의 정보를 다시 조회하여 존재하는지 확인합니다 (없을 경우 404 Not Found).

<br>

### 성공 Response

- **200 OK (회원일 경우)**
    
    ```json
    {
      "userId": 5,
      "loginId": "developer_kim",
      "username": "개발자킴",
      "role": "USER"
    }
    ```
    
- **200 OK (비회원 게스트일 경우)**
    
    ```json
    {
      "userId": null,
      "loginId": null,
      "username": "guest-1712838392012",
      "role": "GUEST"
    }
    ```
    
<br>

### 실패 Response

- **401 Unauthorized - 인증 실패 (유효하지 않은 토큰이거나 인증 정보가 누락된 경우 발생합니다.)**
    
    ```json
    {
      "status": 401,
      "message": "인증이 필요합니다.",
      "timestamp": null
    }
    ```
    
- **404 Not Found - 사용자를 찾을 수 없음** (회원 정보가 삭제된 경우 등)
    
    ```json
    {
      "status": 404,
      "message": "사용자를 찾을 수 없습니다.",
      "timestamp": "2026-04-11T19:30:00"
    }
    ```
    
<br>

### 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **사용자 권한 분기** | 시큐리티 컨텍스트에 담긴 `CustomUserPrincipal`의 역할을 확인하여 회원과 게스트 요청을 분기 처리합니다. |
| **회원 정보 조회** | 회원의 경우 최신 정보를 내려주기 위해 `user_mst` 테이블을 조회합니다. |
| **게스트 정보 응답** | 게스트의 경우 DB 조회 없이 토큰에 저장된 임시 이름(displayName) 정보를 그대로 내려줍니다. |

<br>

### 실행되는 SQL 예시 (회원일 경우)

```sql
-- 회원 토큰으로 접근 시 최신 정보를 확인하기 위해 조회 실행
SELECT * FROM user_mst WHERE user_id = 5;
```
