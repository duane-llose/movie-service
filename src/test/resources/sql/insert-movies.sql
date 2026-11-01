INSERT INTO movie (id, title, director, vote_average, vote_count, release_date, revenue, runtime, backdrop_path, budget, homepage, overview, popularity, poster_path, genres, themes)
VALUES
    (1, 'The Dark Knight', 'Christopher Nolan', 8.5, 3000, '2008-07-18', 1004558444, 152, '/dark-knight-backdrop.jpg', 185000000, 'https://example.com/dark-knight', 'Batman faces the Joker.', 75.5, '/dark-knight-poster.jpg', ARRAY['Action', 'Drama'], ARRAY['justice', 'chaos']),
    (2, 'Inception', 'Christopher Nolan', 8.4, 5000, '2010-07-16', NULL, NULL, '/inception-backdrop.jpg', NULL, NULL, NULL, NULL, '/inception-poster.jpg', ARRAY['Action', 'Sci-Fi'], NULL),
    (3, 'Dark Waters', 'Todd Haynes', 7.6, 1000, '2019-11-22', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Drama'], NULL),
    (4, '100% Wolf', 'Alexs Stadermann', 6.0, 1000, '2020-05-07', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Animation'], NULL),
    (5, 'Future Film', NULL, NULL, NULL, '2099-01-01', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Drama'], NULL),
    (6, 'Unknown Date', 'Jane Doe', 5.0, 500, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Action'], NULL),
    (7, 'Snake_Eyes', 'Brian De Palma', 6.1, 200, '1998-08-07', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ARRAY['Thriller'], NULL);
