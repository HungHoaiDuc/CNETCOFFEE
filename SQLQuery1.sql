-- Xóa database nếu tồn tại (Chạy khi muốn reset database)
IF EXISTS (SELECT * FROM sys.databases WHERE name = 'NetCafeDB')
BEGIN
    ALTER DATABASE NetCafeDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE NetCafeDB;
END

-- Tạo mới database
CREATE DATABASE NetCafeDB;
GO
USE NetCafeDB;
GO

-- Bảng lưu thông tin người dùng (Khách + Admin)
CREATE TABLE prices (
                        type VARCHAR(20) PRIMARY KEY, -- Loại máy ('NORMAL', 'VIP') với độ dài 20 để khớp với computers.type
                        price_per_hour INT NOT NULL   -- Giá tiền mỗi giờ
);
GO

-- Bảng quản lý máy trạm (PC trong quán net)
CREATE TABLE computers (
                           computer_id INT IDENTITY(1,1) PRIMARY KEY,
                           name NVARCHAR(50) UNIQUE NOT NULL,
                           status VARCHAR(20) CHECK (status IN ('AVAILABLE', 'IN USE', 'MAINTENANCE', 'READY')) DEFAULT 'AVAILABLE',
                           isAvailable BIT DEFAULT 0,
                           socket_port varchar(20) NULL, -- Thêm cột socket_port
                           type VARCHAR(20) NULL, -- Định nghĩa type với độ dài 20 để khớp với prices.type
                           CONSTRAINT FK_computers_prices FOREIGN KEY (type) REFERENCES prices(type) -- Thêm khóa ngoại liên kết với bảng prices
);
GO

-- Bảng lưu thông tin người dùng (Khách + Admin)
CREATE TABLE users (
                       user_id INT IDENTITY(1,1) PRIMARY KEY,
                       username NVARCHAR(50) UNIQUE NOT NULL,
                       password NVARCHAR(255) NOT NULL,
                       full_name NVARCHAR(100) NULL, -- Cho phép NULL cho full_name
                       email NVARCHAR(100) NULL,
                       phone NVARCHAR(15) NULL, -- Đảm bảo phone có thể NULL
                       balance DECIMAL(10,2) DEFAULT 0, -- Số tiền còn trong tài khoản
                       role VARCHAR(10) CHECK (role IN ('ADMIN', 'USER', 'STAFF')) DEFAULT 'USER', -- Thêm STAFF vào role
                       status VARCHAR(50) DEFAULT 'inactive', -- Thêm status với giá trị mặc định
                       created_at DATETIME DEFAULT GETDATE(),
                       assigned_computer_id INT NULL,
                       FOREIGN KEY (assigned_computer_id) REFERENCES computers(computer_id) ON DELETE SET NULL
);
GO

-- Bảng quản lý các phiên chơi của khách
CREATE TABLE sessions (
                          session_id INT IDENTITY(1,1) PRIMARY KEY,
                          user_id INT NOT NULL,
                          computer_id INT NOT NULL,
                          start_time DATETIME DEFAULT GETDATE(), -- Thời gian bắt đầu chơi
                          end_time DATETIME NULL, -- Thời gian kết thúc chơi
                          total_minutes INT NULL, -- Tổng số phút đã chơi
                          total_seconds_used INT DEFAULT 0, -- Thêm cột total_seconds_used
                          total_cost DECIMAL(10,2) NULL, -- Tổng số tiền cần thanh toán
                          computer_type VARCHAR(20) NULL, -- Thêm cột computer_type
                          status VARCHAR(20) CHECK (status IN ('ACTIVE', 'ENDED', 'FORCED_STOP')) DEFAULT 'ACTIVE',
                          FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (computer_id) REFERENCES computers(computer_id) ON DELETE CASCADE
);
GO

