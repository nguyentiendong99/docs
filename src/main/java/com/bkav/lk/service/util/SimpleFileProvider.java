package com.bkav.lk.service.util;

import com.bkav.lk.dto.*;
import com.bkav.lk.util.Constants;
import com.bkav.lk.util.DateUtils;
import com.bkav.lk.web.errors.BadRequestAlertException;
import com.itextpdf.text.Font;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.itextpdf.text.Paragraph.ALIGN_CENTER;

@Component
public class SimpleFileProvider {

    private static final Integer DEFAULT_COLUMN_SIZE = 6000;
    private static final Float DEFAULT_PDF_COLUMN_SIZE = 4.0f;

    private static final String[] DOCTOR_APPOINTMENT_HEADERS =
            {"TT", "Mã đặt lịch", "Họ tên bệnh nhân", "Ngày khám", "Thời gian khám", "Bác sĩ", "Lý do", "Trạng thái"};

    private static final String[] DOCTOR_SCHEDULES_HEADERS =
            {"TT", "Mã bác sĩ", "Họ tên bác sĩ", "Phòng khám", "Lịch làm việc"};

    private static final String[] PATIENT_RECORD_HEADERS =
            {"TT", "Mã HSSK", "Họ tên bệnh nhân", "Giới tính", "Tuổi", "Địa chỉ", "Phường/xã", "Quận/huyện", "Số điện thoại"};

    private static final String[] SUBCLINICAL_HEADERS =
            {"TT", "Mã lần khám", "Mã bệnh nhân", "Họ tên bệnh nhân", "Mã DV-CLS", "Tên DV-CLS", "Ký thuật viên", "Phòng thực hiện", "Kết quả"};

    private static final String[] RE_EXAMINATION_HEADERS = {"TT", "Mã lần khám", "Mã bệnh nhân", "Họ tên bệnh nhân", "Ngày tái khám", "Bác sĩ khám"};

    private static final String[] FEEDBACK_HEADERS =
            {"TT", "Tiêu đề", "Nội dung góp ý", "Ngày góp ý", "Đơn vị bị góp ý", "Đơn vị xử lý", "Người xử lý", "Trạng thái"};

    private static final String[] DOCTOR_FEEDBACK_HEADERS =
            {"TT", "Người đánh giá", "Bác sĩ được đánh giá", "Đơn vị", "Đánh giá", "Nội dung đánh giá", "Thời gian đánh giá", "Trạng thái"};

