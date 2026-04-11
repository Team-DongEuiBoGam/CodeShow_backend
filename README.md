# AI 시스템
---

<details>
<summary> <h2> AI 코드 분석 API </summary>
<br>

### API 개요

| **항목** | **내용** |
| --- | --- |
| **API 이름** | AI 코드 분석 (Analyze Code) |
| **설명** | 작성된 원본 코드를 OpenAI API에 전달하여 시각화용 JSON 데이터를 반환함 |
| **HTTP Method** | `POST` |
| **Endpoint** | `/api/ai/analyze` (또는 `/api/animations/analyze`) |
| **요청 형식** | Plain Text (String) |
| **응답 형식** | JSON (String) |
| **인증 필요 여부** | 필요 (로그인한 회원 및 게스트 모두 가능) |

<br>

### Request Body

- 클라이언트에서 전송하는 파라미터 (순수 텍스트 코드):
    
    ```
    int a = 10;
    int b = 20;
    int sum = a + b;
    ```
    

- **Request Body 설명**
    
    
    | **파라미터명** | **위치** | **타입** | **설명** | **필수** |
    | --- | --- | --- | --- | --- |
    | (본문 전체) | Body | String | AI에 분석을 요청할 소스 코드 | Ο |

<br>

### Validations (백엔드 검증 규칙)

- **로그인 및 권한 확인**
    - 요청 헤더의 JWT 토큰 확인.
    - 유효한 인증 토큰이 없으면 401 Unauthorized 처리

<br>

### 성공 Response (출력 결과)

- **200 OK** (OpenAI가 반환한 JSON 문자열)
    
    ```json
    {
      "explanation": "두 개의 정수 변수를 선언하고 초기화한 뒤, 더한 결과를 새로운 변수에 저장합니다.",
      "frames": [
        { "type": "declare", "variable": "a", "value": 10 },
        { "type": "declare", "variable": "b", "value": 20 },
        { "type": "add", "result": "sum", "value": 30 }
      ]
    }
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
    
- **500 Internal Server Error - API 연동 실패 등 서버 오류**
    
    ```json
    {
      "status": 500,
      "message": "서버 내부 오류가 발생했습니다.",
      "timestamp": "2026-04-11T21:49:00"
    }
    ```
    
<br>

### 처리사항 (Backend Logic)

| **처리** | **설명** |
| --- | --- |
| **프롬프트 구성** | OpenAI API에 전달할 프롬프트에 한국어(Korean) 강제 지시 및 오직 단일 JSON 객체 포맷 응답 요구 사항을 추가함 |
| **API 호출** | `RestClient`를 사용하여 OpenAI `gpt-4o-mini` 모델에 HTTP POST 요청을 전송함 |
| **결과 파싱** | ChatGPT 응답 구조(`choices[0].message.content`)에서 실제 메시지 내용만 추출하여 클라이언트에 반환함 |

<br>

### 실행되는 SQL은 없음

**→ 해당 API는 DB 조회를 수행하지 않고 외부 OpenAI API와 통신합니다. (실행되는 SQL 없음)**
</details> <br>
