# Imagen de api-gateway. Contexto de construcción = raíz del repo (igual que calculo-service).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY calculo-service/pom.xml calculo-service/
COPY funcion-service/pom.xml funcion-service/
COPY historial-service/pom.xml historial-service/
COPY api-gateway/src api-gateway/src
RUN mvn -q -B -pl api-gateway -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/api-gateway/target/api-gateway-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
