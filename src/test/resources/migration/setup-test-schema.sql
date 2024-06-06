-- drop public schema
DROP SCHEMA IF EXISTS public CASCADE;

-- create public schema
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
GRANT ALL ON SCHEMA public TO testuser;

-- create movies table
CREATE TABLE public.movies
(
    eidr_code  VARCHAR(255) PRIMARY KEY,
    name       VARCHAR(255)     NOT NULL,
    rating     DOUBLE PRECISION NOT NULL,
    year       INTEGER          NOT NULL,
    status     VARCHAR(255)     NOT NULL DEFAULT 'ACTIVE',
    created_at timestamp        NOT NULL DEFAULT current_timestamp,
    updated_at timestamp        NOT NULL DEFAULT current_timestamp,
    deleted_at timestamp        NULL,

    CONSTRAINT check_movie_status_in_supported_values CHECK (status in ('ACTIVE', 'INACTIVE'))
);

-- create categories table
CREATE TABLE public.categories
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

-- create movies_categories table
CREATE TABLE public.movies_categories
(
    movie_id    VARCHAR(255) NOT NULL,
    category_id INTEGER      NOT NULL,
    PRIMARY KEY (movie_id, category_id),
    FOREIGN KEY (movie_id) REFERENCES public.movies (eidr_code),
    FOREIGN KEY (category_id) REFERENCES public.categories (id)
);

-- Insert categories
INSERT INTO public.categories (name)
VALUES ('Action'),
       ('Comedy'),
       ('Drama'),
       ('Horror'),
       ('Sci-Fi');


-- Insert movies
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/1A2B-3C4D-5E6F-7G8H-9I0J-A', 'Movie One', 7, 2020, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/2B3C-4D5E-6F7G-8H9I-0J1K-B', 'Movie Two', 8, 2019, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/3C4D-5E6F-7G8H-9I0J-1K2L-C', 'Movie Three', 9, 2018, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/4D5E-6F7G-8H9I-0J1K-2L3M-D', 'Movie Four', 6, 2021, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/5E6F-7G8H-9I0J-1K2L-3M4N-E', 'Movie Five', 7, 2017, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/6F7G-8H9I-0J1K-2L3M-4N5O-F', 'Movie Six', 8, 2022, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/7G8H-9I0J-1K2L-3M4N-5O6P-G', 'Movie Seven', 9, 2016, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/8H9I-0J1K-2L3M-4N5O-6P7Q-H', 'Movie Eight', 6, 2015, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/9I0J-1K2L-3M4N-5O6P-7Q8R-I', 'Movie Nine', 7, 2014, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/0J1K-2L3M-4N5O-6P7Q-8R9S-J', 'Movie Ten', 8, 2013, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/1K2L-3M4N-5O6P-7Q8R-9S0T-K', 'Movie Eleven', 9, 2012, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/2L3M-4N5O-6P7Q-8R9S-0T1U-L', 'Movie Twelve', 6, 2011, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/3M4N-5O6P-7Q8R-9S0T-1U2V-M', 'Movie Thirteen', 7, 2010, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/4N5O-6P7Q-8R9S-0T1U-2V3W-N', 'Movie Fourteen', 8, 2009, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/5O6P-7Q8R-9S0T-1U2V-3W4X-O', 'Movie Fifteen', 9, 2008, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/6P7Q-8R9S-0T1U-2V3W-4X5Y-P', 'Movie Sixteen', 6, 2007, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/7Q8R-9S0T-1U2V-3W4X-5Y6Z-Q', 'Movie Seventeen', 7, 2006, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/8R9S-0T1U-2V3W-4X5Y-6Z7A-R', 'Movie Eighteen', 8, 2005, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/9S0T-1U2V-3W4X-5Y6Z-7A8B-S', 'Movie Nineteen', 9, 2004, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/0T1U-2V3W-4X5Y-6Z7A-8B9C-T', 'Movie Twenty', 6, 2003, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/1U2V-3W4X-5Y6Z-7A8B-9C0D-U', 'Movie Twenty-One', 7, 2002, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/2V3W-4X5Y-6Z7A-8B9C-0D1E-V', 'Movie Twenty-Two', 8, 2001, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/3W4X-5Y6Z-7A8B-9C0D-1E2F-W', 'Movie Twenty-Three', 9, 2000, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/4X5Y-6Z7A-8B9C-0D1E-2F3G-X', 'Movie Twenty-Four', 6, 1999, current_timestamp, current_timestamp);
INSERT INTO public.movies (eidr_code, name, rating, year, created_at, updated_at)
VALUES ('10.5240/5Y6Z-7A8B-9C0D-1E2F-3G4H-Y', 'Movie Twenty-Five', 7, 1998, current_timestamp, current_timestamp);


