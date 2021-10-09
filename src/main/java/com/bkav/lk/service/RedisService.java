package com.bkav.lk.service;

public interface RedisService {

    boolean existsByLogin(String login);

    String findByLogin(String login);

    void saveJwt(String login, String jwt);

    void removeJwt(String login);

}
