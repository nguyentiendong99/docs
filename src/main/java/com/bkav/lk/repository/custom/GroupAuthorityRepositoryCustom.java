package com.bkav.lk.repository.custom;

import java.util.List;

public interface GroupAuthorityRepositoryCustom {
    List<String> getRolesCurrentUser(Long userId);
}
