FROM openjdk:8-alpine
ADD target/mascotte-process-1.0-SNAPSHOT.jar mascotte-process.jar

# Set default timezone
ENV TZ=Europe/Moscow

ENTRYPOINT ["java", "-jar", "mascotte-process.jar"]