FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# build/libs 디렉토리에 생성된 실행 가능한 jar 파일을 복사
COPY build/libs/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
