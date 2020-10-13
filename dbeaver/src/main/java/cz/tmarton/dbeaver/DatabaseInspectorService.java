package cz.tmarton.dbeaver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collection;

@Service
public class DatabaseInspectorService {

    private final ObjectMapper mapper = new ObjectMapper();

    public Flux<String> fetchSchemas(Connection connection) {
        DatabaseClient dbClient = createDatabaseClient(connection);

        return dbClient.execute("select schema_name as database_name from information_schema.schemata where SCHEMA_NAME != 'information_schema' order by schema_name;")
                .as(String.class)
                .fetch()
                .all();
    }

    Flux<String> fetchTables(Connection connection, String schema) {
        DatabaseClient dbClient = createDatabaseClient(connection);

        return dbClient.execute("SELECT table_name FROM information_schema.tables WHERE table_schema = :schema;")
                .bind("schema", schema)
                .as(String.class)
                .fetch()
                .all();
    }

    Flux<TableColumn> fetchColumns(Connection connection, String schema, String tableName) {
        DatabaseClient dbClient = createDatabaseClient(connection);

        var sql = String.format("show columns from %s.%s", schema, tableName);
        return dbClient.execute(sql)
                .map(row -> {
                    TableColumn column = new TableColumn();
                    column.setName(row.get("field", String.class));
                    column.setType(row.get("type", String.class));
                    column.setNullable(row.get("null", String.class));
                    column.setKey(row.get("key", String.class));
                    column.setDefaultValue(row.get("default", String.class));
                    return column;
                })
                .all();
    }

    Flux<ObjectNode> fetchData(Connection connection, String schema, String tableName) {
        DatabaseClient dbClient = createDatabaseClient(connection);

        var sql = String.format("select * from %s.%s", schema, tableName);
        return dbClient.execute(sql)
                .map((row, rowMetadata) -> {
                    ObjectNode node = mapper.createObjectNode();
                    Collection<String> columnNames = rowMetadata.getColumnNames();
                    columnNames.forEach(n -> node.putPOJO(n, row.get(n)));
                    return node;
                })
                .all();
    }

    private DatabaseClient createDatabaseClient(Connection connection) {
        ConnectionFactory connectionFactory = createConnectionFactory(connection);
        return DatabaseClient.create(connectionFactory);
    }

    private ConnectionFactory createConnectionFactory(Connection connection) {
        return MySqlConnectionFactory.from(MySqlConnectionConfiguration.builder()
                .host(connection.getHostname())
                .database(connection.getDatabaseName())
                .username(connection.getUsername())
                .password(connection.getPassword()).build());
    }
}
