# Java 25
FROM eclipse-temurin:25-jdk

WORKDIR /app

# Copy project files
COPY . .

# Give permission to Maven wrapper
RUN chmod +x mvnw

# Build Spring Boot JAR
RUN ./mvnw clean package -DskipTests

# Render provides PORT environment variable
EXPOSE 8080

# Start application
CMD ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar target/*.jar"]