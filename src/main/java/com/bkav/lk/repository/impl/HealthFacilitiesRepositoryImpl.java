package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.HealthFacilities;
import com.bkav.lk.repository.custom.HealthFacilitiesRepositoryCustom;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HealthFacilitiesRepositoryImpl implements HealthFacilitiesRepositoryCustom {
    private static final Logger log = LoggerFactory.getLogger(HealthFacilitiesRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<HealthFacilities> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT H FROM HealthFacilities H ");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), HealthFacilities.class);
        values.forEach(query::setParameter);
        if(queryParams.containsKey("pageIsNull")){
            pageable = null;
        }
        if(pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(H) from HealthFacilities H ";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where H.status = (:status) ";
        values.put("status", Constants.ENTITY_STATUS.ACTIVE);

        // search Mobile chỉ search theo tên cơ sở y tế, k search theo địa chỉ.
        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and lower(H.name) like lower(:keyword) ";
            values.put("keyword", "%" + queryParams.get("keyword").get(0).trim() + "%");
        }
        if (queryParams.containsKey("code") && !StrUtil.isBlank(queryParams.get("code").get(0))) {
            sql += " and  H.code = :code";
            values.put("code", queryParams.get("code").get(0));
        }
        if (queryParams.containsKey("parentCode") && !StrUtil.isBlank(queryParams.get("parentCode").get(0))) {
            sql += " and  H.parentCode = :parentCode";
            values.put("parentCode", queryParams.get("parentCode").get(0));
        }
        if (queryParams.containsKey("cityCode") && !StrUtil.isBlank(queryParams.get("cityCode").get(0))) {
            sql +=(" and  H.cityCode LIKE :cityCode ");
            values.put("cityCode", queryParams.get("cityCode").get(0).trim());
        }
        if (queryParams.containsKey("districtCode") && !StrUtil.isBlank(queryParams.get("districtCode").get(0))) {
            sql +=(" and  H.districtCode LIKE :districtCode ");
            values.put("districtCode", queryParams.get("districtCode").get(0).trim());
        }
        if (queryParams.containsKey("wardCode") && !StrUtil.isBlank(queryParams.get("wardCode").get(0))) {
            sql +=(" and  H.wardCode LIKE :wardCode ");
            values.put("wardCode", queryParams.get("wardCode").get(0).trim());
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder( " order by ");
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql.append("H." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql.append(" H.createdDate desc ");
        }
        return sql.toString();
    }

    @Override
    public List<HealthFacilities> findAllChildrenByParent(Long parent) {
        String sql = "with recursive cte (id, parent, name, phone, code, fax, email, address, status, manager, description, img_path, medicalprocess_path, city_code, district_code, ward_code, latitude, longitude) as (" +
                "  select     id," +
                "             parent," +
                "             name," +
                "             phone," +
                "             code," +
                "             fax," +
                "             email," +
                "             address," +
                "             status," +
                "             manager," +
                "             description," +
                "             img_path," +
                "             medicalprocess_path," +
                "             city_code," +
                "             district_code," +
                "             ward_code," +
                "             latitude," +
                "             longitude" +
                "  from       health_facilities" +
                "  where      parent = " + parent +
                "  and        status in (1,2) " +
                "  union all" +
                "  select     h.id," +
                "             h.parent," +
                "             h.name," +
                "             h.phone," +
                "             h.code," +
                "             h.fax," +
                "             h.email," +
                "             h.address," +
                "             h.status," +
                "             h.manager," +
                "             h.description," +
                "             h.img_path," +
                "             h.medicalprocess_path," +
                "             h.city_code," +
                "             h.district_code," +
                "             h.ward_code," +
                "             h.latitude," +
                "             h.longitude" +
                "  from       health_facilities h" +
                "  inner join cte" +
                "          on h.parent = cte.id" +
                ")" +
                "select * from cte";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> objects = query.getResultList();
        List<HealthFacilities> healthFacilitiesList = new ArrayList<>();
        if (!objects.isEmpty()) {
            for (Object[] o: objects) {
                HealthFacilities healthFacilities = new HealthFacilities();
                healthFacilities.setId(Long.valueOf(o[0].toString()));
                healthFacilities.setParent(Long.valueOf(o[1].toString()));
                healthFacilities.setName((String) o[2]);
                healthFacilities.setPhone((String) o[3]);
                healthFacilities.setCode((String) o[4]);
                healthFacilities.setFax((String) o[5]);
                healthFacilities.setEmail((String) o[6]);
                healthFacilities.setAddress((String) o[7]);
                healthFacilities.setStatus((Integer) o[8]);
                healthFacilities.setManager((String) o[9]);
                healthFacilities.setDescription((String) o[10]);
                healthFacilities.setImgPath((String) o[11]);
                healthFacilities.setMedicalProcessPath((String) o[12]);
                healthFacilities.setCityCode((String) o[13]);
                healthFacilities.setDistrictCode((String) o[14]);
                healthFacilities.setWardCode((String) o[15]);
                healthFacilities.setLatitude((Double) o[16]);
                healthFacilities.setLongitude((Double) o[17]);
                healthFacilitiesList.add(healthFacilities);
            }
            return healthFacilitiesList;
        }
        return new ArrayList<>();

    }
}
