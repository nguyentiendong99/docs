package com.bkav.lk.web.rest;

import com.bkav.lk.domain.Position;
import com.bkav.lk.dto.DoctorDTO;
import com.bkav.lk.dto.PositionDTO;
import com.bkav.lk.dto.PositionHistoryDTO;
import com.bkav.lk.service.ActivityLogService;
import com.bkav.lk.service.DoctorService;
import com.bkav.lk.service.PositionService;
import com.bkav.lk.service.UserService;
import com.bkav.lk.service.mapper.PositionMapper;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.service.util.ResultExcel;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.errors.ErrorExcel;
import com.bkav.lk.web.log_custom_annotation.Create;
import com.bkav.lk.web.log_custom_annotation.Delete;
import com.bkav.lk.web.log_custom_annotation.Update;
import com.bkav.lk.web.rest.util.HeaderUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class PositionResource {

    private final Logger log = LoggerFactory.getLogger(PositionResource.class);

    private static final String ENTITY_NAME = "Position";

    private final PositionService service;

    private final DoctorService doctorService;

    private final UserService userService;

    private StorageService storageService;

    private final ActivityLogService activityLogService;

    private final PositionMapper positionMapper;

    public PositionResource(PositionService positionService, DoctorService doctorService, UserService userService,
                            PositionMapper positionMapper, ActivityLogService activityLogService, StorageService storageService) {
        this.service = positionService;
        this.doctorService = doctorService;
        this.userService = userService;
        this.storageService = storageService;
        this.activityLogService = activityLogService;
        this.positionMapper = positionMapper;

    }

    /**
     * create
     *
     * @param positionDTO positionDTO
     * @return PositionDTO
     */
    @Create
    @PostMapping("/positions")
    public ResponseEntity<PositionDTO> create(@Valid @RequestBody PositionDTO positionDTO) throws URISyntaxException {
        log.debug("REST request to save position : {}", positionDTO);
        if (positionDTO.getId() != null) {
            throw new BadRequestAlertException("A new position cannot already have an ID", ENTITY_NAME, "idexists");
        }
        positionDTO = service.save(positionDTO);
        activityLogService.create(Constants.CONTENT_TYPE.POSITION, positionMapper.toEntity(positionDTO));

        return ResponseEntity.created(new URI("/api/positions/" + positionDTO.getId())).body(positionDTO);
    }

    /**
     * update
     *
     * @param positionDTO positionDTO
     * @return PositionDTO
     */
    @Update
    @PutMapping("/positions")
    public ResponseEntity<PositionDTO> update(@RequestBody PositionDTO positionDTO) throws URISyntaxException {
        log.debug("REST request to save position : {}", positionDTO);
        if (positionDTO.getId() == null) {
            throw new BadRequestAlertException("A position cannot save has not ID", ENTITY_NAME, "idnull");
        }
        if (positionDTO.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            //check has child position active or not
            List<PositionDTO> positionDTOS = service.findAllChildrenByParentId(positionDTO.getId());
            if (!positionDTOS.isEmpty()) {
                boolean hasChildActive = positionDTOS.stream().anyMatch(o -> Constants.ENTITY_STATUS.ACTIVE.equals(o.getStatus()));
                if (hasChildActive) {
                    throw new BadRequestAlertException("This position has been used", "Position", "position_child_is_active");
                }
            }
            //check exist any doctor or user use this position or not
            boolean doctorExists = doctorService.existsByPositionId(positionDTO.getId());
            boolean userExists = userService.existsByPositionId(positionDTO.getId());
            if (doctorExists || userExists) {
                throw new BadRequestAlertException("This position has been used", "Position", "position_has_relationship");
            }
        }
        Position oldPosition = service.findById(positionDTO.getId());
        positionDTO = service.save(positionDTO);
        activityLogService.update(Constants.CONTENT_TYPE.POSITION, oldPosition, positionMapper.toEntity(positionDTO));
        return ResponseEntity.ok().body(positionDTO);
    }

    /**
     * findOne
     * Trả về một đối tượng chức vụ
     *
     * @param id id
     * @return PositionDTO
     */
    @GetMapping("/positions/{id}")
    public ResponseEntity<PositionDTO> findOne(@PathVariable Long id) {
        log.debug("REST request to get position : {}", id);
        Optional<PositionDTO> dto = service.findOne(id);
        if (!dto.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(dto);
    }

    /**
     * search
     * Tìm kiếm danh sách chức vụ
     *
     * @param queryParams name
     * @return List<PositionDTO> - Danh sách chức vụ
     */
    @GetMapping("/positions")
    public ResponseEntity<Map<String, Object>> search(@RequestParam Map<String, String> queryParams) {
        log.debug("REST request to search for a page of positions for query {}", queryParams);
        List<PositionDTO> positionDTOList = service.findAll();
        Collections.sort(positionDTOList, Comparator.comparing(PositionDTO::getStatus).thenComparing(PositionDTO::getLastModifiedDate, Comparator.reverseOrder()));
        Map<String, Object> positions = service.handleTreePosition(positionDTOList, queryParams);
        return ResponseEntity.ok().body(positions);
    }

    @GetMapping("/positions/public")
    public ResponseEntity<List<PositionDTO>> findAll(@RequestParam MultiValueMap<String, String> queryParams,
                                                     Pageable pageable) {
        log.debug("REST request to get all positions for query {}", queryParams);
        Page<PositionDTO> page = service.search(queryParams, pageable);
        return ResponseEntity.ok().body(page.getContent());
    }

    /**
     * delete
     *
     * @param id Xóa một chức vụ => status = 0
     * @return void
     */

    @Delete
    @DeleteMapping("/positions/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete position : {}", id);
        List<DoctorDTO> doctorDTOS = doctorService.findAllByPosition(id);
        if (!doctorDTOS.isEmpty()) {
            throw new BadRequestAlertException("This position has been used", "Position", "position_has_been_used");
        }
        service.delete(id);
        Optional<PositionDTO> positionDTO = service.findOne(id);
        positionDTO.ifPresent(dto -> activityLogService.delete(Constants.CONTENT_TYPE.POSITION, positionMapper.toEntity(dto)));

        List<PositionDTO> positionDTOS = service.findAllChildrenByParentId(id);
        if (!positionDTOS.isEmpty()) {
            positionDTOS.forEach(item -> {
                if (item.getStatus().equals(Constants.ENTITY_STATUS.ACTIVE) || item.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
                    service.delete(item.getId());
                }
            });
        }
        return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert("", true, ENTITY_NAME, id.toString()))
                .build();
    }

    /**
     * check exist
     *
     * @param code code only
     * @return true is exist/ otherwise
     */
    @GetMapping("/positions/exist/{code}")
    public ResponseEntity<Boolean> existPositionByCode(@PathVariable String code) {
        log.debug("REST request to get exist position : {}", code);
        return ResponseEntity.ok().body(service.existCode(code));
    }

    @GetMapping("/positions-history")
    public ResponseEntity<List<PositionHistoryDTO>> findAllHistoryPosition(@RequestParam MultiValueMap<String, String> queryParam) {
        log.debug("REST request to get all position history: {}", queryParam.get("name"));
        List<PositionHistoryDTO> list = service.getPositionHistory(queryParam);
        return ResponseEntity.ok().body(list);
    }


    @PostMapping("/positions/bulk-upload")
    public ResponseEntity<ResultExcel> bulkUploadPosition(@RequestParam("file") MultipartFile file) {
        log.debug("REST request upload Position");
        List<PositionDTO> positionDTOS;
        ResultExcel resultExcel = new ResultExcel();
        List<ErrorExcel> details = new ArrayList<>();
        try {
            positionDTOS = service.excelToPositions(file.getInputStream(), details);
            if (details.isEmpty()) {
                resultExcel.setSucess(true);
                if (!positionDTOS.isEmpty()) {
                    for (PositionDTO positionDTO : positionDTOS) {
                        if (positionDTO.getName() != null && positionDTO.getStatus() != null) {
                            String newPotionCode = service.generatePositionCode(positionDTO.getCode(), positionDTO.getName(), false);
                            positionDTO.setCode(newPotionCode);
                            service.save(positionDTO);
                        }
                    }
                }
            } else {
                resultExcel.setSucess(false);
            }
            resultExcel.setErrorExcels(details);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return ResponseEntity.ok().body(resultExcel);
    }

    @GetMapping("/public/positions/download/template-excel")
    public void downloadTemplateExcelPosition(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=" + "positon.xlsx");
        IOUtils.copy(storageService.downloadExcelTemplateFromResource("positon.xlsx"), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/positions/find-all")
    public ResponseEntity<List<PositionDTO>> getAll() {
        List<PositionDTO> results = service.findAllActiveStatus();
        return ResponseEntity.ok().body(results);
    }

}
