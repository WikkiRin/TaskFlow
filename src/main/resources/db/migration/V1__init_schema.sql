-- Таблица пользователей
CREATE TABLE app_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Таблица досок
CREATE TABLE board (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    owner_id BIGINT NOT NULL,
    FOREIGN KEY (owner_id) REFERENCES app_user(id) ON DELETE CASCADE
);

-- Таблица колонок (списки задач)
CREATE TABLE board_column (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    position INT,
    board_id BIGINT NOT NULL,
    FOREIGN KEY (board_id) REFERENCES board(id) ON DELETE CASCADE
);

-- Таблица задач
CREATE TABLE task (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    position INT,
    column_id BIGINT NOT NULL,
    assignee_id BIGINT,
    FOREIGN KEY (column_id) REFERENCES board_column(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES app_user(id) ON DELETE SET NULL
)