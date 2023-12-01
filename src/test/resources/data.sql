DELETE FROM FILM_GENRES;
DELETE FROM FRIENDS;
DELETE FROM LIKES;
DELETE FROM USERS;
DELETE FROM FILM_GENRES;
DELETE FROM FILM_DIRECTORS;
DELETE FROM DIRECTORS;
DELETE FROM FILMS;
ALTER TABLE USERS ALTER COLUMN USER_ID RESTART WITH 1;
ALTER TABLE FILMS ALTER COLUMN FILM_ID RESTART WITH 1;
ALTER TABLE DIRECTORS ALTER COLUMN DIRECTOR_ID RESTART WITH 1;

MERGE INTO GENRES (GENRE_ID, NAME) VALUES ( 1, 'Комедия' );
MERGE INTO GENRES (GENRE_ID, NAME) VALUES ( 2, 'Драма' );
MERGE INTO GENRES (GENRE_ID, NAME) VALUES ( 3, 'Мультфильм' );
MERGE INTO GENRES (GENRE_ID, NAME) VALUES ( 4, 'Триллер' );
MERGE INTO GENRES (GENRE_ID, NAME) VALUES ( 5, 'Документальный' );
MERGE INTO GENRES (GENRE_ID, NAME) VALUES ( 6, 'Боевик' );
MERGE INTO MPA_CATEGORIES (CATEGORY_MPA_ID, NAME) VALUES ( 1, 'G' );
MERGE INTO MPA_CATEGORIES (CATEGORY_MPA_ID, NAME) VALUES ( 2, 'PG' );
MERGE INTO MPA_CATEGORIES (CATEGORY_MPA_ID, NAME) VALUES ( 3, 'PG-13' );
MERGE INTO MPA_CATEGORIES (CATEGORY_MPA_ID, NAME) VALUES ( 4, 'R' );
MERGE INTO MPA_CATEGORIES (CATEGORY_MPA_ID, NAME) VALUES ( 5, 'NC-17' );
MERGE INTO DIRECTORS (DIRECTOR_ID, NAME) VALUES ( 1, 'Director1' );
MERGE INTO DIRECTORS (DIRECTOR_ID, NAME) VALUES ( 2, 'Director2' );