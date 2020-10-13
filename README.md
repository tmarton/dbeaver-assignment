# Preconditions
* JRE
* Maven
* Docker
* Docker Compose

### Installation
* Run `mvn clean install -f dbeaver/pom.xml && mvn clean install -f dbeaver-mvc/pom.xml`
* Run `docker-compose up --build --remove-orphans`
* New DBeaver Reactive Api will be published on [localhost:8080/](http://localhost:8080/swagger-ui/)
* New DBeaver MVC Api will be published on [localhost:8081](http://localhost:8081/swagger-ui/) 



