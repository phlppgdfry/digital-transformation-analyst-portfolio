FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN mvn -B verify
FROM quay.io/wildfly/wildfly:34.0.1.Final-jdk21
COPY --from=build --chown=jboss:root /build/target/drivers/ojdbc11.jar /tmp/ojdbc11.jar
COPY --chown=jboss:root docker/configure.cli /tmp/configure.cli
RUN /opt/jboss/wildfly/bin/jboss-cli.sh --file=/tmp/configure.cli
COPY --from=build --chown=jboss:root /build/target/integration-lab.war /opt/jboss/wildfly/standalone/deployments/
