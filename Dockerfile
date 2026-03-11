FROM eclipse-temurin:17-jre
WORKDIR /app

ARG JAR_FILE=api/build/libs/*.jar
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
