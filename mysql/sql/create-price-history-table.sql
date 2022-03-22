USE kafka_connect_studies;

create table price_history
(
    id bigint auto_increment primary key,
    asset varchar(10) not null,
    date_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    price float not null
);

INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', '2020-01-01 00:00:00', 100.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', '2020-01-01 00:10:00', 101.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', '2020-01-01 00:20:00', 102.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('BTC', '2020-01-01 00:21:00', 106.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', '2020-01-01 00:00:00', 75.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', '2020-01-01 00:01:00', 76.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', '2020-01-01 00:02:00', 75.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ETH', '2020-01-01 00:03:00', 78.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', '2020-01-01 00:00:00', 23.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', '2020-01-01 00:01:00', 22.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', '2020-01-01 00:02:00', 24.0);
INSERT INTO price_history (asset, date_time, price) VALUES ('ADA', '2020-01-01 00:03:00', 30.0);