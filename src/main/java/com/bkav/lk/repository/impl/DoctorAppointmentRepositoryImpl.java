package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.dto.NotifyDoctorAppointmentDTO;
import com.bkav.lk.repository.custom.DoctorAppointmentRepositoryCustom;
import com.bkav.lk.security.SecurityUtils;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.*;
import java.util.*;

import static com.bkav.lk.util.StrUtil.convertList;

public class DoctorAppointmentRepositoryImpl implements DoctorAppointmentRepositoryCustom {

    private static final String ENTITY_NAME = "doctor_appointment";

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<DoctorAppointment> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        if (queryParams.containsKey("isUserApprove") && Boolean.parseBoolean(queryParams.get("isUserApprove").get(0))) {
            sql.append("SELECT D, (CASE WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE + " THEN 1 " +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.WAIT_CONFIRM + " THEN 2 " +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.DENY + " THEN 3 " +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED + " THEN 4 " +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.DONE + " THEN 5 "+
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.CANCEL +" THEN 6 ELSE 7 END) AS orderField FROM DoctorAppointment D");
        } else if (queryParams.containsKey("isUserApprove") && !Boolean.parseBoolean(queryParams.get("isUserApprove").get(0))) {
            sql.append("SELECT D, (CASE WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED + " THEN 1 " +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.DONE + " THEN 2 " +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE + " THEN 3" +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.WAIT_CONFIRM + " THEN 4" +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.DENY + " THEN 5 " +
                    "WHEN D.status = " + Constants.DOCTOR_APPOINTMENT_STATUS.CANCEL + " THEN 6 ELSE 7 END) AS orderField FROM DoctorAppointment D");
        } else {
            sql.append("SELECT D FROM DoctorAppointment D");
        }
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString());
        values.forEach(query::setParameter);
        if(queryParams.containsKey("pageIsNull")){
            pageable = null;
        }
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        if (queryParams.containsKey("isUserApprove")
                && (Boolean.parseBoolean(queryParams.get("isUserApprove").get(0)) || !Boolean.parseBoolean(queryParams.get("isUserApprove").get(0)))) {
            List<DoctorAppointment> list = new ArrayList<>();
            query.getResultList().forEach(item -> {
                Object[] results = (Object[]) item;
                list.add((DoctorAppointment) results[0]);
            });
            return list;
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(D) from DoctorAppointment D ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = "";

        sql += " where D.status != 0 and D.status != -1 ";
        if (queryParams.containsKey("userId") && !StrUtil.isBlank(queryParams.get("userId").get(0))) {
            sql += " and D.patientRecord.userId = :userId";
            values.put("userId", Long.valueOf(queryParams.get("userId").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (D.appointmentCode like :keyword or D.bookingCode like :keyword or D.patientRecord.name like :keyword " +
                    "or D.patientRecord.patientRecordCode " +
                    "like :keyword or D.patientRecord.healthInsuranceCode like :keyword or D.patientRecord.phone like :keyword or D.medicalReason like :keyword)";
            values.put("keyword", '%' + queryParams.get("keyword").get(0) + '%');
        }

        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql += " and  D.status IN (:status)";
            String[] stringList = queryParams.get("status").get(0).split(",");
            List<Integer> statusList = new ArrayList<>();
            for (String str : stringList) {
                statusList.add(Integer.valueOf(str.trim()));
            }
            values.put("status", statusList);
        }

        if (queryParams.containsKey("startDate") && !StrUtil.isBlank(queryParams.get("startDate").get(0))) {
            Instant startDate;
            startDate = DateUtils.parseStartOfDay(queryParams.get("startDate").get(0));
            //   startDate = DateUtils.parseToInstant(queryParams.get("startDate").get(0), DateUtils.NORM_2_DATETIME_PATTERN);
            if (startDate != null) {
                sql += " and  D.startTime >= :startDate";
                values.put("startDate", startDate);
            }
        }

        if (queryParams.containsKey("endDate") && !StrUtil.isBlank(queryParams.get("endDate").get(0))) {
            Instant endDate;
            endDate = DateUtils.parseEndOfDay(queryParams.get("endDate").get(0));
            if (endDate != null) {
                sql += " and  D.endTime <= :endDate";
                values.put("endDate", endDate);
            }
        }

        if (queryParams.containsKey("startTime") && !StrUtil.isBlank(queryParams.get("startTime").get(0))) {
            Instant startTime = Instant.parse(queryParams.get("startTime").get(0));
            int minute = startTime.atZone(ZoneOffset.UTC).getMinute();
            int hour = startTime.atZone(ZoneOffset.UTC).getHour();
            if (startTime != null) {
                sql += " and  HOUR(D.startTime) =:hourStart and MINUTE(D.startTime) =:minuteStart";
                values.put("hourStart", hour);
                values.put("minuteStart", minute);
            }
        }

        if (queryParams.containsKey("endTime") && !StrUtil.isBlank(queryParams.get("endTime").get(0))) {
            Instant endTime = Instant.parse(queryParams.get("endTime").get(0));
            int minute = endTime.atZone(ZoneOffset.UTC).getMinute();
            int hour = endTime.atZone(ZoneOffset.UTC).getHour();
            if (endTime != null) {
                sql += " and  HOUR(D.endTime) =:hourEnd and MINUTE(D.endTime) =:minuteEnd";
                values.put("hourEnd", hour);
                values.put("minuteEnd", minute);
            }
        }

        if (queryParams.containsKey("doctorId") && !StrUtil.isBlank(queryParams.get("doctorId").get(0))) {
            sql += " and  D.doctor.id = :doctorId";
            values.put("doctorId", Long.valueOf(queryParams.get("doctorId").get(0)));
        }

        if (queryParams.containsKey("medicalSpecialityId") && !StrUtil.isBlank(queryParams.get("medicalSpecialityId").get(0))) {
            sql += " and  D.medicalSpeciality.id = :medicalSpecialityId";
            values.put("medicalSpecialityId", Long.valueOf(queryParams.get("medicalSpecialityId").get(0)));
        }

        if (queryParams.containsKey("patientRecordId") && !StrUtil.isBlank(queryParams.get("patientRecordId").get(0))) {
            sql += " and  D.patientRecord.id = :patientRecordId";
            values.put("patientRecordId", Long.valueOf(queryParams.get("patientRecordId").get(0)));
        }

        if (queryParams.containsKey("clinicId") && !StrUtil.isBlank(queryParams.get("clinicId").get(0))) {
            sql += " and  D.clinic.id = :clinicId";
            values.put("clinicId", Long.valueOf(queryParams.get("clinicId").get(0)));
        }

        if (queryParams.containsKey("approvedBy") && !StrUtil.isBlank(queryParams.get("approvedBy").get(0))) {
            sql += " and D.approvedBy = :approvedBy";
            values.put("approvedBy", queryParams.get("approvedBy").get(0));
        }

        if (queryParams.containsKey("districtCode") && !StrUtil.isBlank(queryParams.get("districtCode").get(0))) {
            sql += " and D.patientRecord.district.areaCode = :districtCode";
            values.put("districtCode", queryParams.get("districtCode").get(0));
        }

        if (queryParams.containsKey("wardCode") && !StrUtil.isBlank(queryParams.get("wardCode").get(0))) {
            sql += " and D.patientRecord.ward.areaCode = :wardCode";
            values.put("wardCode", queryParams.get("wardCode").get(0));
        }

        if (queryParams.containsKey("haveHealthInsurance") && !StrUtil.isBlank(queryParams.get("haveHealthInsurance").get(0))) {
            sql += " and D.haveHealthInsurance = :haveHealthInsurance";
            values.put("haveHealthInsurance", Integer.valueOf(queryParams.get("haveHealthInsurance").get(0)));
        }

        if (queryParams.containsKey("gender") && !StrUtil.isBlank(queryParams.get("gender").get(0))) {
            sql += " and D.patientRecord.gender =:gender";
            values.put("gender", queryParams.get("gender").get(0));
        }
        if (queryParams.containsKey("reExaminationDate") && queryParams.get("reExaminationDate").get(0).equals("null")) {
            sql += " and D.reExaminationDate is null ";
        }

        if (queryParams.containsKey("healthFacilityId") && !StrUtil.isBlank(queryParams.get("healthFacilityId").get(0))) {
            sql += " and  healthFacilityId = :healthFacilityId";
            values.put("healthFacilityId", Long.valueOf(queryParams.get("healthFacilityId").get(0)));
        }

        if (queryParams.containsKey("isConfirmed") && !StrUtil.isBlank(queryParams.get("isConfirmed").get(0))) {
            sql += " and  isConfirmed = :isConfirmed";
            values.put("isConfirmed", Long.valueOf(queryParams.get("isConfirmed").get(0)));
        }

        if (queryParams.containsKey("listIdChecked") && queryParams.get("listIdChecked").get(0).length() > 0) {
            List<Long> list = convertList(queryParams.get("listIdChecked").get(0));
            if(list.size() > 0){
                values.put("ids", list);
                sql += " AND D.id IN (:ids)";
            }
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" ORDER BY ");
        if (queryParams.containsKey("sort")) {
            List<String> orders = queryParams.get("sort");
            for (String i : orders) {
                sql.append("D." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(", "));
        } else if (queryParams.containsKey("isUserApprove")) {
            sql.append(" orderField ASC , D.startTime ASC");
        } else if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            String[] stringList = queryParams.get("status").get(0).split(",");
            List<Integer> statusList = new ArrayList<>();
            for (String str : stringList) {
                statusList.add(Integer.valueOf(str.trim()));
            }
            if (statusList.contains(Constants.DOCTOR_APPOINTMENT_STATUS.WAITING_APPROVE) || statusList.contains(Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED)) {
                sql.append(" D.startTime ASC");
            } else {
                sql.append(" D.status ASC , D.createdDate DESC");
            }
        } else {
            sql.append(" D.status ASC , D.createdDate DESC");
        }
        return sql.toString();
    }

    @Override
    public void approve(String ids) {
        String sql = "UPDATE DoctorAppointment SET status =:status, approvedBy =:approvedBy  WHERE id IN (" + ids + ")";
        entityManager.createQuery(sql)
                .setParameter("status", Constants.DOCTOR_APPOINTMENT_STATUS.APPROVED)
                .setParameter("approvedBy", SecurityUtils.getCurrentUserLogin().get())
                .executeUpdate();
    }

    @Override
    public void deny(String ids, String rejectReason) {
        String sql = "UPDATE DoctorAppointment SET status =:status , rejectReason =:rejectReason, approvedBy =: approvedBy WHERE id IN (" + ids + ")";
        entityManager.createQuery(sql)
                .setParameter("status", Constants.DOCTOR_APPOINTMENT_STATUS.DENY)
                .setParameter("rejectReason", rejectReason)
                .setParameter("approvedBy", SecurityUtils.getCurrentUserLogin().get())
                .executeUpdate();
    }

    @Override
    public List<NotifyDoctorAppointmentDTO> getDoctorAppointments(List<Long> ids) {
        String sql = "SELECT da.bookingCode, pr.userId, d2.name, c2.name, hf.name, da.startTime, da.endTime, da.rejectReason, da.id, da.changeAppointmentReason " +
                " FROM DoctorAppointment da " +
                " JOIN PatientRecord pr ON da.patientRecord.id = pr.id " +
                " JOIN Doctor d2 ON da.doctor.id = d2.id " +
                " JOIN Clinic c2 ON da.clinic.id = c2.id " +
                " JOIN HealthFacilities hf ON da.healthFacilityId = hf.id" +
                " AND da.id  IN (:ids) AND da.status NOT IN (-1, 0)";
        Query query = entityManager.createQuery(sql);
        query.setParameter("ids", ids);
        List<Object[]> objects = query.getResultList();
        List<NotifyDoctorAppointmentDTO> listDTO = new ArrayList<>();
        if (!objects.isEmpty()) {
            for (Object[] obj : objects) {
                NotifyDoctorAppointmentDTO dto = new NotifyDoctorAppointmentDTO();
                dto.setBookingCode(obj[0] != null ? obj[0].toString() : null);
                dto.setUserId(Long.valueOf(obj[1].toString()));
                dto.setDoctorName(obj[2] != null ? obj[2].toString() : null);
                dto.setClinicName(obj[3] != null ? obj[3].toString() : null);
                dto.setHealthFacilitiesName(obj[4] != null ? obj[4].toString() : null);
                dto.setStartTime((Instant) obj[5]);
                dto.setEndTime((Instant) obj[6]);
                dto.setRejectReason(obj[7] != null ? obj[7].toString() : null);
                dto.setIdDoctorAppointment(Long.valueOf(obj[8].toString()));
                dto.setChangeAppointmentReason(obj[9] != null ? obj[9].toString() : null);
                listDTO.add(dto);
            }
        }
        return listDTO;
    }

    @Override
    public void cancel(List<Long> ids) {
        String sql = "UPDATE DoctorAppointment SET status =:status, lastModifiedDate =:lastModifiedDate WHERE id IN (:ids)";
        entityManager.createQuery(sql)
                .setParameter("status", Constants.DOCTOR_APPOINTMENT_STATUS.CANCEL)
                .setParameter("lastModifiedDate", Instant.now())
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public List<String> getAppointmentCodesByPatientId(Long patientId, Long healthFacilityId, Integer status) {
        String sql = "SELECT da.appointmentCode FROM DoctorAppointment da " +
                " WHERE da.patientRecord.id = :patientId AND da.status = :status" +
                " AND da.appointmentCode IS NOT NULL ";
        if (healthFacilityId != null) {
            sql += " AND da.healthFacilityId = :healthFacilityId ";
        }
        sql += " ORDER BY da.lastModifiedDate DESC";
        Query query = entityManager.createQuery(sql);
        query.setParameter("patientId", patientId);
        query.setParameter("status", status);
        if (healthFacilityId != null) {
            query.setParameter("healthFacilityId", healthFacilityId);
        }
        List<String> appointmentCodes = query.getResultList();
        return appointmentCodes;
    }

    @Override
    public List<DoctorAppointment> findByHealthFacilityAndPatient(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT da FROM DoctorAppointment da")
                .append(" INNER JOIN PatientRecord pr ON da.patientRecord.id = pr.id")
                .append(" INNER JOIN Patient p ON pr.hisPatientCode = p.patientCode")
                .append(" INNER JOIN HealthFacilities hf ON da.healthFacilityId = hf.id")
                .append(" WHERE 1=1 AND da.status NOT IN (-1, 0) ");
        Map<String, Object> values = new HashMap<>();

        if (queryParams.containsKey("healthFacilityId") && StringUtils.isNotBlank(queryParams.getFirst("healthFacilityId"))) {
            sql.append(" AND da.healthFacilityId = :healthFacilityId");
            values.put("healthFacilityId", Long.parseLong(queryParams.getFirst("healthFacilityId").trim()));
        } else {
            throw new BadRequestAlertException("Health facility ID can't be null", ENTITY_NAME, "doctor_appointment.emptyfield");
        }
        if (queryParams.containsKey("patientRecordCode") && StringUtils.isNotBlank(queryParams.getFirst("patientRecordCode"))) {
            sql.append(" AND p.patientCode LIKE :patientRecordCode");
            values.put("patientRecordCode", queryParams.getFirst("patientRecordCode"));
        } else {
            throw new BadRequestAlertException("Patient record code can't be null", ENTITY_NAME, "doctor_appointment.emptyfield");
        }
        if (queryParams.containsKey("patientRecordName") && StringUtils.isNotBlank(queryParams.getFirst("patientRecordName"))) {
            sql.append(" AND p.patientName LIKE :patientRecordName");
            values.put("patientRecordName", queryParams.getFirst("patientRecordName"));
        } else {
            throw new BadRequestAlertException("Patient record name can't be null", ENTITY_NAME, "doctor_appointment.emptyfield");
        }
        Query query = entityManager.createQuery(sql.toString(), DoctorAppointment.class);
        values.forEach(query::setParameter);

        return query.getResultList();
    }

    @Override
    public void deleteTempDoctorAppointment(List<Long> ids) {
        String sql = "UPDATE DoctorAppointment SET status =:status, lastModifiedDate =:lastModifiedDate, paymentStatus =:paymentStatus WHERE id IN (:ids)";
        entityManager.createQuery(sql)
                .setParameter("status", Constants.DOCTOR_APPOINTMENT_STATUS.DELETE_DOCTOR_APPOINTMENT_TEMP_INVALID)
                .setParameter("lastModifiedDate", Instant.now())
                .setParameter("paymentStatus", Constants.PAYMENT_STATUS.DELETE_TRANSACTION_TEMP_INVALID)
                .setParameter("ids", ids)
                .executeUpdate();
    }
}
