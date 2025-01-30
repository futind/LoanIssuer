FROM maven:3.9.9-eclipse-temurin-21

LABEL maintainer="futinde@gmail.com"

RUN mkdir -p /home/application/ms-statement
RUN mkdir -p /home/application/Libs
WORKDIR /home/application/ms-statement

COPY MS-Statement/pom.xml /home/application/ms-statement
COPY MS-Statement/src /home/application/ms-statement/src

COPY Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar /home/application/Libs/

EXPOSE 8082

RUN mvn install:install-file -Dfile=../Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar -DgroupId=ru.neoflex -DartifactId=loan-issuer-library -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
RUN mvn clean package
CMD ["java", "-jar", "/home/application/ms-statement/target/MS-Statement-0.0.1-SNAPSHOT.jar"]