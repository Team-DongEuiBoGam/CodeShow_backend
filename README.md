# 애니메이션
---

<details>
<summary> <h2> 애니메이션 저장 API </summary>
<br>

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | 애니메이션 저장 (Create Animation) |
| **설명** | 사용자가 작성한 원본 코드와 AI가 분석한 JSON 데이터를 매핑하여 저장함 |
| **HTTP Method** | `POST` |
| **Endpoint** | `/api/animations` |
| **요청 형식** | JSON (Request Body) |
| **응답 형식** | JSON |
| **인증 필요 여부** | 필요 (회원인 `ROLE_USER`만 가능, 비회원 게스트 불가) |

<br>

### Request Body

- 클라이언트에서 전송하는 JSON 파라미터:

JSON

```json
{
  "animationName": "버블 정렬 시각화",
  "originalCode": "int[] arr = {5, 3, 1};",
  "languageId": 1,
  "jsonData": "{\"frames\": 10, \"type\": \"sort\"}"
}
```

- **Request Body 설명**

| **파라미터명** | **위치** | **타입** | **설명** | **필수** |
| --- | --- | --- | --- | --- |
| `animationName` | Body | String | 애니메이션 이름 (최대 25자) | O |
| `originalCode` | Body | String | 작성된 원본 소스 코드 | O |
| `languageId` | Body | Integer | 사용된 프로그래밍 언어의 고유 ID (`language_id`) | O |
| `jsonData` | Body | String | AI 모델이 반환한 시각화용 JSON 문자열 (최대 10000자) | O |

<br>

### Validations (백엔드 검증 규칙)

- **로그인 및 권한 확인**
    - 요청 헤더의 JWT 토큰 확인.
    - 게스트(`ROLE_GUEST`) 로그인 상태이거나 토큰이 없으면 저장 불가
- **입력값 유효성 검사 (Validation)**
    - `animationName`, `originalCode`, `languageId`, `jsonData` 누락 여부 확인
    - `originalCode`에 이미지 확장자(.png, .jpg 등)가 포함되면 `ImageNotSupportedException` 발생 후 `400 Bad Request` 처리
    - `jsonData`가 유효한 JSON 구조인지 ObjectMapper로 파싱 테스트, 실패 시 `400 Bad Request`
- **존재 여부 확인**
    - 토큰에 해당하는 사용자가 DB에 존재하는지, `languageId`가 실제 DB에 있는지 확인 (`404 Not Found`)

<br>

### 성공 Response

- **201 Created**

```json
{
  "animationId": 12,
  "animationName": "버블 정렬 시각화",
  "originalCode": "int[] arr = {5, 3, 1};",
  "jsonData": "{\"frames\": 10, \"type\": \"sort\"}",
  "languageId": 1,
  "languageName": "Java",
  "creatorUserNumber": 5,
  "creatorUsername": "개발자킴",
  "createdAt": "2026-04-11"
}
```

<br>

### 실패 Response

- **400 Bad Request — 잘못된 요청 / 이미지 첨부 시도**

```json
{
  "status": 400,
  "message": "이미지 파일은 지원하지 않습니다. 텍스트 형태의 코드를 직접 입력해 주세요.",
  "timestamp": "2026-04-11T19:03:00"
}
```

- **403 Forbidden — 게스트 권한으로 생성 시도**

```json
{
  "status": 403,
  "message": "접근 권한이 없습니다.",
  "timestamp": null
}
```

<br>

### 삭제 후/생성 시 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **참조 조회** | 사용자(`user_mst`)와 언어(`language_mst`) 정보를 DB에서 조회 |
| **JSON 검증** | `objectMapper.readTree()`를 사용해 `jsonData` 포맷 무결성 검증 |
| **데이터 삽입** | `animation_mst` 테이블에 데이터 Insert (생성일자 `createdAt` 자동 기록) |

<br>

### **실행되는 SQL 예시**

```sql
-- 1. 외래키 검증을 위한 조회
SELECT * FROM user_mst WHERE user_id = 5;
SELECT * FROM language_mst WHERE language_id = 1;

-- 2. 애니메이션 생성
INSERT INTO animation_mst (animation_name, original_code, json_data, create_date, user_id, language_id)
VALUES ('버블 정렬 시각화', 'int[] arr = {5, 3, 1};', '{"frames": 10, "type": "sort"}', '2026-04-11', 5, 1);
```
</details>

