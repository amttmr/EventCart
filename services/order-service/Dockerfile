FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
RUN chmod +x mvnw && ./mvnw -pl services/order-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/services/order-service/target/order-service-*.jar /app/app.jar
EXPOSE 8083
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
