CREATE TABLE IF NOT EXISTS publisher
(
    chat_id    bigint NOT NULL PRIMARY KEY,
    user_name  text   NOT NULL,
    type       text   NOT NULL,
    first_name text,
    last_name  text,
    token      text
);


CREATE TABLE IF NOT EXISTS box
(
    generated_token text NOT NULL PRIMARY KEY ,
    publisher_id    bigint
);
