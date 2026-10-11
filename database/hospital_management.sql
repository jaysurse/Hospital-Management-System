-- =====================================================================
-- Hospital Management System (HMS) - Database Initialization Script
-- Database: hospital_management
-- Suitable for: MySQL 8.0+ / MariaDB / H2 (MySQL Mode)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS hospital_management;
USE hospital_management;

-- ---------------------------------------------------------------------
-- 1. Table: users (Authentication and Role-Based Access)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS bill_items;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    salt VARCHAR(32) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL, -- 'ADMIN', 'DOCTOR', 'RECEPTIONIST'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 2. Table: patients (Patient Demographics and Registration)
-- ---------------------------------------------------------------------
CREATE TABLE patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255) NOT NULL,
    blood_group VARCHAR(5) NOT NULL,
    emergency_contact VARCHAR(20),
    registration_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 3. Table: doctors (Doctor Profiles and Availability)
-- ---------------------------------------------------------------------
CREATE TABLE doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 500.00,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    room_number VARCHAR(20) NOT NULL,
    joining_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- 4. Table: appointments (Appointment Booking and Scheduling)
-- ---------------------------------------------------------------------
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Scheduled', -- 'Scheduled', 'Completed', 'Cancelled'
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE,
    CONSTRAINT uq_doctor_slot UNIQUE (doctor_id, appointment_date, appointment_time)
);

-- ---------------------------------------------------------------------
-- 5. Table: medical_records (Clinical Diagnoses and Prescriptions)
-- ---------------------------------------------------------------------
CREATE TABLE medical_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NULL,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    diagnosis TEXT NOT NULL,
    prescription TEXT NOT NULL,
    treatment_notes TEXT,
    recorded_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_record_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    CONSTRAINT fk_record_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 6. Table: bills (Invoicing and Financial Transactions)
-- ---------------------------------------------------------------------
CREATE TABLE bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NULL,
    patient_id INT NOT NULL,
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    additional_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PAID'
    payment_method VARCHAR(30) NULL, -- 'CASH', 'CARD', 'UPI', 'INSURANCE'
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_date TIMESTAMP NULL,
    CONSTRAINT fk_bill_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id) ON DELETE SET NULL,
    CONSTRAINT fk_bill_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- 7. Table: bill_items (Itemized charges demonstrating Composition)
-- ---------------------------------------------------------------------
CREATE TABLE bill_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    description VARCHAR(150) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_item_bill FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Indexes for Performance
-- ---------------------------------------------------------------------
CREATE INDEX idx_patient_phone ON patients(phone);
CREATE INDEX idx_patient_name ON patients(full_name);
CREATE INDEX idx_doctor_specialization ON doctors(specialization);
CREATE INDEX idx_appointment_date ON appointments(appointment_date);
CREATE INDEX idx_appointment_status ON appointments(status);
CREATE INDEX idx_bill_status ON bills(payment_status);

-- ---------------------------------------------------------------------
-- Seed Data: Users
-- Passwords:
-- admin / admin123
-- dr_sharma / doctor123
-- dr_patel / doctor123
-- receptionist / recep123
-- ---------------------------------------------------------------------
INSERT INTO users (username, password_hash, salt, full_name, email, role) VALUES
('admin', '21ad260b578fc1616f84fd6421d9e60ea0c952d5909473e0fecc3f3be9e9de62', 'salt_admin_001', 'Administrator Dr. Rajesh Roy', 'admin@hospital.org', 'ADMIN'),
('dr_sharma', 'be40b07146cdbe7ce9e7f4c7d560fef8a0a3facffeb8a53cbb557cc07825e148', 'salt_doctor_001', 'Dr. Arun Sharma', 'arun.sharma@hospital.org', 'DOCTOR'),
('dr_patel', 'be40b07146cdbe7ce9e7f4c7d560fef8a0a3facffeb8a53cbb557cc07825e148', 'salt_doctor_001', 'Dr. Priya Patel', 'priya.patel@hospital.org', 'DOCTOR'),
('receptionist', '8846ff2336ca08a03791ad618eece6d8d52cfe4f50ced272e34a7046eb67bda8', 'salt_recep_001', 'Sunita Rao (Front Desk)', 'reception@hospital.org', 'RECEPTIONIST');

