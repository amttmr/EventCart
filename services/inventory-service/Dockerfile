FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -pl services/inventory-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/inventory-service/target/inventory-service-*.jar /app/app.jar
EXPOSE 8084
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
