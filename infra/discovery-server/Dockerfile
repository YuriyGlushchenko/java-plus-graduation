FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/discovery-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8761
EXPOSE 8085

ENTRYPOINT ["java","-jar","/app/app.jar"]