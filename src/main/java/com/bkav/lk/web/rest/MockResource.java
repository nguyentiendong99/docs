package com.bkav.lk.web.rest;

import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorVM;
import com.bkav.lk.web.errors.validation.validator.HealthInsuranceCodeValidator;
import com.bkav.lk.web.rest.vm.HisDoctor;
import com.bkav.lk.web.rest.vm.HisPatientContentVM;
import com.bkav.lk.web.rest.vm.MedicalResultVM;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/public")
public class MockResource {

    private static final Logger log = LoggerFactory.getLogger(MockResource.class);

//    private static final String MOCK_PATIENT_CODE_123 = "HSBN-123";
//    private static final String MOCK_PATIENT_CODE_124 = "HSBN-124";
//    private static final String MOCK_PATIENT_CODE_1000 = "HSBN-1000";
//
//    private static final String MOCK_PATIENT_NAME_123 = "Nguyễn Hoàng A";
//    private static final String MOCK_PATIENT_NAME_124 = "Nguyễn Hoàng B";
//    private static final String MOCK_PATIENT_NAME_1000 = "Nguyễn Hoàng C";
//
//    private static final String MOCK_PATIENT_APPOINTMENT_CODE_12344 = "12344";
//    private static final String MOCK_PATIENT_APPOINTMENT_CODE_12345 = "12345";
//    private static final String MOCK_PATIENT_APPOINTMENT_CODE_12346 = "12346";
//    private static final String MOCK_PATIENT_APPOINTMENT_CODE_12347 = "12347";
//    private static final String MOCK_PATIENT_APPOINTMENT_CODE_12348 = "12348";
//    private static final String MOCK_PATIENT_APPOINTMENT_CODE_12349 = "12349";
//
//    private static final String MOCK_PATIENT_A_PHONE = "0353968323";
//    private static final String MOCK_PATIENT_B_PHONE = "0961907098";
//    private static final String MOCK_PATIENT_C_PHONE = "0332422262";
//
//    private static final String MOCK_HEALTH_FACILITY_CODE = "BVNYB";
//    private static final String MOCK_HEALTH_FACILITY_NAME = "Bệnh viện nhi Yên Bái";

    private final ObjectMapper objectMapper;
    private List<MedicalResultVM> medicalResultVMs = null;
    private List<HisPatientContentVM> hisPatientContentVMs = null;

