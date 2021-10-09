package com.bkav.lk.web.rest;

import com.bkav.lk.service.StatisticalReportService;
import com.bkav.lk.web.errors.BadRequestAlertException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatisticalReportController {

    private final Logger log = LoggerFactory.getLogger(StatisticalReportController.class);

    private static final String REPORT_ZIP_EXPORT_NAME = "reports.zip";

    private final StatisticalReportService statisticalReportService;

    @Autowired
    public StatisticalReportController(StatisticalReportService statisticalReportService) {
        this.statisticalReportService = statisticalReportService;
    }

    @GetMapping("/statistical-reports")
    public ResponseEntity<List<?>> search(@RequestParam MultiValueMap<String, String> queryParams) {
        log.info("REST request for search all report equal with conditions: {}", queryParams);
        List<?> results = statisticalReportService.search(queryParams);
        return ResponseEntity.ok().body(results);
    }

    @GetMapping("/public/statistical-reports")
    public ResponseEntity<Void> exportFile(@RequestParam MultiValueMap<String, String> queryParams, HttpServletResponse response) throws IOException {
        String[] fileFormats = null;
        if (queryParams.containsKey("fileFormat")) {
            fileFormats = StringUtils.split(queryParams.get("fileFormat").get(0), ",");
        }
        if (fileFormats.length > 0) {
            Integer contentType = null;
            if (queryParams.containsKey("contentType") && StringUtils.isNotBlank(queryParams.getFirst("contentType"))) {
                contentType = Integer.parseInt(queryParams.getFirst("contentType").trim());
            } else {
                throw new BadRequestAlertException("You're not choose content type of report", null, "statistical_report.null_content_type");
            }

            Map<String, byte[]> fileDataMap = statisticalReportService.generateExportData(
                    statisticalReportService.search(queryParams), fileFormats, contentType);

            if (fileDataMap.size() > 0) {
                if (fileDataMap.size() == 1) {
                    Map.Entry<String, byte[]> entry = fileDataMap.entrySet().iterator().next();
                    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    response.addHeader("Content-disposition", "attachment; filename=" + entry.getKey());
                    IOUtils.copy(new ByteArrayInputStream(entry.getValue()), response.getOutputStream());
                    response.flushBuffer();
                } else {
                    response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
                    response.addHeader("Content-disposition", "attachment; filename=" + this.REPORT_ZIP_EXPORT_NAME);
                    statisticalReportService.storeZipData(fileDataMap, response.getOutputStream());
                    response.flushBuffer();
                }
            }
        }
        return ResponseEntity.noContent().build();
    }

}
