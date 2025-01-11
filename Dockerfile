FROM gradle:8-jdk17-alpine AS build

WORKDIR /home/gradle/src

COPY build.gradle settings.gradle ./

RUN gradle clean build --no-daemon > /dev/null 2>&1 || true

COPY . .

RUN gradle clean build --no-daemon -x test

FROM openjdk:17-alpine

EXPOSE 8080

WORKDIR /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/lanchonete-pedido.jar

ENTRYPOINT ["java","-jar","/app/lanchonete-pedido.jar"]