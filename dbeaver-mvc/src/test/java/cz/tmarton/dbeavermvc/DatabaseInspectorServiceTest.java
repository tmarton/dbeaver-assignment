package cz.tmarton.dbeavermvc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DatabaseInspectorServiceTest {

    @Container
    public static final MySQLContainer mySQLContainer = new MySQLContainer()
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void mysqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
    }

    @Autowired
    private DatabaseInspectorService inspectorService;

    @Test
    public void testFetchSchema() throws SQLException {
        var conn = createTestConnection();

        List<String> schemas = inspectorService.fetchSchemas(conn);
        assertIterableEquals(schemas, asList("test"));
    }

    @Test
    public void testFetchTables() throws SQLException {
        var conn = createTestConnection();
        var schema = "test";

        List<String> schemas = inspectorService.fetchTables(conn, schema);
        assertIterableEquals(asList("connections", "flyway_schema_history"), schemas);
    }

    @Test
    public void testFetchColumns() throws SQLException {
        var conn = createTestConnection();
        var schema = "test";
        var table = "flyway_schema_history";

        List<String> columns = inspectorService.fetchColumns(conn, schema, table)
                .stream()
                .map(TableColumn::getName)
                .collect(Collectors.toList());
        assertIterableEquals(asList("installed_rank", "version", "description", "type", "script", "checksum",
                "installed_by", "installed_on", "execution_time", "success"), columns);
    }

    @Test
    public void testFetchData() throws SQLException {
        var conn = createTestConnection();
        var schema = "test";
        var table = "flyway_schema_history";

        List<ObjectNode> data = inspectorService.fetchData(conn, schema, table);
        assertEquals(1, data.size());
        ObjectNode dataRecord = data.get(0);
        assertEquals(1, dataRecord.get("installed_rank").asInt());
        assertEquals("1.1.0.0", dataRecord.get("version").asText());
        assertEquals("init", dataRecord.get("description").asText());
        assertEquals("SQL", dataRecord.get("type").asText());
        assertEquals("V1_1_0_0__init.sql", dataRecord.get("script").asText());
        assertEquals("test", dataRecord.get("installed_by").asText());
        assertTrue(dataRecord.get("success").asBoolean());
    }

    @Test
    public void testFetchDataEmpty() throws SQLException {
        var conn = createTestConnection();
        var schema = "test";
        var table = "connections";

        List<ObjectNode> data = inspectorService.fetchData(conn, schema, table);
        assertEquals(0, data.size());
    }

    private UserConnection createTestConnection() {
        var userConnection = new UserConnection();
        userConnection.setHostname(mySQLContainer.getHost());
        userConnection.setPort(mySQLContainer.getFirstMappedPort());
        userConnection.setDatabaseName(mySQLContainer.getDatabaseName());
        userConnection.setUsername(mySQLContainer.getUsername());
        userConnection.setPassword(mySQLContainer.getPassword());
        userConnection.setName("test connection");
        return userConnection;
    }

}