-- ---------------------------------------------------------------------
-- Seed Data: Doctors
-- ---------------------------------------------------------------------
INSERT INTO doctors (full_name, specialization, qualification, phone, email, consultation_fee, is_available, room_number, joining_date) VALUES
('Dr. Arun Sharma', 'Cardiology', 'MBBS, MD, DM (Cardiology)', '+91 98234 56789', 'arun.sharma@hospital.org', 800.00, TRUE, 'OPD-101', '2021-03-15'),
('Dr. Priya Patel', 'Pediatrics', 'MBBS, DCH, MD (Pediatrics)', '+91 98123 45678', 'priya.patel@hospital.org', 600.00, TRUE, 'OPD-102', '2021-08-01'),
('Dr. Vikram Verma', 'Orthopedics', 'MBBS, MS (Ortho), M.Ch', '+91 98345 67890', 'vikram.verma@hospital.org', 750.00, TRUE, 'OPD-103', '2022-01-10'),
('Dr. Sneha Nair', 'Neurology', 'MBBS, MD, DM (Neurology)', '+91 98456 78901', 'sneha.nair@hospital.org', 900.00, TRUE, 'OPD-201', '2022-06-20'),
('Dr. Amit Kulkarni', 'General Medicine', 'MBBS, MD (Medicine)', '+91 98567 89012', 'amit.kulkarni@hospital.org', 500.00, TRUE, 'OPD-104', '2020-11-12'),
('Dr. Meera Iyer', 'Dermatology', 'MBBS, DVD, MD (Dermatology)', '+91 98678 90123', 'meera.iyer@hospital.org', 550.00, FALSE, 'OPD-205', '2023-02-14');

-- ---------------------------------------------------------------------
-- Seed Data: Patients
-- ---------------------------------------------------------------------
INSERT INTO patients (full_name, age, gender, phone, email, address, blood_group, emergency_contact, registration_date) VALUES
('Ramesh Chandra Joshi', 48, 'Male', '+91 97111 22334', 'ramesh.joshi@email.com', 'Flat 402, Green Valley Apts, Civil Lines', 'B+', '+91 97111 22335', '2026-09-10'),
('Anita Deshmukh', 34, 'Female', '+91 97222 33445', 'anita.d@email.com', '12B, Lake View Residency, Sector 4', 'O+', '+91 97222 33440', '2026-09-15'),
('Karan Singhania', 12, 'Male', '+91 97333 44556', 'singhania.parent@email.com', '78, Hill Crest Avenue, Model Town', 'A+', '+91 97333 44557', '2026-09-20'),
('Pooja Sundaram', 29, 'Female', '+91 97444 55667', 'pooja.s@email.com', 'B-14, Shanti Niketan, Mall Road', 'AB+', '+91 97444 55668', '2026-09-25'),
('Mohammad Zafar', 62, 'Male', '+91 97555 66778', 'zafar.m@email.com', '304, Heritage Towers, Station Road', 'O-', '+91 97555 66779', '2026-10-01'),
('Kavita Rathore', 55, 'Female', '+91 97666 77889', 'kavita.r@email.com', '56, Mayur Vihar Phase 2', 'A-', '+91 97666 77880', '2026-10-03');

