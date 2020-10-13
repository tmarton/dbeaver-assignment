FROM openjdk:11.0.3-jdk
RUN apt-get update && apt-get install bash
COPY target/dbeaver-0.0.1-SNAPSHOT.jar ./dbeaver-0.0.1-SNAPSHOT.jar

CMD ["java", "-jar", "./dbeaver-0.0.1-SNAPSHOT.jar"]