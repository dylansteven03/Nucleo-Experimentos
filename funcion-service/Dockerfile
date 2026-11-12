# Imagen de funcion-service. Contexto de construcción = raíz del repo (igual que calculo-service),
# porque el pom padre y los pom de los demás módulos son necesarios para resolver el reactor de Maven.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY calculo-service/pom.xml calculo-service/
COPY funcion-service/pom.xml funcion-service/
COPY historial-service/pom.xml historial-service/
COPY funcion-service/src funcion-service/src
RUN mvn -q -B -pl funcion-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/funcion-service/target/funcion-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
