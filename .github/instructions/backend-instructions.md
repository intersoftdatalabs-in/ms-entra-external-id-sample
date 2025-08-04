
---
applyTo: "backend/**/java, backend/**/resources"
---

Java 17 best practices:
- The project use Java 17 compliance level. Ensure that your code adheres to Java 17 features and best practices.
- Use `var` for local variable type inference where it improves readability.
- Use `record` for simple data carrier classes to reduce boilerplate code.
- Use `sealed` classes and interfaces to restrict which other classes or interfaces may extend or implement them.
- Use `switch` expressions for more concise and readable switch statements.
- Use `Optional` to represent optional values instead of using `null`.
- Use `Stream` API for processing collections in a functional style.
- Use `java.time` package for date and time manipulation instead of the old `java.util.Date` and `java.util.Calendar`.
- Use `Pattern Matching for instanceof` to simplify type checks and casts.
- Use `Text Blocks` for multi-line string literals to improve readability.
- Use `Enhanced Pseudo-Random Number Generators` for better performance and security in random number generation.
- Use `Foreign Function & Memory API` (if applicable) for interoperability with native code.
- Use `JEP 356: Enhanced Datagram Transport Layer Security (DTLS)` (if applicable) for secure communication.
- Use `JEP 382: New macOS Rendering Pipeline` (if applicable) for better performance on macOS.
- Use `JEP 391: macOS/AArch64 Port` (if applicable) for running on Apple Silicon.
- Use `JEP 409: Sealed Classes` for defining restricted class hierarchies.
- Use `JEP 411: Deprecate the Security Manager for better security practices.
- Use `JEP 412: Foreign Function & Memory API (Incubator)` for advanced interoperability.
- Use `JEP 414: Vector API (Incubator)` for performance improvements in numerical computations.
- Use `JEP 415: Context-Specific Deserialization Filters` for improved security in deserialization. 
- Use `JEP 416: Reimplement Core Reflection with Method Handles` for better performance in reflection.
- Use `JEP 417: Vector API (Second Incubator)` for further enhancements in vector computations.
- Use `JEP 418: Internet-Address Resolution SPI` for custom DNS resolution.
- Use `JEP 419: Foreign Function & Memory API (Second Incubator)` for advanced native interoperability.
- Use `JEP 420: Pattern Matching for switch (Preview)`
- Use `JEP 421: Deprecate the Security Manager for Removal` for better security practices.
- Use `JEP 422: Linux/RISC-V Port` (if applicable) for running on RISC-V architecture.
- Use `JEP 423: Generational ZGC` for better garbage collection performance.
- Use `JEP 424: Foreign Function & Memory API (Third Incubator)` for advanced native interoperability.
- Use `JEP 425: Virtual Threads (Preview)` for lightweight concurrency.
- Use `JEP 426: Pattern Matching for switch (Second Preview)`
- Use `JEP 427: Structured Concurrency (Incubator)` for better concurrency
- Use `JEP 428: Deprecate the Security Manager for Removal` for better security practices.
- Use `JEP 429: Scoped Values (Incubator)` for better context propagation.
- Use `JEP 430: String Templates (Preview)` for easier string interpolation.
- Use `JEP 431: Sequenced Collections` for ordered collections.
- Use `JEP 432: Foreign Function & Memory API (Fourth Incubator)` for advanced native interoperability.
- Use `JEP 433: Virtual Threads (Second Preview)` for lightweight concurrency.
- Use `JEP 434: Pattern Matching for switch (Third Preview)`
- Use `JEP 435: Structured Concurrency (Second Incubator)` for better concurrency
- Use `JEP 436: Deprecate the Security Manager for Removal` for better security practices.
- Use `JEP 437: Foreign Function & Memory API (Fifth Incubator)` for advanced native interoperability.
- Use `JEP 438: Virtual Threads (Third Preview)` for lightweight concurrency.
- Use `JEP 439: Pattern Matching for switch (Fourth Preview)
- Use `JEP 440: Structured Concurrency (Third Incubator)` for better concurrency
- Use `JEP 441: Deprecate the Security Manager for Removal` for better security practices.
- Use `JEP 442: Foreign Function & Memory API (Sixth Incubator)` for advanced native interoperability.
- Use `JEP 443: Virtual Threads (Fourth Preview)` for lightweight concurrency.
- Use `JEP 444: Pattern Matching for switch (Fifth Preview)
- Use `JEP 445: Structured Concurrency (Fourth Incubator)` for better concurrency
- Use `JEP 446: Deprecate the Security Manager for Removal` for better security practices.
- Use `JEP 447: Foreign Function & Memory API (Seventh Incubator)` for advanced native interoperability.
- Use `JEP 448: Virtual Threads (Fifth Preview)` for lightweight concurrency.
- Use `JEP 449: Pattern Matching for switch (Sixth Preview)
- Use `JEP 450: Structured Concurrency (Fifth Incubator)` for better concurrency
- Use `JEP 451: Deprecate the Security Manager for Removal` for better security practices.
- Use `JEP 452: Foreign Function & Memory API (Eighth Incubator)` for advanced native interoperability.
- Use `JEP 453: Virtual
- Follow the Java naming conventions for classes, methods, and variables.
- Use meaningful names for classes, methods, and variables to improve code readability.
- Use Javadoc comments for public classes and methods to provide documentation.
- Follow Google Java Style Guide for consistent code formatting.
- Use `@Override` annotation for methods that override a superclass method to improve code readability.
- Use `@Deprecated` annotation for methods and classes that are no longer recommended for use.
- Use `@SuppressWarnings` annotation to suppress specific compiler warnings when necessary.
- Use `@FunctionalInterface` annotation for functional interfaces to improve code readability.
- Use `@SafeVarargs` annotation for methods that accept varargs to indicate that the method does not perform unsafe operations on the varargs parameter.
- Use `@NonNull` and `@Nullable` annotations to indicate nullability of method parameters and return values.
- Use `@JsonProperty`, `@JsonIgnore`, and `@JsonInclude` annotations from Jackson for JSON serialization
  and deserialization.
- Use `@Autowired` annotation for dependency injection in Spring.
- Use `@Value` annotation for injecting configuration properties in Spring.
- Use `@Configuration`, `@Component`, `@Service`, and `@Repository` annotations for defining Spring beans.
- Use `@RestController` and `@RequestMapping` annotations for defining RESTful APIs in Spring.
- Use `@GetMapping`, `@PostMapping`, `@PutMapping`, and `@DeleteMapping` annotations for defining HTTP methods in Spring.
- Use `@PathVariable`, `@RequestParam`, and `@RequestBody` annotations for handling request parameters
  and body in Spring.
- Use `@ResponseStatus` annotation for setting HTTP response status in Spring.
- Use `@ExceptionHandler` annotation for handling exceptions in Spring.
- Use `@ControllerAdvice` annotation for global exception handling in Spring.
- Use `@Transactional` annotation for managing transactions in Spring.
- Use `@Cacheable`, `@CachePut`, and `@CacheEvict` annotations for caching in Spring.
- Use `@Scheduled` annotation for scheduling tasks in Spring.
- Use `@Async` annotation for asynchronous method execution in Spring.
- Use `@EnableAsync` and `@EnableScheduling` annotations for enabling asynchronous and scheduled
  execution in Spring.
- Use `@Profile` annotation for defining different profiles in Spring.
- Use `@ConditionalOnProperty` annotation for conditional bean creation based on configuration
  properties in Spring.
- Use `@ConditionalOnClass`, `@ConditionalOnMissingClass`, `@ConditionalOnBean`, and `@ConditionalOnMissingBean` annotations for conditional bean creation based on classpath and bean presence in Spring.
- Use `@ConditionalOnExpression` annotation for conditional bean creation based on SpEL expressions in Spring.
- Use MSAL4J for Microsoft Entra External Id authentication in the application
    - Add the MSAL4J dependency to your `pom.xml` file.
    - Configure the MSAL4J client with the necessary credentials and endpoints.
    - Use the MSAL4J client to acquire tokens and authenticate users.
    - Follow best practices for Microsoft Entra External Id authentication, such as using secure storage for credentials and handling token expiration gracefully.
    - 
  # Maven Configuration
- Ensure that the `pom.xml` file is configured to use Java 21 as the source and target version.
- Use the `maven-compiler-plugin` to set the Java version:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <version>3.8.1</version>
  <configuration>
    <source>21</source>
    <target>21</target>
  </configuration>
</plugin>
```
- Use the `spring-boot-maven-plugin` to build and run the Spring Boot application:
```xml
<plugin>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-maven-plugin</artifactId>
  <version>3.0.0</version>
</plugin> 
```
- Use the `maven-surefire-plugin` for running tests:
```xml
<plugin>  
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>2.22.2</version>
</plugin>         
```
- Use the `maven-jar-plugin` for packaging the application as a JAR file:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-jar-plugin</artifactId>         
  <version>3.2.0</version>
  <configuration>
    <archive>
      <manifest>
        <addClasspath>true</addClasspath>         
        <mainClass>com.example.Application</mainClass>
      </manifest>
    </archive>
  </configuration>
