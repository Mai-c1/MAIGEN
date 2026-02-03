FROM docker.1ms.run/eclipse-temurin:17-jdk-focal

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

COPY app.jar /app.jar

EXPOSE 13001

ENTRYPOINT [ "java","-jar","/app.jar"]