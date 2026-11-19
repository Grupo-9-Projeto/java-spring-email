
FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# Copia o projeto completo para o container
COPY . .

# Executa o Spring Boot
CMD ["mvn", "spring-boot:run"]
