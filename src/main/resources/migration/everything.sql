-- drop public schema
DROP SCHEMA IF EXISTS public CASCADE;

-- create public schema
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
GRANT ALL ON SCHEMA public TO devuser;

-- create movies table
CREATE TABLE public.movies
(
    eidrCode   VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    rating     INTEGER      NOT NULL,
    year       DATE         NOT NULL,
    created_at timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at timestamp    NOT NULL DEFAULT current_timestamp,
    deleted_at timestamp    NULL
);

-- create categories table
CREATE TABLE public.categories
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at timestamp    NOT NULL DEFAULT current_timestamp,
    updated_at timestamp    NOT NULL DEFAULT current_timestamp,
    deleted_at timestamp    NULL
);

-- create movies_categories table
CREATE TABLE public.movies_categories
(
    movie_id    VARCHAR(255) NOT NULL,
    category_id INTEGER      NOT NULL,
    PRIMARY KEY (movie_id, category_id),
    FOREIGN KEY (movie_id) REFERENCES public.movies (eidrCode),
    FOREIGN KEY (category_id) REFERENCES public.categories (id)
);