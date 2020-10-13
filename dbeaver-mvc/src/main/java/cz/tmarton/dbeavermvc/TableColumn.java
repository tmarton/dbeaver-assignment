package cz.tmarton.dbeavermvc;

import lombok.Data;

@Data
public class TableColumn {

    private String name;

    private String type;

    private String nullable;

    private String key;

    private String defaultValue;
}
