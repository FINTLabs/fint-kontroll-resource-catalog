CREATE TABLE lisensmodell_kodeverk (
     id BIGINT generated by default as identity PRIMARY KEY,
     name VARCHAR(255) NOT NULL,
     description VARCHAR(255),
     category VARCHAR(255)
);

