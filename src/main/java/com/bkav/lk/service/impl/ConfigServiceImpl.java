package com.bkav.lk.service.impl;

import com.bkav.lk.domain.Config;
import com.bkav.lk.dto.CategoryConfigFieldDTO;
import com.bkav.lk.dto.CategoryConfigFieldMainDTO;
import com.bkav.lk.dto.CategoryConfigIconDTO;
import com.bkav.lk.repository.ConfigRepository;
import com.bkav.lk.service.CategoryConfigFieldMainService;
import com.bkav.lk.service.CategoryConfigFieldService;
import com.bkav.lk.service.CategoryConfigIconService;
import com.bkav.lk.service.ConfigService;
import com.bkav.lk.util.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ConfigServiceImpl implements ConfigService {

    private String TERM_OF_USE = "term_of_use";

    private String TERM_OF_USE_NAME = "Term Of Use";

    private final ConfigRepository configRepository;

    private final CategoryConfigFieldService categoryConfigFieldService;
    private final CategoryConfigFieldMainService categoryConfigFieldMainService;
    private final CategoryConfigIconService categoryConfigIconService;

    @Value("${his.host}")
    private String HIS_HOST;

    @Value("${social-insurance.host}")
    private String SOCIAL_INSURANCE_HOST;

    public ConfigServiceImpl(ConfigRepository configRepository, CategoryConfigFieldService categoryConfigFieldService, CategoryConfigFieldMainService categoryConfigFieldMainService, CategoryConfigIconService categoryConfigIconService) {
        this.configRepository = configRepository;
        this.categoryConfigFieldService = categoryConfigFieldService;
        this.categoryConfigFieldMainService = categoryConfigFieldMainService;
        this.categoryConfigIconService = categoryConfigIconService;
    }


    @PostConstruct
    public void init(){
        // Init Host HIS
        Config hisHost = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.HIS_HOST);
        if (Objects.isNull(hisHost)) {
            Config hisConfig = new Config();
            hisConfig.setName(Constants.CONFIG_PROPERTY.HIS_NAME);
            hisConfig.setPropertyCode(Constants.CONFIG_PROPERTY.HIS_HOST);
            hisConfig.setPropertyGroup(Constants.CONFIG_PROPERTY.HIS_GROUP);
            hisConfig.setPropertyValue(this.HIS_HOST);
            hisConfig.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(hisConfig);
        }

        // Init Host BHYT
        Config socialInsuranceHost = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_INSURANCE_HOST);
        if(Objects.isNull(socialInsuranceHost)) {
            Config siConfig = new Config();
            siConfig.setName(Constants.CONFIG_PROPERTY.SOCIAL_INSURANCE_NAME);
            siConfig.setPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_INSURANCE_HOST);
            siConfig.setPropertyGroup(Constants.CONFIG_PROPERTY.SOCIAL_INSURANCE_GROUP);
            siConfig.setPropertyValue(this.SOCIAL_INSURANCE_HOST);
            siConfig.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(siConfig);
        }

        // Init Social Network SignIn - Config
        Config googleSignInConfig = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_GOOGLE_CODE);
        if(Objects.isNull(googleSignInConfig)) {
            Config ggSignInConfig = new Config();
            ggSignInConfig.setName(Constants.CONFIG_PROPERTY.SOCIAL_GOOGLE_NAME);
            ggSignInConfig.setPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_GOOGLE_CODE);
            ggSignInConfig.setPropertyGroup(Constants.CONFIG_PROPERTY.SOCIAL_NETWORK_SIGNIN_GROUP);
            ggSignInConfig.setPropertyValue(Constants.ENTITY_STATUS.ACTIVE.toString());
            ggSignInConfig.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(ggSignInConfig);
        }

        Config fBookSignInConfig = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_FACEBOOK_CODE);
        if(Objects.isNull(fBookSignInConfig)) {
            Config fbSignInConfig = new Config();
            fbSignInConfig.setName(Constants.CONFIG_PROPERTY.SOCIAL_FACEBOOK_NAME);
            fbSignInConfig.setPropertyCode(Constants.CONFIG_PROPERTY.SOCIAL_FACEBOOK_CODE);
            fbSignInConfig.setPropertyGroup(Constants.CONFIG_PROPERTY.SOCIAL_NETWORK_SIGNIN_GROUP);
            fbSignInConfig.setPropertyValue(Constants.ENTITY_STATUS.ACTIVE.toString());
            fbSignInConfig.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(fbSignInConfig);
        }

        // Init Config Other - DoctorAppointment
        Config timeAllowBeforeBooking = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.TIME_ALLOW_BEFORE_BOOKING);
        if(Objects.isNull(timeAllowBeforeBooking)) {
            Config config = new Config();
            config.setName(Constants.CONFIG_PROPERTY.TIME_ALLOW_BEFORE_BOOKING);
            config.setPropertyCode(Constants.CONFIG_PROPERTY.TIME_ALLOW_BEFORE_BOOKING);
            config.setPropertyGroup(Constants.CONFIG_PROPERTY.CONFIG_OTHER_GROUP);
            config.setPropertyValue("16:30");
            config.setPropertyType(Constants.CONFIG_PROPERTY.TEXT_TYPE);
            config.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(config);
        }

        Config allowBookingBeforeDay = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.ALLOW_BOOKING_BEFORE_DAY);
        if(Objects.isNull(allowBookingBeforeDay)) {
            Config config = new Config();
            config.setName(Constants.CONFIG_PROPERTY.ALLOW_BOOKING_BEFORE_DAY);
            config.setPropertyCode(Constants.CONFIG_PROPERTY.ALLOW_BOOKING_BEFORE_DAY);
            config.setPropertyGroup(Constants.CONFIG_PROPERTY.CONFIG_OTHER_GROUP);
            config.setPropertyValue(String.valueOf(30));
            config.setPropertyType(Constants.CONFIG_PROPERTY.NUMBER_TYPE);
            config.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(config);
        }

        Config blockAccountExceedDay = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_DAY);
        if(Objects.isNull(blockAccountExceedDay)) {
            Config config = new Config();
            config.setName(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_DAY);
            config.setPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_DAY);
            config.setPropertyGroup(Constants.CONFIG_PROPERTY.CONFIG_OTHER_GROUP);
            config.setPropertyValue(String.valueOf(3));
            config.setPropertyType(Constants.CONFIG_PROPERTY.NUMBER_TYPE);
            config.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(config);
        }

        Config blockAccountExceedWeek = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_WEEK);
        if(Objects.isNull(blockAccountExceedWeek)) {
            Config config = new Config();
            config.setName(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_WEEK);
            config.setPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_EXCEED_WEEK);
            config.setPropertyGroup(Constants.CONFIG_PROPERTY.CONFIG_OTHER_GROUP);
            config.setPropertyValue(String.valueOf(5));
            config.setPropertyType(Constants.CONFIG_PROPERTY.NUMBER_TYPE);
            config.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(config);
        }

        Config blockAccountInDay = configRepository.findByPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_IN_DAY);
        if(Objects.isNull(blockAccountInDay)) {
            Config config = new Config();
            config.setName(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_IN_DAY);
            config.setPropertyCode(Constants.CONFIG_PROPERTY.BLOCK_ACCOUNT_IN_DAY);
            config.setPropertyGroup(Constants.CONFIG_PROPERTY.CONFIG_OTHER_GROUP);
            config.setPropertyValue(String.valueOf(15));
            config.setPropertyType(Constants.CONFIG_PROPERTY.NUMBER_TYPE);
            config.setStatus(Constants.CONFIG_PROPERTY.STATUS_ACTIVE);
            configRepository.save(config);
        }

    }
    @Override
    public Config findByPropertyCode(String code) {
        return configRepository.findByPropertyCode(code);
    }

    @Override
    public Config createNewTermOfUseConfig() {
        Config config = new Config();
        config.setPropertyCode(TERM_OF_USE);
        config.setName(TERM_OF_USE_NAME);
        config.setStatus(Constants.ENTITY_STATUS.ACTIVE);
        return configRepository.save(config);
    }

    @Override
    public Config save(Config config) {
        return configRepository.save(config);
    }

    @Override
    public List<Config> findByPropertyGroup(String group) {
        return configRepository.findByPropertyGroup(group);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateConfigCategory(Map<String, List<Object>> mapConfig) {
        ObjectMapper mapper = new ObjectMapper();

        // Field Main
        List<CategoryConfigFieldMainDTO> listFieldMain = mapper
                .convertValue(mapConfig.get("fieldMain").stream().toArray(), new TypeReference<List<CategoryConfigFieldMainDTO>>() { });
        categoryConfigFieldMainService.update(listFieldMain);

        // Field Custom
        List<CategoryConfigFieldDTO> listFieldCustom = mapper
                .convertValue(mapConfig.get("fieldCustom").stream().toArray(), new TypeReference<List<CategoryConfigFieldDTO>>() { });
        if (listFieldCustom.size() > 0) {
            List<CategoryConfigFieldDTO> listCreate = listFieldCustom.stream().filter(item -> item.getId() == null).collect(Collectors.toList());
            if (listCreate.size() > 0) {
                categoryConfigFieldService.create(listCreate);
            }
            List<CategoryConfigFieldDTO> listUpdate = listFieldCustom.stream().filter(item -> item.getId() != null).collect(Collectors.toList());
            if (listUpdate.size() > 0) {
                categoryConfigFieldService.update(listUpdate);
            }
        }
        // Icon Funcition
        List<CategoryConfigIconDTO> listIcon = mapper
                .convertValue(mapConfig.get("iconFunction").stream().toArray(), new TypeReference<List<CategoryConfigIconDTO>>() { });

        categoryConfigIconService.update(listIcon);
        return true;
    }
}
