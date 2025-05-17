FROM openjdk:17-jdk-slim
WORKDIR /zariyo

COPY build/libs/zariyo-main.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
