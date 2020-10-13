create table connections (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(255) not null,
    hostname varchar(255) not null,
    port int not null,
    database_name varchar(255) not null,
    username varchar(255) not null,
    password varchar(255) not null,
    primary key(id)
) engine=InnoDB;