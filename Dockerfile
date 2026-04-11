# 1단계: 빌드용 이미지
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# 소스 코드 복사 (끝에 ./ 를 붙여 디렉터리임을 명시합니다)
COPY . ./

# gradlew 실행 권한 부여 및 빌드
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

# 2단계: 실행용 가벼운 이미지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 빌드된 jar 파일 복사 (--from=build 로 수정하고 목적지 경로를 수정했습니다)
# settings.gradle의 name(codeshow_backend)과 build.gradle의 version(0.0.1-SNAPSHOT) 기준입니다.
COPY --from=build /app/build/libs/codeshow_backend-0.0.1-SNAPSHOT.jar ./app.jar

# 서버 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]