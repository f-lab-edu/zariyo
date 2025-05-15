FROM openjdk:17-jdk-slim
WORKDIR /zariyo
ARG JWT_SECRET
ARG DB_USERNAME
ARG DB_PASSWORD

ENV JWT_SECRET=$JWT_SECRET
ENV DB_USERNAME=$DB_USERNAME
ENV DB_PASSWORD=$DB_PASSWORD
COPY zariyo-main/build/libs/zariyo-main.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
