FROM maven:3.9.9-eclipse-temurin-21

LABEL maintainer="futinde@gmail.com"

RUN mkdir -p /home/application/ms-calculator
RUN mkdir -p /home/application/Libs
WORKDIR /home/application/ms-calculator

COPY MS-Calculator/pom.xml /home/application/ms-calculator
COPY MS-Calculator/src /home/application/ms-calculator/src

COPY Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar /home/application/Libs/

EXPOSE 8080

RUN mvn install:install-file -Dfile=../Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar -DgroupId=ru.neoflex -DartifactId=loan-issuer-library -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
RUN mvn -X clean package
CMD ["java", "-jar", "/home/application/ms-calculator/target/MS-Calculator-0.0.1-SNAPSHOT.jar"]