SET REFERENTIAL_INTEGRITY FALSE;

DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS pickup_points;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS manufacturers;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS units;

SET REFERENTIAL_INTEGRITY TRUE;

CREATE TABLE roles (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50));
CREATE TABLE units (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50));
CREATE TABLE categories (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100));
CREATE TABLE manufacturers (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100));
CREATE TABLE suppliers (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100));
CREATE TABLE pickup_points (id INT AUTO_INCREMENT PRIMARY KEY, address VARCHAR(255));

CREATE TABLE users (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       role_id INT,
                       fio VARCHAR(150),
                       login VARCHAR(100),
                       password VARCHAR(100)
);

CREATE TABLE products (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          article VARCHAR(50),
                          name VARCHAR(150),
                          unit_id INT,
                          price DECIMAL(10,2),
                          supplier_id INT,
                          manufacturer_id INT,
                          category_id INT,
                          discount INT,
                          stock INT,
                          description VARCHAR(500),
                          photo_path VARCHAR(255)
);

CREATE TABLE orders (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        user_id INT,
                        pickup_point_id INT,
                        date_order DATE,
                        date_delivery DATE,
                        code VARCHAR(50),
                        status VARCHAR(50)
);

CREATE TABLE order_items (
                             id INT AUTO_INCREMENT PRIMARY KEY,
                             order_id INT,
                             product_id INT,
                             quantity INT
);

INSERT INTO roles VALUES (1, 'Администратор'), (2, 'Менеджер'), (3, 'Авторизированный клиент');
INSERT INTO units VALUES (1, 'шт.');
INSERT INTO categories VALUES (1, 'Женская обувь'), (2, 'Мужская обувь');
INSERT INTO manufacturers VALUES (1, 'Kari'), (2, 'Marco Tozzi'), (3, 'Рос'), (4, 'Rieker'), (5, 'Alessio Nesca'), (6, 'CROSBY'), (7, 'Caprice');
INSERT INTO suppliers VALUES (1, 'Kari'), (2, 'Обувь для вас');

INSERT INTO users VALUES (1, 1, 'Никифорова Весения Николаевна', '94d5ous@gmail.com', 'uzWC67');
INSERT INTO users VALUES (2, 1, 'Сазонов Руслан Германович', 'uth4iz@mail.com', '2L6KZG');
INSERT INTO users VALUES (3, 1, 'Одинцов Серафим Артёмович', 'yzls62@outlook.com', 'JlFRCZ');
INSERT INTO users VALUES (4, 2, 'Степанов Михаил Артёмович', '1diph5e@tutanota.com', '8ntwUp');
INSERT INTO users VALUES (5, 2, 'Ворсин Петр Евгеньевич', 'tjde7c@yahoo.com', 'YOyhfR');
INSERT INTO users VALUES (6, 2, 'Старикова Елена Павловна', 'wpmrc3do@tutanota.com', 'RSbvHv');
INSERT INTO users VALUES (7, 3, 'Михайлюк Анна Вячеславовна', '5d4zbu@tutanota.com', 'rwVDh9');
INSERT INTO users VALUES (8, 3, 'Ситдикова Елена Анатольевна', 'ptec8ym@yahoo.com', 'LdNyos');
INSERT INTO users VALUES (9, 3, 'Ворсин Петр Евгеньевич', '1qz4kw@mail.com', 'gynQMT');
INSERT INTO users VALUES (10, 3, 'Старикова Елена Павловна', '4np6se@mail.com', 'AtnDjr');

INSERT INTO pickup_points VALUES (1, '420151, г. Лесной, ул. Вишневая, 32');
INSERT INTO pickup_points VALUES (2, '125061, г. Лесной, ул. Подгорная, 8');
INSERT INTO pickup_points VALUES (3, '630370, г. Лесной, ул. Шоссейная, 24');
INSERT INTO pickup_points VALUES (4, '400562, г. Лесной, ул. Зеленая, 32');
INSERT INTO pickup_points VALUES (5, '614510, г. Лесной, ул. Маяковского, 47');
INSERT INTO pickup_points VALUES (11, '410172, г. Лесной, ул. Северная, 13');
INSERT INTO pickup_points VALUES (15, '603036, г. Лесной, ул. Садовая, 4');
INSERT INTO pickup_points VALUES (19, '625683, г. Лесной, ул. 8 Марта');

INSERT INTO products VALUES (1, 'А112Т4', 'Ботинки', 1, 4990.00, 1, 1, 1, 3, 6, 'Женские Ботинки', '1.jpg');
INSERT INTO products VALUES (2, 'F635R4', 'Ботинки', 1, 3244.00, 2, 2, 1, 2, 13, 'Ботинки Marco Tozzi', '2.jpg');
INSERT INTO products VALUES (3, 'H782T5', 'Туфли', 1, 4499.00, 1, 1, 2, 4, 5, 'Туфли kari', '3.jpg');
INSERT INTO products VALUES (4, 'G783F5', 'Ботинки', 1, 5900.00, 1, 3, 2, 2, 8, 'Ботинки Рос-Обувь', '4.jpg');
INSERT INTO products VALUES (5, 'J384T6', 'Ботинки', 1, 3800.00, 2, 4, 2, 2, 16, 'Полуботинки Rieker', '5.jpg');
INSERT INTO products VALUES (6, 'D572U8', 'Кроссовки', 1, 4100.00, 2, 3, 2, 3, 6, 'Кроссовки', '6.jpg');
INSERT INTO products VALUES (7, 'F572H7', 'Туфли', 1, 2700.00, 1, 2, 1, 2, 14, 'Туфли Marco Tozzi', '7.jpg');
INSERT INTO products VALUES (8, 'D329H3', 'Полуботинки', 1, 1890.00, 2, 5, 1, 4, 4, 'Полуботинки Alessio', '8.jpg');
INSERT INTO products VALUES (9, 'B320R5', 'Туфли', 1, 4300.00, 1, 4, 1, 2, 6, 'Туфли Rieker', '9.jpg');
INSERT INTO products VALUES (10, 'G432E4', 'Туфли', 1, 2800.00, 1, 1, 1, 3, 15, 'Туфли kari', '10.jpg');
INSERT INTO products VALUES (11, 'S213E3', 'Полуботинки', 1, 2156.00, 2, 6, 2, 3, 6, 'Полуботинки CROSBY', 'picture.png');
INSERT INTO products VALUES (12, 'E482R4', 'Полуботинки', 1, 1800.00, 1, 1, 1, 2, 14, 'Полуботинки kari', 'picture.png');
INSERT INTO products VALUES (13, 'S634B5', 'Кеды', 1, 5500.00, 2, 7, 2, 3, 0, 'Кеды Caprice', 'picture.png');

INSERT INTO orders VALUES (1, 4, 1, '2025-02-27', '2025-04-20', '901', 'Завершен');
INSERT INTO orders VALUES (2, 1, 11, '2025-09-28', '2025-04-21', '902', 'Завершен');
INSERT INTO orders VALUES (3, 2, 2, '2025-03-21', '2025-04-22', '903', 'Завершен');

INSERT INTO order_items VALUES (1, 1, 1, 2);
INSERT INTO order_items VALUES (2, 1, 2, 2);
INSERT INTO order_items VALUES (3, 2, 3, 1);
INSERT INTO order_items VALUES (4, 2, 4, 1);