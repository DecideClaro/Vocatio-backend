FROM eclipse-temurin:21-jdk
WORKDIR /server
COPY ./target/vocatio-0.0.1-SNAPSHOT.jar /server/vocatio-api.jar
COPY ./src/main/resources/application-prod.properties /server/app.properties
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "vocatio-api.jar", "--spring.config.location=./app.properties"]