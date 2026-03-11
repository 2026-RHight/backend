FROM eclipse-temurin:17-jre
WORKDIR /app

RUN groupadd -g 10001 app && useradd -u 10001 -g app -s /usr/sbin/nologin -m app

ARG JAR_FILE=api/build/libs/*.jar
COPY --chown=10001:10001 ${JAR_FILE} /app/app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
