package com.bkav.lk.repository.impl;

import com.bkav.lk.repository.custom.GroupAuthorityRepositoryCustom;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class GroupAuthorityRepositoryImpl implements GroupAuthorityRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<String> getRolesCurrentUser(Long userId) {
        String sql = "SELECT g.authorityName FROM GroupAuthority g WHERE g.groupId in (SELECT u.groupId FROM GroupUser u where u.userId = :userId)";
        Query query = entityManager.createQuery(sql, String.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
}
