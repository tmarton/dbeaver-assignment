package cz.tmarton.dbeaver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("connections")
public class Connection {

    @Id
    private Long id;

    @Column
    private String name;

    @Column
    private String hostname;

    @Column
    private Integer port;

    @Column
    private String databaseName;

    @Column
    private String username;

    @Column
    private String password;
}
