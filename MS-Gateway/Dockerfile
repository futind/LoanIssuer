FROM maven:3.9.9-eclipse-temurin-21

LABEL maintainer="futinde@gmail.com"

RUN mkdir -p /home/application/ms-gateway
RUN mkdir -p /home/application/Libs
WORKDIR /home/application/ms-gateway

COPY MS-Gateway/pom.xml /home/application/ms-gateway
COPY MS-Gateway/src /home/application/ms-gateway/src

COPY Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar /home/application/Libs/

EXPOSE 8084

RUN mvn install:install-file -Dfile=../Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar -DgroupId=ru.neoflex -DartifactId=loan-issuer-library -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
RUN mvn clean package
CMD ["java", "-jar", "/home/application/ms-gateway/target/MS-Gateway-0.0.1-SNAPSHOT.jar"]