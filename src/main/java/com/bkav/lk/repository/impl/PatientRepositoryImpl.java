package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Patient;
import com.bkav.lk.repository.custom.PatientRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PatientRepositoryImpl implements PatientRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Patient> findByOldAppointmentCodeAndLogin(String appointmentCode, String login) {
        StringBuilder sql = new StringBuilder("SELECT P FROM MedicalResult MR INNER JOIN MR.patient P" +
                " WHERE MR.doctorAppointmentCode = :appointmentCode AND P.createdBy LIKE :login");
        Map<String, Object> values = new HashMap<>();
        values.put("appointmentCode", appointmentCode);
        values.put("login", login);

        Query query = entityManager.createQuery(sql.toString(), Patient.class);
        values.forEach(query::setParameter);

        Patient result = null;
        try {
            result = (Patient) query.getSingleResult();
        } catch (NoResultException ex) {
            //Ignore this because as per your logic this is ok!
        }
        return Optional.ofNullable(result);
    }
}
