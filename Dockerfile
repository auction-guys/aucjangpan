FROM openjdk:17
WORKDIR /app
COPY build/libs/aucjangpan-0.0.1-SNAPSHOT.jar aucjangpan-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "aucjangpan-0.0.1-SNAPSHOT.jar"]