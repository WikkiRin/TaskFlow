-- Поле title не пустое и не соержит пробелов
ALTER TABLE board
ADD CONSTRAINT chk_title_not_blank CHECK (trim(title) <> '');