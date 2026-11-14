-- Демо: кинотеатр, две таблицы связаны по ключу sessions.film_id -> films.id
-- Запуск на пустой базе: RUN 'demo.sql'
CREATE TABLE films (id INT PRIMARY KEY, title STRING, genre STRING, rating DOUBLE, premiere DATE, in_release BOOLEAN)
CREATE TABLE sessions (id INT PRIMARY KEY, film_id INT REFERENCES films(id), hall INT, start_date DATE, price DOUBLE, is_3d BOOLEAN)
SHOW TABLES
DESCRIBE sessions
INSERT INTO films VALUES (1, 'Dune', 'sci-fi', 8.0, '2021-10-21', FALSE), (2, 'Oppenheimer', 'drama', 8.3, '2023-07-20', TRUE), (3, 'Barbie', 'comedy', 6.8, '2023-07-20', TRUE), (4, 'Up', 'cartoon', 8.3, '2009-05-29', FALSE)
INSERT INTO sessions VALUES (1, 2, 1, '2026-11-15', 450.0, FALSE), (2, 2, 3, '2026-11-15', 600.0, TRUE), (3, 3, 2, '2026-11-16', 400.0, FALSE)
INSERT INTO sessions (id, film_id, hall) VALUES (4, 1, 1)
SELECT * FROM films
SELECT title, rating FROM films WHERE rating > 8
SELECT * FROM sessions WHERE film_id = 2
UPDATE films SET in_release = TRUE WHERE id = 1
UPDATE sessions SET price = 500 WHERE hall = 1
SELECT * FROM sessions
-- ошибки, которые программа должна поймать
INSERT INTO films VALUES (1, 'Copy', 'drama', 5.0, '2020-01-01', TRUE)
INSERT INTO sessions VALUES (5, 99, 1, '2026-11-17', 300.0, FALSE)
DELETE FROM films WHERE id = 2
DROP TABLE films
SELECT * FROM cinemas
-- сначала удаляем сеансы, потом сам фильм
DELETE FROM sessions WHERE film_id = 3
DELETE FROM films WHERE title = 'Barbie'
SELECT * FROM films
