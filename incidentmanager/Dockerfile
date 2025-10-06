# Step 1: Use Java base image
FROM openjdk:17-jdk-slim

# Step 2: Set working directory
WORKDIR /app

# Step 3: Copy everything into the image
COPY . .

# Step 4: Build and run the Spring Boot app
RUN ./mvnw clean package -DskipTests

# Step 5: Run the generated JAR file
ENTRYPOINT ["java", "-jar", "target/incidentmanager-0.0.1-SNAPSHOT.jar"]


# Optional: Expose backend port
EXPOSE 8082
