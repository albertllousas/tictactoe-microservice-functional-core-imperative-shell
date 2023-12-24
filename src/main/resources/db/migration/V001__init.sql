CREATE TABLE public.games
(
    id      UUID            NOT NULL,
    status  TEXT            NOT NULL,
    board   TEXT            NOT NULL,
    created TIMESTAMPTZ     NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT pk_games PRIMARY KEY (id)
);
