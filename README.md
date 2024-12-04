# JAVA-SPRING-PG-OPENAPI

<p>This is an example of a Microservice for decommissioning of vehicles with a number of REST / JMS communication 
with other microservices. <br>An upstream microservice passes on a case request for a vehicle to be decommisioned by 
means of a REST request. Although REST communication is synchronous, <br>this microservice will verify the request and
subsequently respond with an error message or a successful response message along with a status object and subsequently<br> 
process the case request. As soon as this microservice finishes the case request, it will send the final response to 
the upstream microservice at a different endpoint. <br>In this wy the original synchronous communication becomes a loosly 
coupled asynchronuos communication between microservices.</p>
<p>Since business processes can be of undetermined length, the communication between the microervices is enhanced by 
intermediate reports from this microservice to others<br> and by interruption reports from other microservices to this 
microservice</p>

<h3>Controller error handling</h3>
<p>During the handling of the case request of the soliciting microservice, any error thrown by the code is captured by 
a handler class and methode with Spring <br>@RestControllerAdvice and @ExceptionHandler annotations respectively which returns a 400 BAD REQUEST
error message.</p>

<h3>FeignClient for communication with microservices</h3>
<p>For the outgoing REST communication with other microservices we use the Spring @FeignClient on an interface </p>



