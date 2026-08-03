ARG JAVA_BUILD_IMAGE=eclipse-temurin:21-jdk
ARG JAVA_RUNTIME_IMAGE=eclipse-temurin:21-jre

FROM ${JAVA_BUILD_IMAGE} AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -pl services/catalog-service -am -DskipTests package

FROM ${JAVA_RUNTIME_IMAGE}
WORKDIR /app
COPY --from=build /workspace/services/catalog-service/target/catalog-service-*.jar /app/app.jar
EXPOSE 8081
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