<br>

<details>
<summary> <h2> 애니메이션 목록 조회 API </summary>
<br>

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | 애니메이션 목록 조회 (Get Animations) |
| **설명** | 저장된 전체 애니메이션 목록을 요약하여 조회함 (페이징 지원) |
| **HTTP Method** | `GET` |
| **Endpoint** | `/api/animations` |
| **요청 형식** | Query Parameter |
| **응답 형식** | JSON (Array) |
| **인증 필요 여부** | 필요 (회원인 `ROLE_USER` 및 비회원 `ROLE_GUEST` 모두 가능) |

<br>

### **Request Parameter 설명**

| **파라미터명** | **위치** | **타입** | **설명** | **필수** |
| --- | --- | --- | --- | --- |
| `page` | Query | Integer | 페이지 번호 (0부터 시작) | X |
| `size` | Query | Integer | 페이지당 반환할 항목 수 | X |

<br>

### Validations (백엔드 검증 규칙)

- **로그인 및 권한 확인**
    - 요청 헤더의 JWT 토큰 확인.
    - 회원 또는 게스트 권한이 없는 비인증 요청인 경우 401 Unauthorized 처리

<br>

### 성공 Response

- **200 OK**

```json
[
  {
    "animationId": 1,
    "animationName": "버블 정렬 시각화",
    "languageId": 1,
    "languageName": "Java",
    "creatorUserNumber": 5,
    "creatorUsername": "개발자킴",
    "createdAt": "2026-04-11"
  },
  {
    "animationId": 2,
    "animationName": "DFS 탐색",
    "languageId": 2,
    "languageName": "Python",
    "creatorUserNumber": 3,
    "creatorUsername": "알고리즘마스터",
    "createdAt": "2026-04-11"
  }
]
```

<br>

### 실패 Response

- **401 Unauthorized - 인증 정보 없음**

```json
{
  "status": 401,
  "message": "인증이 필요합니다.",
  "timestamp": null
}
```

<br>

### 조회 시 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **권한 검증** | `validateViewer` 메서드를 통해 조회 권한 확인 |
| **데이터 조회** | `findAllWithLanguageAndCreator()`를 호출하여 `animation_mst`, `language_mst`, `user_mst`를 Fetch Join으로 한 번에 조회 (N+1 문제 방지) |
| **페이징 처리** | 조회된 리스트에서 `page`와 `size` 값에 따라 메모리상에서 서브리스트 분리 |

<br>

### 실행되는 SQL 예시

```sql
-- Fetch Join으로 연관된 언어와 작성자 정보를 한 번의 쿼리로 조회
SELECT a.*, l.*, u.* FROM animation_mst a 
INNER JOIN language_mst l ON a.language_id = l.language_id 
INNER JOIN user_mst u ON a.user_id = u.user_id;
```
</details>

<br>

<details>
<summary> <h2> 애니메이션 상세 조회 API </summary>
<br>

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | 애니메이션 상세 조회 (Get Animation Detail) |
| **설명** | 특정 애니메이션의 원본 코드 및 JSON 데이터를 포함한 상세 정보를 조회함 |
| **HTTP Method** | `GET` |
| **Endpoint** | `/api/animations/{animationId}` |
| **요청 형식** | Path Variable |
| **응답 형식** | JSON |
| **인증 필요 여부** | 필요 (회원인 `ROLE_USER` 및 비회원 `ROLE_GUEST` 모두 가능) |

<br>

### Request Parameter 설명

| **파라미터명** | **위치** | **타입** | **설명** | **필수** |
| --- | --- | --- | --- | --- |
| `animationId` | Path | Integer | 조회할 애니메이션의 고유 ID (`animation_code`) | O |

<br>

## Validations (백엔드 검증 규칙)

- **로그인 및 권한 확인**
    - 요청 헤더의 JWT 토큰 확인.
    - 권한이 없는 비인증 요청인 경우 401 Unauthorized 처리
- **존재 여부 확인**
    - `animationId`에 해당하는 애니메이션 기록이 DB에 존재하는지 확인
    - 없으면 404 Not Found 처리