    public List<CellData> prepareRowDataListByType(Integer contentType) {
        List<CellData> cellDataList = null;
        if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(contentType)) {
            cellDataList = this.prepareHeaderRowDataList(this.DOCTOR_APPOINTMENT_HEADERS);
        } else if (Constants.CONTENT_TYPE.DOCTOR_SCHEDULE.equals(contentType)) {
            cellDataList = this.prepareHeaderRowDataList(this.DOCTOR_SCHEDULES_HEADERS);
        } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(contentType)) {
            cellDataList = this.prepareHeaderRowDataList(this.PATIENT_RECORD_HEADERS);
        } else if (Constants.CONTENT_TYPE.SUBCLINICAL_RESULT.equals(contentType)) {
            cellDataList = this.prepareHeaderRowDataList(this.SUBCLINICAL_HEADERS);
        } else if (Constants.CONTENT_TYPE.RE_EXAMINATION.equals(contentType)) {
            cellDataList = this.prepareHeaderRowDataList(this.RE_EXAMINATION_HEADERS);
        } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(contentType)) {
            cellDataList = this.prepareHeaderRowDataList(this.FEEDBACK_HEADERS);
        } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(contentType)) {
            cellDataList = this.prepareHeaderRowDataList(this.DOCTOR_FEEDBACK_HEADERS);
        } else {
            throw new BadRequestAlertException("You're choose unsupported content type of report", null, "statistical_report.unsupported_content_type");
        }
        return cellDataList;
    }

    public List<CellData> prepareHeaderRowDataList(String... columnNames) {
        List<CellData> cellDataList = new ArrayList<>();
        CellData cellData = null;
        if (columnNames.length > 0) {
            for (int i = 0; i < columnNames.length; i++) {
                cellData = new CellData();
                cellData.setIndex(i);
                cellData.setWidth(DEFAULT_COLUMN_SIZE);
                cellData.setName(columnNames[i]);
                cellDataList.add(cellData);
            }
        }
        return cellDataList;
    }

    public void storeZipData(Map<String, byte[]> inputs, OutputStream out) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(out);
             ZipOutputStream zos = new ZipOutputStream(bos)) {
            for (Map.Entry<String, byte[]> entry : inputs.entrySet()) {
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                zipEntry.setSize(entry.getValue().length);
                zos.putNextEntry(zipEntry);
                zos.write(entry.getValue());
            }
            zos.closeEntry();
        }
    }

    public <T> byte[] generateExcel(String sheetName, List<T> dataList, Integer contentType) throws IOException {
        List<CellData> cellDataList = this.prepareRowDataListByType(contentType);
        byte[] bytes = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Workbook workbook = new XSSFWorkbook();) {
            CellStyle defaultHeaderStyle = null;
            XSSFFont defaultHeaderFont = null;
            CellStyle defaultBodyStyle = null;
            Cell headerCell = null;

            //Prepare sheet for excel
            Sheet sheet = workbook.createSheet(sheetName);
            for (CellData cellData : cellDataList) {
                sheet.setColumnWidth(cellData.getIndex(), cellData.getWidth());
            }

            //Create header for excel
            Row header = sheet.createRow(0);
            defaultHeaderStyle = workbook.createCellStyle();
            defaultHeaderStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            defaultHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            defaultHeaderFont = ((XSSFWorkbook) workbook).createFont();
            defaultHeaderFont.setFontName("Arial");
            defaultHeaderFont.setFontHeightInPoints((short) 16);
            defaultHeaderFont.setColor(IndexedColors.WHITE.getIndex());
            defaultHeaderFont.setBold(true);

            defaultHeaderStyle.setFont(defaultHeaderFont);

            for (CellData cellData : cellDataList) {
                headerCell = header.createCell(cellData.getIndex());
                headerCell.setCellValue(cellData.getName());
                headerCell.setCellStyle(defaultHeaderStyle);
            }

            //Create body for excel
            defaultBodyStyle = workbook.createCellStyle();
            defaultBodyStyle.setWrapText(true);

            if (!CollectionUtils.isEmpty(dataList)) {
                if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(contentType)) {
                    this.createCellForDoctorAppointment(sheet, cellDataList, dataList, defaultBodyStyle);
                } else if (Constants.CONTENT_TYPE.DOCTOR_SCHEDULE.equals(contentType)) {
                    this.createCellForDoctorSchedule(sheet, cellDataList, dataList, defaultBodyStyle);
                } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(contentType)) {
                    this.createCellForPatientRecord(sheet, cellDataList, dataList, defaultBodyStyle);
                } else if (Constants.CONTENT_TYPE.SUBCLINICAL_RESULT.equals(contentType)) {
                    this.createCellForSubclinicalResult(sheet, cellDataList, dataList, defaultBodyStyle);
                } else if (Constants.CONTENT_TYPE.RE_EXAMINATION.equals(contentType)) {
                    this.createCellForReExamination(sheet, cellDataList, dataList, defaultBodyStyle);
                } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(contentType)) {
                    this.createCellForFeedback(sheet, cellDataList, dataList, defaultBodyStyle);
                } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(contentType)) {
                    this.createCellForDoctorFeedback(sheet, cellDataList, dataList, defaultBodyStyle);
                } else {
                    throw new BadRequestAlertException("You're choose unsupported content type of report", null, "statistical_report.unsupported_content_type");
                }
            }
            workbook.write(bos);
            bytes = bos.toByteArray();
        }
        return bytes;
    }

    public <T> byte[] generateWord(String titleName, List<T> dataList, Integer contentType) throws IOException {
        List<CellData> prepareRowDataList = this.prepareRowDataListByType(contentType);
        byte[] bytes = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph headerCell = null;
            XWPFRun headerCellRun = null;

            //prepare title
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun titleRun = title.createRun();
            titleRun.setText(titleName);
            titleRun.setColor("009933");
            titleRun.setBold(true);
            titleRun.setFontFamily("Arial");
            titleRun.setFontSize(16);

            XWPFTable table = document.createTable(dataList.size() + 1, prepareRowDataList.size());

            //Create header for word
            for (CellData cellData : prepareRowDataList) {
                headerCell = table.getRow(0).getCell(cellData.getIndex()).getParagraphs().get(0);
                headerCell.setAlignment(ParagraphAlignment.CENTER);
                headerCell.setWordWrapped(true);

                headerCellRun = headerCell.createRun();
                headerCellRun.setBold(true);
                headerCellRun.setText(cellData.getName());
            }

            //Create body for word
            if (!CollectionUtils.isEmpty(dataList)) {
                if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(contentType)) {
                    this.generateWordForDoctorAppointment(table, prepareRowDataList, dataList);
                } else if (Constants.CONTENT_TYPE.DOCTOR_SCHEDULE.equals(contentType)) {
                    this.generateWordForDoctorSchedule(table, prepareRowDataList, dataList);
                } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(contentType)) {
                    this.generateWordForPatientRecord(table, prepareRowDataList, dataList);
                } else if (Constants.CONTENT_TYPE.SUBCLINICAL_RESULT.equals(contentType)) {
                    this.generateWordForSubclinicalResult(table, prepareRowDataList, dataList);
                } else if (Constants.CONTENT_TYPE.RE_EXAMINATION.equals(contentType)) {
                    this.generateWordForReExamination(table, prepareRowDataList, dataList);
                } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(contentType)) {
                    this.generateWordForFeedback(table, prepareRowDataList, dataList);
                } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(contentType)) {
                    this.generateWordForDoctorFeedback(table, prepareRowDataList, dataList);
                } else {
                    throw new BadRequestAlertException("not supported type!", null, "type_notsupported");
                }
            }
            document.write(bos);
            bytes = bos.toByteArray();
        }
        return bytes;
    }

    public <T> byte[] generatePdf(String titleName, List<T> dataList, Integer contentType) throws IOException, DocumentException {
        List<CellData> prepareRowDataList = this.prepareRowDataListByType(contentType);
        byte[] bytes = null;
        try (ByteArrayOutputStream daos = new ByteArrayOutputStream()) {
            Resource fontFile = new ClassPathResource("fonts/vuArial.ttf");
            Resource fontHeaderFile = new ClassPathResource("fonts/vuArialBold.ttf");

            com.itextpdf.text.Document document = new com.itextpdf.text.Document(PageSize.A4);
            com.itextpdf.text.Font titleFont = null;
            com.itextpdf.text.Font headerFont = null;
            com.itextpdf.text.Font bodyFont = null;

            PdfWriter.getInstance(document, daos);
            BaseFont fontHeader = BaseFont.createFont("vuArial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, IOUtils.toByteArray(fontFile.getInputStream()), null);
            BaseFont fontBody = BaseFont.createFont("vuArialBold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, IOUtils.toByteArray(fontHeaderFile.getInputStream()), null);

            document.open();
            titleFont = new Font(fontHeader, 15);
            titleFont.setColor(BaseColor.BLUE);
            headerFont = new Font(fontBody, 14);
            headerFont.setColor(BaseColor.WHITE);
            bodyFont = new Font(fontBody, 14);
            bodyFont.setColor(BaseColor.BLACK);

            Paragraph paragraph = new Paragraph(titleName, titleFont);
            paragraph.setAlignment(ALIGN_CENTER);
            document.add(paragraph);

            PdfPTable table = new PdfPTable(prepareRowDataList.size());
            table.setWidthPercentage(100f);
            table.setWidths(this.preparePdfColumnsWidthDefault(prepareRowDataList.size()));
            table.setSpacingBefore(10);
            if (!CollectionUtils.isEmpty(dataList)) {
                writeTableHeader(table, prepareRowDataList, headerFont);
                writeTableData(table, prepareRowDataList, dataList, bodyFont, contentType);
                document.add(table);
            }

            document.close();
            bytes = daos.toByteArray();
        }
        return bytes;
    }

    public <T> byte[] generateXml(String titleName, List<T> dataList, Integer contentType) throws JAXBException, IOException {
        JAXBContext context = null;
        Marshaller marshaller = null;
        byte[] bytes = null;
        if (!CollectionUtils.isEmpty(dataList)) {
            try (ByteArrayOutputStream daos = new ByteArrayOutputStream()) {
                if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(contentType)) {
                    context = JAXBContext.newInstance(DoctorAppointmentDTO[].class);
                    JAXBElement<DoctorAppointmentDTO[]> root = new JAXBElement<>(new QName("DOCTOR_APPOINTMENT_LIST"),
                            DoctorAppointmentDTO[].class, dataList.toArray(new DoctorAppointmentDTO[dataList.size()]));
                    marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    marshaller.marshal(root, daos);
                } else if (Constants.CONTENT_TYPE.DOCTOR_SCHEDULE.equals(contentType)) {
                    context = JAXBContext.newInstance(DoctorScheduleDTO[].class);
                    JAXBElement<DoctorScheduleDTO[]> root = new JAXBElement<>(new QName("DOCTOR_SCHEDULE_LIST"),
                            DoctorScheduleDTO[].class, dataList.toArray(new DoctorScheduleDTO[dataList.size()]));
                    marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    marshaller.marshal(root, daos);
                } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(contentType)) {
                    context = JAXBContext.newInstance(PatientRecordDTO[].class);
                    JAXBElement<PatientRecordDTO[]> root = new JAXBElement<>(new QName("PATIENT_RECORD_LIST"),
                            PatientRecordDTO[].class, dataList.toArray(new PatientRecordDTO[dataList.size()]));
                    marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    marshaller.marshal(root, daos);
                } else if (Constants.CONTENT_TYPE.SUBCLINICAL_RESULT.equals(contentType)) {
                    context = JAXBContext.newInstance(SubclinicalDTO[].class);
                    JAXBElement<SubclinicalDTO[]> root = new JAXBElement<>(new QName("SUBCLINICAL_RESULT_LIST"),
                            SubclinicalDTO[].class, dataList.toArray(new SubclinicalDTO[dataList.size()]));
                    marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    marshaller.marshal(root, daos);
                } else if (Constants.CONTENT_TYPE.RE_EXAMINATION.equals(contentType)) {
                    context = JAXBContext.newInstance(DoctorAppointmentDTO[].class);
                    JAXBElement<DoctorAppointmentDTO[]> root = new JAXBElement<>(new QName("RE_EXAMINATION_LIST"),
                            DoctorAppointmentDTO[].class, dataList.toArray(new DoctorAppointmentDTO[dataList.size()]));
                    marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    marshaller.marshal(root, daos);
                } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(contentType)) {
                    context = JAXBContext.newInstance(FeedbackDTO[].class);
                    JAXBElement<FeedbackDTO[]> root = new JAXBElement<>(new QName("FEEDBACK_LIST"),
                            FeedbackDTO[].class, dataList.toArray(new FeedbackDTO[dataList.size()]));
                    marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    marshaller.marshal(root, daos);
                } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(contentType)) {
                    context = JAXBContext.newInstance(DoctorFeedbackDTO[].class);
                    JAXBElement<DoctorFeedbackDTO[]> root = new JAXBElement<>(new QName("DOCTOR_FEEDBACK_LIST"),
                            DoctorFeedbackDTO[].class, dataList.toArray(new DoctorFeedbackDTO[dataList.size()]));
                    marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                    marshaller.marshal(root, daos);
                } else {
                    throw new BadRequestAlertException("not supported type!", null, "type_notsupported");
                }

                if (daos.size() > 0) {
                    bytes = daos.toByteArray();
                }
            }
        }
        return bytes;
    }

    private void writeTableHeader(PdfPTable table, List<CellData> prepareRowDataList, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(BaseColor.BLUE);
        cell.setPadding(5);

        for (CellData cellData : prepareRowDataList) {
            cell.setPhrase(new Phrase(cellData.getName(), font));
            table.addCell(cell);
        }
    }

    private <T> void writeTableData(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font, Integer contentType) {
        if (Constants.CONTENT_TYPE.DOCTOR_APPOINTMENT.equals(contentType)) {
            this.generatePdfTableForDoctorAppointment(table, prepareRowDataList, dataList, font);
        } else if (Constants.CONTENT_TYPE.DOCTOR_SCHEDULE.equals(contentType)) {
            this.generatePdfTableForDoctorSchedule(table, prepareRowDataList, dataList, font);
        } else if (Constants.CONTENT_TYPE.PATIENT_RECORD.equals(contentType)) {
            this.generatePdfTableForPatientRecord(table, prepareRowDataList, dataList, font);
        } else if (Constants.CONTENT_TYPE.SUBCLINICAL_RESULT.equals(contentType)) {
            this.generatePdfTableForSubclinicalResult(table, prepareRowDataList, dataList, font);
        } else if (Constants.CONTENT_TYPE.RE_EXAMINATION.equals(contentType)) {
            this.generatePdfTableForReExamination(table, prepareRowDataList, dataList, font);
        } else if (Constants.CONTENT_TYPE.FEEDBACK.equals(contentType)) {
            this.generatePdfTableForFeedback(table, prepareRowDataList, dataList, font);
        } else if (Constants.CONTENT_TYPE.DOCTOR_FEEDBACK.equals(contentType)) {
            this.generatePdfTableForDoctorFeedback(table, prepareRowDataList, dataList, font);
        } else {
            throw new BadRequestAlertException("not supported type!", null, "type_notsupported");
        }
    }

    private <T> void generatePdfTableForDoctorAppointment(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font) {
        DoctorAppointmentDTO doctorAppointmentDTO = null;
        Constants.DOCTOR_APPOINTMENT_STATUS_LIST appointment = null;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorAppointmentDTO = (DoctorAppointmentDTO) data;
            for (CellData cellData : prepareRowDataList) {
                switch (cellData.getIndex()) {
                    case 0:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), font)));
                        break;
                    case 1:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getBookingCode(), font)));
                        break;
                    case 2:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getPatientName(), font)));
                        break;
                    case 3:
                        table.addCell(new PdfPCell(new Phrase(
                                DateUtils.formatInstantAsString(doctorAppointmentDTO.getStartTime(),
                                        DateUtils.NORM_3_DATE_PATTERN), font)));
                        break;
                    case 4:
                        table.addCell(new PdfPCell(new Phrase(
                                DateUtils.formatInstantAsString(doctorAppointmentDTO.getStartTime(),
                                        DateUtils.NORM_2_TIME_PATTERN) + " - "
                                        + DateUtils.formatInstantAsString(doctorAppointmentDTO.getEndTime(),
                                        DateUtils.NORM_2_TIME_PATTERN), font)));
                        break;
                    case 5:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getDoctorName(), font)));
                        break;
                    case 6:
                        table.addCell(new PdfPCell(
                                new Phrase(doctorAppointmentDTO.getMedicalReason(), font)));
                        break;
                    case 7:
                        appointment = Constants.DOCTOR_APPOINTMENT_STATUS_LIST.getById(doctorAppointmentDTO.getStatus());
                        table.addCell(new PdfPCell(new Phrase(
                                Objects.nonNull(appointment) ? StringUtils.upperCase(appointment.getText()) : null, font)));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void generatePdfTableForDoctorSchedule(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font) {
        DoctorScheduleDTO doctorScheduleDTO = null;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorScheduleDTO = (DoctorScheduleDTO) data;
            for (CellData cellData : prepareRowDataList) {
                switch (cellData.getIndex()) {
                    case 0:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), font)));
                        break;
                    case 1:
                        table.addCell(new PdfPCell(new Phrase(doctorScheduleDTO.getDoctorCode(), font)));
                        break;
                    case 2:
                        table.addCell(new PdfPCell(new Phrase(doctorScheduleDTO.getDoctorName(), font)));
                        break;
                    case 3:
                        table.addCell(new PdfPCell(new Phrase(doctorScheduleDTO.getClinicName(), font)));
                        break;
                    case 4:
                        table.addCell(Utils.getWorkingTime(doctorScheduleDTO.getWorkingTime()));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void generatePdfTableForPatientRecord(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font) {
        PatientRecordDTO patientRecordDTO = null;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            patientRecordDTO = (PatientRecordDTO) data;
            for (CellData cellData : prepareRowDataList) {
                switch (cellData.getIndex()) {
                    case 0:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), font)));
                        break;
                    case 1:
                        table.addCell(new PdfPCell(new Phrase(patientRecordDTO.getPatientRecordCode(), font)));
                        break;
                    case 2:
                        table.addCell(new PdfPCell(new Phrase(patientRecordDTO.getName(), font)));
                        break;
                    case 3:
                        table.addCell(new PdfPCell(new Phrase(
                                Utils.getGenderName(patientRecordDTO.getGender()), font)));
                        break;
                    case 4:
                        table.addCell(new PdfPCell(new Phrase(
                                String.valueOf(Utils.getAge(patientRecordDTO.getDob())), font)));
                        break;
                    case 5:
                        table.addCell(new PdfPCell(new Phrase(patientRecordDTO.getAddress(), font)));
                        break;
                    case 6:
                        table.addCell(new PdfPCell(
                                new Phrase(patientRecordDTO.getWardName(), font)));
                        break;
                    case 7:
                        table.addCell(new PdfPCell(new Phrase(patientRecordDTO.getDistrictName(), font)));
                        break;
                    case 8:
                        table.addCell(new PdfPCell(new Phrase(patientRecordDTO.getPhone(), font)));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void generatePdfTableForSubclinicalResult(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font) {
        SubclinicalDTO subclinicalDTO = null;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            subclinicalDTO = (SubclinicalDTO) data;
            for (CellData cellData : prepareRowDataList) {
                switch (cellData.getIndex()) {
                    case 0:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), font)));
                        break;
                    case 1:
                        table.addCell(new PdfPCell(new Phrase(subclinicalDTO.getDoctorAppointmentCode(), font)));
                        break;
                    case 2:
                        table.addCell(new PdfPCell(new Phrase(subclinicalDTO.getPatientRecordCode(), font)));
                        break;
                    case 3:
                        table.addCell(new PdfPCell(new Phrase(subclinicalDTO.getPatientRecordName(), font)));
                        break;
                    case 4:
                        table.addCell(new PdfPCell(new Phrase(subclinicalDTO.getTechnician(), font)));
                        break;
                    case 5:
                        table.addCell(new PdfPCell(new Phrase(subclinicalDTO.getRoom(), font)));
                        break;
                    case 6:
                        table.addCell(new PdfPCell(new Phrase(
                                Utils.getNotificationStatus(subclinicalDTO.getStatus()), font)));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void generatePdfTableForReExamination(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font) {
        DoctorAppointmentDTO doctorAppointmentDTO = null;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorAppointmentDTO = (DoctorAppointmentDTO) data;
            for (CellData cellData : prepareRowDataList) {
                switch (cellData.getIndex()) {
                    case 0:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), font)));
                        break;
                    case 1:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getAppointmentCode(), font)));
                        break;
                    case 2:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getPatientCode(), font)));
                        break;
                    case 3:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getPatientName(), font)));
                        break;
                    case 4:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getReExaminationDate() != null
                                ? DateUtils.convertFromInstantToString(doctorAppointmentDTO.getReExaminationDate()) : "null", font)));
                        break;
                    case 5:
                        table.addCell(new PdfPCell(new Phrase(doctorAppointmentDTO.getDoctorName(), font)));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void generatePdfTableForFeedback(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font) {
        FeedbackDTO feedbackDTO = null;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            feedbackDTO = (FeedbackDTO) data;
            for (CellData cellData : prepareRowDataList) {
                switch (cellData.getIndex()) {
                    case 0:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), font)));
                        break;
                    case 1:
                        table.addCell(new PdfPCell(new Phrase(feedbackDTO.getTopicName(), font)));
                        break;
                    case 2:
                        table.addCell(new PdfPCell(new Phrase(feedbackDTO.getContent(), font)));
                        break;
                    case 3:
                        table.addCell(new PdfPCell(new Phrase(
                                DateUtils.formatInstantAsString(feedbackDTO.getCreatedDate(),
                                        DateUtils.NORM_3_DATETIME_PATTERN), font)));
                        break;
                    case 4:
                        table.addCell(new PdfPCell(new Phrase(feedbackDTO.getFeedbackedUnitName(), font)));
                        break;
                    case 5:
                        table.addCell(new PdfPCell(new Phrase(feedbackDTO.getProcessingUnitName(), font)));
                        break;
                    case 6:
                        table.addCell(new PdfPCell(new Phrase(feedbackDTO.getProcessedBy(), font)));
                        break;
                    case 7:
                        table.addCell(new PdfPCell(new Phrase(Utils.getFeedbackStatusName(feedbackDTO.getStatus()), font)));
                        break;
                    default:
                }
            }
        }
    }


    private <T> void generatePdfTableForDoctorFeedback(PdfPTable table, List<CellData> prepareRowDataList, List<T> dataList, Font font) {
        DoctorFeedbackDTO doctorFeedbackDTO = null;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorFeedbackDTO = (DoctorFeedbackDTO) data;
            for (CellData cellData : prepareRowDataList) {
                switch (cellData.getIndex()) {
                    case 0:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(i + 1), font)));
                        break;
                    case 1:
                        table.addCell(new PdfPCell(new Phrase(doctorFeedbackDTO.getUserName(), font)));
                        break;
                    case 2:
                        table.addCell(new PdfPCell(new Phrase(doctorFeedbackDTO.getDoctorName(), font)));
                        break;
                    case 3:
                        table.addCell(new PdfPCell(new Phrase(doctorFeedbackDTO.getHealthFacilityName(), font)));
                        break;
                    case 4:
                        table.addCell(new PdfPCell(new Phrase(String.valueOf(doctorFeedbackDTO.getRate()), font)));
                        break;
                    case 5:
                        table.addCell(new PdfPCell(new Phrase(doctorFeedbackDTO.getContent(), font)));
                        break;
                    case 6:
                        table.addCell(new PdfPCell(new Phrase(DateUtils.formatInstantAsString(
                                doctorFeedbackDTO.getCreatedDate(), DateUtils.NORM_3_DATETIME_PATTERN), font)));
                        break;
                    case 7:
                        table.addCell(new PdfPCell(new Phrase(Utils.getFeedbackStatusName(doctorFeedbackDTO.getStatus()), font)));
                        break;
                    default:
                }
            }
        }
    }

    private float[] preparePdfColumnsWidthDefault(Integer size) {
        float[] widths = new float[size];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = DEFAULT_PDF_COLUMN_SIZE;
        }
        return widths;
    }

    private <T> void createCellForDoctorAppointment(Sheet sheet, List<CellData> cellDataList, List<T> dataList, CellStyle cellStyle) {
        DoctorAppointmentDTO doctorAppointmentDTO = null;
        Constants.DOCTOR_APPOINTMENT_STATUS_LIST appointment = null;
        Row row = null;
        Cell bodyCell = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            doctorAppointmentDTO = (DoctorAppointmentDTO) dataList.get(i);
            row = sheet.createRow(index++);
            for (CellData cellData : cellDataList) {
                bodyCell = row.createCell(cellData.getIndex());
                bodyCell.setCellStyle(cellStyle);
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCell.setCellValue((double) i + 1);
                        break;
                    case 1:
                        bodyCell.setCellValue(doctorAppointmentDTO.getBookingCode());
                        break;
                    case 2:
                        bodyCell.setCellValue(doctorAppointmentDTO.getPatientName());
                        break;
                    case 3:
                        bodyCell.setCellValue(DateUtils.formatInstantAsString(doctorAppointmentDTO.getStartTime(),
                                DateUtils.NORM_3_DATE_PATTERN));
                        break;
                    case 4:
                        bodyCell.setCellValue(DateUtils.formatInstantAsString(doctorAppointmentDTO.getStartTime(),
                                DateUtils.NORM_2_TIME_PATTERN) + " - "
                                + DateUtils.formatInstantAsString(doctorAppointmentDTO.getEndTime(),
                                DateUtils.NORM_2_TIME_PATTERN));
                        break;
                    case 5:
                        bodyCell.setCellValue(doctorAppointmentDTO.getDoctorName());
                        break;
                    case 6:
                        bodyCell.setCellValue(doctorAppointmentDTO.getMedicalReason());
                        break;
                    case 7:
                        appointment = Constants.DOCTOR_APPOINTMENT_STATUS_LIST.getById(doctorAppointmentDTO.getStatus());
                        bodyCell.setCellValue(Objects.nonNull(appointment) ? StringUtils.upperCase(appointment.getText()) : null);
                        break;
                    default:
                }
            }
        }
    }

    private <T> void createCellForDoctorSchedule(Sheet sheet, List<CellData> cellDataList, List<T> dataList, CellStyle cellStyle) {
        DoctorScheduleDTO doctorScheduleDTO = null;
        Row row = null;
        Cell bodyCell = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            doctorScheduleDTO = (DoctorScheduleDTO) dataList.get(i);
            row = sheet.createRow(index++);
            for (CellData cellData : cellDataList) {
                bodyCell = row.createCell(cellData.getIndex());
                bodyCell.setCellStyle(cellStyle);
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCell.setCellValue((double) i + 1);
                        break;
                    case 1:
                        bodyCell.setCellValue(doctorScheduleDTO.getDoctorCode());
                        break;
                    case 2:
                        bodyCell.setCellValue(doctorScheduleDTO.getDoctorName());
                        break;
                    case 3:
                        bodyCell.setCellValue(doctorScheduleDTO.getClinicName());
                        break;
                    case 4:
                        bodyCell.setCellValue(Utils.getWorkingTime(doctorScheduleDTO.getWorkingTime()));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void createCellForPatientRecord(Sheet sheet, List<CellData> cellDataList, List<T> dataList, CellStyle cellStyle) {
        PatientRecordDTO patientRecordDTO = null;
        Row row = null;
        Cell bodyCell = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            patientRecordDTO = (PatientRecordDTO) dataList.get(i);
            row = sheet.createRow(index++);
            for (CellData cellData : cellDataList) {
                bodyCell = row.createCell(cellData.getIndex());
                bodyCell.setCellStyle(cellStyle);
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCell.setCellValue((double) i + 1);
                        break;
                    case 1:
                        bodyCell.setCellValue(patientRecordDTO.getPatientRecordCode());
                        break;
                    case 2:
                        bodyCell.setCellValue(patientRecordDTO.getName());
                        break;
                    case 3:
                        bodyCell.setCellValue(Utils.getGenderName(patientRecordDTO.getGender()));
                        break;
                    case 4:
                        bodyCell.setCellValue(Utils.getAge(patientRecordDTO.getDob()));
                        break;
                    case 5:
                        bodyCell.setCellValue(patientRecordDTO.getAddress());
                        break;
                    case 6:
                        bodyCell.setCellValue(patientRecordDTO.getWardName());
                        break;
                    case 7:
                        bodyCell.setCellValue(patientRecordDTO.getDistrictName());
                        break;
                    case 8:
                        bodyCell.setCellValue(patientRecordDTO.getPhone());
                        break;
                    default:
                }
            }
        }
    }

    private <T> void createCellForSubclinicalResult(Sheet sheet, List<CellData> cellDataList, List<T> dataList, CellStyle cellStyle) {
        SubclinicalDTO subclinicalDTO = null;
        Row row = null;
        Cell bodyCell = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            subclinicalDTO = (SubclinicalDTO) dataList.get(i);
            row = sheet.createRow(index++);
            for (CellData cellData : cellDataList) {
                bodyCell = row.createCell(cellData.getIndex());
                bodyCell.setCellStyle(cellStyle);
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCell.setCellValue((double) i + 1);
                        break;
                    case 1:
                        bodyCell.setCellValue(subclinicalDTO.getDoctorAppointmentCode());
                        break;
                    case 2:
                        bodyCell.setCellValue(subclinicalDTO.getPatientRecordCode());
                        break;
                    case 3:
                        bodyCell.setCellValue(subclinicalDTO.getPatientRecordName());
                        break;
                    case 4:
                        bodyCell.setCellValue(subclinicalDTO.getTechnician());
                        break;
                    case 5:
                        bodyCell.setCellValue(subclinicalDTO.getRoom());
                        break;
                    case 6:
                        bodyCell.setCellValue(Utils.getNotificationStatus(subclinicalDTO.getStatus()));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void createCellForReExamination(Sheet sheet, List<CellData> cellDataList, List<T> dataList, CellStyle cellStyle) {
        DoctorAppointmentDTO doctorAppointmentDTO = null;
        Row row = null;
        Cell bodyCell = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            doctorAppointmentDTO = (DoctorAppointmentDTO) dataList.get(i);
            row = sheet.createRow(index++);
            for (CellData cellData : cellDataList) {
                bodyCell = row.createCell(cellData.getIndex());
                bodyCell.setCellStyle(cellStyle);
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCell.setCellValue((double) i + 1);
                        break;
                    case 1:
                        bodyCell.setCellValue(doctorAppointmentDTO.getAppointmentCode());
                        break;
                    case 2:
                        bodyCell.setCellValue(doctorAppointmentDTO.getPatientCode());
                        break;
                    case 3:
                        bodyCell.setCellValue(doctorAppointmentDTO.getPatientName());
                        break;
                    case 4:
                        bodyCell.setCellValue(doctorAppointmentDTO.getReExaminationDate() != null
                                ? DateUtils.convertFromInstantToString(doctorAppointmentDTO.getReExaminationDate()) : "null");
                        break;
                    case 5:
                        bodyCell.setCellValue(doctorAppointmentDTO.getDoctorName());
                        break;
                    default:
                }
            }
        }
    }

    private <T> void createCellForFeedback(Sheet sheet, List<CellData> cellDataList, List<T> dataList, CellStyle cellStyle) {
        FeedbackDTO feedbackDTO = null;
        Row row = null;
        Cell bodyCell = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            feedbackDTO = (FeedbackDTO) dataList.get(i);
            row = sheet.createRow(index++);
            for (CellData cellData : cellDataList) {
                bodyCell = row.createCell(cellData.getIndex());
                bodyCell.setCellStyle(cellStyle);
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCell.setCellValue((double) i + 1);
                        break;
                    case 1:
                        bodyCell.setCellValue(feedbackDTO.getTopicName());
                        break;
                    case 2:
                        bodyCell.setCellValue(feedbackDTO.getContent());
                        break;
                    case 3:
                        bodyCell.setCellValue(DateUtils.formatInstantAsString(feedbackDTO.getCreatedDate(), DateUtils.NORM_3_DATETIME_PATTERN));
                        break;
                    case 4:
                        bodyCell.setCellValue(feedbackDTO.getFeedbackedUnitName());
                        break;
                    case 5:
                        bodyCell.setCellValue(feedbackDTO.getProcessingUnitName());
                        break;
                    case 6:
                        bodyCell.setCellValue(feedbackDTO.getProcessedBy());
                        break;
                    case 7:
                        bodyCell.setCellValue(Utils.getFeedbackStatusName(feedbackDTO.getStatus()));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void createCellForDoctorFeedback(Sheet sheet, List<CellData> cellDataList, List<T> dataList, CellStyle cellStyle) {
        DoctorFeedbackDTO doctorFeedbackDTO = null;
        Row row = null;
        Cell bodyCell = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            doctorFeedbackDTO = (DoctorFeedbackDTO) dataList.get(i);
            row = sheet.createRow(index++);
            for (CellData cellData : cellDataList) {
                bodyCell = row.createCell(cellData.getIndex());
                bodyCell.setCellStyle(cellStyle);
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCell.setCellValue((double) i + 1);
                        break;
                    case 1:
                        bodyCell.setCellValue(doctorFeedbackDTO.getUserName());
                        break;
                    case 2:
                        bodyCell.setCellValue(doctorFeedbackDTO.getDoctorName());
                        break;
                    case 3:
                        bodyCell.setCellValue(doctorFeedbackDTO.getHealthFacilityName());
                        break;
                    case 4:
                        bodyCell.setCellValue(String.valueOf(doctorFeedbackDTO.getRate()));
                        break;
                    case 5:
                        bodyCell.setCellValue(doctorFeedbackDTO.getContent());
                        break;
                    case 6:
                        bodyCell.setCellValue(DateUtils.formatInstantAsString(doctorFeedbackDTO.getCreatedDate(), DateUtils.NORM_3_DATETIME_PATTERN));
                        break;
                    case 7:
                        bodyCell.setCellValue(Utils.getFeedbackStatusName(doctorFeedbackDTO.getStatus()));
                        break;
                    default:
                }
            }
        }
    }

    private <T> void generateWordForDoctorAppointment(XWPFTable table, List<CellData> prepareRowDataList, List<T> dataList) {
        XWPFParagraph bodyCell = null;
        XWPFRun bodyCellRun = null;
        DoctorAppointmentDTO doctorAppointmentDTO = null;
        Constants.DOCTOR_APPOINTMENT_STATUS_LIST appointment = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorAppointmentDTO = (DoctorAppointmentDTO) data;
            for (CellData cellData : prepareRowDataList) {
                bodyCell = table.getRow(index).getCell(cellData.getIndex()).getParagraphs().get(0);
                bodyCell.setAlignment(ParagraphAlignment.CENTER);
                bodyCell.setWordWrapped(true);

                bodyCellRun = bodyCell.createRun();
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCellRun.setText(String.valueOf(i + 1));
                        break;
                    case 1:
                        bodyCellRun.setText(doctorAppointmentDTO.getBookingCode());
                        break;
                    case 2:
                        bodyCellRun.setText(doctorAppointmentDTO.getPatientName());
                        break;
                    case 3:
                        bodyCellRun.setText(DateUtils.formatInstantAsString(doctorAppointmentDTO.getStartTime(),
                                DateUtils.NORM_3_DATE_PATTERN));
                        break;
                    case 4:
                        bodyCellRun.setText(DateUtils.formatInstantAsString(doctorAppointmentDTO.getStartTime(),
                                DateUtils.NORM_2_TIME_PATTERN) + " - "
                                + DateUtils.formatInstantAsString(doctorAppointmentDTO.getEndTime(),
                                DateUtils.NORM_2_TIME_PATTERN));
                        break;
                    case 5:
                        bodyCellRun.setText(doctorAppointmentDTO.getDoctorName());
                        break;
                    case 6:
                        bodyCellRun.setText(doctorAppointmentDTO.getMedicalReason());
                        break;
                    case 7:
                        appointment = Constants.DOCTOR_APPOINTMENT_STATUS_LIST.getById(doctorAppointmentDTO.getStatus());
                        bodyCellRun.setText(Objects.nonNull(appointment) ? StringUtils.upperCase(appointment.getText()) : null);
                        break;
                    default:
                }
            }
            index++;
        }
    }

    private <T> void generateWordForDoctorSchedule(XWPFTable table, List<CellData> prepareRowDataList, List<T> dataList) {
        XWPFParagraph bodyCell = null;
        XWPFRun bodyCellRun = null;
        DoctorScheduleDTO doctorScheduleDTO = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorScheduleDTO = (DoctorScheduleDTO) data;
            for (CellData cellData : prepareRowDataList) {
                bodyCell = table.getRow(index).getCell(cellData.getIndex()).getParagraphs().get(0);
                bodyCell.setAlignment(ParagraphAlignment.CENTER);
                bodyCell.setWordWrapped(true);

                bodyCellRun = bodyCell.createRun();
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCellRun.setText(String.valueOf(i + 1));
                        break;
                    case 1:
                        bodyCellRun.setText(doctorScheduleDTO.getDoctorCode());
                        break;
                    case 2:
                        bodyCellRun.setText(doctorScheduleDTO.getDoctorName());
                        break;
                    case 3:
                        bodyCellRun.setText(doctorScheduleDTO.getClinicName());
                        break;
                    case 4:
                        bodyCellRun.setText(Utils.getWorkingTime(doctorScheduleDTO.getWorkingTime()));
                        break;
                    default:
                }
            }
            index++;
        }
    }

    private <T> void generateWordForPatientRecord(XWPFTable table, List<CellData> prepareRowDataList, List<T> dataList) {
        XWPFParagraph bodyCell = null;
        XWPFRun bodyCellRun = null;
        PatientRecordDTO patientRecordDTO = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            patientRecordDTO = (PatientRecordDTO) data;
            for (CellData cellData : prepareRowDataList) {
                bodyCell = table.getRow(index).getCell(cellData.getIndex()).getParagraphs().get(0);
                bodyCell.setAlignment(ParagraphAlignment.CENTER);
                bodyCell.setWordWrapped(true);

                bodyCellRun = bodyCell.createRun();
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCellRun.setText(String.valueOf(i + 1));
                        break;
                    case 1:
                        bodyCellRun.setText(patientRecordDTO.getPatientRecordCode());
                        break;
                    case 2:
                        bodyCellRun.setText(patientRecordDTO.getName());
                        break;
                    case 3:
                        bodyCellRun.setText(Utils.getGenderName(patientRecordDTO.getGender()));
                        break;
                    case 4:
                        bodyCellRun.setText(String.valueOf(Utils.getAge(patientRecordDTO.getDob())));
                        break;
                    case 5:
                        bodyCellRun.setText(patientRecordDTO.getAddress());
                        break;
                    case 6:
                        bodyCellRun.setText(patientRecordDTO.getWardName());
                        break;
                    case 7:
                        bodyCellRun.setText(patientRecordDTO.getDistrictName());
                        break;
                    case 8:
                        bodyCellRun.setText(patientRecordDTO.getPhone());
                        break;
                    default:
                }
            }
            index++;
        }
    }

    private <T> void generateWordForSubclinicalResult(XWPFTable table, List<CellData> prepareRowDataList, List<T> dataList) {
        XWPFParagraph bodyCell = null;
        XWPFRun bodyCellRun = null;
        SubclinicalDTO subclinicalDTO = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            subclinicalDTO = (SubclinicalDTO) data;
            for (CellData cellData : prepareRowDataList) {
                bodyCell = table.getRow(index).getCell(cellData.getIndex()).getParagraphs().get(0);
                bodyCell.setAlignment(ParagraphAlignment.CENTER);
                bodyCell.setWordWrapped(true);

                bodyCellRun = bodyCell.createRun();
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCellRun.setText(String.valueOf(i + 1));
                        break;
                    case 1:
                        bodyCellRun.setText(subclinicalDTO.getDoctorAppointmentCode());
                        break;
                    case 2:
                        bodyCellRun.setText(subclinicalDTO.getPatientRecordCode());
                        break;
                    case 3:
                        bodyCellRun.setText(subclinicalDTO.getPatientRecordName());
                        break;
                    case 4:
                        bodyCellRun.setText(subclinicalDTO.getTechnician());
                        break;
                    case 5:
                        bodyCellRun.setText(subclinicalDTO.getRoom());
                        break;
                    case 6:
                        bodyCellRun.setText(Utils.getNotificationStatus(subclinicalDTO.getStatus()));
                        break;
                    default:
                }
            }
            index++;
        }
    }

    private <T> void generateWordForReExamination(XWPFTable table, List<CellData> prepareRowDataList, List<T> dataList) {
        XWPFParagraph bodyCell = null;
        XWPFRun bodyCellRun = null;
        DoctorAppointmentDTO doctorAppointmentDTO = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorAppointmentDTO = (DoctorAppointmentDTO) data;
            for (CellData cellData : prepareRowDataList) {
                bodyCell = table.getRow(index).getCell(cellData.getIndex()).getParagraphs().get(0);
                bodyCell.setAlignment(ParagraphAlignment.CENTER);
                bodyCell.setWordWrapped(true);

                bodyCellRun = bodyCell.createRun();
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCellRun.setText(String.valueOf(i + 1));
                        break;
                    case 1:
                        bodyCellRun.setText(doctorAppointmentDTO.getAppointmentCode());
                        break;
                    case 2:
                        bodyCellRun.setText(doctorAppointmentDTO.getPatientCode());
                        break;
                    case 3:
                        bodyCellRun.setText(doctorAppointmentDTO.getPatientName());
                        break;
                    case 4:
                        bodyCellRun.setText(doctorAppointmentDTO.getReExaminationDate() != null
                                ? DateUtils.convertFromInstantToString(doctorAppointmentDTO.getReExaminationDate()) : "null");
                        break;
                    case 5:
                        bodyCellRun.setText(doctorAppointmentDTO.getDoctorName());
                        break;
                    default:
                }
            }
            index++;
        }
    }

    private <T> void generateWordForFeedback(XWPFTable table, List<CellData> prepareRowDataList, List<T> dataList) {
        XWPFParagraph bodyCell = null;
        XWPFRun bodyCellRun = null;
        FeedbackDTO feedbackDTO = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            feedbackDTO = (FeedbackDTO) data;
            for (CellData cellData : prepareRowDataList) {
                bodyCell = table.getRow(index).getCell(cellData.getIndex()).getParagraphs().get(0);
                bodyCell.setAlignment(ParagraphAlignment.CENTER);
                bodyCell.setWordWrapped(true);

                bodyCellRun = bodyCell.createRun();
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCellRun.setText(String.valueOf(i + 1));
                        break;
                    case 1:
                        bodyCellRun.setText(feedbackDTO.getTopicName());
                        break;
                    case 2:
                        bodyCellRun.setText(feedbackDTO.getContent());
                        break;
                    case 3:
                        bodyCellRun.setText(DateUtils.formatInstantAsString(
                                feedbackDTO.getCreatedDate(), DateUtils.NORM_3_DATETIME_PATTERN));
                        break;
                    case 4:
                        bodyCellRun.setText(feedbackDTO.getFeedbackedUnitName());
                        break;
                    case 5:
                        bodyCellRun.setText(feedbackDTO.getProcessingUnitName());
                        break;
                    case 6:
                        bodyCellRun.setText(feedbackDTO.getProcessedBy());
                        break;
                    case 7:
                        bodyCellRun.setText(Utils.getFeedbackStatusName(feedbackDTO.getStatus()));
                        break;
                    default:
                }
            }
            index++;
        }
    }

    private <T> void generateWordForDoctorFeedback(XWPFTable table, List<CellData> prepareRowDataList, List<T> dataList) {
        XWPFParagraph bodyCell = null;
        XWPFRun bodyCellRun = null;
        DoctorFeedbackDTO doctorFeedbackDTO = null;
        int index = 1;
        for (int i = 0; i < dataList.size(); i++) {
            T data = dataList.get(i);
            doctorFeedbackDTO = (DoctorFeedbackDTO) data;
            for (CellData cellData : prepareRowDataList) {
                bodyCell = table.getRow(index).getCell(cellData.getIndex()).getParagraphs().get(0);
                bodyCell.setAlignment(ParagraphAlignment.CENTER);
                bodyCell.setWordWrapped(true);

                bodyCellRun = bodyCell.createRun();
                switch (cellData.getIndex()) {
                    case 0:
                        bodyCellRun.setText(String.valueOf(i + 1));
                        break;
                    case 1:
                        bodyCellRun.setText(doctorFeedbackDTO.getUserName());
                        break;
                    case 2:
                        bodyCellRun.setText(doctorFeedbackDTO.getDoctorName());
                        break;
                    case 3:
                        bodyCellRun.setText(doctorFeedbackDTO.getHealthFacilityName());
                        break;
                    case 4:
                        bodyCellRun.setText(String.valueOf(doctorFeedbackDTO.getRate()));
                        break;
                    case 5:
                        bodyCellRun.setText(doctorFeedbackDTO.getContent());
                        break;
                    case 6:
                        bodyCellRun.setText(DateUtils.formatInstantAsString(doctorFeedbackDTO.getCreatedDate(), DateUtils.NORM_3_DATETIME_PATTERN));
                        break;
                    case 7:
                        bodyCellRun.setText(Utils.getFeedbackStatusName(doctorFeedbackDTO.getStatus()));
                        break;
                    default:
                }
            }
            index++;
        }
    }
}
