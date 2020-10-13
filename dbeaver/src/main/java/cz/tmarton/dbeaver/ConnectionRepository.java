package cz.tmarton.dbeaver;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ConnectionRepository  extends ReactiveCrudRepository<Connection, Long> {
}
