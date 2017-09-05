# Docker file for OneOps Secrets Proxy.

FROM openjdk:8-jre-alpine

VOLUME /tmp /log

ADD target/secrets-proxy-1.1.0.jar app.jar

ENV JAVA_OPTS="-server -XX:+UseG1GC"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]