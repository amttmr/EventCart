ARG JAVA_BUILD_IMAGE=eclipse-temurin:25-jdk
ARG JAVA_RUNTIME_IMAGE=eclipse-temurin:25-jre

FROM ${JAVA_BUILD_IMAGE} AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -pl services/inventory-service -am -DskipTests package

FROM ${JAVA_RUNTIME_IMAGE}
WORKDIR /app
COPY --from=build /workspace/services/inventory-service/target/inventory-service-*.jar /app/app.jar
EXPOSE 8084
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
