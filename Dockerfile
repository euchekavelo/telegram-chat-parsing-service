# -------- STAGE 1: Сборка --------
FROM gradle:8.10-jdk23-alpine as builder
WORKDIR /build
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test --no-daemon


# -------- STAGE 2: Запуск в окружении с готовым JRE --------
FROM eclipse-temurin:23-jre-alpine
RUN mkdir /app
COPY --from=builder /build/build/libs/*.jar /app/telegram-chat-parsing-service.jar
EXPOSE 8080
WORKDIR /app
CMD java -jar telegram-chat-parsing-service.jar