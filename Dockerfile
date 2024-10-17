# Stage 1: Build the Spring Models project
FROM maven:3.9.9-eclipse-temurin-21 AS build-models

WORKDIR /usr/src/models

# Clone and build Spring Models
RUN git clone https://nahushr18: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@github.com/nahushr18/SpringModels.git
WORKDIR /usr/src/models/SpringModels
RUN git checkout development
RUN mvn clean install

# Stage 2: Build the Spring API project
FROM maven:3.9.9-eclipse-temurin-21 AS build-api

WORKDIR /usr/src/api

# Copy the built Spring Models jar into the Spring API project
COPY --from=build-models /usr/src/models/SpringModels/target/SpringModels-1.0-SNAPSHOT.jar ./libs/SpringModels.jar

# Copy the Spring API project files into the container
COPY . .

# Build the Spring API project
RUN mvn clean package -DskipTests

# Stage 3: Create the runtime image
FROM eclipse-temurin:21-jdk-slim

WORKDIR /app

# Copy the built project files from the Maven build stage
COPY --from=build-api /usr/src/api/target/SpringApi-0.0.1-SNAPSHOT.jar /app/SpringApi-0.0.1-SNAPSHOT.jar

# Define the command to run the application
CMD ["java", "-jar", "SpringApi-0.0.1-SNAPSHOT.jar"]
