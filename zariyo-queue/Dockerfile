FROM openjdk:17-jdk-slim
WORKDIR /zariyo

COPY build/libs/zariyo-queue.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
