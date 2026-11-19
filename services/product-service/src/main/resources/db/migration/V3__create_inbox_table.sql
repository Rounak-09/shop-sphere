
CREATE TABLE inbox_events
(
    event_id UUID PRIMARY KEY,
    consumer_name VARCHAR(150) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_inbox_consumer
    ON inbox_events(consumer_name, processed_at DESC);
