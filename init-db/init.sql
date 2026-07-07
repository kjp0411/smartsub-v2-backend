CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS p_guide_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    store_id UUID NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1536)
);
