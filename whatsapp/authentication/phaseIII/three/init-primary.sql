-- Clean existing users (important for repeat runs)
DROP USER IF EXISTS 'repl_user'@'%';
DROP USER IF EXISTS 'app_user'@'%';

CREATE USER 'repl_user'@'%' IDENTIFIED BY 'repl_pass';
GRANT REPLICATION SLAVE ON *.* TO 'repl_user'@'%';

CREATE USER 'app_user'@'%' IDENTIFIED BY 'app_pass';
GRANT ALL PRIVILEGES ON whatsapp_db.* TO 'app_user'@'%';

FLUSH PRIVILEGES;