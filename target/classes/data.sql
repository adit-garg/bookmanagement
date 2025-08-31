-- data.sql (placed in src/main/resources/)
-- Initial data for testing

-- Insert admin user (password: admin123)
INSERT INTO users (username, email, password, address, customer_id, role, age, created_date) 
VALUES ('admin', 'admin@bookstore.com', 'admin123', 
        'Admin Address', 'ADMIN001', 'ADMIN', 30, CURRENT_TIMESTAMP);

-- Insert sample books
INSERT INTO books (title, author, price, description, category, stock) VALUES
('The Great Gatsby', 'F. Scott Fitzgerald', 12.99, 'A classic American novel', 'Fiction', 50),
('To Kill a Mockingbird', 'Harper Lee', 13.99, 'A gripping tale of racial injustice', 'Fiction', 30),
('1984', 'George Orwell', 14.99, 'Dystopian social science fiction', 'Fiction', 45),
('Clean Code', 'Robert Martin', 45.99, 'A handbook of agile software craftsmanship', 'Technology', 25),
('Java: The Complete Reference', 'Herbert Schildt', 55.99, 'Comprehensive Java programming guide', 'Technology', 20),
('The Psychology of Money', 'Morgan Housel', 18.99, 'Timeless lessons on wealth and happiness', 'Finance', 35);