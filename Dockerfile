FROM maven:3.9-eclipse-temurin-11 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests

FROM tomcat:9-jdk11
COPY --from=builder /app/server/target/launcher.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
