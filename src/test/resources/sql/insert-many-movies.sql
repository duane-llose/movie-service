INSERT INTO movie (id, title, vote_count)
SELECT g, 'Movie ' || g, g FROM generate_series(1000, 1120) g;
