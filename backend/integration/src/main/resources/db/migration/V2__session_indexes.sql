-- Additional indexes to optimize session chain operations
CREATE INDEX IF NOT EXISTS idx_sessions_replaced_by ON sessions (replaced_by_session_id);
CREATE INDEX IF NOT EXISTS idx_sessions_user_active ON sessions (user_id) WHERE revoked_at IS NULL;
