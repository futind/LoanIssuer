FROM maven:3.9.9-eclipse-temurin-21

LABEL maintainer="futinde@gmail.com"

RUN mkdir -p /home/application/ms-deal
RUN mkdir -p /home/application/Libs
WORKDIR /home/application/ms-deal

COPY MS-Deal/pom.xml /home/application/ms-deal
COPY MS-Deal/.env.properties /home/application/ms-deal
COPY MS-Deal/src /home/application/ms-deal/src

COPY Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar /home/application/Libs/

EXPOSE 8081

RUN mvn install:install-file -Dfile=../Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar -DgroupId=ru.neoflex -DartifactId=loan-issuer-library -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
RUN mvn clean package
CMD ["java", "-jar", "/home/application/ms-deal/target/MS-Deal-0.0.1-SNAPSHOT.jar"]