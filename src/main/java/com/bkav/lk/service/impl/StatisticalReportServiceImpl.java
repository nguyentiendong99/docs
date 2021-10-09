package com.bkav.lk.service.impl;

import com.bkav.lk.dto.FeedbackContentDTO;
import com.bkav.lk.dto.FeedbackDTO;
import com.bkav.lk.dto.TopicDTO;
import com.bkav.lk.repository.*;
import com.bkav.lk.service.StatisticalReportService;
import com.bkav.lk.service.mapper.*;
import com.bkav.lk.service.util.SimpleFileProvider;
import com.bkav.lk.util.Constants;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.itextpdf.text.DocumentException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StatisticalReportServiceImpl implements StatisticalReportService {

    private static final Logger log = LoggerFactory.getLogger(StatisticalReportService.class);

    private static final String REPORT_EXCEL_EXPORT_NAME = "reports.xlsx";
    private static final String REPORT_WORD_EXPORT_NAME = "reports.docx";
    private static final String REPORT_PDF_EXPORT_NAME = "reports.pdf";
    private static final String REPORT_XML_EXPORT_NAME = "reports.xml";
    private static final String REPORT_BANNER = "Báo cáo thống kê";

    private final SimpleFileProvider simpleFileProvider;
    private final DoctorAppointmentRepository doctorAppointmentRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final PatientRecordRepository patientRecordRepository;
    private final SubclinicalRepository subclinicalRepository;
    private final FeedbackRepository feedbackRepository;
    private final DoctorFeedbackRepository doctorFeedbackRepository;
    private final FeedbackContentRepository feedbackContentRepository;
    private final TopicRepository topicRepository;
    private final DoctorAppointmentMapper doctorAppointmentMapper;
    private final DoctorScheduleMapper doctorScheduleMapper;
    private final PatientRecordMapper patientRecordMapper;
    private final SubclinicalMapper subclinicalMapper;
    private final FeedbackMapper feedbackMapper;
    private final DoctorFeedBackMapper doctorFeedBackMapper;
    private final FeedbackContentMapper feedbackContentMapper;
    private final TopicMapper topicMapper;

    @Autowired
    public StatisticalReportServiceImpl(
            SimpleFileProvider simpleFileProvider,
            DoctorAppointmentRepository doctorAppointmentRepository,
            DoctorScheduleRepository doctorScheduleRepository,
            PatientRecordRepository patientRecordRepository,
            SubclinicalRepository subclinicalRepository,
            FeedbackRepository feedbackRepository,
            DoctorFeedbackRepository doctorFeedbackRepository,
            FeedbackContentRepository feedbackContentRepository,
            TopicRepository topicRepository,
            DoctorAppointmentMapper doctorAppointmentMapper,
            DoctorScheduleMapper doctorScheduleMapper,
            PatientRecordMapper patientRecordMapper,
            SubclinicalMapper subclinicalMapper,
            FeedbackMapper feedbackMapper,
            DoctorFeedBackMapper doctorFeedBackMapper,
            FeedbackContentMapper feedbackContentMapper,
            TopicMapper topicMapper) {
        this.simpleFileProvider = simpleFileProvider;
        this.doctorAppointmentRepository = doctorAppointmentRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.patientRecordRepository = patientRecordRepository;
        this.subclinicalRepository = subclinicalRepository;
        this.feedbackRepository = feedbackRepository;
        this.doctorFeedbackRepository = doctorFeedbackRepository;
        this.feedbackContentRepository = feedbackContentRepository;
        this.topicRepository = topicRepository;
        this.doctorAppointmentMapper = doctorAppointmentMapper;
        this.doctorScheduleMapper = doctorScheduleMapper;
        this.patientRecordMapper = patientRecordMapper;
        this.subclinicalMapper = subclinicalMapper;
        this.feedbackMapper = feedbackMapper;
        this.doctorFeedBackMapper = doctorFeedBackMapper;
        this.feedbackContentMapper = feedbackContentMapper;
        this.topicMapper = topicMapper;
    }

    @Override
    public List<?> search(MultiValueMap<String, String> queryParams) {
        Integer contentType = null;
        Integer status = null;
        Long healthFacilityId = null;
        if (queryParams.containsKey("contentType") && StringUtils.isNotBlank(queryParams.getFirst("contentType"))) {
            contentType = Integer.parseInt(queryParams.getFirst("contentType").trim());
        } else {
            throw new BadRequestAlertException("You're not choose content type of report", null, "statistical_report.null_content_type");
        }

        if (queryParams.containsKey("healthFacilityId") && StringUtils.isNotBlank(queryParams.getFirst("healthFacilityId"))) {
            try {
                healthFacilityId = Long.parseLong(queryParams.getFirst("healthFacilityId").trim());
                if (healthFacilityId.equals(0L)) {
                    queryParams.remove("healthFacilityId");
                }
            } catch (NumberFormatException e) {
                queryParams.remove("healthFacilityId");
            }
        }

        if (queryParams.containsKey("status") && StringUtils.isNotBlank(queryParams.getFirst("status"))) {
            try {
                status = Integer.parseInt(queryParams.getFirst("status").trim());
                if (status.equals(0)) {
                    queryParams.remove("status");
                }
            } catch (NumberFormatException e) {
                queryParams.remove("status");
            }
        }

        List<?> contents = null;
        if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(contentType)) {
            contents = doctorAppointmentMapper.toDto(doctorAppointmentRepository.search(queryParams, null));
        } else if (Constants.CONTENT_TYPE.DOCTOR_SCHEDULE.equals(contentType)) {
            if (queryParams.containsKey("startDate") && StringUtils.isNotBlank( queryParams.getFirst("startDate"))) {
                queryParams.set("fromDate", queryParams.getFirst("startDate"));
            }
            if (queryParams.containsKey("endDate") && StringUtils.isNotBlank( queryParams.getFirst("endDate"))) {
                queryParams.set("toDate", queryParams.getFirst("endDate"));
            }
            contents = doctorScheduleMapper.toDto(doctorScheduleRepository.search(queryParams, null));
        } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(contentType)) {
            queryParams.set("statisticalReport", "true");
            contents = patientRecordMapper.toDto(patientRecordRepository.search(queryParams, null));
        } else if (Constants.CONTENT_TYPE.SUBCLINICAL_RESULT.equals(contentType)) {
            contents = subclinicalMapper.toDto(subclinicalRepository.search(queryParams, null));
        } else if (Constants.CONTENT_TYPE.RE_EXAMINATION.equals(contentType)) {
            queryParams.set("status", String.valueOf(3));
            contents = doctorAppointmentMapper.toDto(doctorAppointmentRepository.search(queryParams, null));
        } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(contentType)) {
            if (queryParams.containsKey("startDate") && StringUtils.isNotBlank( queryParams.getFirst("startDate"))) {
                queryParams.set("startTime", queryParams.getFirst("startDate").replace("-","/"));
            }
            if (queryParams.containsKey("endDate") && StringUtils.isNotBlank( queryParams.getFirst("endDate"))) {
                queryParams.set("endTime", queryParams.getFirst("endDate").replace("-","/"));
            }
            contents = feedbackMapper.toDto(feedbackRepository.search(queryParams, null));
            contents.forEach(item -> {
                List<FeedbackContentDTO> feedbackContentDTOList = feedbackContentMapper.toDto(feedbackContentRepository.findByFeedbackId(((FeedbackDTO) item).getId()));
                ((FeedbackDTO) item).setFeedbackContentDTOList(feedbackContentDTOList);
                Optional<TopicDTO> topicDTO = topicRepository.findById(((FeedbackDTO) item).getId()).map(topicMapper::toDto);
                ((FeedbackDTO) item).setTopicName(topicDTO.isPresent() ? topicDTO.get().getName() : null);
            });
        } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(contentType)) {
            queryParams.set("statisticalReport", "true");
            contents = doctorFeedBackMapper.toDto(doctorFeedbackRepository.search(queryParams, null));
        } else {
            throw new BadRequestAlertException("You're choose unsupported content type of report", null, "statistical_report.unsupported_content_type");
        }
        return contents;
    }

    @Override
    public <T> Map<String, byte[]> generateExportData(List<T> reportContents, String[] fileFormats, Integer contentType) {
        Map<String, byte[]> fileDataMap = new HashMap<>();
        for (String format : fileFormats) {
            if (Constants.FILE_FORMAT.WORD.equals(Integer.parseInt(format.trim()))) {
                fileDataMap.put(this.REPORT_WORD_EXPORT_NAME, this.exportToWordFile(reportContents, contentType));
            }
            if (Constants.FILE_FORMAT.EXCEL.equals(Integer.parseInt(format.trim()))) {
                fileDataMap.put(this.REPORT_EXCEL_EXPORT_NAME, this.exportToExcelFile(reportContents, contentType));
            }
            if (Constants.FILE_FORMAT.PDF.equals(Integer.parseInt(format.trim()))) {
                fileDataMap.put(this.REPORT_PDF_EXPORT_NAME, this.exportToPdfFile(reportContents, contentType));
            }
            if (Constants.FILE_FORMAT.XML.equals(Integer.parseInt(format.trim()))) {
                fileDataMap.put(this.REPORT_XML_EXPORT_NAME, this.exportToXmlFile(reportContents, contentType));
            }
        }
        return fileDataMap;
    }

    @Override
    public void storeZipData(Map<String, byte[]> inputs, OutputStream outputStream) {
        try {
            simpleFileProvider.storeZipData(inputs, outputStream);
        } catch (IOException e) {
            log.error("Error: ", e);
        }
    }

    private <T> byte[] exportToExcelFile(List<T> contents, Integer contentType) {
        byte[] bytes = null;
        try {
            bytes = simpleFileProvider.generateExcel(this.REPORT_BANNER, contents, contentType);
        } catch (IOException e) {
            log.error("Error: ", e);
        }
        return bytes;
    }

    private <T> byte[] exportToWordFile(List<T> contents, Integer contentType) {
        byte[] bytes = null;
        try {
            bytes = simpleFileProvider.generateWord(this.REPORT_BANNER, contents, contentType);
        } catch (IOException e) {
            log.error("Error: ", e);
        }
        return bytes;
    }

    private <T> byte[] exportToPdfFile(List<T> contents, Integer contentType) {
        byte[] bytes = null;
        try {
            bytes = simpleFileProvider.generatePdf(this.REPORT_BANNER, contents, contentType);
        } catch (IOException | DocumentException e) {
            log.error("Error: ", e);
        }
        return bytes;
    }

    private <T> byte[] exportToXmlFile(List<T> contents, Integer contentType) {
        byte[] bytes = null;
        try {
            bytes = simpleFileProvider.generateXml(this.REPORT_BANNER, contents, contentType);
        } catch (JAXBException | IOException e) {
            log.error("Error: ", e);
        }
        return bytes;
    }
}
