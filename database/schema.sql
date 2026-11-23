-- RELOOP database schema (Evaluation 1)
-- Run with:  mysql -u root -p < database/schema.sql

CREATE DATABASE IF NOT EXISTS reloop CHARACTER SET utf8mb4;
USE reloop;

CREATE TABLE IF NOT EXISTS recycling_stations (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    address     VARCHAR(200) NOT NULL,
    area        VARCHAR(80)  NOT NULL,
    open_time   TIME NOT NULL,
    close_time  TIME NOT NULL
);

CREATE TABLE IF NOT EXISTS materials (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(60) NOT NULL UNIQUE,
    unit             ENUM('KG','PIECE') NOT NULL,
    points_per_unit  INT NOT NULL CHECK (points_per_unit > 0),
    active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS station_materials (
    station_id  INT NOT NULL,
    material_id INT NOT NULL,
    PRIMARY KEY (station_id, material_id),
    FOREIGN KEY (station_id)  REFERENCES recycling_stations(id),
    FOREIGN KEY (material_id) REFERENCES materials(id)
);

CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,          -- format: salt:hash (PBKDF2)
    role          ENUM('CUSTOMER','OPERATOR','ADMIN') NOT NULL,
    phone         VARCHAR(20),
    station_id    INT NULL,                       -- only operators have a station
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (station_id) REFERENCES recycling_stations(id)
);

CREATE TABLE IF NOT EXISTS wallets (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL UNIQUE,
    balance     INT NOT NULL DEFAULT 0 CHECK (balance >= 0),
    FOREIGN KEY (customer_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS submissions (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    customer_id   INT NOT NULL,
    station_id    INT NOT NULL,
    operator_id   INT NULL,
    status        ENUM('PENDING','VERIFIED','REJECTED') NOT NULL DEFAULT 'PENDING',
    reject_reason VARCHAR(255),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    verified_at   TIMESTAMP NULL,
    FOREIGN KEY (customer_id) REFERENCES users(id),
    FOREIGN KEY (station_id)  REFERENCES recycling_stations(id),
    FOREIGN KEY (operator_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS submission_items (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    submission_id INT NOT NULL,
    material_id   INT NOT NULL,
    quantity      DECIMAL(10,2) NOT NULL CHECK (quantity > 0),
    points_earned INT NOT NULL DEFAULT 0,
    FOREIGN KEY (submission_id) REFERENCES submissions(id),
    FOREIGN KEY (material_id)   REFERENCES materials(id)
);

CREATE TABLE IF NOT EXISTS points_transactions (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    wallet_id     INT NOT NULL,
    submission_id INT NULL,
    points        INT NOT NULL,
    type          ENUM('EARNED','REDEEMED') NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id)     REFERENCES wallets(id),
    FOREIGN KEY (submission_id) REFERENCES submissions(id)
);

-- ---------- Seed data (safe to re-run) ----------
INSERT IGNORE INTO materials (name, unit, points_per_unit) VALUES
 ('Plastic Bottles','PIECE',2),
 ('Aluminium Cans','PIECE',3),
 ('Paper','KG',10),
 ('Cardboard','KG',8),
 ('Glass Jars','PIECE',4);

INSERT IGNORE INTO recycling_stations (name, address, area, open_time, close_time) VALUES
 ('GreenMart Central Station','Ground Floor Parking, GreenMart','City Centre','08:00:00','20:00:00'),
 ('FreshBasket Eastside Station','Gate 2, FreshBasket Supermarket','Model Town','09:00:00','21:00:00'),
 ('MetroMart Railway Station','Beside MetroMart Entrance','Railway Road','07:30:00','19:30:00');

-- Every station accepts every material except Glass Jars at the third station
INSERT IGNORE INTO station_materials (station_id, material_id)
 SELECT s.id, m.id FROM recycling_stations s CROSS JOIN materials m
 WHERE NOT (s.name = 'MetroMart Railway Station' AND m.name = 'Glass Jars');
