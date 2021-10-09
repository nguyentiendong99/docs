package com.bkav.lk.service.impl;

import com.bkav.lk.domain.HisClsGoiY;
import com.bkav.lk.repository.HisClsGoiYRepository;
import com.bkav.lk.service.HisClsGoiYService;
import org.springframework.stereotype.Service;

@Service
public class HisClsGoiYServiceImpl implements HisClsGoiYService {

    private final HisClsGoiYRepository  hisClsGoiYRepository;

    public HisClsGoiYServiceImpl(HisClsGoiYRepository hisClsGoiYRepository) {
        this.hisClsGoiYRepository = hisClsGoiYRepository;
    }

    @Override
    public HisClsGoiY save(HisClsGoiY item) {
        return hisClsGoiYRepository.save(item);
    }
}
