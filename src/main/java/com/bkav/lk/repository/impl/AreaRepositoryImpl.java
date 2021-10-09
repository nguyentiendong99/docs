package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Area;
import com.bkav.lk.repository.custom.AreaRepositoryCustom;
import com.bkav.lk.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaRepositoryImpl implements AreaRepositoryCustom {

    private final Logger log = LoggerFactory.getLogger(AreaRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<Area> findAllByParentCode(String areaCode) {
        StringBuilder sql = new StringBuilder();

        sql.append("select new Area(A.id,A.name,A.areaCode,A.alias,A.parentCode,A.type,A.level,A.postalCode,A.status," +
                "A.priority,A.latitude,A.longitude,A.shortName) from Area A WHERE ");
        Map<String, Object> values = new HashMap<>();
        if (StringUtils.isEmpty(areaCode)) {
            sql.append(" A.level = ");
            sql.append(1);
            sql.append(" AND A.status = ");
            sql.append(Constants.ENTITY_STATUS.ACTIVE);
        } else {
            sql.append(" A.parentCode = :areaCode AND A.status = ");
            sql.append(Constants.ENTITY_STATUS.ACTIVE);
            values.put("areaCode", areaCode);
        }
        sql.append(" ORDER BY A.priority ASC");
        Query query = entityManager.createQuery(sql.toString(), Area.class);
        values.forEach(query::setParameter);
        return query.getResultList();
    }
}
