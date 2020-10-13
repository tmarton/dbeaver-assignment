package cz.tmarton.dbeaver;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionRepository repository;

    private final DatabaseInspectorService inspectorService;

    @GetMapping
    Flux<Connection> findAll() {
        return repository.findAll();
    }

    @GetMapping("/{connectionId}")
    Mono<Connection> findById(@PathVariable("connectionId") Long connectionId) {
        return repository.findById(connectionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId))));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Mono<Connection> save(@Validated @RequestBody Connection connection) {
        return repository.save(connection);
    }

    @PutMapping("/{connectionId}")
    @ResponseStatus()
    Mono<Connection> update(@PathVariable("connectionId") Long connectionId, @RequestBody Connection connection) {
        return repository.findById(connectionId)
                .flatMap(c -> repository.save(connection))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId))));
    }

    @GetMapping(value = "/{connectionId}/schema", produces= MediaType.APPLICATION_JSON_VALUE)
    Mono<List<String>> inspectSchemas(@PathVariable("connectionId") Long connectionId) {
        return repository.findById(connectionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId))))
                .flatMapMany(inspectorService::fetchSchemas)
                .collectList();
    }

    @GetMapping(value = "/{connectionId}/schema/{schema}")
    Mono<List<String>> inspectSchemas(@PathVariable("connectionId") Long connectionId, @PathVariable("schema") String schema) {
        return repository.findById(connectionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId))))
                .flatMapMany(c -> inspectorService.fetchTables(c, schema))
                .collectList();
    }

    @GetMapping(value = "/{connectionId}/schema/{schema}/table/{tableName}")
    Flux<TableColumn> inspectColumns(@PathVariable("connectionId") Long connectionId, @PathVariable("schema") String schema, @PathVariable("tableName") String tableName) {
        return repository.findById(connectionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId))))
                .flatMapMany(c -> inspectorService.fetchColumns(c, schema, tableName));
    }

    @GetMapping(value = "/{connectionId}/schema/{schema}/table/{tableName}/data")
    Flux<ObjectNode> inspectData(@PathVariable("connectionId") Long connectionId, @PathVariable("schema") String schema, @PathVariable("tableName") String tableName) {
        return repository.findById(connectionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(String.format("Connection with id %s not found", connectionId))))
                .flatMapMany(c -> inspectorService.fetchData(c, schema, tableName));
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Illegal arguments")
    @ExceptionHandler(IllegalArgumentException.class)
    public void illegalArgumentHandler() {
        // entity not found error handler
    }
}
