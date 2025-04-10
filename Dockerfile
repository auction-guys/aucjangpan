FROM openjdk:17
WORKDIR /app
COPY build/libs/auction-0.0.1-SNAPSHOT.jar auction-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "auction-0.0.1-SNAPSHOT.jar"]