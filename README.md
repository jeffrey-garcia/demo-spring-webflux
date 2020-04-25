# Demo project for Reactive Web Service using Spring Boot2 and Webflux

Typical restful controllers receive a request and a thread is occupied until
the response is sent. In that time, the controller has to retrieve the data
the thread is blocking while the data store performs the query. That turns
in a performance bottleneck with rising concurrent requests. With reactive
programming, the thread can perform other tasks while the data store
retrieves the data. That offers a performance boost but requires a change
to the reactive programming paradigm.

The goal of this project is to be reactive from top to bottom. It demonstrates 
bridging web endpoint with the Supplier of data (functional mechanism) that 
will be fed into spring-cloud-stream framework, the approach can be used with 
foreign event-driven sources, where the actual source of data may be coming 
from a classical REST endpoint (external system which is not a binder).

<b>Table of Contents:</b>
- [Technology Stack](#technology_stack)
- [Reactive Systems and Spring WebFlux](#reactive_system_webflux)
    - [The Problem](#what_is_the_problem)
    - [Non-blocking Web Frameworks](#what_is_the_solution)
    - [Reactive Programming](#why_reactive_non_blocking_matters)
    - [Spring WebFlux](#what_is_spring_webflux)
    - [Limitation](#what_is_webflux_limitation)
- [References](#references)    
<br/>

### <a name="technology_stack"></a>Technology Stack
- Java 8 (1.8.0_221)
- SpringBoot 2.2.2
- Spring Webflux 5.2.2
- Spring data mongodb reactive 2.2.2
- Spring cloud stream 3.0.3
- Tomcat 9.0.29 (embedded web server in SpringBoot)
- MongoDB 4.0.3

For the purpose of this project, we swap the embedded web server in
SpringBoot from Netty to Tomcat, so we have fine grained control to
calibrate the max-connection and max-thread, in order to easily
demonstrate the efficiency gain using the flux architecture from
end-to-end.

<br/>

### <a name="reactive_system_webflux"></a>Reactive Systems and Spring WebFlux
To get clearer on what reactive systems are, it's helpful to understand
the fundamental problem they're designed to solve.

<br/>

##### <a name="what_is_the_problem"></a>The Problem

In traditional web applications, when a web server receives a request
from a client, it accepts that request and places it in an execution
queue. A thread in the execution queue’s thread pool then receives the
request, reads its input parameters, and generates a response. Along the
way, if the execution thread needs to call a blocking resource such as
a database, a filesystem, or another web service, that thread executes
the blocking request and awaits a response. In this paradigm the thread
is effectively blocked until the external resource responds, which
causes performance issues and limits scalability.

For example if you have 100 threads in your web server’s thread pool, 
and 101 requests arrive, then that last extra request will not be 
served until one of the others finish processing their requests. 

To combat these issues, developers typically create generously sized 
thread pools, so that while one thread is blocked another thread can 
continue to process requests. This require to scale the resource 
capacity for each running instance of the web service vertically, or 
scaling horizontally by creating more instances of the web service. 
Which is not ideal because both incur additional operating cost while 
not fully utilising the computation power of the commodity hardware.

<br/>

##### <a name="what_is_the_solution"></a>Non-Blocking Web Frameworks

Non-blocking web frameworks such as NodeJS takes a different approach.
Instead of executing a blocking request and waiting for it to complete,
they use non-blocking I/O. In this paradigm, an application executes a
request, provides code to be executed when a response is returned, and
then gives its thread back to the server. When an external resource
returns a response, the provided code will be executed. Internally,
non-blocking frameworks operate using an event loop. Within the loop,
the application code either provides a callback or a future containing
the code to execute when the asynchronous loop completes.

In principle, non-blocking framework provides application the ability 
to scale with a small, fixed number of threads and less memory. That 
makes applications more resilient under load, because they scale in a 
more predictable way. In order to observe those benefits, however, you 
need to have some latency (including a mix of slow and unpredictable
network I/O). That is where the reactive stack begins to show its
strengths, and the differences can be dramatic.

<br/>

##### <a name="why_reactive_non_blocking_matters"></a>Reactive Programming

“Reactive,” refers to programming models that are built around reacting
to change — network components reacting to I/O events, UI controllers
reacting to mouse events, and others. In that sense, non-blocking is
reactive, because, instead of being blocked, we are now in the mode of
reacting to notifications as operations complete or data becomes
available. It also becomes important to control the rate of events so
that a fast producer does not overwhelm its destination.

`IMPORTANT`
```
The purpose of Reactive Streams is only to establish the mechanism and
a boundary. If a publisher cannot slow down, it has to decide whether
to buffer, drop, or fail.
```

If you can free up threads (and thus free up the thread they’re 
utilising) faster than new requests arrive, and the time spent in 
those threads is mostly due to input/output, great! There's possibly 
no need for reactive programming.

<br/>

##### <a name="what_is_spring_webflux"></a>Spring WebFlux

To satisfy the need for building a reactive, non-blocking web stack to
handle concurrency with a small number of threads and scale with fewer
hardware resources. Spring development team created a new common API to
serve as a foundation across any non-blocking runtime. That is important
because of servers (such as Netty) that are well-established in the
async, non-blocking space.

In Spring WebFlux (and non-blocking servers in general), it is assumed
that applications do not block, and, therefore, non-blocking servers
use a small, fixed-size thread pool (event loop workers) to handle
requests.

While Reactor is the reactive library of choice for Spring WebFlux. WebFlux
requires Reactor as a core dependency but it is interoperable with other
reactive libraries via Reactive Streams. WebFlux adapts transparently
to the use of RxJava or any other reactive library.

<br/>

##### <a name="what_is_webflux_limitation"></a>Limitation

If you have blocking persistence APIs (JPA, JDBC) or networking APIs to
use, Spring MVC is the best choice for common architectures at least.
It is technically feasible to have a mix of applications with either
Spring MVC or Spring WebFlux controllers, while having the Reactor and
RxJava to perform blocking calls on a separate thread, but then you
would not be making the most of a non-blocking web stack.

WebFlux (or generally reactive and non-blocking framework) CANNOT make
applications run faster. It can, in some cases, (for example, if using
the WebClient to execute remote calls in parallel). Overall speaking,
WebFlux requires more work to do things the non-blocking way and that
can increase slightly the required processing time.

WebFlux also requires a different programming paradigm and a new approach
to reasoning about how your code will be executed. Once you've wrapped
your head around it, reactive programming can lead to very scalable
applications.

<br/>

### <a name="references"></a>References:
##### Spring Web Flux
- [Road to Reactive Spring Cloud](https://spring.io/blog/2018/06/20/the-road-to-reactive-spring-cloud)
- [Notes on Reactive Programming Part I:](https://spring.io/blog/2016/06/07/notes-on-reactive-programming-part-i-the-reactive-landscape)
- [Notes on Reactive Programming Part II:](https://spring.io/blog/2016/06/13/notes-on-reactive-programming-part-ii-writing-some-code)
- [Notes on Reactive Programming Part III:](https://spring.io/blog/2016/07/20/notes-on-reactive-programming-part-iii-a-simple-http-server-application)
- [Web on Reactive Stack](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html)
- [Building a Reactive Web Service](https://spring.io/guides/gs/reactive-rest-service/)
- [Spring Boot use another embedded web server](https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-web-servers.html)

##### Project Reactor
- [Reactor 3 Reference Guide](http://projectreactor.io/docs/core/release/reference/)
- [Reactor Flux API](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html)
- [Reactor Error Handling](https://projectreactor.io/docs/core/release/reference/#error.handling)
- [Reactor Scheduler](https://projectreactor.io/docs/core/release/api/reactor/core/scheduler/Schedulers.html)

##### Spring Cloud Stream (SCSt)
- [SCSt Blog - Demystified and Simplified](https://spring.io/blog/2019/10/14/spring-cloud-stream-demystified-and-simplified)
- [SCSt Blog - Functional and Reactive](https://spring.io/blog/2019/10/17/spring-cloud-stream-functional-and-reactive)
- [SCSt Blog - Event Driven Microservices with SCSt](https://spring.io/blog/2019/10/15/simple-event-driven-microservices-with-spring-cloud-stream)
- [SCSt Blog - Event Routing with Functions](https://spring.io/blog/2019/10/31/spring-cloud-stream-event-routing)
- [SCSt - Reactive Functions Support](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/current/reference/html/spring-cloud-stream.html#_reactive_functions_support)
- [SCSt - Event Routing](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/current/reference/html/spring-cloud-stream.html#_event_routing)
- [SCSt - Foreign Driven Event Source](https://cloud.spring.io/spring-cloud-static/spring-cloud-stream/3.0.3.RELEASE/reference/html/spring-cloud-stream.html#_foreign_event_driven_sources)

##### Spring Cloud Function
- [Spring Cloud Function - Reference Guide](https://cloud.spring.io/spring-cloud-function/reference/html/spring-cloud-function.html#_introduction)

##### Spring Asepct Oriented Programming (AOP)
- [Spring AOP - Reference Guide](https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop)