package com.bkav.lk.service;


import com.bkav.lk.domain.VnpayInformation;

public interface VnpayInformationService {

    VnpayInformation findByHealthFacilityId(Long healthFacilityId);
}
