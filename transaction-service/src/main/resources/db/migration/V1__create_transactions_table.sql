CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_account VARCHAR(64) NOT NULL,
    recipient_account VARCHAR(64) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount_gbp NUMERIC(19, 4),
    status VARCHAR(20) NOT NULL,
    risk_level VARCHAR(20),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_transactions_status ON transactions (status);
CREATE INDEX idx_transactions_sender_account ON transactions (sender_account);
CREATE INDEX idx_transactions_created_at ON transactions (created_at DESC);
