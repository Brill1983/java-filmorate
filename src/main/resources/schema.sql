create table if not exists GENRES
(
    GENRE_ID INTEGER auto_increment,
    NAME     CHARACTER VARYING not null,
    constraint "GENRES_pk"
        primary key (GENRE_ID)
);
ALTER TABLE GENRES ALTER COLUMN GENRE_ID RESTART WITH 7;

create table if not exists MPA_CATEGORIES
(
    CATEGORY_MPA_ID INTEGER auto_increment,
    NAME            CHARACTER VARYING not null,
    constraint "MPA_CATEGORIES_pk"
        primary key (CATEGORY_MPA_ID)
);
ALTER TABLE MPA_CATEGORIES ALTER COLUMN CATEGORY_MPA_ID RESTART WITH 6;

CREATE TABLE IF NOT EXISTS DIRECTORS
(
     DIRECTOR_ID INT PRIMARY KEY AUTO_INCREMENT,
     NAME        CHARACTER VARYING NOT NULL,
     CONSTRAINT "DIRECTORS_pk" PRIMARY KEY (DIRECTOR_ID)
);

create table if not exists FILMS
(
    FILM_ID         INTEGER auto_increment,
    NAME            CHARACTER VARYING not null,
    RELEASE_DATE    DATE,
    DESCRIPTION     CHARACTER VARYING,
    CATEGORY_MPA_ID INTEGER,
    DURATION        INTEGER,
--     RATE            INTEGER,
    constraint "FILMS_pk"
        primary key (FILM_ID),
    constraint FILMS_MPA_CATEGORIES_CATEGORY_MPA_ID_FK
        foreign key (CATEGORY_MPA_ID) references MPA_CATEGORIES
);

create table if not exists FILM_GENRES
(
    FILM_ID  INTEGER not null,
    GENRE_ID INTEGER not null,
    constraint "FILM_GENRES_FILMS_FILM_ID_fk"
        foreign key (FILM_ID) references FILMS,
    constraint "FILM_GENRES_GENRES_GENRE_ID_fk"
        foreign key (GENRE_ID) references GENRES
);

CREATE TABLE IF NOT EXISTS FILM_DIRECTORS (
      FILM_ID     INT NOT NULL,
      DIRECTOR_ID INT NOT NULL,
      CONSTRAINT FILM_DIRECTORS_DIRECTORS_DIRECTOR_ID_FK
          FOREIGN KEY (DIRECTOR_ID) REFERENCES DIRECTORS,
      CONSTRAINT "FILM_DIRECTORS_FILMS_FILM_ID_fk"
          FOREIGN KEY (FILM_ID) REFERENCES FILMS
);

create table if not exists USERS
(
    USER_ID  INTEGER auto_increment,
    EMAIL    CHARACTER VARYING not null,
    NAME     CHARACTER VARYING not null,
    LOGIN    CHARACTER VARYING,
    BIRTHDAY DATE,
    constraint "USERS_pk"
        primary key (USER_ID)
);

create table if not exists LIKES
(
    FILM_ID INTEGER not null,
    USER_ID INTEGER not null,
    constraint "LIKES_FILMS_FILM_ID_fk"
        foreign key (FILM_ID) references FILMS,
    constraint "LIKES_USERS_USER_ID_fk"
        foreign key (USER_ID) references USERS
);

create table if not exists FRIENDS
(
    USER_ID   INTEGER not null,
    FRIEND_ID INTEGER not null,
    constraint "FRIENDS_USERS_USER_ID_fk"
        foreign key (USER_ID) references USERS,
    constraint "FRIENDS_USERS_USER_ID_fk2"
        foreign key (FRIEND_ID) references USERS
);