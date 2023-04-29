FROM openjdk:17-oracle as mysqldoc
EXPOSE 8084
WORKDIR /app

# Copy maven executable to the image
COPY mvnw .
#COPY .mvn .mvn

# Copy the pom.xml file
COPY pom.xml .

# Copy the project source
FROM openjdk:17-oracle
MAINTAINER baeldung.com
COPY target/spring-social-0.0.1-SNAPSHOT.jar spring-social-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/spring-social-0.0.1-SNAPSHOT.jar"]