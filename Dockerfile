FROM docker.io/maven:eclipse-temurin AS builder
WORKDIR /app
COPY src/main ./src/main
COPY pom.xml .
RUN mvn clean compile assembly:single

FROM docker.io/eclipse-temurin:23.0.2_7-jre-ubi9-minimal
ARG JAR_NAME
ARG VERSION
WORKDIR /app
COPY --from=builder /app/target/$JAR_NAME-$VERSION-jar-with-dependencies.jar /app/$JAR_NAME-$VERSION.jar
RUN echo "java -jar $JAR_NAME-$VERSION.jar" > "entrypoint.sh" && chmod 744 "entrypoint.sh"
ENTRYPOINT ["/bin/sh"]
CMD ["-c", "/app/entrypoint.sh"]
