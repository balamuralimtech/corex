CREATE INDEX idx_chat_message_conversation_created_at
    ON chat_message (conversation_id, created_at, id);

CREATE INDEX idx_chat_conversation_participant_conversation_user
    ON chat_conversation_participant (conversation_id, user_id);

CREATE INDEX idx_chat_conversation_participant_user_conversation
    ON chat_conversation_participant (user_id, conversation_id);
