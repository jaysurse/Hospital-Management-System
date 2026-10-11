package com.hospital;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import static org.junit.jupiter.api.Assertions.*;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.BillDAO;
import com.hospital.dao.DBConnection;
import com.hospital.dao.DoctorDAO;
import com.hospital.dao.MedicalRecordDAO;
import com.hospital.dao.PatientDAO;
import com.hospital.dao.UserDAO;
import com.hospital.exception.AppointmentException;
import com.hospital.exception.InvalidPatientException;
import com.hospital.model.Appointment;
import com.hospital.model.AppointmentStatus;
import com.hospital.model.Bill;
import com.hospital.model.BillItem;
import com.hospital.model.Doctor;
import com.hospital.model.MedicalRecord;
import com.hospital.model.Patient;
import com.hospital.model.PaymentStatus;
import com.hospital.model.User;
import com.hospital.service.AppointmentService;
import com.hospital.service.AuthService;
import com.hospital.service.BillingService;
import com.hospital.service.PatientService;
import com.hospital.service.ReportService;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseAndServiceTest {

    private static PatientDAO patientDAO;
    private static DoctorDAO doctorDAO;
    private static AppointmentDAO appointmentDAO;
    private static BillDAO billDAO;
    private static MedicalRecordDAO medicalRecordDAO;
    private static UserDAO userDAO;

    @BeforeAll
    public static void setUp() {
        // Ensure DB connection is initialized
        assertTrue(DBConnection.getInstance().testConnection(), "Database connection must be valid");
        patientDAO = new PatientDAO();
        doctorDAO = new DoctorDAO();
        appointmentDAO = new AppointmentDAO();
        billDAO = new BillDAO();
        medicalRecordDAO = new MedicalRecordDAO();
        userDAO = new UserDAO();
    }

    @Test
    @Order(1)
    @DisplayName("Verify Database Initialization and Seed Data")
    public void testDatabaseSeedData() throws Exception {
        assertTrue(patientDAO.count() > 0, "Patients table should have seed data");
        assertTrue(doctorDAO.count() > 0, "Doctors table should have seed data");
        assertTrue(appointmentDAO.findAll().size() > 0, "Appointments table should have seed data");
        assertTrue(billDAO.findAll().size() > 0, "Bills table should have seed data");
    }

    @Test
    @Order(2)
    @DisplayName("Test User Authentication with SHA-256 and Salt")
    public void testAuthentication() throws Exception {
        User admin = userDAO.authenticate("admin", "admin123");
        assertNotNull(admin, "Admin user must authenticate successfully");
        assertEquals("ADMIN", admin.getRole().name());

        User invalid = userDAO.authenticate("admin", "wrongpassword");
        assertNull(invalid, "Invalid password must fail authentication");

        AuthService auth = AuthService.getInstance();
        User loggedIn = auth.login("admin", "admin123");
        assertNotNull(loggedIn);
        assertTrue(auth.isLoggedIn());
        assertTrue(auth.isAdmin());
        auth.logout();
        assertFalse(auth.isLoggedIn());
    }

    @Test
    @Order(3)
    @DisplayName("Test Patient CRUD, Polymorphism, and Overloaded Search")
    public void testPatientOperations() throws Exception {
        PatientService service = new PatientService(patientDAO);

        Patient newPatient = new Patient(
            "Aditya Vikram Singhania",
            30,
            "Male",
            "+91 99999 88888",
            "701, Marina Heights, Gateway Road",
            "O+"
        );
        newPatient.setEmail("aditya.v@singhania.com");
        newPatient.setEmergencyContact("+91 99999 88880");

        // Runtime Polymorphism test: call abstract method getDetails() on Person reference
        com.hospital.model.Person personRef = newPatient;
        assertNotNull(personRef.getDetails());
        assertEquals("Patient", personRef.getRole());

        Patient saved = service.registerPatient(newPatient);
        assertNotNull(saved.getId(), "Patient ID must be auto-generated");
        assertTrue(saved.getId() > 0);

        // Search test (Method Overloading)
        List<Patient> foundByKeyword = patientDAO.search("Singhania");
        assertFalse(foundByKeyword.isEmpty());

        List<Patient> foundByNameAndPhone = patientDAO.search("Aditya", "99999");
        assertFalse(foundByNameAndPhone.isEmpty());

        // Update
        saved.setPhone("+91 99999 77777");
        service.updatePatient(saved);
        Patient updated = service.getPatientById(saved.getId());
        assertEquals("+91 99999 77777", updated.getPhone());
    }

    @Test
    @Order(4)
    @DisplayName("Test Doctor Creation and Specialization Filtering")
    public void testDoctorOperations() throws Exception {
        Doctor doc = new Doctor(
            "Dr. Rajesh Varma",
            "Oncology",
            "+91 98989 12345",
            1000.0,
            "OPD-305"
        );
        doc.setQualification("MBBS, MD (Radiation Oncology)");

        com.hospital.model.Person docPerson = doc;
        assertEquals("Doctor", docPerson.getRole());
        assertTrue(docPerson.getDetails().contains("OPD-305"));

        Doctor saved = doctorDAO.save(doc);
        assertNotNull(saved.getId());

        List<Doctor> oncologists = doctorDAO.searchBySpecialization("Oncology");
        assertFalse(oncologists.isEmpty());
    }

    @Test
    @Order(5)
    @DisplayName("Test Appointment Scheduling and Conflict Prevention")
    public void testAppointmentConflictPrevention() throws Exception {
        AppointmentService apptService = new AppointmentService(appointmentDAO, doctorDAO, patientDAO, null);

        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LocalTime slot = LocalTime.of(15, 0);

        // Doctor 1 booking
        Appointment apt1 = new Appointment(1, 1, tomorrow, slot, "Routine consultation");
        Appointment booked1 = apptService.scheduleAppointment(apt1);
        assertNotNull(booked1.getId());

        // Attempt second booking for Doctor 1 at the EXACT same date and time slot
        Appointment aptConflict = new Appointment(2, 1, tomorrow, slot, "Urgent cardiac review");
        assertThrows(AppointmentException.class, () -> {
            apptService.scheduleAppointment(aptConflict);
        }, "Must reject conflicting appointment for same doctor at same date and time slot");
    }

    @Test
    @Order(6)
    @DisplayName("Test Bill Generation with Transaction and Composition")
    public void testBillingTransactionAndComposition() throws Exception {
        BillingService billingService = new BillingService(billDAO, patientDAO, null);

        Bill bill = new Bill(null, 1, 800.00, 400.00);
        bill.addItem(new BillItem("Specialist Consultation Fee", 800.00));
        bill.addItem(new BillItem("High-Resolution Ultrasound", 400.00));
        bill.calculateTotal();

        // 800 + 400 = 1200; 5% tax = 60.00; Total = 1260.00
        assertEquals(1260.00, bill.getTotalAmount(), 0.01);
        assertEquals(2, bill.getItems().size(), "Bill should compose 2 items");

        Bill saved = billingService.generateBill(bill);
        assertNotNull(saved.getId());
        assertEquals(PaymentStatus.PENDING, saved.getPaymentStatus());

        // Verify composed items persisted via transaction
        Bill loaded = billDAO.findById(saved.getId());
        assertEquals(2, loaded.getItems().size());

        // Mark as paid
        boolean paid = billingService.markAsPaid(saved.getId(), "CARD");
        assertTrue(paid);

        Bill reloaded = billDAO.findById(saved.getId());
        assertEquals(PaymentStatus.PAID, reloaded.getPaymentStatus());
        assertEquals("CARD", reloaded.getPaymentMethod());
    }

    @Test
    @Order(7)
    @DisplayName("Test Clinical Medical Record History")
    public void testMedicalRecords() throws Exception {
        MedicalRecord record = new MedicalRecord(
            null, 1, 1,
            "Type 2 Diabetes Mellitus - Controlled",
            "Tab. Metformin 500mg (1 BD after meals)",
            "Advised 3-month HbA1c test and diet control."
        );
        MedicalRecord saved = medicalRecordDAO.save(record);
        assertNotNull(saved.getId());

        List<MedicalRecord> history = medicalRecordDAO.findByPatientId(1);
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(r -> r.getDiagnosis().contains("Diabetes")));
    }

    @Test
    @Order(8)
    @DisplayName("Test Report Generation and Iterator Pattern Traversal")
    public void testReportGeneration() throws Exception {
        ReportService reportService = new ReportService(patientDAO, doctorDAO, appointmentDAO, billDAO);

        ReportService.ReportData patientReport = reportService.generatePatientReport();
        assertNotNull(patientReport);
        assertFalse(patientReport.getRows().isEmpty());
        assertTrue(patientReport.getSummaryLines().size() > 0);

        ReportService.ReportData revenueReport = reportService.generateRevenueReport();
        assertNotNull(revenueReport);
        assertTrue(revenueReport.getSummaryLines().stream().anyMatch(l -> l.contains("Gross Billed Revenue")));
    }
}
