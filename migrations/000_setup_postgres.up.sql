CREATE SCHEMA objective8;

CREATE TABLE objective8.users (
    _id         SERIAL PRIMARY KEY,
    _created_at timestamp DEFAULT current_timestamp,
    twitter_id  varchar NOT NULL,
    username    varchar UNIQUE NOT NULL,
    user_data   json NOT NULL
);

CREATE TABLE objective8.objectives (
    _id            SERIAL PRIMARY KEY,
    _created_at    timestamp DEFAULT current_timestamp,
    created_by_id  integer REFERENCES objective8.users (_id) NOT NULL,
    end_date       timestamp NOT NULL,
    objective      json NOT NULL
);

CREATE TABLE objective8.comments (
    _id           SERIAL PRIMARY KEY,
    _created_at   timestamp DEFAULT current_timestamp,
    created_by_id integer REFERENCES objective8.users (_id) NOT NULL,
    objective_id  integer REFERENCES objective8.objectives (_id) NOT NULL,
    comment       json NOT NULL
);
