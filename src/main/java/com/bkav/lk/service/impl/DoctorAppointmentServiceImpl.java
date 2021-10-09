package com.bkav.lk.service.impl;

import com.bkav.lk.domain.*;
import com.bkav.lk.dto.*;
import com.bkav.lk.repository.*;
import com.bkav.lk.security.SecurityUtils;
import com.bkav.lk.service.DoctorAppointmentService;
import com.bkav.lk.service.HealthFacilitiesService;
import com.bkav.lk.service.NotificationService;
import com.bkav.lk.service.TransactionService;
import com.bkav.lk.service.mapper.DoctorAppointmentMapper;
import com.bkav.lk.service.notification.firebase.FirebaseData;
import com.bkav.lk.service.util.Utils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DoctorAppointmentServiceImpl implements DoctorAppointmentService {

    private static final Logger log = LoggerFactory.getLogger(DoctorAppointmentService.class);

    private final DoctorAppointmentMapper doctorAppointmentMapper;

    private final DoctorAppointmentRepository doctorAppointmentRepository;

    private final DoctorRepository doctorRepository;

    private final MedicalServiceRepository medicalServiceRepository;

    private final ClinicRepository clinicRepository;

    private final ActivityLogRepository activityLogRepository;

    private final ObjectMapper objectMapper;

    private final MedicalSpecialityRepository medicalSpecialityRepository;

    private final NotificationService notificationService;

    private final HealthFacilitiesService healthFacilitiesService;

    private final TransactionService transactionService;

    public DoctorAppointmentServiceImpl(DoctorAppointmentMapper doctorAppointmentMapper,
                                        DoctorAppointmentRepository doctorAppointmentRepository1,
                                        DoctorRepository doctorRepository,
                                        MedicalServiceRepository medicalServiceRepository,
                                        ClinicRepository clinicRepository,
                                        ActivityLogRepository activityLogRepository,
                                        MedicalSpecialityRepository medicalSpecialityRepository, NotificationService notificationService, HealthFacilitiesService healthFacilitiesService, TransactionService transactionService) {
        this.doctorAppointmentMapper = doctorAppointmentMapper;
        this.doctorAppointmentRepository = doctorAppointmentRepository1;
        this.doctorRepository = doctorRepository;
        this.medicalServiceRepository = medicalServiceRepository;
        this.clinicRepository = clinicRepository;
        this.activityLogRepository = activityLogRepository;
        this.medicalSpecialityRepository = medicalSpecialityRepository;
        this.notificationService = notificationService;
        this.healthFacilitiesService = healthFacilitiesService;
        this.transactionService = transactionService;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public Page<DoctorAppointmentDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable, User user) {
        List<DoctorAppointmentDTO> doctorAppointmentDTOList = doctorAppointmentMapper.toDto(doctorAppointmentRepository.search(queryParams, pageable));
        if (user != null && user.getDoctorId() != null) {
            doctorAppointmentDTOList = doctorAppointmentDTOList.stream().filter(item -> item.getDoctorId() == user.getDoctorId()).collect(Collectors.toList());
            queryParams.set("doctorId", user.getDoctorId().toString());
        }
        doctorAppointmentDTOList.forEach(item -> {
            DateTimeFormatter formatterByDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (item.getReExaminationDate() != null) {
                item.setReExaminationDateFormat(formatterByDate.format(item.getReExaminationDate().atZone(ZoneId.systemDefault()).toLocalDateTime()));
            }
        });
        return new PageImpl<>(doctorAppointmentDTOList, pageable, doctorAppointmentRepository.count(queryParams));
    }

    @Override
    public Optional<DoctorAppointmentDTO> findOne(Long id) {
        return doctorAppointmentRepository.findById(id).map(doctorAppointmentMapper::toDto);
    }

    @Override
    public List<DoctorAppointmentDTO> findAllByIds(List<Long> ids) {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findByIdIn(ids));
    }

    public DoctorAppointmentDTO findByAppointmentCode(String appointmentCode) {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findByAppointmentCode(appointmentCode));
    }

    @Override
    public void approve(String ids) {
        List<Long> listIds = Arrays.stream(ids.split(",")).map(Long::valueOf).collect(Collectors.toList());
        List<DoctorAppointment> doctorAppointments = doctorAppointmentRepository.findByIdIn(listIds);
        for (DoctorAppointment doctorAppointment : doctorAppointments) {
            if (Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE.equals(doctorAppointment.getStatus())) {
                doctorAppointment.setApprovedBy(SecurityUtils.getCurrentUserLogin().get());
                doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
            }
        }
        doctorAppointmentRepository.saveAll(doctorAppointments);
    }

    @Override
    public void deny(String ids, String rejectReason) {
        doctorAppointmentRepository.deny(ids, rejectReason);
    }

    @Override
    public DoctorAppointmentDTO confirm(Long id) {
        Optional<DoctorAppointment> optional = doctorAppointmentRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        DoctorAppointment doctorAppointment = optional.get();
        doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED);
        doctorAppointment.setIsConfirmed(Constants.BOOL_NUMBER.TRUE);
        doctorAppointmentRepository.save(doctorAppointment);
        return doctorAppointmentMapper.toDto(doctorAppointment);
    }

    @Override
    public DoctorAppointmentDTO cancel(Long id) {
        Optional<DoctorAppointment> optional = doctorAppointmentRepository.findById(id);
        if (!optional.isPresent()) {
            return null;
        }
        DoctorAppointment doctorAppointment = optional.get();
        doctorAppointment.setIsConfirmed(Constants.BOOL_NUMBER.TRUE);
        doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.CANCEL);
        doctorAppointmentRepository.save(doctorAppointment);
        return doctorAppointmentMapper.toDto(doctorAppointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object update(List<DoctorAppointmentDTO> listDoctorAppointmentDTO) {
        List<String> bookingCodes = new ArrayList<>();
        Optional<MedicalSpeciality> medicalSpecialityOptional = null;
        if (listDoctorAppointmentDTO.size() == 0) {
            return false;
        }
        for (DoctorAppointmentDTO item : listDoctorAppointmentDTO) {
            Optional<DoctorAppointment> optional = doctorAppointmentRepository.findById(item.getId());
            if (optional.isPresent()) {
                DoctorAppointment doctorAppointment = optional.get();
                if (Objects.nonNull(item.getType())) { // dùng cho người dùng tự cập nhật
                    if (!item.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE) && !item.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DOCTOR)) {
                        return false;
                    } else {
                        if (item.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE)) { // THEO NGAY
                            // Check chon Bac si
                            if (Objects.nonNull(item.getDoctorId())) {
                                Optional<Doctor> doctor = doctorRepository.findById(item.getDoctorId());
                                if (doctor.isPresent()) {
                                    doctorAppointment.setDoctor(doctor.get());
                                    // Chuyen khoa
                                    medicalSpecialityOptional = medicalSpecialityRepository.findById(doctor.get().getMedicalSpeciality().getId());
                                    if (item.getMedicalSpecialityId() == null) {
                                        doctorAppointment.setMedicalSpeciality(medicalSpecialityOptional.orElse(null));
                                    }
                                } else {
                                    throw new BadRequestAlertException("Doctor is not exist or inactive", "DoctorAppointment", "doctor_not_exist");
                                }
                            } else {
                                doctorAppointment.setDoctor(null);
                                doctorAppointment.setMedicalSpeciality(null);
                                doctorAppointment.setClinic(null);
                            }
                        }
                        if (item.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DOCTOR)) { // THEO BAC SI
                            if (Objects.nonNull(item.getDoctorId())) {
                                Optional<Doctor> doctor = doctorRepository.findById(item.getDoctorId());
                                if (doctor.isPresent()) {
                                    doctorAppointment.setDoctor(doctor.get());
                                    // Chuyen khoa
                                    medicalSpecialityOptional = medicalSpecialityRepository.findById(doctor.get().getMedicalSpeciality().getId());
                                    if (item.getMedicalSpecialityId() == null) {
                                        doctorAppointment.setMedicalSpeciality(medicalSpecialityOptional.orElse(null));
                                    }
                                } else {
                                    throw new BadRequestAlertException("Doctor is not exist or inactive", "DoctorAppointment", "doctor_not_exist");
                                }
                            } else {
                                throw new BadRequestAlertException("Doctor is obligatory", "DoctorAppointment", "doctor_is_obligatory");
                            }
                        }
                    }
                    // Chon Chuyen khoa
                    if (Objects.nonNull(item.getMedicalSpecialityId()) && doctorAppointment.getMedicalSpeciality() == null) {
                        Optional<MedicalSpeciality> medicalSpeciality = medicalSpecialityRepository.findById(item.getMedicalSpecialityId());
                        if (medicalSpeciality.isPresent()) {
                            doctorAppointment.setMedicalSpeciality(medicalSpeciality.get());
                        } else {
                            throw new BadRequestAlertException("Medical Speciality is not exist or inactive", "DoctorAppointment", "medicalSpeciality_not_exist");
                        }
                    }
                    // Chon Phong Kham
                    if (Objects.nonNull(doctorAppointment.getDoctor())) {
                        doctorAppointment.setClinic(clinicRepository.findByDoctorId(doctorAppointment.getDoctor().getId()).orElse(null));
                    }
                    if (Objects.isNull(doctorAppointment.getClinic()) && Objects.nonNull(item.getClinicId())) {
                        doctorAppointment.setClinic(clinicRepository.findById(item.getClinicId()).orElse(null));
                    }
                    doctorAppointment.setType(item.getType());
                    doctorAppointment.setHaveHealthInsurance(item.getHaveHealthInsurance());
                    doctorAppointment.setIsReExamination(item.getIsReExamination());
                    doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE);
                    doctorAppointment.setIsConfirmed(Constants.BOOL_NUMBER.TRUE);
                    // Kiem tra xem lich da thanh toan chua
                    if (doctorAppointment.getPaymentStatus().equals(Constants.PAYMENT_STATUS.PAID_SUCCESS)) { // Da thanh toan online
                        // Kiem tra neu Dich vu khac -> Hoan lai tien
                        if (!item.getMedicalServiceId().equals(doctorAppointment.getMedicalService().getId())) {
                            TransactionDTO transactionDTO = transactionService
                                    .findTopByBookingCodeAndTypeCodeAndPaymentStatus(optional.get().getBookingCode(), Constants.TRANSACTION_TYPE_CODE.DEPOSIT, Constants.PAYMENT_STATUS.PAID_SUCCESS);
                            if (transactionDTO != null) {
                                transactionDTO.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING);
                                transactionDTO.setContent("Hoàn tiền: " + transactionDTO.getTotalAmount() + " VNĐ");
                                transactionDTO.setTypeCode(Constants.TRANSACTION_TYPE_CODE.WITHDRAW);
                                transactionService.save(transactionDTO);
                            }
                            DoctorAppointmentDTO doctorAppointmentDTO = doctorAppointmentMapper.toDto(doctorAppointment);
                            // Huy lich cu
                            doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.DELETE_DOCTOR_APPOINTMENT_TEMP_INVALID);
                            doctorAppointment.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING);
                            // Tao lich moi
                            doctorAppointmentDTO.setPaymentStatus(Constants.PAYMENT_STATUS.PAID_WATING);
                            doctorAppointmentDTO.setMedicalReason(item.getMedicalReason());
                            doctorAppointmentDTO.setMedicalServiceId(item.getMedicalServiceId());
                            if (Objects.nonNull(item.getOldAppointmentCode())) {
                                doctorAppointmentDTO.setOldAppointmentCode(item.getOldAppointmentCode());
                            }
                            doctorAppointmentDTO.setMedicalServiceId(item.getMedicalServiceId());
                            doctorAppointmentDTO.setId(null);
                            doctorAppointmentDTO.setBookingCode(null);
                            doctorAppointmentDTO.setOldBookingCode(null);
                            doctorAppointmentDTO = this.save(doctorAppointmentDTO);
                            bookingCodes.add(doctorAppointmentDTO.getBookingCode());
                        } else {
                            doctorAppointment.setMedicalReason(item.getMedicalReason());
                            setterAppointmentFieldNonNull(doctorAppointment, item);
                        }
                    } else {
                        doctorAppointment.setMedicalReason(item.getMedicalReason());
                        setterAppointmentFieldNonNull(doctorAppointment, item);
                    }

                } else { // admin chuyển lịch
                    if (Objects.nonNull(item.getDoctorId())) {
                        Optional<Doctor> doctor = doctorRepository.findById(item.getDoctorId());
                        doctorAppointment.setDoctor(doctor.orElse(null));
                    } else {
                        doctorAppointment.setDoctor(null);
                    }
                    doctorAppointment.setStatus(Constants.DOCTOR_APPOINTMENT_STATUS.WAIT_CONFIRM);
                    doctorAppointment.setIsConfirmed(Constants.BOOL_NUMBER.FALSE);
                }
                doctorAppointment.setStartTime(item.getStartTime());
                doctorAppointment.setEndTime(item.getEndTime());
                doctorAppointment.setChangeAppointmentReason(item.getChangeAppointmentReason());
                doctorAppointmentRepository.save(doctorAppointment);
            }
        }
        return bookingCodes.size() > 0 ? bookingCodes : true;
    }

    private void setterAppointmentFieldNonNull(DoctorAppointment doctorAppointment, DoctorAppointmentDTO item) {
        if (Objects.nonNull(item.getOldAppointmentCode())) {
            doctorAppointment.setOldAppointmentCode(item.getOldAppointmentCode());
        }
        if (Objects.nonNull(item.getMedicalServiceId())) {
            Optional<MedicalService> medicalService = medicalServiceRepository.findById(item.getMedicalServiceId());
            doctorAppointment.setMedicalService(medicalService.orElse(null));
        } else {
            doctorAppointment.setMedicalService(null);
        }
    }

    @Override
    public Integer countByStatus(Integer status, Long healthFacilityId) {
        return doctorAppointmentRepository.countByStatusAndHealthFacilityId(status, healthFacilityId);
    }

    @Override
    public Integer countByStatus(Long doctorId, Integer status, Long healthFacilityId) {
        return doctorAppointmentRepository.countByDoctorIdAndStatusAndHealthFacilityId(doctorId, status, healthFacilityId);
    }

    @Override
    public DoctorAppointmentDTO save(DoctorAppointmentDTO doctorAppointmentDTO) {
        Optional<MedicalSpeciality> medicalSpecialityOptional = null;
        DoctorAppointment doctorAppointment = doctorAppointmentMapper.toEntity(doctorAppointmentDTO);
        // Todo check chọn đặt lịch theo bác sĩ
        if (doctorAppointmentDTO.getType() == null) {
            doctorAppointment.setType(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE);
        }

        // Todo check chọn bác sĩ
        if (doctorAppointmentDTO.getDoctorId() != null) {
            Optional<Doctor> doctorOptional = doctorRepository.findById(doctorAppointmentDTO.getDoctorId());
            if (doctorOptional.isPresent()) {
                doctorAppointment.setDoctor(doctorOptional.get());
                medicalSpecialityOptional = medicalSpecialityRepository.findById(doctorOptional.get().getMedicalSpeciality().getId());
                if (doctorAppointmentDTO.getMedicalSpecialityId() == null) {
                    doctorAppointment.setMedicalSpeciality(medicalSpecialityOptional.orElse(null));
                }
            } else {
                throw new BadRequestAlertException("Doctor is not exist", "DoctorAppointment", "doctor_not_exist");
            }
        }
        // Cây này đóng do bác sĩ không còn bắt buộc theo phòng khám
        // Todo check doctorAppointmentDTO co ca doctorId va medicalspecialityId
//        if (doctorAppointmentDTO.getDoctorId() != null && doctorAppointmentDTO.getMedicalSpecialityId() != null) {
//            if (medicalSpecialityOptional.isPresent()) {
//                if (!medicalSpecialityOptional.get().getId().equals(doctorAppointmentDTO.getMedicalSpecialityId())) {
//                    throw new BadRequestAlertException("Medical Speciality have not Doctor", "DoctorAppointment", "medicalSpeciality_have_not_doctor");
//                }
//            }
//        }
        // Todo check chọn chuyên khoa
        if (doctorAppointmentDTO.getMedicalSpecialityId() != null) {
            medicalSpecialityOptional = medicalSpecialityRepository.findById(doctorAppointmentDTO.getMedicalSpecialityId());
            if (medicalSpecialityOptional.isPresent()) {
                doctorAppointment.setMedicalSpeciality(medicalSpecialityOptional.orElse(null));
            } else {
                throw new BadRequestAlertException("Medical Speciality is not exist", "DoctorAppointment", "medicalSpeciality_not_exist");
            }
        }
        // Todo check chọn phòng khám
        if (Objects.nonNull(doctorAppointment.getDoctor())) {
            doctorAppointment.setClinic(clinicRepository.findByDoctorId(doctorAppointment.getDoctor().getId()).orElse(null));
        }
        if (Objects.isNull(doctorAppointment.getClinic()) && Objects.nonNull(doctorAppointmentDTO.getClinicId())) {
            doctorAppointment.setClinic(clinicRepository.findById(doctorAppointmentDTO.getClinicId()).orElse(null));
        }

        // Todo check chọn dịch vụ khám
        if (doctorAppointmentDTO.getMedicalServiceId() != null) {
            Optional<MedicalService> medicalServiceDTOOptional = medicalServiceRepository.findById(doctorAppointmentDTO.getMedicalServiceId());
            doctorAppointment.setMedicalService(medicalServiceDTOOptional.orElse(null));
        }

        doctorAppointment = doctorAppointmentRepository.save(doctorAppointment);
        doctorAppointment.setBookingCode(Utils.createOrderCode(doctorAppointment.getId()));
        return doctorAppointmentMapper.toDto(doctorAppointment);
    }

    @Override
    public DoctorAppointmentDTO saveNormal(DoctorAppointmentDTO doctorAppointmentDTO) {
        DoctorAppointment doctorAppointment = doctorAppointmentMapper.toEntity(doctorAppointmentDTO);
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.save(doctorAppointment));
    }

    @Override
    public List<NotifyDoctorAppointmentDTO> getDoctorAppointments(List<Long> ids) {
        return doctorAppointmentRepository.getDoctorAppointments(ids);
    }

    @Override
    public void schedulingDoctorAppointmentJob() {
        List<DoctorAppointmentDTO> list;
        Integer[] listStatus = {Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE, Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED, Constants.DOCTOR_APPOINTMENT_STATUS.DENY};
        String nowStr = DateUtils.now();
        Instant now = DateUtils.parseToInstant(nowStr, DateUtils.NORM_2_DATETIME_PATTERN);
        list = doctorAppointmentMapper.toDto(doctorAppointmentRepository.findDoctorAppointmentStatusNotDone(now, listStatus));
        if (list.size() > 0) {
            List<Long> ids = list.stream().map(DoctorAppointmentDTO::getId).collect(Collectors.toList());
            doctorAppointmentRepository.cancel(ids);
        }
    }

    @Override
    public List<String> getAppointmentCodesByPatientId(Long patientId, Long healthFacilityId, Integer status) {
        return doctorAppointmentRepository.getAppointmentCodesByPatientId(patientId, healthFacilityId, status);
    }

    @Override
    public List<DoctorAppointmentHistoryDTO> getHistory(Long id) {
        List<DoctorAppointmentHistoryDTO> listDoctorAppointmentHistory = new ArrayList<>();
        List<ActivityLog> listActivityLog = activityLogRepository.findByContentIdAndContentTypeOrderByCreatedDateDesc(id, Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT);
        for (ActivityLog activityLog : listActivityLog) {
            List<String> contentList = new ArrayList<>();
            DoctorAppointmentHistoryDTO history = new DoctorAppointmentHistoryDTO();
            history.setCreatedDate(activityLog.getCreatedDate());
            history.setCreatedBy(activityLog.getCreatedBy());
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CREATE)) {
                contentList.add("Thêm mới");
                history.setContent(contentList);
                listDoctorAppointmentHistory.add(history);
                continue;
            }
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.APPROVE)) {
                contentList.add("Duyệt lịch khám");
                history.setContent(contentList);
                listDoctorAppointmentHistory.add(history);
                continue;
            }
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.DENY)) {
                contentList.add("Từ chối lịch khám");
                history.setContent(contentList);
                listDoctorAppointmentHistory.add(history);
                continue;
            }
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.UPDATE)) {
                contentList.add("Thay đổi lịch khám");
                DoctorAppointment oldAppointment = convertToDoctorAppointment(activityLog.getOldContent());
                DoctorAppointment newAppointment = convertToDoctorAppointment(activityLog.getContent());
                createContent(oldAppointment, newAppointment, contentList);
                history.setContent(contentList);
                listDoctorAppointmentHistory.add(history);
            }
        }
        return listDoctorAppointmentHistory;
    }

    @Override
    public boolean existByDoctorId(Long doctorId) {
        Integer[] status = {Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST, Constants.DOCTOR_APPOINTMENT_STATUS.DELETE_DOCTOR_APPOINTMENT_TEMP_INVALID};
        return doctorAppointmentRepository.existsByDoctorIdAndStatusNotIn(doctorId, status);
    }

    @Override
    public boolean existByPatientRecordId(Long patientRecordId) {
        Integer[] status = {Constants.DOCTOR_APPOINTMENT_STATUS.REQUEST, Constants.DOCTOR_APPOINTMENT_STATUS.DELETE_DOCTOR_APPOINTMENT_TEMP_INVALID};
        return doctorAppointmentRepository.existsByPatientRecordIdAndStatusNotIn(patientRecordId, status);
    }

    private DoctorAppointment convertToDoctorAppointment(String input) {
        if (input.isEmpty()) {
            return null;
        }
        DoctorAppointment result = null;
        try {
            result = objectMapper.readValue(input, DoctorAppointment.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private void createContent(DoctorAppointment oldAppointment, DoctorAppointment newAppointment, List<String> contentList) {
        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm");
        if (oldAppointment == null) {
            return;
        }
        if (oldAppointment.getDoctor() == null && newAppointment.getDoctor() != null) {
            Doctor newDoctor = doctorRepository.getOne(newAppointment.getDoctor().getId());
            contentList.add("Bác sĩ: " + newDoctor.getName());
        } else if (oldAppointment.getDoctor() != null && newAppointment.getDoctor() == null) {
            Doctor oldDoctor = doctorRepository.getOne(oldAppointment.getDoctor().getId());
            contentList.add("Bác sĩ: " + oldDoctor.getName());
        } else if (oldAppointment.getDoctor() == null && newAppointment.getDoctor() == null) {
            contentList.add("Bác sĩ mặc định");
        } else if (!oldAppointment.getDoctor().getId().equals(newAppointment.getDoctor().getId())) {
            Doctor oldDoctor = doctorRepository.getOne(oldAppointment.getDoctor().getId());
            Doctor newDoctor = doctorRepository.getOne(newAppointment.getDoctor().getId());
            contentList.add("Bác sĩ: " + oldDoctor.getName() + " => " + newDoctor.getName());
        }
        if (!(oldAppointment.getStartTime().equals(newAppointment.getStartTime()) && oldAppointment.getEndTime().equals(newAppointment.getEndTime()))) {
            Date oldStartDate = Date.from(oldAppointment.getStartTime());
            Date oldEndDate = Date.from(oldAppointment.getEndTime());
            Date newStartDate = Date.from(newAppointment.getStartTime());
            Date newEndDate = Date.from(newAppointment.getStartTime());
            String oldDate = formatDate.format(oldStartDate);
            String startOldTime = formatTime.format(oldStartDate);
            String endOldTime = formatTime.format(oldEndDate);
            String newDate = formatDate.format(newStartDate);
            String startNewTime = formatTime.format(newStartDate);
            String endNewTime = formatTime.format(newEndDate);
            contentList.add("Thời gian khám: " + oldDate + " " + startOldTime + "-" + endOldTime + " => " +
                    newDate + " " + startNewTime + "-" + endNewTime);
        }
        if (newAppointment.getChangeAppointmentReason() != null) {
            contentList.add("Lý do đổi lịch: " + newAppointment.getChangeAppointmentReason());
        }
    }

    @Override
    public List<DoctorAppointmentDTO> findAll() {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findAll());
    }

    @Override
    public List<DoctorAppointmentDTO> findByIds(List<Long> ids) {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findByIdIn(ids));
    }

    @Override
    public ByteArrayInputStream exportToExcel(List<DoctorAppointmentDTO> list, InputStream file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 12;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for (DoctorAppointmentDTO dsDTO : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(dsDTO.getBookingCode());
                row.createCell(1).setCellValue(dsDTO.getPatientName());
                row.createCell(2).setCellValue(DateUtils.convertFromInstantToString(dsDTO.getStartTime()));
                row.createCell(3).setCellValue(DateUtils.convertFromInstantToHour(dsDTO.getStartTime()) + "-" + DateUtils.convertFromInstantToHour(dsDTO.getEndTime()));
                row.createCell(4).setCellValue((dsDTO.getDoctorName()));
                row.createCell(5).setCellValue((dsDTO.getMedicalReason()));
                row.createCell(6).setCellValue((dsDTO.getStatus()));
                rowCount++;
            }
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public void updatePayStatus(Long id) {
        doctorAppointmentRepository.findById(id).ifPresent(item -> item.setPaymentStatus(Constants.PAYMENT_STATUS.REFUNDED_WATTING));
    }

    public List<DoctorAppointmentDTO> findByHealthFacilityAndPatient(MultiValueMap<String, String> queryParams) {
        List<DoctorAppointment> doctorAppointments = doctorAppointmentRepository.findByHealthFacilityAndPatient(queryParams);
        return doctorAppointmentMapper.toDto(doctorAppointments);
    }

    @Override
    public Optional<DoctorAppointmentDTO> findByBookingCode(String bookingCode) {
        return doctorAppointmentRepository.findByBookingCode(bookingCode).map(doctorAppointmentMapper::toDto);
    }

    @Override
    public Optional<DoctorAppointmentDTO> findByBookingCodeAndStatus(String bookingCode, Integer status) {
        return doctorAppointmentRepository.findByBookingCodeAndStatus(bookingCode, status).map(doctorAppointmentMapper::toDto);
    }

    @Override
    public DoctorAppointmentDTO findTopByBookingCode(String bookingCode) {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findTopByBookingCode(bookingCode));
    }

    @Override
    public List<DoctorAppointmentDTO> schedulingReminderAppointmentJob() {
        ZonedDateTime startTime = ZonedDateTime.of(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), LocalTime.MIN, ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1);
        ZonedDateTime endTime = ZonedDateTime.of(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), LocalTime.MAX, ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1);
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findAllReminderAppointment(startTime.toInstant(), endTime.toInstant()));
    }

    @Override
    public List<DoctorAppointmentDTO> findByClinicAndStatusNot(Long clinicId, Integer status) {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findByClinicAndStatusNot(clinicId, status));
    }

    @Override
    public Integer countHealthFacilityAndDoctorId(Long healthFacilityId, Long doctorId, Instant startTime, Instant endTime) {
        return doctorAppointmentRepository.countDoctorAppointmentInRangeTime(healthFacilityId, doctorId, startTime, endTime);
    }

    @Override
    public List<DoctorAppointmentDTO> findByHealthFacility(Long healthFacilityId, Integer[] status) {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findByHealthFacilityIdAndStatusIn(healthFacilityId, status));
    }

    @Override
    public void schedulingDoctorAppointmentReminderNotificationJob(List<DoctorAppointmentConfigurationDTO> list) {
        // Danh sach tat ca lich kham o trang thai: Cho kham
        List<DoctorAppointmentDTO> doctorAppointmentDTOS = doctorAppointmentMapper.toDto(doctorAppointmentRepository
                .findByStatus(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED));

        list = list.stream().filter(item -> item.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE)).collect(Collectors.toList());

        List<Long> idPushNotify = new ArrayList<>();

        if (doctorAppointmentDTOS.isEmpty()) return;

        Map<Long, Integer> mapConfig = list.stream()
                .collect(Collectors.toMap(DoctorAppointmentConfigurationDTO::getHealthFacilitiesId, DoctorAppointmentConfigurationDTO::getPeriodConfig));

        LocalDate localDateNow = LocalDate.now(DateUtils.getZoneHCM());
        doctorAppointmentDTOS.forEach(doctorAppointmentDTO -> {
            if (mapConfig.get(doctorAppointmentDTO.getHealthFacilityId()) != null
                    && ChronoUnit.DAYS.between(localDateNow, doctorAppointmentDTO.getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate()) <= mapConfig.get(doctorAppointmentDTO.getHealthFacilityId()) / 1440
                    && ChronoUnit.DAYS.between(localDateNow, doctorAppointmentDTO.getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate()) > 0) {
                idPushNotify.add(doctorAppointmentDTO.getId());
            } else if (mapConfig.get(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT) != null // Lay cau hinh mac dinh
                    && ChronoUnit.DAYS.between(localDateNow, doctorAppointmentDTO.getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate()) <= mapConfig.get(Constants.DOCTOR_APPOINTMENT_CONFIG.HEALTH_FACILITIES_DEFAULT) / 1440
                    && ChronoUnit.DAYS.between(localDateNow, doctorAppointmentDTO.getStartTime().atZone(DateUtils.getZoneHCM()).toLocalDate()) > 0) {
                idPushNotify.add(doctorAppointmentDTO.getId());
            }
        });

        if (!idPushNotify.isEmpty()) {
            List<DoctorAppointmentDTO> doctorAppointmentDTOList = doctorAppointmentMapper.toDto(doctorAppointmentRepository.findByIdIn(idPushNotify));
            if (!doctorAppointmentDTOList.isEmpty()) {
                doctorAppointmentDTOList.forEach(doctorAppointmentDTO -> {
                    HealthFacilitiesDTO healthFacilitiesDTO = healthFacilitiesService.findById(doctorAppointmentDTO.getHealthFacilityId());
                    // Todo - Gửi thông báo
                    FirebaseData firebaseData = new FirebaseData();
                    firebaseData.setObjectId(doctorAppointmentDTO.getId().toString());
                    try {
                        firebaseData.setObject(objectMapper.writeValueAsString(doctorAppointmentDTO).replaceAll("\\n|\\r", ""));
                    } catch (JsonProcessingException e) {
                        log.error("Error: ", e);
                    }
                    String template;
                    List<String> paramsBody = new ArrayList<>();
                    if (doctorAppointmentDTO.getType().equals(Constants.DOCTOR_APPOINTMENT_TYPE.BY_DATE)) {
                        firebaseData.setType(String.valueOf(Constants.NotificationConstants.APPOINTMENT_REMINDER.id)); // thong bao nhac lich kham/tai kham
                        template = Constants.NotificationConstants.APPOINTMENT_REMINDER.template;
                        paramsBody.add(DateUtils.convertFromInstantToHour2(doctorAppointmentDTO.getStartTime()));
                        paramsBody.add(DateUtils.convertFromInstantToString(doctorAppointmentDTO.getStartTime()));
                        paramsBody.add(healthFacilitiesDTO.getName());
                    } else {
                        firebaseData.setType(String.valueOf(Constants.NotificationConstants.APPOINTMENT_REMINDER_DOCTOR.id));
                        template = Constants.NotificationConstants.APPOINTMENT_REMINDER_DOCTOR.template;
                        paramsBody.add(doctorAppointmentDTO.getDoctorName());
                        paramsBody.add(DateUtils.convertFromInstantToHour2(doctorAppointmentDTO.getStartTime()));
                        paramsBody.add(DateUtils.convertFromInstantToString(doctorAppointmentDTO.getStartTime()));
                        paramsBody.add(healthFacilitiesDTO.getName());
                    }
                    notificationService.pushNotification(template, firebaseData, null, paramsBody, doctorAppointmentDTO.getUserId());
                });
            }
        }
    }

    @Override
    public ByteArrayInputStream exportToExcelReExam(List<DoctorAppointmentDTO> list, InputStream file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 4;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for (DoctorAppointmentDTO dsDTO : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(dsDTO.getAppointmentCode());
                row.createCell(1).setCellValue(dsDTO.getPatientCode());
                row.createCell(2).setCellValue(dsDTO.getPatientName());
                if (dsDTO.getReExaminationDate() != null) {
                    row.createCell(3).setCellValue(DateUtils.convertFromInstantToString(dsDTO.getReExaminationDate()));
                }
                row.createCell(4).setCellValue((dsDTO.getDoctorName()));
                rowCount++;
            }
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean existsByAppointmentCode(String appointmentCode) {
        return doctorAppointmentRepository.existsByAppointmentCode(appointmentCode);
    }

    @Override
    public List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidWithDoctor(Integer status, Long healthFacilityId, Long doctorId, Instant startTime, Instant endTime, Integer timeout) {
        Instant now = Instant.now();
        return doctorAppointmentMapper
                .toDto(doctorAppointmentRepository.findTempDoctorAppointmentInvalidWithDoctor(status, healthFacilityId, doctorId, startTime, endTime, now.minus(timeout, ChronoUnit.MINUTES)));
    }

    @Override
    public List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidWithDoctor(Integer status, Long doctorId, Instant startTime, Instant endTime, Integer timeout) {
        Instant now = Instant.now();
        return doctorAppointmentMapper
                .toDto(doctorAppointmentRepository.findTempDoctorAppointmentInvalidWithDoctor(status, doctorId, startTime, endTime, now.minus(timeout, ChronoUnit.MINUTES)));
    }

    @Override
    public List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidNotWithDoctor(Integer status, Long healthFacilityId, Instant startTime, Instant endTime, Integer timeout) {
        Instant now = Instant.now();
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findTempDoctorAppointmentInvalidNotWithDoctor(status, healthFacilityId, startTime, endTime, now.minus(timeout, ChronoUnit.MINUTES)));
    }

    @Override
    public List<DoctorAppointmentDTO> findTempDoctorAppointmentInvalidBothDoctorAndNotDoctor(Integer status, Long healthFacilityId, Instant startTime, Instant endTime, Integer timeout) {
        Instant now = Instant.now();
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findTempDoctorAppointmentInvalidBothDoctorAndNotDoctor(status, healthFacilityId, startTime, endTime, now.minus(timeout, ChronoUnit.MINUTES)));
    }

    @Override
    public void deleteTempDoctorAppointment(List<Long> ids) {
        doctorAppointmentRepository.deleteTempDoctorAppointment(ids);
    }

    @Override
    public List<DoctorAppointmentDTO> findAllByHealthFacilityIdStatusInAndStartTimeIsGreaterThanEqual(Long healthFacilityId, Integer[] status, Instant now) {
        return doctorAppointmentMapper.toDto(doctorAppointmentRepository.findAllByHealthFacilityIdAndStatusInAndStartTimeIsGreaterThanEqual(healthFacilityId, status, now));
    }
}