<br>

## 성공 Response

- **200 OK**

```json
{
  "animationId": 12,
  "animationName": "버블 정렬 시각화",
  "originalCode": "int[] arr = {5, 3, 1};",
  "jsonData": "{\"frames\": 10, \"type\": \"sort\"}",
  "languageId": 1,
  "languageName": "Java",
  "creatorUserNumber": 5,
  "creatorUsername": "개발자킴",
  "createdAt": "2026-04-11"
}
```

<br>

## 실패 Response

- **401 Unauthorized - 인증 정보 없음**

```json
{
  "status": 401,
  "message": "인증이 필요합니다.",
  "timestamp": null
}
```

- **404 Not Found - 존재하지 않는 애니메이션**

```json
{
  "status": 404,
  "message": "애니메이션을 찾을 수 없습니다.",
  "timestamp": "2026-04-11T19:28:00"
}
```

<br>

### 조회 시 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **권한 검증** | `validateViewer` 메서드를 통해 조회 권한(회원/비회원) 유효성 확인 |
| **단건 데이터 조회** | `findByIdWithLanguageAndCreator()`를 호출하여 특정 ID의 애니메이션 데이터와 작성자, 언어 정보를 Fetch Join으로 한 번에 조회 |

<br>

### 실행되는 SQL 예시

```sql
-- 특정 ID에 대해 Fetch Join으로 연관된 언어와 작성자 정보를 함께 조회
SELECT a.*, l.*, u.* FROM animation_mst a 
INNER JOIN language_mst l ON a.language_id = l.language_id 
INNER JOIN user_mst u ON a.user_id = u.user_id
WHERE a.animation_code = 12;
```
</details>

<br>

<details>
<summary> <h2> 애니메이션 수정 API </summary>
<br>

### API 개요

| **항목** | **내용** |
| --- | --- |
| API 이름 | 애니메이션 수정 (Update Animation) |
| 설명 | 생성된 애니메이션의 이름을 수정함 |
| HTTP Method | PATCH |
| Endpoint | `/api/animations/{animationId}` |
| 요청 형식 | JSON (Request Body) |
| 응답 형식 | JSON |
| 인증 필요 여부 | 필요 (회원인 ROLE_USER 중 본인만 가능, 비회원 게스트 불가) |

### Request Path Variable & Body

클라이언트에서 전송하는 경로 변수 및 JSON 파라미터:

- **Path Variable:** `animationId` (수정할 대상 애니메이션의 ID)
- **JSON:**
    
    ```json
    {
      "animationName": "수정된 이름"
    }
    ```
    

**Request 파라미터 설명**

| **파라미터명** | **위치** | **타입** | **설명** | **필수** |
| --- | --- | --- | --- | --- |
| `animationId` | Path | Integer | 수정할 애니메이션의 고유 ID | Ο |
| `animationName` | Body | String | 수정할 애니메이션 이름 (최대 25자) | Ο |

### **Validations (백엔드 검증 규칙)**

- **로그인 및 권한 확인**
    - 요청 헤더의 JWT 토큰 확인.
    - 게스트(ROLE_GUEST) 로그인 상태이거나 토큰이 없으면 수정 불가.
- **본인 확인 및 존재 여부**
    - `animationId`에 해당하는 애니메이션이 실제 DB에 존재하는지 확인 (404 Not Found).
    - 조회한 애니메이션의 작성자(creator)와 현재 로그인한 사용자의 ID가 일치하는지 확인 (불일치 시 403 Forbidden).
- **입력값 유효성 검사**
    - `animationName` 누락 여부 및 25자 초과 여부 확인 (실패 시 400 Bad Request).

### **성공 Response**

- **200 OK**
    
    ```json
    {
      "animationId": 12,
      "animationName": "수정된 멋진 이름",
      "originalCode": "int[] arr = {5, 3, 1};",
      "jsonData": "{\"frames\": 10, \"type\": \"sort\"}",
      "languageId": 1,
      "languageName": "Java",
      "creatorUserNumber": 5,
      "creatorUsername": "개발자킴",
      "createdAt": "2026-04-11"
    }
    ```
    

### 실패 Response

