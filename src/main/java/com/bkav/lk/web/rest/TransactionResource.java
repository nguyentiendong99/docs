package com.bkav.lk.web.rest;

import com.bkav.lk.dto.*;
import com.bkav.lk.service.*;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TransactionResource {

    private final UserService userService;

    private final TransactionService transactionService;

    private static final String ENTITY_NAME = "Transaction";

    private final DoctorAppointmentService doctorAppointmentService;

    private final DoctorAppointmentConfigurationService doctorAppointmentConfigurationService;

    private final PatientRecordService patientRecordService;

    @Value("${spring.application.name}")
    private String applicationName;
    public TransactionResource(UserService userService, TransactionService transactionService, DoctorAppointmentService doctorAppointmentService, DoctorAppointmentConfigurationService doctorAppointmentConfigurationService, PatientRecordService patientRecordService) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.doctorAppointmentService = doctorAppointmentService;
        this.doctorAppointmentConfigurationService = doctorAppointmentConfigurationService;
        this.patientRecordService = patientRecordService;
    }


    @PostMapping("/transactions")
    public ResponseEntity<TransactionDTO> create(@RequestBody @Valid TransactionDTO transactionDTO) throws URISyntaxException {

        DoctorAppointmentDTO doctorAppointment = doctorAppointmentService.findTopByBookingCode(transactionDTO.getBookingCode());
        if (Objects.isNull(doctorAppointment)) {
            throw new BadRequestAlertException("BookingCode not found", ENTITY_NAME, "not exist");
        }
        String paymentMethod = transactionDTO.getPaymentMethod();
        if (!(Constants.PAYMENT_METHOD.VISA.code.equals(paymentMethod) || Constants.PAYMENT_METHOD.PATIENT_CARD.code.equals(paymentMethod) || Constants.PAYMENT_METHOD.E_WALLET.code.equals(paymentMethod) || Constants.PAYMENT_METHOD.ATM.code.equals(paymentMethod) ||Constants.PAYMENT_METHOD.CASH.code.equals(paymentMethod))) {
            throw new BadRequestAlertException("PaymentMethod not found", ENTITY_NAME, "not exist");
        }
        if (transactionDTO.getAmount() == null || transactionDTO.getTotalAmount() == null ) {
            throw new BadRequestAlertException("Amount not null", ENTITY_NAME, "null");
        }

        DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOneByHealthFacilitiesId(transactionDTO.getHealthFacilityId());
        if (Constants.PAYMENT_METHOD.CASH.code.equals(transactionDTO.getPaymentMethod())) {
            doctorAppointment.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_WATING);
        } else {
            doctorAppointment.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_SUCCESS);
            transactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_SUCCESS);
        }
        if(Constants.DOCTOR_APPOINTMENT_CONFIG.UN_CONNECT_WITH_HIS_APPROVAL_AUTOMATIC.equals(config.getConnectWithHis())){
            doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
        }else {
            doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
        }
        doctorAppointmentService.save(doctorAppointment);
        if (Constants.PAYMENT_METHOD.CASH.code.equals(transactionDTO.getPaymentMethod())) {
            return ResponseEntity.ok().body(null);
        }
        transactionDTO.setHealthFacilityId(doctorAppointment.getHealthFacilityId());
        transactionDTO.setTypeCode(Constants.TRANSACTION_TYPE_CODE.DEPOSIT);
        // Tim userId tu DoctorAppointment
        Optional<PatientRecordDTO> patientRecordDTO = patientRecordService.findOne(doctorAppointment.getPatientRecordId());
        if (!patientRecordDTO.isPresent()) {
            throw new BadRequestAlertException("Patient Record is not found", ENTITY_NAME, "null");
        } else {
            transactionDTO.setUserId(patientRecordDTO.get().getUserId());
            transactionDTO.setTransactionCode(Utils.generateCodeFromId(patientRecordDTO.get().getUserId()));
        }
        TransactionDTO transaction = transactionService.save(transactionDTO);

        return ResponseEntity.created(new URI("/api/transactions/" + transactionDTO.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        transaction.getId().toString()))
                .body(transaction);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionDTO>> getAll() {
        List<TransactionDTO> transaction = transactionService.getAll();
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/transactions/banks")
    public ResponseEntity<List<ConstantsBankDTO>> getAllBank() {
        Constants.BANK[] banks = Constants.BANK.values();
        List<ConstantsBankDTO> constantsBankDTOList = new ArrayList<>();

        ConstantsBankDTO constantsBankDTO = null;
        for(Constants.BANK i: banks) {
            constantsBankDTO = new ConstantsBankDTO();
            constantsBankDTO.setCode(i.code);
            constantsBankDTO.setImgURL(i.imgURL);
            constantsBankDTO.setName(i.name);
            constantsBankDTOList.add(constantsBankDTO);
        }
      return ResponseEntity.ok(constantsBankDTOList);
    }

    @GetMapping("/public/transactions/payment-method")
    public ResponseEntity<List<ConstantsPayDTO>> getPaymentMethod(@RequestParam("healthFacilityId") Long healthFacilityId) {
        Constants.PAYMENT_METHOD[] payment = Constants.PAYMENT_METHOD.values();
        List<ConstantsPayDTO> list = new ArrayList<>();
        ConstantsPayDTO constantsPayDTO = null;

        DoctorAppointmentConfigurationDTO config = doctorAppointmentConfigurationService.findOneByHealthFacilitiesId(healthFacilityId);
        boolean paymentMethodATM;
        if (config == null) {
            paymentMethodATM = false;
        } else {
            paymentMethodATM = config.getPrepaymentMedicalService() != null && (config.getPrepaymentMedicalService().equals(Constants.ENTITY_STATUS.ACTIVE));
        }

        for(Constants.PAYMENT_METHOD i: payment) {
            constantsPayDTO = new ConstantsPayDTO();
            constantsPayDTO.setCode(i.code);
            constantsPayDTO.setValue(i.value);
            list.add(constantsPayDTO);
        }

        if (!paymentMethodATM) {
            list.removeIf(e -> e.code.equals(Constants.PAYMENT_METHOD.PATIENT_CARD.code));
            list.removeIf(e -> e.code.equals(Constants.PAYMENT_METHOD.VISA.code));
            list.removeIf(e -> e.code.equals(Constants.PAYMENT_METHOD.ATM.code));
            list.removeIf(e -> e.code.equals(Constants.PAYMENT_METHOD.E_WALLET.code));
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("/public/transactions/banks")
    public ResponseEntity<List<ConstantsBankDTO>> getAllBanksPublic() {
        Constants.BANK[] banks = Constants.BANK.values();
        List<ConstantsBankDTO> constantsBankDTOList = new ArrayList<>();

        ConstantsBankDTO constantsBankDTO = null;
        for(Constants.BANK i: banks) {
            constantsBankDTO = new ConstantsBankDTO();
            constantsBankDTO.setCode(i.code);
            constantsBankDTO.setImgURL(i.imgURL);
            constantsBankDTO.setName(i.name);
            constantsBankDTOList.add(constantsBankDTO);
        }
        return ResponseEntity.ok(constantsBankDTOList);
    }
}
