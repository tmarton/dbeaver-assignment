package cz.tmarton.dbeavermvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DatabaseInspectorService {

    //TODO implement some connection caching

    private final ObjectMapper mapper = new ObjectMapper();

    public List<String> fetchSchemas(UserConnection userConnection) throws SQLException {
        var sql = "select schema_name as database_name from information_schema.schemata where SCHEMA_NAME != 'information_schema' order by schema_name;";
        try(
                Connection c = createJDBCConnection(userConnection);
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            var result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString("database_name"));
            }
            return result;
        }
    }

    List<String> fetchTables(UserConnection userConnection, String schema) throws SQLException {
        var sql = "SELECT table_name as table_name FROM information_schema.tables WHERE table_schema = ?;";

        try(
                Connection c = createJDBCConnection(userConnection);
                PreparedStatement ps = c.prepareStatement(sql);
        ) {
            ps.setString(1, schema);
            try (ResultSet rs = ps.executeQuery()) {
                var result = new ArrayList<String>();
                while (rs.next()) {
                    result.add(rs.getString("table_name"));
                }
                return result;
            }
        }
    }

    List<TableColumn> fetchColumns(UserConnection userConnection, String schema, String tableName) throws SQLException {
        //TODO fix parameter handling
        var sql = String.format("show columns from %s.%s", schema, tableName);

        try(
                Connection c = createJDBCConnection(userConnection);
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
        ) {
            var result = new ArrayList<TableColumn>();
            while (rs.next()) {
                TableColumn column = new TableColumn();
                column.setName(rs.getString("field"));
                column.setType(rs.getString("type"));
                column.setNullable(rs.getString("null"));
                column.setKey(rs.getString("key"));
                column.setDefaultValue(rs.getString("default"));
                result.add(column);
            }
            return result;
        }
    }

    List<ObjectNode> fetchData(UserConnection userConnection, String schema, String tableName) throws SQLException {
        // TODO fix parameter handling
        var sql = String.format("select * from %s.%s", schema, tableName);

        try (
                Connection c = createJDBCConnection(userConnection);
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
        ) {
            var result = new ArrayList<ObjectNode>();
            while (rs.next()) {
                ObjectNode node = mapper.createObjectNode();

                var columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = rs.getMetaData().getColumnName(i);
                    node.putPOJO(columnName, rs.getObject(columnName));
                }
                result.add(node);
            }
            return result;
        }
    }

    private Connection createJDBCConnection(UserConnection userConnection) throws SQLException {
        return DriverManager.getConnection(
                String.format("jdbc:mysql://%s:%s/%s", userConnection.getHostname(), userConnection.getPort(), userConnection.getDatabaseName()),
                userConnection.getUsername(),
                userConnection.getPassword()
        );
    }
}
