FROM adoptopenjdk/openjdk11:alpine-jre
EXPOSE 8080
RUN mkdir -p /app/
ADD build/libs/resources-0.0.1-SNAPSHOT.jar /app/resources-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/app/resources-0.0.1-SNAPSHOT.jar"]
