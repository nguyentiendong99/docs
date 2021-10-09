package com.bkav.lk.web.rest;

import com.bkav.lk.domain.Group;
import com.bkav.lk.domain.User;
import com.bkav.lk.dto.GroupDTO;
import com.bkav.lk.dto.GroupHistoryDTO;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.GroupService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.GroupMapper;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.log_custom_annotation.Create;
import com.bkav.lk.web.log_custom_annotation.Delete;
import com.bkav.lk.web.log_custom_annotation.Update;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class GroupResource {
    private final Logger log = LoggerFactory.getLogger(HealthFacilitiesResource.class);

    private static final String ENTITY_NAME = "group";

    private final GroupService groupService;

    private final UserService userService;

    private final ActivityLogService activityLogService;

    private final GroupMapper groupMapper;


    public GroupResource(GroupService groupService, UserService userService,GroupMapper groupMapper, ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
        this.groupService = groupService;
        this.userService = userService;
        this.groupMapper = groupMapper;
    }

    /**
     * Lấy 1 bản ghi
     */
    @GetMapping("/groups/{id}")
    public ResponseEntity<GroupDTO> getGroup(@PathVariable Long id) {
        Optional<GroupDTO> groupDTO = groupService.findOne(id);
        if (!groupDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(groupDTO);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<GroupDTO>> search(@RequestParam MultiValueMap<String, String> queryParams,
                                                 Pageable pageable) {

        Page<GroupDTO> page = groupService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
    @Create
    @PostMapping("/groups")
    public ResponseEntity<GroupDTO> create(@RequestBody @Valid GroupDTO groupDTO) {
        Optional<GroupDTO> existGroupDTO = groupService.findByGroupName(groupDTO.getGroupName());
        if (groupDTO.getId() != null) {
            throw new BadRequestAlertException("A new Group cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (existGroupDTO.isPresent()) {
            throw new BadRequestAlertException("exist group", ENTITY_NAME, "group_exist");
        }
        GroupDTO result = groupService.save(groupDTO);
        activityLogService.create(Constants.CONTENT_TYPE.GROUP, groupMapper.toEntity(result));
        return ResponseEntity.ok(result);
    }

    @Update
    @PutMapping("/groups")
    public ResponseEntity<GroupDTO> update(@RequestBody @Valid GroupDTO groupDTO) {
        Optional<GroupDTO> existGroupDTO = groupService.findByGroupName(groupDTO.getGroupName());
        Group old = groupService.findById(groupDTO.getId());
        if (groupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (existGroupDTO.isPresent() && !existGroupDTO.get().getId().equals(groupDTO.getId())) {
            throw new BadRequestAlertException("exist group", ENTITY_NAME, "group_exist");
        }
        activityLogService.update(Constants.CONTENT_TYPE.GROUP, old, groupMapper.toEntity(groupDTO));
        GroupDTO result = groupService.save(groupDTO);
        return ResponseEntity.ok(result);
    }
    @Delete
    @DeleteMapping("/groups/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        List<User> user = userService.findByGroup(id);
        Group group = groupService.findById(id);
        if (!user.isEmpty()) {
            return ResponseEntity.ok().body(false);
        }
        activityLogService.delete(Constants.CONTENT_TYPE.GROUP, group);
        groupService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, true,
                ENTITY_NAME, id.toString())).build();
    }
    @GetMapping("/groups/users")
    public ResponseEntity<List<HashMap<String, String>>> getCreatedByUsers(){
        List<HashMap<String, String>> list = new ArrayList<>();
        List<String> createdByUsers = groupService.getCreatedByUsers();
        for(String user: createdByUsers){
            HashMap<String, String> rs = new HashMap<>();
            rs.put("id", user);
            rs.put("text", user);
            list.add(rs);
        }
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/groups/history")
    public ResponseEntity<List<GroupHistoryDTO>> findAllHistoryPosition(@RequestParam MultiValueMap<String, String> queryParam) {
        log.debug("REST request to get all group history: {}", queryParam.get("name"));
        List<GroupHistoryDTO> list = groupService.getGroupHistory(queryParam);
        return ResponseEntity.ok().body(list);
    }

}
