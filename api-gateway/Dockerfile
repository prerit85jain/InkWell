FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY post-service/pom.xml post-service/
COPY comment-service/pom.xml comment-service/
COPY category-service/pom.xml category-service/
COPY media-service/pom.xml media-service/
COPY newsletter-service/pom.xml newsletter-service/
COPY notification-service/pom.xml notification-service/
COPY inkwell-web/pom.xml inkwell-web/
RUN apk add maven
COPY api-gateway/src api-gateway/src
RUN mvn -f api-gateway/pom clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
