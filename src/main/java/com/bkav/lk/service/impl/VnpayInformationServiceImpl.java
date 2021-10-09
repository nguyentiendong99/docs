package com.bkav.lk.service.impl;

import com.bkav.lk.domain.VnpayInformation;
import com.bkav.lk.repository.VnpayInformationRepository;
import com.bkav.lk.service.VnpayInformationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class VnpayInformationServiceImpl implements VnpayInformationService {
    private final VnpayInformationRepository vnpayInformationRepository;


    public VnpayInformationServiceImpl(VnpayInformationRepository vnpayInformationRepository) {
        this.vnpayInformationRepository = vnpayInformationRepository;
    }

    @Override
    public VnpayInformation findByHealthFacilityId(Long healthFacilityId) {
        return vnpayInformationRepository.findByHealthFacilityId(healthFacilityId).orElse(null);
    }
}
