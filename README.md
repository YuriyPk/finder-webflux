
Execute the command from the project’s root directory to run the app: 
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
(Or “mvnw.cmd …” on windows)

Home page:
http://localhost:8080

Metrics for upstream services:
http://localhost:8080/hystrix/monitor?stream=http://localhost:8080/actuator/hystrix.stream&title=Finder

Health check:
http://localhost:8080/actuator/health
