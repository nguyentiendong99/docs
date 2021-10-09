package com.bkav.lk.service.impl;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.domain.Position;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.ActivityLogForManagementDTO;
import com.bkav.lk.repository.ActivityLogRepository;
import com.bkav.lk.repository.PositionRepository;
import com.bkav.lk.repository.UserRepository;
import com.bkav.lk.service.ActivityLogManagementService;
import com.bkav.lk.util.Constants;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ActivityLogManagementServiceImpl implements ActivityLogManagementService {

    private final ActivityLogRepository activityLogRepository;

    private final UserRepository userRepository;

    private final PositionRepository positionRepository;

    public ActivityLogManagementServiceImpl(ActivityLogRepository activityLogRepository, UserRepository userRepository, PositionRepository positionRepository) {
        this.activityLogRepository = activityLogRepository;
        this.userRepository = userRepository;
        this.positionRepository = positionRepository;
    }

    @Override
    public Page<ActivityLogForManagementDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<ActivityLog> activityLogList = activityLogRepository.searchForManagement(queryParams, pageable);
        List<ActivityLogForManagementDTO> activityLogForManagementDTOList = convertToDTO(activityLogList);
        return new PageImpl<>(activityLogForManagementDTOList, pageable, activityLogRepository.count(queryParams));
    }

    private List<ActivityLogForManagementDTO> convertToDTO(List<ActivityLog> activityLogList){
        List<ActivityLogForManagementDTO> activityLogForManagementDTOList = new ArrayList<>();
        for(ActivityLog activityLog: activityLogList){
            ActivityLogForManagementDTO dto = new ActivityLogForManagementDTO();
            dto.setId(activityLog.getId());
            activityLogForManagementDTOList.add(dto);
            dto.setModuleName(Constants.MODULE.getById(activityLog.getContentType()).name);
            dto.setActionType(Constants.ACTION_NAME.getById(activityLog.getActionType()).name);
            dto.setCreatedBy(activityLog.getCreatedBy());
            dto.setCreatedDate(activityLog.getCreatedDate());
            Optional<User> optional = userRepository.findByLogin(activityLog.getCreatedBy());
            if(optional.isPresent()){
                User user = optional.get();
                Position position = user.getPosition();
                if(position != null){
                    dto.setPositionName(position.getName());
                }
            }
            dto.setDescription(createDescription(activityLog));
        }
        return activityLogForManagementDTOList;
    }

    private String createDescription(ActivityLog activityLog){
        String description = "";
        description += "Id: " + activityLog.getContentId();
        description += " - " + Constants.ACTION_NAME.getById(activityLog.getActionType()).name;
        description += " " + Constants.CONTENT.getById(activityLog.getContentType()).name;
        return description;
    }
}