</plugin>
```
- Use the `maven-dependency-plugin` for managing dependencies:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-dependency-plugin</artifactId>      
  <version>3.1.2</version>
  <executions>
    <execution> 
      <id>copy-dependencies</id>
      <phase>package</phase>
      <goals>
        <goal>copy-dependencies</goal>
      </goals>
      <configuration>
        <outputDirectory>${project.build.directory}/lib</outputDirectory>
      </configuration>
    </execution>
  </executions>   
</plugin>
```

- Use the `maven-resources-plugin` for copying resources:
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-resources-plugin</artifactId>         
  <version>3.2.0</version>
  <executions>
    <execution>
      <id>copy-resources</id>
      <phase>process-resources</phase>
      <goals>
        <goal>copy-resources</goal>
      </goals>        
      <configuration>
        <outputDirectory>${project.build.directory}/classes</outputDirectory>
        <resources>
          <resource>
            <directory>src/main/resources</directory>
            <filtering>true</filtering>
          </resource>
        </resources>
      </configuration>
    </execution>
  </executions>
</plugin>
```
- When making code changes, ensure that the changed code compiles by executing the following command as an agent:
```
mvn -q compile
```
- Run tests to ensure that the code changes do not break existing functionalityby by executing the following command as an agent:
```
mvn -q test
```
- Always prefer the jakarta namespace for imports and annotations, as the javax namespace is deprecated in Java 21.
- If #codebase/.env is present, source the file to set environment variables for the application prior to running the application.
```bash
source .env
```
- Use the `spring-boot:run` goal to run the application in development mode:
```bash
mvn spring-boot:run
```