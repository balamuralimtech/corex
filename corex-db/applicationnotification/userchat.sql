DROP TABLE IF EXISTS chat_message;
DROP TABLE IF EXISTS chat_conversation_participant;
DROP TABLE IF EXISTS chat_conversation;

CREATE TABLE chat_conversation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    created_by_user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_chat_conversation_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES UserDetails(user_id)
);

CREATE TABLE chat_conversation_participant (
    id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_read_at TIMESTAMP NULL DEFAULT NULL,
    CONSTRAINT fk_chat_participant_conversation
        FOREIGN KEY (conversation_id) REFERENCES chat_conversation(id),
    CONSTRAINT fk_chat_participant_user
        FOREIGN KEY (user_id) REFERENCES UserDetails(user_id),
    CONSTRAINT uk_chat_conversation_participant UNIQUE (conversation_id, user_id)
);

CREATE TABLE chat_message (
    id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    sender_user_id INT NOT NULL,
    message VARCHAR(2000) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chat_message_conversation
        FOREIGN KEY (conversation_id) REFERENCES chat_conversation(id),
    CONSTRAINT fk_chat_message_sender
        FOREIGN KEY (sender_user_id) REFERENCES UserDetails(user_id)
);

CREATE INDEX idx_chat_participant_user ON chat_conversation_participant(user_id, conversation_id);
CREATE INDEX idx_chat_message_conversation ON chat_message(conversation_id, created_at);
CREATE INDEX idx_chat_conversation_last_message ON chat_conversation(last_message_at, id);
