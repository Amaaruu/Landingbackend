# ETAPA 1: Construcción (Build)
# Usamos una imagen con el JDK completo para poder compilar el código
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiamos el Maven Wrapper y el archivo pom.xml primero
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Damos permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Descargamos las dependencias (Esto se cachea para que las siguientes builds sean más rápidas)
RUN ./mvnw dependency:go-offline

# Copiamos el código fuente de la aplicación
COPY src src

# Compilamos el proyecto creando el archivo .jar (omitiendo los tests para mayor velocidad)
RUN ./mvnw clean package -DskipTests

# ETAPA 2: Ejecución (Run)
# Usamos una imagen mucho más ligera (JRE) que solo sirve para ejecutar Java, no para compilar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el archivo .jar generado en la Etapa 1
COPY --from=build /app/target/Backend-0.0.1-SNAPSHOT.jar app.jar

# Exponemos el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]