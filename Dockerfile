FROM maven:3.9.8-eclipse-temurin-22-alpine AS build
COPY src app/src
COPY src/main/resources/application-prod.properties app/src/main/resources/application.properties
COPY pom.xml app/
RUN mvn -f app/pom.xml -Dmaven.test.skip package

FROM openjdk:23-oracle
COPY --from=build app/target/hsse-tech-backend-0.0.1-SNAPSHOT.jar /usr/local/lib/hsse-tech.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/hsse-tech.jar"]
