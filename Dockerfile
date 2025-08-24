# Step 1: Dependencies
FROM docker.io/library/eclipse-temurin:17 as deps

WORKDIR /app

# Copy necessary files for dependency resolution
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew
RUN ./gradlew dependencies

# Step 2: Builder
FROM docker.io/library/eclipse-temurin:17 as builder

WORKDIR /app

# Copy dependencies from the previous stage
COPY --from=deps /app ./

# Copy the source code
COPY src src

RUN ./gradlew bootJar

# Step 3: Runner
FROM docker.io/library/eclipse-temurin:17 as runner
WORKDIR /app

# Use a non-root user for security (optional)
RUN addgroup --system --gid 1001 spring && adduser --system --uid 1001 springuser

# Copy the built jar file from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar
RUN chown springuser:spring /app/app.jar
USER springuser

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]