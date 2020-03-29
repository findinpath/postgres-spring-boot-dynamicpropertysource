Proof of concept for using the `DynamicPropertySource` spring annotation in tests requiring PostgreSQL
======================================================================================================

This simple project shows how to work with the newly introduced
`org.springframework.test.context.DynamicPropertySource` annotation which
can be used in spring tests that make use of [testcontainers](https://www.testcontainers.org/).


Details about the process that led to the introduction of the `DynamicPropertySource` annotation can
be found in the Github issue:

https://github.com/spring-projects/spring-framework/issues/24540

Before introducing this  annotation, in order to interact via spring boot with a test container
that was mapped via the `spring.datasource` in the `application.yml` file (for triggering via 
`org.springframework.boot.autoconfigure.jdbc.DataSourceInitializationConfiguration` the creation of the much needed
spring bean instance of type `javax.sql.DataSource` for JDBC/JPA tests), there was needed an
implementation of the `ApplicationContextInitializer` to introduce the required properties in
the configurable application context:

```java
@SpringJUnitConfig(classes = {Application.class}, initializers = {PostgresIntegrationTest.Initializer.class})
@Testcontainers
public class PostgresIntegrationTest {

  @Container
  public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:12")
      .withDatabaseName("integration-tests-db")
      .withUsername("sa")
      .withPassword("sa");


  @Autowired
  private JdbcTemplate jdbcTemplate;

  static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
          "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
          "spring.datasource.username=" + postgreSQLContainer.getUsername(),
          "spring.datasource.password=" + postgreSQLContainer.getPassword()
      ).applyTo(configurableApplicationContext.getEnvironment());
    }
  }
 
  @Test
  public void demo(){
     jdbcTemplate.execute("SELECT 1");
  }
}
```

With the introduction of the `@DynamicPropertySource` there is no need for an extra `ApplicationContextInitializer`:

```java
@SpringJUnitConfig(classes = {Application.class})
@Testcontainers
public class PostgresIntegrationTest {

  @Container
  public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:12")
      .withDatabaseName("integration-tests-db")
      .withUsername("sa")
      .withPassword("sa");


  @Autowired
  private JdbcTemplate jdbcTemplate;

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
  }

  @Test
  public void demo() {
    jdbcTemplate.execute("SELECT 1");
  }
}
```

As can be seen from above, the newly introduced `@DynamicPropertySource` is somehow similar to the 
commonly used `@TestPropertySource` annotation with the mention that it allows the usage of dynamic resources
such as the IP and port assigned to the container (needed in the `jdbcUrl` in the example above).

Check out the full source code (and corresponding documentation) of 
the [PostgresIntegrationTest.java](src/test/java/com/findinpath/springboot/testcontainers/PostgresIntegrationTest.java) class. 


See details about the usage of the `@DynamicPropertyResource` in the Spring framework 
[documentation](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/testing.html#testcontext-ctx-management-dynamic-property-sources).


