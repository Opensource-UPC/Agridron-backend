# ---------------------------------------------------------------------------
# Build stage
# ---------------------------------------------------------------------------
FROM eclipse-temurin:27-jdk AS build
WORKDIR /app

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src ./src
RUN ./mvnw -B -q clean package -DskipTests

# ---------------------------------------------------------------------------
# Runtime stage
# ---------------------------------------------------------------------------
FROM eclipse-temurin:27-jre

RUN groupadd --system agridron \
    && useradd --system --gid agridron --home-dir /app agridron

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

USER agridron
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseSerialGC -Xmx320m -XX:MaxMetaspaceSize=128m -Xss512k"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
