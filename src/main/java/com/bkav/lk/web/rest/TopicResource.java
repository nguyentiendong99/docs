package com.bkav.lk.web.rest;

import com.bkav.lk.dto.TopicCustomConfigDTO;
import com.bkav.lk.dto.TopicDTO;
import com.bkav.lk.service.TopicService;
import com.bkav.lk.service.storage.StorageService;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.StrUtil;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.bkav.lk.web.rest.util.HeaderUtil;
import com.bkav.lk.web.rest.util.PaginationUtil;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class TopicResource {

    private final Logger log = LoggerFactory.getLogger(TopicResource.class);

    private static final String ENTITY_NAME = "topic";

    private final TopicService topicService;

    private StorageService storageService;

    @Value("${spring.application.name}")
    private String applicationName;

    public TopicResource(TopicService topicService, StorageService storageService) {
        this.topicService = topicService;
        this.storageService = storageService;
    }

    /**
     * Lấy 1 bản ghi
     */
    @GetMapping("/topics/{id}")
    public ResponseEntity<TopicDTO> getTopic(@PathVariable Long id) {
        log.debug("REST request to get Topic : {}", id);
        Optional<TopicDTO> topicDTO = topicService.findOne(id);
        if (!topicDTO.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.of(topicDTO);
    }

    /**
     * Tìm kiếm
     */
    @GetMapping("/topics")
    public ResponseEntity<List<TopicDTO>> search(@RequestParam MultiValueMap<String, String> queryParams,
                                                 Pageable pageable) {
        log.debug("REST request to search for a page of Topics for query {}", queryParams);
        Page<TopicDTO> page = topicService.search(queryParams, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * Tạo mới 1 bản ghi
     */
    @PostMapping("/topics")
    public ResponseEntity<TopicDTO> create(@Valid @RequestBody TopicDTO topicDTO) throws URISyntaxException {
        log.debug("REST request to save Style : {}", topicDTO);
        if (topicDTO.getId() != null) {
            throw new BadRequestAlertException("A new topic cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TopicDTO result = topicService.save(topicDTO);
        return ResponseEntity.created(new URI("/api/topics/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    /**
     * Cập nhật 1 bản ghi
     */
    @PutMapping("/topics")
    public ResponseEntity<TopicDTO> update(@Valid @RequestBody TopicDTO topicDTO) {
        log.debug("REST request to update Topic : {}", topicDTO);
        if (topicDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        if(topicDTO.getStatus().equals(Constants.ENTITY_STATUS.DEACTIVATE) && !topicService.isDeactivable(topicDTO.getId())){
            throw new BadRequestAlertException("Chuyên khoa này đang được sử dụng", ENTITY_NAME, "topic.cant_deactivate");
        }

        Optional<TopicDTO> optional = topicService.findOne(topicDTO.getId());
        if (!optional.isPresent()) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        TopicDTO result = topicService.save(topicDTO);
        return ResponseEntity.ok().
                headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME,
                        topicDTO.getId().toString()))
                .body(result);
    }

    /**
     * Xóa 1 bản ghi
     */
    @DeleteMapping("/topics/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.debug("REST request to delete Topic : {}", id);
        topicService.delete(id.toString());
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true,
                ENTITY_NAME, id.toString())).build();
    }

    /**
     * Xóa nhiều bản ghi
     */
    @PostMapping("/topics/delete")
    public ResponseEntity<Void> deleteListTopic(@RequestBody Map<String, String> queryParams) {
        log.debug("REST request to delete List Topics : {}", queryParams);
        String ids = null;
        if (queryParams.containsKey("ids") && !StrUtil.isBlank(queryParams.get("ids"))) {
            ids = queryParams.get("ids");
        }
        if (StrUtil.isBlank(ids)) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        topicService.delete(ids);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy ra tất cả bản ghi
     */
    @GetMapping("topics/all")
    public ResponseEntity<List<TopicDTO>> getAll(@RequestParam(name = "status") Integer status) {
        log.debug("REST request to get all Topics by status ", status);
        List<TopicDTO> topicDTOList = topicService.findByStatus(status);
        return ResponseEntity.ok().body(topicDTOList);
    }

    @GetMapping("topics/list")
    public ResponseEntity<List<TopicDTO>> getAll() {
        List<TopicDTO> topicDTOList = topicService.findByStatus(Constants.BOOL_NUMBER.TRUE);
        return ResponseEntity.ok(topicDTOList);
    }

    @GetMapping("/public/topics/export-excel")
    public ResponseEntity<String> exportExcelSearch(@RequestParam MultiValueMap<String, String> queryParams,
                                                           Pageable pageable  ) throws IOException {
        queryParams.set("pageIsNull", null);
        List<TopicDTO> list = topicService.search(queryParams, pageable).getContent();
        String nameFile = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        try (InputStream is = new ClassPathResource("/excel_template/topic.xlsx").getInputStream()) {
            try (OutputStream os = new FileOutputStream(System.getProperty("java.io.tmpdir") + "/report_topic_" + nameFile + ".xlsx")) {
                Context context = new Context();
                context.putVar("lists", list);
                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        }
        return ResponseEntity.ok().body(nameFile);
    }
    @GetMapping("/public/topics/export-excel/download")
    public void exportExcelTopic(@RequestParam String nameFile, HttpServletResponse response) throws IOException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + "/report_topic_" + nameFile + ".xlsx");
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.addHeader("Content-disposition", "attachment;filename=report_topic_" + nameFile + ".xlsx");
        IOUtils.copy(new ByteArrayInputStream(FileUtils.readFileToByteArray(tempFile)), response.getOutputStream());
        response.flushBuffer();
    }

    @GetMapping("/topics/custom-config/{id}")
    public ResponseEntity<List<TopicCustomConfigDTO>> findAllCustomConfig(@PathVariable("id") Long id, @RequestHeader("healthFacilityId") Long healthFacilityId) {
        log.info("REST request to get list custom config by topic id input: {}", id);
        return ResponseEntity.ok().body(topicService.findAllCustomConfigByTopicId(id, healthFacilityId));
    }
}
