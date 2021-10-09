package com.bkav.lk.service.impl;

import com.bkav.lk.config.AuthenticationProperties;
import com.bkav.lk.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RedisServiceImpl implements RedisService {

    private static final Logger log = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Value("${spring.redis.cache-name.jwt}")
    private String jwtCacheName;

    private final AuthenticationProperties properties;

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisServiceImpl(AuthenticationProperties properties, RedisTemplate<String, Object> redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean existsByLogin(String login) {
        return redisTemplate.opsForValue().getOperations().hasKey(jwtCacheName + ":" + login);
    }

    @Override
    public String findByLogin(String login) {
        log.info("Process of retrieving jwt tokens with the user: {}", login);
        Object result = redisTemplate.opsForValue().get(jwtCacheName + ":" + login);
        return Objects.nonNull(result) ? String.valueOf(result) : "";
    }

    @Override
    public void saveJwt(String login, String jwt) {
        log.info("Process of storing jwt tokens - {} with the user: {}", jwt, login);
        BoundValueOperations<String, Object> boundValueOperations = redisTemplate.boundValueOps(jwtCacheName + ":" + login);
        boundValueOperations.set(jwt);
        boundValueOperations.expire(1000 * properties.getTokenValidityInSeconds(), TimeUnit.MILLISECONDS);
    }

    @Override
    public void removeJwt(String login) {
        log.info("Process of deleting jwt tokens with the user: {}", login);
        redisTemplate.opsForValue().getOperations().delete(jwtCacheName + ":" + login);
    }
}
