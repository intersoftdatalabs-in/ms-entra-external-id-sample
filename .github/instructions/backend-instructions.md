
---
applyTo: "backend/**/java, backend/**/resources"
---

Java 21 best practices:
- Use the latest Java 21 features such as pattern matching for switch, record patterns, and
  sealed classes where applicable.
- Ensure that the code is compatible with Java 21 by using the appropriate language level in your
  build configuration (maven)
- Use the `java.time` package for date and time operations instead of the old `java.util.Date` and
  `java.util.Calendar` classes.
- Use `var` for local variable type inference where it improves readability.
- Use `List.of()`, `Set.of()`, and `Map.of()` for creating immutable collections.
- Use `CompletableFuture` for asynchronous programming instead of `Future`.
- Use `try-with-resources` for managing resources that implement `AutoCloseable`.

- Use `Stream` API for processing collections in a functional style.
- Use `Optional` to avoid null checks and improve code readability.
- Use `Text Blocks` for multi-line strings to improve readability.
- Use `Pattern.compile()` with the `Pattern.DOTALL` flag for regex patterns that span multiple lines.
- Use `java.nio.file` package for file I/O operations instead of `java.io.File
- Use `java.util.concurrent` package for concurrency and parallelism.
- Use `java.util.function` package for functional interfaces and lambda expressions.

- Use `java.util.stream` package for stream operations.
- Use `java.util.logging` or `java.util.logging.Logger` for logging instead of `System.out.println()`.
- Use `java.util.concurrent.atomic` package for atomic operations on variables.
- Use `java.util.concurrent.locks` package for advanced locking mechanisms.
- Use `java.util.concurrent.ExecutorService` for managing threads and thread pools.
- Use `java.util.concurrent.CompletableFuture` for asynchronous programming.
- Use `java.util.concurrent.Flow` for reactive programming.
- Use `java.util.concurrent.ScheduledExecutorService` for scheduling tasks.
- Use `java.util.concurrent.TimeUnit` for time-related operations.


- Use `java.util.concurrent.CountDownLatch` and `java.util.concurrent.CyclicBarrier` for synchronization.
- Use `java.util.concurrent.Semaphore` for controlling access to resources.
- Use `java.util.concurrent.locks.ReentrantLock` for advanced locking mechanisms.
- Use `java.util.concurrent.locks.Condition` for condition variables.
- Use `java.util.concurrent.locks.ReadWriteLock` for read-write locks.
- Use `java.util.concurrent.locks.StampedLock` for optimistic locking.
- Use `java.util.concurrent.atomic.AtomicInteger`, `AtomicLong`, `AtomicBoolean`, and `AtomicReference` for atomic operations on variables.
- Use `java.util.concurrent.atomic.AtomicIntegerArray`, `AtomicLongArray`, and `AtomicReferenceArray` for atomic operations on arrays.
- Use `java.util.concurrent.atomic.AtomicMarkableReference
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
    - Add the MSAL4J dependency to your `pom.xml` or `build.gradle` file.
    - Configure the MSAL4J client with the necessary credentials and endpoints.
    - Use the MSAL4J client to acquire tokens and authenticate users.
    - Follow best practices for Microsoft Entra External Id authentication, such as using secure storage for credentials and handling token expiration gracefully.
    - 