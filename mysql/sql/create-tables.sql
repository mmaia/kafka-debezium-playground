USE kafka_connect_studies;

-- auto-generated definition
create table users
(
    id         bigint auto_increment primary key,
    first_name varchar(255) NOT NULL,
    last_name  varchar(255) NOT NULL,
    timestamp  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    title      varchar(255) NOT NULL,
    version    int          NOT NULL
);

create table pets
(
    id         bigint auto_increment PRIMARY KEY,
    name       varchar(255) NOT NULL,
    breed      varchar(255) NOT NULL,
    user_id    bigint NOT NULL REFERENCES users(id)
);


# create users data
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Selene', 'Mitchell', '2021-10-11 09:47:18', 'Dynamic Configuration Administrator', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Lowell', 'Hoppe', '2021-10-11 09:47:28', 'Central Identity Facilitator', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Isaias', 'Anderson', '2021-10-11 09:47:38', 'Internal Applications Director', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Eusebio', 'Hagenes', '2021-10-11 09:47:48', 'International Mobility Strategist', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Marie', 'Hansen', '2021-10-11 09:47:58', 'Senior Implementation Analyst', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Dominique', 'Gislason', '2021-10-11 09:48:08', 'Regional Operations Assistant', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Mitch', 'Wolf', '2021-10-11 09:48:18', 'Product Applications Technician', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Joan', 'Ortiz', '2021-10-11 09:48:28', 'Senior Mobility Orchestrator', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Sara', 'Rau', '2021-10-11 09:48:38', 'Central Tactics Technician', 0);
INSERT INTO users ( first_name, last_name, timestamp, title, version) VALUES ('Milagro', 'Bergnaum', '2021-10-11 09:48:48', 'Chief Security Planner', 0);

# create pets data
insert into pets (name, breed, user_id) values ('Belo', 'Dog', 1);
insert into pets (name, breed, user_id) values ('Flipper', 'Dolphin', 1);
insert into pets (name, breed, user_id) values ('Tom', 'Mouse', 1);
insert into pets (name, breed, user_id) values ('Jerry', 'Cat', 2);
insert into pets (name, breed, user_id) values ('Micky', 'Mouse', 3);