package com.bkav.lk.dto;

import java.util.List;

public class TopicDTO {

    private Long id;

    private String code;

    private String name;

    private Integer status;

    private List<TopicCustomConfigDTO> topicCustomConfigDTOS;

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

    public List<TopicCustomConfigDTO> getTopicCustomConfigDTOS() {
        return topicCustomConfigDTOS;
    }

    public void setTopicCustomConfigDTOS(List<TopicCustomConfigDTO> topicCustomConfigDTOS) {
        this.topicCustomConfigDTOS = topicCustomConfigDTOS;
    }
}
