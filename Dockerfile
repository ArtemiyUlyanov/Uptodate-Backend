FROM openjdk:17-jdk-slim

WORKDIR /app
COPY target/uptodate-1.0-SNAPSHOT.jar /app/uptodate-1.0-SNAPSHOT.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "uptodate-1.0-SNAPSHOT.jar"]