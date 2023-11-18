CREATE TABLE IF NOT EXISTS GENRES
(
    GENRE_ID INTEGER AUTO_INCREMENT,
    NAME     CHARACTER VARYING NOT NULL,
    CONSTRAINT "GENRES_PK"
        PRIMARY KEY (GENRE_ID)
);
ALTER TABLE GENRES ALTER COLUMN GENRE_ID RESTART WITH 7;

CREATE TABLE IF NOT EXISTS MPA_CATEGORIES
(
    CATEGORY_MPA_ID INTEGER AUTO_INCREMENT,
    NAME            CHARACTER VARYING NOT NULL,
    CONSTRAINT "MPA_CATEGORIES_PK"
        PRIMARY KEY (CATEGORY_MPA_ID)
);
ALTER TABLE MPA_CATEGORIES ALTER COLUMN CATEGORY_MPA_ID RESTART WITH 6;

CREATE TABLE IF NOT EXISTS DIRECTORS
(
     DIRECTOR_ID INT PRIMARY KEY AUTO_INCREMENT,
     NAME        CHARACTER VARYING NOT NULL,
     CONSTRAINT "DIRECTORS_PK" PRIMARY KEY (DIRECTOR_ID)
);

CREATE TABLE IF NOT EXISTS FILMS
(
    FILM_ID         INTEGER AUTO_INCREMENT,
    NAME            CHARACTER VARYING NOT NULL,
    RELEASE_DATE    DATE,
    DESCRIPTION     CHARACTER VARYING,
    CATEGORY_MPA_ID INTEGER,
    DURATION        INTEGER,
    CONSTRAINT "FILMS_PK"
        PRIMARY KEY (FILM_ID),
    CONSTRAINT FILMS_MPA_CATEGORIES_CATEGORY_MPA_ID_FK
        FOREIGN KEY (CATEGORY_MPA_ID) REFERENCES MPA_CATEGORIES
);

CREATE TABLE IF NOT EXISTS FILM_GENRES
(
    FILM_ID  INTEGER NOT NULL,
    GENRE_ID INTEGER NOT NULL,
    CONSTRAINT "FILM_GENRES_FILMS_FILM_ID_FK"
        FOREIGN KEY (FILM_ID) REFERENCES FILMS,
    CONSTRAINT "FILM_GENRES_GENRES_GENRE_ID_FK"
        FOREIGN KEY (GENRE_ID) REFERENCES GENRES
);

CREATE TABLE IF NOT EXISTS FILM_DIRECTORS (
      FILM_ID     INT NOT NULL,
      DIRECTOR_ID INT NOT NULL,
      CONSTRAINT FILM_DIRECTORS_DIRECTORS_DIRECTOR_ID_FK
          FOREIGN KEY (DIRECTOR_ID) REFERENCES DIRECTORS,
      CONSTRAINT "FILM_DIRECTORS_FILMS_FILM_ID_FK"
          FOREIGN KEY (FILM_ID) REFERENCES FILMS
);

CREATE TABLE IF NOT EXISTS USERS
(
    USER_ID  INTEGER AUTO_INCREMENT,
    EMAIL    CHARACTER VARYING NOT NULL,
    NAME     CHARACTER VARYING NOT NULL,
    LOGIN    CHARACTER VARYING,
    BIRTHDAY DATE,
    CONSTRAINT "USERS_PK"
        PRIMARY KEY (USER_ID)
);

CREATE TABLE IF NOT EXISTS LIKES
(
    FILM_ID INTEGER NOT NULL,
    USER_ID INTEGER NOT NULL,
    CONSTRAINT "LIKES_FILMS_FILM_ID_FK"
        FOREIGN KEY (FILM_ID) REFERENCES FILMS,
    CONSTRAINT "LIKES_USERS_USER_ID_FK"
        FOREIGN KEY (USER_ID) REFERENCES USERS
);

CREATE TABLE IF NOT EXISTS FRIENDS
(
    USER_ID   INTEGER NOT NULL,
    FRIEND_ID INTEGER NOT NULL,
    CONSTRAINT "FRIENDS_USERS_USER_ID_FK"
        FOREIGN KEY (USER_ID) REFERENCES USERS,
    CONSTRAINT "FRIENDS_USERS_USER_ID_FK2"
        FOREIGN KEY (FRIEND_ID) REFERENCES USERS
);

CREATE TABLE IF NOT EXISTS REVIEWS (
    REVIEW_ID INT PRIMARY KEY AUTO_INCREMENT,
    CONTENT VARCHAR(200) NOT NULL,
    IS_POSITIVE BOOLEAN,
    USER_ID INT NOT NULL REFERENCES USERS (USER_ID),
    FILM_ID INT NOT NULL REFERENCES FILMS (FILM_ID),
    USEFUL INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS REVIEWS_LIKES (
    REVIEWS_LIKES_ID INT PRIMARY KEY AUTO_INCREMENT,
    REVIEW_ID INT NOT NULL REFERENCES REVIEWS (REVIEW_ID) ON DELETE CASCADE,
    USER_ID INT NOT NULL REFERENCES USERS (USER_ID) ON DELETE CASCADE,
    IS_LIKE BOOLEAN
);

CREATE TABLE IF NOT EXISTS EVENTS (
    EVENT_ID INT PRIMARY KEY AUTO_INCREMENT,
    USER_ID INT NOT NULL,
    EVENT_TYPE VARCHAR(10) NOT NULL,
    OPERATION VARCHAR(10) NOT NULL,
    ENTITY_ID INT NOT NULL,
    TIMESTAMP BIGINT NOT NULL,
    CONSTRAINT FK_EVENTS_USERS
      FOREIGN KEY (USER_ID)
          REFERENCES USERS(USER_ID)
)