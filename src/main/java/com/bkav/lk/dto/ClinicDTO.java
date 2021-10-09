package com.bkav.lk.dto;

//<editor-fold desc="IMPORT">

import java.io.Serializable;
import java.util.List;
//</editor-fold>

public class ClinicDTO  implements Serializable {

    private Long id;

    private String code;

    private String name;

    private Integer status;

    private Long healthFacilityId;

    private String healthFacilityCode;

    private List<ClinicCustomConfigDTO> clinicCustomConfigDTOS;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<ClinicCustomConfigDTO> getClinicCustomConfigDTOS() {
        return clinicCustomConfigDTOS;
    }

    public void setClinicCustomConfigDTOS(List<ClinicCustomConfigDTO> clinicCustomConfigDTOS) {
        this.clinicCustomConfigDTOS = clinicCustomConfigDTOS;
    }

    public Long getHealthFacilityId() {
        return healthFacilityId;
    }

    public void setHealthFacilityId(Long healthFacilityId) {
        this.healthFacilityId = healthFacilityId;
    }

    public String getHealthFacilityCode() {
        return healthFacilityCode;
    }

    public void setHealthFacilityCode(String healthFacilityCode) {
        this.healthFacilityCode = healthFacilityCode;
    }
}
