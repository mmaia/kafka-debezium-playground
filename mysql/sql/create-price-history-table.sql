USE kafka_connect_studies;

create table price_history
(
    id bigint auto_increment primary key,
    asset varchar(10) not null,
    date_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    price float not null
);

INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', NOW(), 100.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', NOW(), 101.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', NOW(), 102.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', NOW(), 106.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', NOW(), 75.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', NOW(), 76.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', NOW(), 75.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', NOW(), 78.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', NOW(), 23.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', NOW(), 22.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', NOW(), 24.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', NOW(), 30.0);