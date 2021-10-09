package com.bkav.lk.service.impl;

import com.bkav.lk.domain.ActivityLog;
import com.bkav.lk.domain.Authority;
import com.bkav.lk.domain.Group;
import com.bkav.lk.dto.GroupDTO;
import com.bkav.lk.dto.GroupHistoryDTO;
import com.bkav.lk.repository.GroupRepository;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.GroupService;
import com.bkav.lk.service.mapper.GroupMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;

    private final GroupMapper groupMapper;

    private ActivityLogService activityLogService;

    public GroupServiceImpl(GroupRepository groupRepository, GroupMapper groupMapper, ActivityLogService activityLogService) {
        this.groupRepository = groupRepository;
        this.groupMapper = groupMapper;
        this.activityLogService = activityLogService;
    }

    @Override
    public Optional<GroupDTO> findOne(Long id) {
        return groupRepository.findById(id).map(groupMapper::toDto);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        groupRepository.delete(id);
    }

    @Override
    public List<String> getCreatedByUsers() {
        return groupRepository.getCreatedByUsers();
    }

    @Override
    public List<GroupHistoryDTO> getGroupHistory(MultiValueMap<String, String> queryParam) {
        List<ActivityLog> activityLogs = activityLogService.search(queryParam);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        List<GroupHistoryDTO> groupHistoryDTOArrayList = new ArrayList<>();


        for (ActivityLog activityLog : activityLogs) {
            GroupHistoryDTO history = new GroupHistoryDTO();
            history.setCreatedDate(activityLog.getCreatedDate());
            history.setCreatedBy(activityLog.getCreatedBy());
            List<String> newContentList = new ArrayList<>();
            List<String> oldContentList = new ArrayList<>();
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.CREATE)) {
                newContentList.add("Thêm mới");
                history.setNewContents(newContentList);
                groupHistoryDTOArrayList.add(history);
                continue;
            }
            if (activityLog.getActionType().equals(Constants.ACTION_TYPE.UPDATE)) {
                Group oldP = convertToGroup(activityLog.getOldContent());
                Group newP = convertToGroup(activityLog.getContent());
                createContentList(oldP, newP, oldContentList, newContentList);
                history.setOldContents(oldContentList);
                history.setNewContents(newContentList);
                groupHistoryDTOArrayList.add(history);
            }
            if (oldContentList.size() == 0 || newContentList.size() == 0) {
                continue;
            }
        }

        return groupHistoryDTOArrayList;
    }

    private Group convertToGroup(String input) {
        if (input.isEmpty()) {
            return null;
        }
        Gson gson = new Gson();
        return gson.fromJson(input, Group.class);
    }

    private void createContentList(Group oldP, Group newP, List<String> oldContentList, List<String> newContentList) {
        if (oldP == null) {
            return;
        }
        if (!oldP.getGroupName().equals(newP.getGroupName())) {
            oldContentList.add("Tên : " + oldP.getGroupName());
            newContentList.add("Tên : " + newP.getGroupName());
        }
        for (Authority value : oldP.getAuthorities()) {
            boolean isIncludes = newP.getAuthorities().contains(value);
            if (!isIncludes) {
                newContentList.add("Xóa quyền: " + StrUtil.getStringFromBundle(value.getName(), com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, null));
            }
        }
        for (Authority value : newP.getAuthorities()) {
            boolean isIncludes = oldP.getAuthorities().contains(value);
            if (!isIncludes) {
                newContentList.add("Thêm quyền: " + StrUtil.getStringFromBundle(value.getName(), com.bkav.lk.config.Constants.DEFAULT_LANGUAGE, null));
            }
        }

        if (!oldP.getNote().equals(newP.getNote())) {
            oldContentList.add("Ghi chú: " + oldP.getNote());
            newContentList.add("Ghi chú: " + newP.getNote());
        }

        if (!oldP.getStatus().equals(newP.getStatus())) {
            oldContentList.add("Trạng thái: " +
                    (oldP.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
            newContentList.add("Trạng thái: " +
                    (newP.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) ? "Đang hoạt động" : "Dừng hoạt động"));
        }
    }

    @Override
    @Transactional
    public GroupDTO save(GroupDTO groupDTO) {
        Group group = groupMapper.toEntity(groupDTO);
        return groupMapper.toDto(groupRepository.save(group));
    }

    @Override
    public Group findById(Long id) {
        return groupRepository.findByIdAndStatus(id, Constants.ENTITY_STATUS.ACTIVE);
    }

    @Override
    public Optional<GroupDTO> findByGroupName(String name) {
        return groupRepository.findGroupByGroupName(name).map(groupMapper::toDto);
    }

    @Override
    public Page<GroupDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        if (queryParams.containsKey("pageable")) {
            pageable = null;
        }
        List<Group> groups = groupRepository.search(queryParams, pageable);
        if (pageable == null) {
            return new PageImpl<>(groupMapper.toDto(groups));
        }
        Page<Group> page = new PageImpl<>(groups, pageable, groupRepository.count(queryParams));
        return page.map(groupMapper::toDto);
    }
}
