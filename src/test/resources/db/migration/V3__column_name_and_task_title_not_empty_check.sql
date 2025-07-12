-- Поле name не пустое и не соержит пробелов
ALTER TABLE board_column
ADD CONSTRAINT chk_name_not_blank CHECK (trim(name) <> '');

-- Поле title не пустое и не соержит пробелов
ALTER TABLE task
ADD CONSTRAINT chk_title_not_blank CHECK (trim(title) <> '');