- **400 Bad Request** - 잘못된 요청 (이름 누락 또는 글자 수 초과)
    
    ```json
    {
      "status": 400,
      "message": "애니메이션 이름은 필수입니다.",
      "timestamp": "2026-04-11T19:05:00"
    }
    ```
    
- **403 Forbidden** - 게스트 권한으로 시도하거나 본인이 생성하지 않은 애니메이션 수정 시도
    
    ```json
    {
      "status": 403,
      "message": "본인이 저장한 애니메이션만 수정/삭제할 수 있습니다.",
      "timestamp": "2026-04-11T19:05:00"
    }
    ```
    

### 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| 권한 및 참조 조회 | `animation_mst`에서 데이터를 조회하고, `user_mst`의 정보와 비교하여 소유자 권한 검증 |
| 데이터 업데이트 | 엔티티의 `updateName()`을 호출하여 `animation_mst` 테이블의 이름 변경 (Dirty Checking) |

### **실행되는 SQL 예시**

```sql
-- 1. 애니메이션 정보 및 소유자 조회
SELECT * FROM animation_mst a JOIN user_mst u ON a.user_id = u.user_id WHERE a.animation_code = 12;

-- 2. 애니메이션 이름 수정
UPDATE animation_mst SET animation_name = '수정된 멋진 이름' WHERE animation_code = 12;
```
</details>

<br>

<details>
<summary> <h2> 애니메이션 삭제 API </summary>
<br>

### API 개요

| **항목** | **내용** |
| --- | --- |
| API 이름 | 애니메이션 삭제 (Delete Animation) |
| 설명 | 저장된 애니메이션을 데이터베이스에서 삭제함 |
| HTTP Method | DELETE |
| Endpoint | `/api/animations/{animationId}` |
| 요청 형식 | 없음 (Path Variable만 사용) |
| 응답 형식 | 없음 (No Content) |
| 인증 필요 여부 | 필요 (회원인 ROLE_USER 중 본인만 가능, 비회원 게스트 불가) |

### **Request Path Variable**

- **Path Variable:** `animationId` (삭제할 대상 애니메이션의 ID)

- **Request Path Variable**
    
    
    | **파라미터명** | **위치** | **타입** | **설명** | **필수** |
    | --- | --- | --- | --- | --- |
    | `animationId` | Path | Integer | 삭제할 애니메이션의 고유 ID | Ο |

### **Validations (백엔드 검증 규칙)**

- **로그인 및 권한 확인**
    - 요청 헤더의 JWT 토큰 확인.
    - 게스트(ROLE_GUEST) 로그인 상태이거나 토큰이 없으면 삭제 불가.
- **본인 확인 및 존재 여부**
    - `animationId`에 해당하는 애니메이션이 실제 DB에 존재하는지 확인 (404 Not Found).
    - 조회한 애니메이션의 작성자(creator)와 현재 로그인한 사용자의 ID가 일치하는지 확인 (불일치 시 403 Forbidden).

### 성공 Response

- **204 No Content**
    
    ```
    (응답 Body 없음)
    ```
    

### **실패 Response**

- **403 Forbidden** - 게스트 권한으로 시도하거나 본인이 생성하지 않은 애니메이션 삭제 시도
    
    ```json
    {
      "status": 403,
      "message": "본인이 저장한 애니메이션만 수정/삭제할 수 있습니다.",
      "timestamp": "2026-04-11T19:08:00"
    }
    ```
    
- **404 Not Found** - 존재하지 않는 애니메이션 삭제 시도
    
    ```json
    {
      "status": 404,
      "message": "애니메이션을 찾을 수 없습니다.",
      "timestamp": "2026-04-11T19:08:00"
    }
    ```
    

### 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| 권한 및 참조 조회 | `animation_mst`에서 데이터를 조회하고 소유자 권한 검증 (수정 로직과 동일한 검증 메서드 사용) |
| 데이터 삭제 | `animation_mst` 테이블에서 해당 레코드 Delete 처리 |

### **실행되는 SQL 예시**

```sql
-- 1. 애니메이션 정보 및 소유자 조회
SELECT * FROM animation_mst a JOIN user_mst u ON a.user_id = u.user_id WHERE a.animation_code = 12;

-- 2. 애니메이션 삭제
DELETE FROM animation_mst WHERE animation_code = 12;
```
</details>

<br>
