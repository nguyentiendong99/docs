package com.bkav.lk.repository.impl;

import com.bkav.lk.domain.Position;
import com.bkav.lk.repository.custom.PositionRepositoryCustom;
import com.bkav.lk.util.StrUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PositionRepositoryImpl implements PositionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Position> findAllChildrenByParentId(Long parentId) {
        String sql = "with recursive cte (id, code, name, parent_id, status) as (" +
                "  select     id," +
                "             code," +
                "             name," +
                "             parent_id," +
                "             status" +
                "  from       position" +
                "  where      parent_id = " + parentId +
                "  union all" +
                "  select     p.id," +
                "             p.code," +
                "             p.name," +
                "             p.parent_id," +
                "             p.status" +
                "  from       position p" +
                "  inner join cte" +
                "          on p.parent_id = cte.id" +
                ")" +
                "select * from cte";
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> objects = query.getResultList();
        List<Position> positions = new ArrayList<>();
        if (!objects.isEmpty()) {
            for (Object[] o: objects) {
                Position position = new Position();
                position.setId(Long.valueOf(o[0].toString()));
                position.setCode((String) o[1]);
                position.setName((String) o[2]);
                position.setParentId(Long.valueOf(o[3].toString()));
                position.setStatus((Integer) o[4]);
                positions.add(position);
            }
            return positions;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Position> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT P FROM Position P");
        Map<String, Object> values = new HashMap<>();
        sql.append(createWhereQuery(queryParams, values));
        sql.append(createOrderQuery(queryParams));
        Query query = entityManager.createQuery(sql.toString(), Position.class);
        values.forEach(query::setParameter);
        if(queryParams.containsKey("pageIsNull")){
            pageable = null;
        }
        if (pageable != null) {
            query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            query.setMaxResults(pageable.getPageSize());
        }
        return query.getResultList();
    }

    @Override
    public Long count(MultiValueMap<String, String> queryParams) {
        String sql = "select count(P) from Position P";
        Map<String, Object> values = new HashMap<>();
        sql += createWhereQuery(queryParams, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(MultiValueMap<String, String> queryParams, Map<String, Object> values) {
        String sql = " where 1=1 ";
        if (queryParams.containsKey("status") && !StrUtil.isBlank(queryParams.get("status").get(0))) {
            sql = " where P.status = :status ";
            values.put("status", Integer.valueOf(queryParams.get("status").get(0)));
        }

        if (queryParams.containsKey("keyword") && !StrUtil.isBlank(queryParams.get("keyword").get(0))) {
            sql += " and (lower(P.name) like lower(:keyword) or lower(P.code) like lower(:keyword)) ";
            values.put("keyword", "%" + queryParams.get("keyword").get(0) + "%");
        } else {
            if (queryParams.containsKey("name") && !StrUtil.isBlank(queryParams.get("name").get(0))) {
                sql += " and lower(P.name) like lower(:name) ";
                values.put("name", "%" + queryParams.get("name").get(0) + "%");
            }

            if (queryParams.containsKey("code") && !StrUtil.isBlank(queryParams.get("code").get(0))) {
                sql += " and lower(P.code) like lower(:code) ";
                values.put("code", "%" + queryParams.get("code").get(0) + "%");
            }
        }
        return sql;
    }

    private String createOrderQuery(MultiValueMap<String, String> queryParams) {
        StringBuilder sql = new StringBuilder(" order by ");
        if (queryParams.containsKey("sort")) {
            List<String> orderByList = queryParams.get("sort");
            for (String i : orderByList) {
                sql.append("P." + i.replace(",", " ") + ", ");
            }
            sql.substring(0, sql.lastIndexOf(","));
        } else {
            sql.append(" P.createdDate desc ");
        }
        return sql.toString();
    }

}