    @Autowired
    public MockResource() {
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    private void initDumpMedicalResult() {
        try {
            Resource medicalResultResource = new ClassPathResource("mock/medical_results.json");
            Resource patientResource = new ClassPathResource("mock/patient.json");
            medicalResultVMs = objectMapper.readValue(medicalResultResource.getInputStream(), new TypeReference<List<MedicalResultVM>>() {
            });
            hisPatientContentVMs = objectMapper.readValue(patientResource.getInputStream(), new TypeReference<List<HisPatientContentVM>>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @GetMapping("/makham/{his_makham}")
    public ResponseEntity<ErrorVM> checkAppointmentCode(
            @PathVariable(name = "his_makham") String appointmentCode) {
        ErrorVM result = new ErrorVM();
        boolean codeExist = medicalResultVMs.stream().anyMatch(o -> appointmentCode.equalsIgnoreCase(o.getDoctorAppointmentCode()));
        if (codeExist) {
            result.setErrorCode("0");
        } else {
            result.setErrorCode("400");
            result.setErrorMsg("Appointment code isn't exist");
        }
        return ResponseEntity.ok().body(result);
    }

    /**
     * API Lấy danh sách bác sĩ
     *
     * @return
     */
    @GetMapping("/danhsachbacsykham")
    public ResponseEntity<List<HisDoctor>> getDoctors() {
        List<HisDoctor> doctors = new ArrayList<>();
        HisDoctor hisDoctor = new HisDoctor();
        hisDoctor.setDoctorName("Lê Anh Minh");
        hisDoctor.setDoctorCode("MinhLA");
        doctors.add(hisDoctor);
        return ResponseEntity.ok().body(doctors);
    }

    /**
     * API lấy kết quả khám
     *
     * @param patientCode
     * @param appointmentCode
     * @return
     */
    @PostMapping("/ketquakham/{his_mabenhnhan}/{his_makham}")
    public ResponseEntity<MedicalResultVM> getAppointmentResult(@PathVariable(name = "his_mabenhnhan") String patientCode,
                                                                @PathVariable(name = "his_makham") String appointmentCode) {
        log.debug("Payload data: patientCode - {}; appointmentCode - {}", patientCode, appointmentCode);
        Optional<MedicalResultVM> dumpMedicalResult = medicalResultVMs.stream()
                .filter(o -> patientCode.equalsIgnoreCase(o.getPatientCode()) && appointmentCode.equalsIgnoreCase(o.getDoctorAppointmentCode()))
                .findFirst();
        return ResponseEntity.ok().body(dumpMedicalResult.isPresent() ? dumpMedicalResult.get() : null);

    }

    /**
     * API lấy kết quả khám (không cần mã bệnh nhân)
     *
     * @param appointmentCode
     * @return
     */
    @PostMapping("/ketquakham/{his_makham}")
    public ResponseEntity<MedicalResultVM> getAppointmentResult(@PathVariable(name = "his_makham") String appointmentCode) {
        log.debug("Payload data: appointmentCode - {}", appointmentCode);
        Optional<MedicalResultVM> dumpMedicalResult = medicalResultVMs.stream()
                .filter(o -> appointmentCode.equalsIgnoreCase(o.getDoctorAppointmentCode()))
                .findFirst();
        return ResponseEntity.ok().body(dumpMedicalResult.get());

    }

    /**
     * API lấy danh sách kết quả khám của bệnh nhân
     *
     * @param patientCode
     * @param patientName
     * @return
     */
    @GetMapping("/benhnhan/{his_mabenhnhan}/{his_tenbenhnhan}")
    public ResponseEntity<Object> getPatientResult(
            @PathVariable("his_mabenhnhan") String patientCode,
            @PathVariable("his_tenbenhnhan") String patientName) {
        log.debug("Payload data: patientCode - {}, patientName - {}", patientCode, patientName);
        ErrorVM responseError = new ErrorVM();
        Optional<HisPatientContentVM> responseData = hisPatientContentVMs.stream()
                .filter(o -> patientCode.equalsIgnoreCase(o.getConnectionCode()) && patientName.equalsIgnoreCase(o.getPatientRecordName()))
                .findFirst();
        if (!responseData.isPresent()) {
            responseError.setErrorCode("01");
            return ResponseEntity.badRequest().body(responseError);
        }
        return ResponseEntity.ok().body(responseData.get());
    }

    @GetMapping("/baohiemyte")
    public ResponseEntity<Map<String, String>> getHealthInsurance(@RequestParam(name = "maThe") String maThe,
                                                                  @RequestParam(name = "hoTen") String hoTen,
                                                                  @RequestParam(name = "ngaySinh") String ngaySinh) {
        log.debug("Payload data: maThe - {}", maThe);
        Map<String, String> result = new HashMap<>();
        if (maThe.equals("HX1234567899875")) { // Khong khop thong tin
            result.put("code", "wrong_information");
            result.put("msg", "Wrong Information");
        } else if (!HealthInsuranceCodeValidator.isValid(maThe)) {
            throw new BadRequestAlertException(HealthInsuranceCodeValidator.ERROR_DEFAULT_MESSAGE,
                    "baohiemyte", "health_insurance_code.wrong_format");
        } else if (maThe.equals("HX1234567899874")) { // The BHYT het han
            result.put("code", "expired");
            result.put("msg", "Expired");
        } else if (maThe.equals("HX1234567899873")) { // Khong tim thay the BHYT
            result.put("code", "invalid");
            result.put("msg", "Invalid");
        } else {
            result.put("code", "valid");
            result.put("msg", "Valid");
            result.put("maThe", maThe);
            result.put("hoTen", hoTen);
            result.put("ngaySinh", ngaySinh);
            result.put("maCSKCB", "Hehehehe");
            result.put("ngayBD", "01/01/2021");
            Instant instant = Instant.now().atZone(DateUtils.getZoneHCM()).toInstant().plus(15, ChronoUnit.DAYS);
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String ngayKT= formatter.format(Date.from(instant));
            result.put("ngayKT",ngayKT);
        }
        return ResponseEntity.ok().body(result);
    }

    /**
     * API lấy số điện thoại của bệnh nhân theo mã khám
     *
     * @return
     */
    @GetMapping("/benhnhan/makham/{his_makham}/sodienthoai")
    public ResponseEntity<Object> getPatientResult(@PathVariable("his_makham") String appointmentCode) {
        log.debug("Payload data: appointmentCode - {}", appointmentCode);
        ErrorVM responseError = new ErrorVM();
        Map<String, String> responseData = new HashMap<>();
        Optional<String> phoneNumber = hisPatientContentVMs.stream()
                .filter(o -> o.getMedicalResults().stream().anyMatch(ms -> appointmentCode.equalsIgnoreCase(ms.getDoctorAppointmentCode())))
                .map(HisPatientContentVM::getPatientRecordPhone)
                .findFirst();
        if (!phoneNumber.isPresent()) {
            responseError.setErrorCode("01");
            return ResponseEntity.badRequest().body(responseError);
        } else {
            responseData.put("his_sodienthoai", phoneNumber.get());
        }

        return ResponseEntity.ok().body(responseData);
    }
}
