# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Chỉ copy pom.xml trước để tận dụng cache của Docker cho các dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn vào và tiến hành build
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Chạy ứng dụng
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Mở cổng 8080
EXPOSE 8080

# Copy file jar từ Stage 1 sang Stage 2
COPY --from=build /app/target/unidocs-0.0.1-SNAPSHOT.jar app.jar

# Lệnh khởi chạy server
ENTRYPOINT ["java", "-jar", "app.jar"]
