FROM openjdk:22-oracle
VOLUME /tmp
COPY target/webapp-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]