# ---------- Etapa 1: compilar la biblioteca nativa ----------
FROM gcc:13 AS native
WORKDIR /build
COPY native/ native/
RUN gcc -shared -fPIC -O2 -Wall -Inative/include \
    -o libnumerico.so native/src/numerico.c

# ---------- Etapa 2: compilar el JAR con Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY funcion-service/pom.xml funcion-service/
COPY historial-service/pom.xml historial-service/
COPY calculo-service/pom.xml calculo-service/
COPY calculo-service/src calculo-service/src
RUN mvn -q -pl calculo-service -am -DskipTests package

# ---------- Etapa 3: imagen final ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=native /build/libnumerico.so /app/lib/libnumerico.so
COPY --from=build /src/calculo-service/target/*.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-Djna.library.path=/app/lib", "-jar", "/app/app.jar"]