FROM openjdk:17-oracle
WORKDIR /
ADD build/libs/gateway.jar gateway.jar
EXPOSE 8080
CMD java -Dio.netty.handler.ssl.noOpenSsl=true -jar gateway.jar
