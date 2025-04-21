-- Tạo login cấp server
CREATE LOGIN tienlam14 WITH PASSWORD = '123456';
-- Chuyển context sang database cần cấp quyền
USE NetCafeDB;
-- Tạo user trong database tương ứng với login
CREATE USER tienlam14 FOR LOGIN tienlam14;
-- Gán quyền db_owner cho user trong database
EXEC sp_addrolemember 'db_owner', 'tienlam14';

