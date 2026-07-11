CREATE TABLE IF NOT EXISTS interactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    weight FLOAT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (user_id, event_id)
);

-- 2. Таблица коэффициентов сходства мероприятий
CREATE TABLE IF NOT EXISTS similarities (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event1 BIGINT NOT NULL,
    event2 BIGINT NOT NULL,
    similarity FLOAT NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    UNIQUE (event1, event2),
    CHECK (event1 < event2)
);

CREATE INDEX IF NOT EXISTS idx_similarities_event1_similarity
    ON similarities (event1, similarity DESC);

CREATE INDEX IF NOT EXISTS idx_similarities_event2_similarity
    ON similarities (event2, similarity DESC);