-- Insert movie-category associations
INSERT INTO public.movies_categories (movie_id, category_id)
VALUES ('10.5240/1A2B-3C4D-5E6F-7G8H-9I0J-A', 1),
       ('10.5240/1A2B-3C4D-5E6F-7G8H-9I0J-A', 2),
       ('10.5240/2B3C-4D5E-6F7G-8H9I-0J1K-B', 1),
       ('10.5240/2B3C-4D5E-6F7G-8H9I-0J1K-B', 3),
       ('10.5240/3C4D-5E6F-7G8H-9I0J-1K2L-C', 1),
       ('10.5240/3C4D-5E6F-7G8H-9I0J-1K2L-C', 4),
       ('10.5240/4D5E-6F7G-8H9I-0J1K-2L3M-D', 2),
       ('10.5240/4D5E-6F7G-8H9I-0J1K-2L3M-D', 3),
       ('10.5240/5E6F-7G8H-9I0J-1K2L-3M4N-E', 2),
       ('10.5240/5E6F-7G8H-9I0J-1K2L-3M4N-E', 5),
       ('10.5240/6F7G-8H9I-0J1K-2L3M-4N5O-F', 3),
       ('10.5240/6F7G-8H9I-0J1K-2L3M-4N5O-F', 4),
       ('10.5240/7G8H-9I0J-1K2L-3M4N-5O6P-G', 3),
       ('10.5240/7G8H-9I0J-1K2L-3M4N-5O6P-G', 5),
       ('10.5240/8H9I-0J1K-2L3M-4N5O-6P7Q-H', 4),
       ('10.5240/8H9I-0J1K-2L3M-4N5O-6P7Q-H', 1),
       ('10.5240/9I0J-1K2L-3M4N-5O6P-7Q8R-I', 4),
       ('10.5240/9I0J-1K2L-3M4N-5O6P-7Q8R-I', 2),
       ('10.5240/0J1K-2L3M-4N5O-6P7Q-8R9S-J', 5),
       ('10.5240/0J1K-2L3M-4N5O-6P7Q-8R9S-J', 3),
       ('10.5240/1K2L-3M4N-5O6P-7Q8R-9S0T-K', 5),
       ('10.5240/1K2L-3M4N-5O6P-7Q8R-9S0T-K', 4),
       ('10.5240/2L3M-4N5O-6P7Q-8R9S-0T1U-L', 1),
       ('10.5240/2L3M-4N5O-6P7Q-8R9S-0T1U-L', 5),
       ('10.5240/3M4N-5O6P-7Q8R-9S0T-1U2V-M', 2),
       ('10.5240/3M4N-5O6P-7Q8R-9S0T-1U2V-M', 1),
       ('10.5240/4N5O-6P7Q-8R9S-0T1U-2V3W-N', 2),
       ('10.5240/4N5O-6P7Q-8R9S-0T1U-2V3W-N', 3),
       ('10.5240/5O6P-7Q8R-9S0T-1U2V-3W4X-O', 3),
       ('10.5240/5O6P-7Q8R-9S0T-1U2V-3W4X-O', 1),
       ('10.5240/6P7Q-8R9S-0T1U-2V3W-4X5Y-P', 3),
       ('10.5240/6P7Q-8R9S-0T1U-2V3W-4X5Y-P', 5),
       ('10.5240/7Q8R-9S0T-1U2V-3W4X-5Y6Z-Q', 4),
       ('10.5240/7Q8R-9S0T-1U2V-3W4X-5Y6Z-Q', 2),
       ('10.5240/8R9S-0T1U-2V3W-4X5Y-6Z7A-R', 4),
       ('10.5240/8R9S-0T1U-2V3W-4X5Y-6Z7A-R', 1),
       ('10.5240/9S0T-1U2V-3W4X-5Y6Z-7A8B-S', 5),
       ('10.5240/9S0T-1U2V-3W4X-5Y6Z-7A8B-S', 3),
       ('10.5240/0T1U-2V3W-4X5Y-6Z7A-8B9C-T', 5),
       ('10.5240/0T1U-2V3W-4X5Y-6Z7A-8B9C-T', 4),
       ('10.5240/1U2V-3W4X-5Y6Z-7A8B-9C0D-U', 1),
       ('10.5240/1U2V-3W4X-5Y6Z-7A8B-9C0D-U', 5),
       ('10.5240/2V3W-4X5Y-6Z7A-8B9C-0D1E-V', 2),
       ('10.5240/2V3W-4X5Y-6Z7A-8B9C-0D1E-V', 1),
       ('10.5240/3W4X-5Y6Z-7A8B-9C0D-1E2F-W', 2),
       ('10.5240/3W4X-5Y6Z-7A8B-9C0D-1E2F-W', 3),
       ('10.5240/4X5Y-6Z7A-8B9C-0D1E-2F3G-X', 3),
       ('10.5240/4X5Y-6Z7A-8B9C-0D1E-2F3G-X', 4),
       ('10.5240/5Y6Z-7A8B-9C0D-1E2F-3G4H-Y', 4),
       ('10.5240/5Y6Z-7A8B-9C0D-1E2F-3G4H-Y', 5);
