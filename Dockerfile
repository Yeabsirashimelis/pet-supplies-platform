FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN /bin/sh -c 'for i in 1 2 3 4 5; do mvn -B -DskipTests -Dmaven.wagon.http.retryHandler.count=5 clean package && exit 0; echo "maven build attempt ${i} failed, retrying..."; sleep 5; done; exit 1'

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/pet-platform-1.0.0.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
