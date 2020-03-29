package com.findinpath.springboot.testcontainers;

import com.findinpath.springboot.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * This test shows how to make use of the spring framework's {@link DynamicPropertySource} annotation
 * in making use of <a href="https://www.testcontainers.org/">testcontainers</a> library.
 * <p/>
 * The test class is annotated with {@link SpringJUnitConfig} which points out to JUnit Jupiter
 * where from to load the spring context in the scope of the unit tests from this class.
 * <p/>
 * The annotation {@link Testcontainers} is a JUnit Jupiter annotation which is used along
 * with the {@link Container} annotation for starting before running all the tests from the class
 * (and respectively shutting down after running all the tests from the class) the PostgreSQL
 * database docker throwaway container required for the tests.
 */
@SpringJUnitConfig(classes = {Application.class})
@Testcontainers
public class PostgresIntegrationTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(PostgresIntegrationTest.class);

  @Container
  public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:12")
      .withDatabaseName("integration-tests-db")
      .withUsername("sa")
      .withPassword("sa");


  @Autowired
  private JdbcTemplate jdbcTemplate;

  /**
   * This static method is used for filling up the spring framework's registry
   * with properties required for the {@link javax.sql.DataSource} spring bean instantiation
   * that is required for interacting with the PostgreSQL database.
   *
   * @param registry the spring framework's property registry
   */
  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
  }

  @Test
  public void demo() {
    LOGGER.info("Start of the demo test");
    jdbcTemplate.execute("SELECT 1");
    LOGGER.info("Current date: " +jdbcTemplate.queryForObject("SELECT CURRENT_DATE", String.class));
  }
}