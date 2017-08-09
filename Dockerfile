#
# Docker file for OneOps Keywhiz Proxy.
#
# Create by : Suresh G
#######################################

FROM openjdk:8-jre-alpine

VOLUME /tmp /log

ADD target/keywhiz-proxy-1.0.0.jar app.jar

ENV JAVA_OPTS="-server -XX:+UseG1GC"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]