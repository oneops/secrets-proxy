FROM openjdk:8-jre-alpine as build
ARG VERSION
ADD . /app
RUN cd /app \
    && ./mvnw clean package

FROM openjdk:8-jre-alpine
VOLUME /secrets /log
COPY --from=build /app/target/secrets-proxy-*.jar app.jar
EXPOSE 8443
ENV JAVA_OPTS="-server -XX:+UseG1GC"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]