-- Bảng quản lý nạp tiền vào tài khoản
CREATE TABLE deposits (
                          deposit_id INT IDENTITY(1,1) PRIMARY KEY,
                          user_id INT NOT NULL,
                          amount DECIMAL(10,2) CHECK (amount > 0) NOT NULL,
                          deposit_time DATETIME DEFAULT GETDATE(),
                          FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
GO

-- Bảng dịch vụ ăn uống
CREATE TABLE foods (
                       food_id INT IDENTITY(1,1) PRIMARY KEY,
                       food_name VARCHAR(100) UNIQUE NOT NULL,
                       price DECIMAL(10,2) NOT NULL,
                       category VARCHAR(10) CHECK (category IN ('FOOD', 'DRINK')) NOT NULL,
                       availability VARCHAR(30) CHECK (availability IN ('Available', 'Out of Stock', 'Stop Doing Business')) NOT NULL DEFAULT 'Available', -- Cập nhật ràng buộc availability
                       image VARBINARY(MAX) NULL
);
GO

-- Bảng đặt món ăn
CREATE TABLE orders (
                        order_id INT IDENTITY(1,1) PRIMARY KEY,
                        user_id INT NOT NULL,
                        session_id INT NULL, -- Cho phép NULL cho session_id
                        order_time DATETIME DEFAULT GETDATE(),
                        total_price DECIMAL(10,2) NOT NULL,
                        status VARCHAR(20) CHECK (status IN ('PENDING', 'BEING PREPARED', 'COMPLETED', 'CANCELLED')) DEFAULT 'PENDING',
                        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                        FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE NO ACTION
);
GO

-- Bảng chi tiết đơn hàng (mỗi order có nhiều món)
CREATE TABLE order_details (
                               order_detail_id INT IDENTITY(1,1) PRIMARY KEY,
                               order_id INT NOT NULL,
                               food_id INT NOT NULL,
                               quantity INT CHECK (quantity > 0) NOT NULL,
                               subtotal DECIMAL(10,2) NOT NULL,
                               FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
                               FOREIGN KEY (food_id) REFERENCES foods(food_id) ON DELETE CASCADE
);
GO

-- Bảng tin nhắn giữa Admin & User (Hỗ trợ chat)
CREATE TABLE messages (
                          message_id INT IDENTITY(1,1) PRIMARY KEY,
                          sender_id INT NOT NULL,
                          receiver_id INT NOT NULL,
                          content NVARCHAR(MAX) NOT NULL,
                          sent_time DATETIME DEFAULT GETDATE(),
                          session_id INT NOT NULL, -- Thêm session_id
                          FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE NO ACTION, -- Không cho phép xóa user khi có tin nhắn
                          FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE NO ACTION,
                          FOREIGN KEY (session_id) REFERENCES sessions(session_id) ON DELETE CASCADE -- Khóa ngoại liên kết với bảng sessions
);
GO

CREATE TABLE order_items (
                             order_item_id INT PRIMARY KEY IDENTITY(1,1),
                             order_id INT NOT NULL,
                             food_id INT NOT NULL,
                             product_name VARCHAR(255) NOT NULL,
                             quantity INT NOT NULL,
                             price DECIMAL(10, 2) NOT NULL,
                             FOREIGN KEY (order_id) REFERENCES orders(order_id)
);
GO

-- Thêm giá tiền mặc định
INSERT INTO prices (type, price_per_hour) VALUES ('NORMAL', 5000), ('VIP', 7000);

-- Thêm dữ liệu mẫu vào bảng users
INSERT INTO users (username, password, full_name, email, phone, balance, role, created_at)
VALUES ('admin', '123456', 'Administrator', 'admin@example.com', '0123456789', 0, 'ADMIN', GETDATE());
INSERT INTO users (username, password, full_name, email, phone, balance, role, created_at)
VALUES ('staff', '123', 'Staff', 'staff@example.com', '0123456789', 0, 'STAFF', GETDATE());
INSERT INTO users (username, password, full_name, role, status)
VALUES ('Guest', '', N'Khách vãng lai', 'USER', 'active');
INSERT INTO users (username, password, full_name, role, balance, status)
VALUES ('guest_user', 'guest', N'Khách vãng lai', 'USER', 0, 'active');