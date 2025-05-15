FROM openjdk:17-jdk-slim
WORKDIR /zariyo
COPY zariyo-main/build/libs/zariyo-queue.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
