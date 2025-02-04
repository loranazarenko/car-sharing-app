INSERT INTO users (id, email, password, first_name, last_name, telegram_chat_id, is_deleted)
VALUES
    (1, 'alice@example.com', '1111111111', 'Alice', 'Cooper', 12345, false),
    (2, 'bob@example.com', 'password2', 'Bob', 'Johnson', 12345, false),
    (3, 'admin@example.com', 'adminpass', 'Admin', 'Admin', 12345, false);
insert into users_roles (user_id, role_id)
values (1, 2),
       (2, 2),
       (3, 1);