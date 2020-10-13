package cz.tmarton.dbeavermvc;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/connections")
public class ConnectionController {

    private final UserConnectionRepository repository;

    private final DatabaseInspectorService inspectorService;

    @GetMapping
    List<UserConnection> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{connectionId}")
    UserConnection findById(@PathVariable("connectionId") Long connectionId) {
        return repository.findById(connectionId)
                .orElseThrow(() ->new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    UserConnection save(@Validated @RequestBody UserConnection userConnection) {
        return repository.save(userConnection);
    }

    @PutMapping("/{connectionId}")
    @ResponseStatus()
    UserConnection update(@PathVariable("connectionId") Long connectionId, @RequestBody UserConnection userConnection) {
        var conn = repository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId)));

        return repository.save(conn);
    }

    @GetMapping(value = "/{connectionId}/schema", produces= MediaType.APPLICATION_JSON_VALUE)
    List<String> inspectSchemas(@PathVariable("connectionId") Long connectionId) throws SQLException {
        var conn = repository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId)));

        return inspectorService.fetchSchemas(conn);
    }

    @GetMapping(value = "/{connectionId}/schema/{schema}")
    List<String> inspectSchemas(@PathVariable("connectionId") Long connectionId, @PathVariable("schema") String schema) throws SQLException {
        var conn = repository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId)));

        return inspectorService.fetchTables(conn, schema);
    }

    @GetMapping(value = "/{connectionId}/schema/{schema}/table/{tableName}")
    List<TableColumn> inspectColumns(@PathVariable("connectionId") Long connectionId, @PathVariable("schema") String schema, @PathVariable("tableName") String tableName) throws SQLException {
        var conn = repository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId)));

        return inspectorService.fetchColumns(conn, schema, tableName);
    }

    @GetMapping(value = "/{connectionId}/schema/{schema}/table/{tableName}/data")
    List<ObjectNode> inspectData(@PathVariable("connectionId") Long connectionId, @PathVariable("schema") String schema, @PathVariable("tableName") String tableName) throws SQLException {
        var conn = repository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId)));

        return inspectorService.fetchData(conn, schema, tableName);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Illegal arguments")
    @ExceptionHandler(IllegalArgumentException.class)
    public void illegalArgumentHandler() {
        // entity not found error handler
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Object> illegalArgumentHandler(SQLException ex) {
        // sql error handler
        return new ResponseEntity<>(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
