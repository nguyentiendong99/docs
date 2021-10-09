package com.bkav.lk.service.mapper;

import com.bkav.lk.domain.Topic;
import com.bkav.lk.dto.TopicDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TopicMapper extends EntityMapper<TopicDTO, Topic>{

    Topic toEntity(TopicDTO topicDTO);

    TopicDTO toDto(Topic topic);

    default Topic fromId(Long id) {
        if(id == null) {
            return null;
        }
        Topic topic = new Topic();
        topic.setId(id);
        return topic;
    }
}
