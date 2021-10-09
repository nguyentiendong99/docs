FROM openjdk:8-jdk-alpine

# Set the current working directory inside the image
WORKDIR /server

# Copy maven executable to the image
COPY . .

# Package the application
RUN ./mvnw verify -Pprod -DskipTests

EXPOSE 8080

ENTRYPOINT ["java","-jar","./target/lk-backend-0.0.1-SNAPSHOT.war","--spring.profiles.active=dev"]