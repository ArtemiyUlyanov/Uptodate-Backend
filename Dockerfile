FROM openjdk:17-jdk-slim

WORKDIR /app
<<<<<<< HEAD
COPY target/uptodate-1.0-SNAPSHOT.jar /app/uptodate-1.0-SNAPSHOT.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "uptodate-1.0-SNAPSHOT.jar"]
=======

COPY target/uptodate-0.2.1.jar /app/uptodate-0.2.1.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "uptodate-0.2.1.jar"]
>>>>>>> dev
