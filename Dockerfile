# Dockerfile
FROM eclipse-temurin:21-jdk-alpine

# Directorio de la app dentro del contenedor
WORKDIR /app

# Copia el jar generado por Maven
COPY target/*.jar app.jar

# Exponer el puerto de Spring Boot
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java","-jar","app.jar"]