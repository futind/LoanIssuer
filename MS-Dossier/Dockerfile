FROM maven:3.9.9-eclipse-temurin-21

LABEL maintainer="futinde@gmail.com"

RUN mkdir -p /home/application/ms-dossier
RUN mkdir -p /home/application/Libs
WORKDIR /home/application/ms-dossier

COPY MS-Dossier/pom.xml /home/application/ms-dossier
COPY MS-Dossier/.env.properties /home/application/ms-dossier
COPY MS-Dossier/src /home/application/ms-dossier/src

COPY Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar /home/application/Libs/

EXPOSE 8080

RUN mvn install:install-file -Dfile=../Libs/LoanIssuerLibrary-0.0.1-SNAPSHOT.jar -DgroupId=ru.neoflex -DartifactId=loan-issuer-library -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
RUN mvn clean package
CMD ["java", "-jar", "/home/application/ms-dossier/target/MS-Dossier-0.0.1-SNAPSHOT.jar"]