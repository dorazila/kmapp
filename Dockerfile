FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn clean package -DskipTests -B -q

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/kimia-webapp-0.1.0.jar app.jar
ENV KIMIA_DB_PATH=/tmp/kimia_data_normalizzato.db
EXPOSE 8080
CMD ["sh", "-c", "java -Xmx400m --enable-native-access=ALL-UNNAMED -Dorg.sqlite.tmpdir=/tmp -jar app.jar"]