-- ---------------------------------------------------------------------
-- Seed Data: Appointments
-- Note: Uses CURRENT_DATE for today's appointment queries on the dashboard!
-- ---------------------------------------------------------------------
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason, status, notes) VALUES
(1, 1, CURRENT_DATE, '09:30:00', 'Routine Cardiac Follow-up & ECG Review', 'Scheduled', 'Patient reports mild chest tightness after exercise.'),
(2, 2, CURRENT_DATE, '10:15:00', 'Persistent cough and mild fever', 'Scheduled', 'Child has had dry cough for 3 days.'),
(3, 2, CURRENT_DATE, '11:00:00', 'Annual pediatric vaccination check', 'Scheduled', 'Scheduled booster dose evaluation.'),
(4, 3, CURRENT_DATE, '11:45:00', 'Right knee pain and swelling', 'Scheduled', 'Suspected ligament sprain during badminton.'),
(5, 5, CURRENT_DATE, '14:00:00', 'High blood sugar and fatigue evaluation', 'Scheduled', 'Type 2 diabetes quarterly review.'),
(1, 1, '2026-09-28', '10:00:00', 'Initial cardiology consultation', 'Completed', 'ECG done, blood pressure 138/88 mmHg.'),
(2, 5, '2026-09-30', '11:30:00', 'Seasonal allergic rhinitis checkup', 'Completed', 'Prescribed antihistamines and nasal spray.'),
(6, 4, CURRENT_DATE, '16:30:00', 'Migraine and visual aura consultation', 'Cancelled', 'Cancelled by patient due to family travel.');

-- ---------------------------------------------------------------------
-- Seed Data: Medical Records
-- ---------------------------------------------------------------------
INSERT INTO medical_records (appointment_id, patient_id, doctor_id, diagnosis, prescription, treatment_notes, recorded_date) VALUES
(6, 1, 1, 'Mild Essential Hypertension, Sinus Tachycardia', 'Tab. Telmisartan 40mg (1 OD morning) x 30 days\nTab. Metoprolol 25mg (1 OD) x 30 days', 'Advised low sodium diet (<2g/day) and daily 30-min brisk walk. Repeat lipid profile after 4 weeks.', '2026-09-28 10:45:00'),
(7, 2, 5, 'Acute Allergic Rhinosinusitis', 'Tab. Levocetirizine 5mg (1 HS) x 7 days\nFluticasone Furoate Nasal Spray (2 sprays each nostril OD)', 'Steam inhalation twice daily. Avoid cold exposure and air pollutants.', '2026-09-30 12:10:00');

-- ---------------------------------------------------------------------
-- Seed Data: Bills & Bill Items
-- ---------------------------------------------------------------------
INSERT INTO bills (appointment_id, patient_id, consultation_fee, additional_charges, tax_amount, discount, total_amount, payment_status, payment_method, bill_date, paid_date) VALUES
(6, 1, 800.00, 450.00, 62.50, 50.00, 1262.50, 'PAID', 'CARD', '2026-09-28 11:00:00', '2026-09-28 11:05:00'),
(7, 2, 500.00, 150.00, 32.50, 0.00, 682.50, 'PAID', 'UPI', '2026-09-30 12:20:00', '2026-09-30 12:22:00'),
(1, 1, 800.00, 300.00, 55.00, 0.00, 1155.00, 'PENDING', NULL, CURRENT_TIMESTAMP, NULL),
(4, 4, 750.00, 650.00, 70.00, 0.00, 1470.00, 'PENDING', NULL, CURRENT_TIMESTAMP, NULL);

INSERT INTO bill_items (bill_id, description, amount) VALUES
(1, 'Doctor Consultation (Cardiology)', 800.00),
(1, '12-Lead Electrocardiogram (ECG)', 450.00),
(2, 'Doctor Consultation (General Medicine)', 500.00),
(2, 'Rapid Allergy Panel Screening', 150.00),
(3, 'Doctor Consultation (Cardiology Follow-up)', 800.00),
(3, 'Blood Sugar & Serum Electrolytes Test', 300.00),
(4, 'Doctor Consultation (Orthopedics)', 750.00),
(4, 'Digital X-Ray Right Knee (AP & Lat)', 650.00);
