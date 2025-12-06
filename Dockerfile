FROM openjdk:17-jdk-slim
ARG JAR_FILE=target/app-0.0.1.jar
COPY ${JAR_FILE} app_lavaderoSepulveda.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app_lavaderoSepulveda.jar"]