# 🔹 1단계: 빌드용 베이스 이미지
FROM gradle:7.6-jdk17 AS builder

WORKDIR /app
COPY . .

# GitHub Packages 인증을 위한 ARG 정의
ARG GITHUB_USERNAME
ARG GITHUB_PACKAGES_TOKEN

# 환경변수로 설정
ENV USERNAME=${GITHUB_USERNAME}
ENV GITHUB_PACKAGES_TOKEN=${GITHUB_PACKAGES_TOKEN}

RUN ./gradlew build -x test -Dspring.profiles.active=prod --no-daemon && \
    find build/libs -name "*.jar" | grep -q . || (echo "❌ JAR 파일이 생성되지 않았습니다." && exit 1)

# 🔹 2단계: 런타임 이미지
FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y curl

COPY --from=builder /app/build/libs/app.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
EXPOSE 8080
