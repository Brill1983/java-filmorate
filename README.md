# java-filmorate

![DB scheme.png](DB%20scheme.png)

## Описание диаграммы базы фильмов\пользователей

База данных состоит из следующих таблиц:
1.	films – данные по внесенным в базу фильмам
2.	likes – идентификаторы пользователей поставивших лайк фильму
3.	mpa_categories – наименования рейтингов фильмов Ассоциации кинокомпаний
4.	genres – наименования жанров фильмов
5.	film_genres – фильмы и жанры этих фильмов
6.	users – данные пользователей сервиса
7.	friends – идентификаторы пользователей, добавленных в друзья, отдельного поля для подтверждение «дружбы» не требуется.

## Поля и ключи таблиц:

### `films`
1.	film_id  (PK) – уникальный идентификатор фильма 
2.	name – наименование фильма
3.	release_date – дата выхода на экраны (YYYY-MM-DD)
4.  description - описание фильма
5.  category_mpa_id (FK) – идентификатор рейтинга
6.  duration - продолжительность в минутах

### `likes` 
1.	film_id (FK) – идентификатор  фильма
2.	user_id (FK) – идентификатор пользователя, лайкнувшего фильм

### `film_genres`
1.	film_id (FK) – идентификатор фильма
2.	genre_id (FK) – идентификатор жанра

### `genres` 
1.	genre_id (PK) – идентификатор жанра
2.	name – наименование жанра

### `mpa_categories` 
1.	category_mpa_id (PK) – идентификатор рейтингов Ассоциации кинокомпаний
2.	name – наименование рейтинга

### `users`
1.	id (PK) – идентификатор пользователя
2.	email – эл. почта пользователя
3.	login – логин пользователя
4.	name – имя пользователя
5.	birthday – дата рождения пользователя

### `friends` 
1.	user_id (PK) – идентификатор пользователя
2.	friend_id (FK) – идентификатор друга пользователя

## Примеры запросов:
1. Получить данные по фильму с ID 2, включая наименование категоии МРА:
```
   SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME 
   FROM FILMS AS F 
   JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID 
   WHERE F.FILM_ID = 2;
   ```
2. Получить список из 10 самых популярных фильмов:
```
  SELECT F.FILM_ID, F.NAME AS FILM_NAME, F.RELEASE_DATE, F.DESCRIPTION, F.DURATION, 
         F.CATEGORY_MPA_ID, M.NAME AS MPA_NAME, COUNT(L.USER_ID)
  FROM FILMS AS F
  JOIN MPA_CATEGORIES AS M ON F.CATEGORY_MPA_ID = M.CATEGORY_MPA_ID 
  JOIN LIKES AS L ON F.FILM_ID = L.FILM_ID
  GROUP BY F.FILM_ID
  ORDER BY COUNT(L.USER_ID) DESC 
  LIMIT 10;
  ```

3. Получить список из общих друзей пользователя с ID 1 и пользователя с ID 2:
```
  SELECT * FROM USERS WHERE USER_ID IN (
        SELECT FRIEND_ID 
        FROM FRIENDS 
        WHERE USER_ID = 1 
        INTERSECT 
        SELECT FRIEND_ID 
        FROM FRIENDS 
        WHERE USER_ID = 2
        );
  ```
