# java-filmorate
Template repository for Filmorate project.

---

#### database link https://dbdiagram.io/d/filmorate-68dda008d2b621e422dc659b

База данных спроектирована в соответствии с принципами нормализации (3NF) и оптимизирована для бизнес-логики приложения Filmorate.

Основные сущности:
- users - хранение данных пользователей

- films - хранение данных фильмов

- mpa_ratings - справочник возрастных рейтингов

- genres - справочник жанров

- film_likes - лайки пользователей

- friendships - система дружбы между пользователями

---

#### Примеры запросов

- Получение популярных фильмов (топ N)

```
SELECT f.id, f.name, f.description, f.release_date, f.duration,
        m.code as mpa_rating,
       COUNT(fl.user_id) as likes_count
FROM films f
LEFT JOIN film_likes fl ON f.id = fl.film_id
LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
GROUP BY f.id, m.code
ORDER BY likes_count DESC
LIMIT 10;
```

- Получение общих друзей между двумя пользователями

```
SELECT u.id, u.login, u.name, u.email
FROM users u
JOIN friendships f1 ON u.id = f1.friend_id 
                   AND f1.user_id = 123 
                   AND f1.status = 'CONFIRMED'
JOIN friendships f2 ON u.id = f2.friend_id 
                   AND f2.user_id = 456 
                   AND f2.status = 'CONFIRMED';
```