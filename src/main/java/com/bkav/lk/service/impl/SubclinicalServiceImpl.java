package com.bkav.lk.service.impl;

import com.bkav.lk.domain.DoctorAppointment;
import com.bkav.lk.domain.PatientRecord;
import com.bkav.lk.domain.Subclinical;
import com.bkav.lk.dto.SubclinicalDTO;
import com.bkav.lk.repository.DoctorAppointmentRepository;
import com.bkav.lk.repository.SubclinicalRepository;
import com.bkav.lk.service.SubclinicalService;
import com.bkav.lk.service.mapper.SubclinicalMapper;
import com.bkav.lk.util.Constants;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SubclinicalServiceImpl implements SubclinicalService {

    private final SubclinicalRepository subclinicalRepository;

    private final DoctorAppointmentRepository doctorAppointmentRepository;

    private final SubclinicalMapper subclinicalMapper;

    public SubclinicalServiceImpl(
            SubclinicalRepository subclinicalRepository,
            DoctorAppointmentRepository doctorAppointmentRepository,
            SubclinicalMapper subclinicalMapper) {
        this.subclinicalRepository = subclinicalRepository;
        this.doctorAppointmentRepository = doctorAppointmentRepository;
        this.subclinicalMapper = subclinicalMapper;
    }

    @Override
    public Page<SubclinicalDTO> search(MultiValueMap<String, String> queryParams, Pageable pageable) {
        List<Subclinical> listEntity = subclinicalRepository.search(queryParams, pageable);
        List<SubclinicalDTO> listDTO = convertToDTO(listEntity);
        return new PageImpl<>(listDTO, pageable, subclinicalRepository.count(queryParams));
    }

    @Override
    public List<SubclinicalDTO> update(List<SubclinicalDTO> subclinicalDTO) {
        List<SubclinicalDTO> listDTO = new ArrayList<>();
        if (subclinicalDTO.size() == 0) {
            return listDTO;
        }
        List<Subclinical> listEntity = new ArrayList<>();
        for (SubclinicalDTO item : subclinicalDTO) {
            Optional<Subclinical> optional = subclinicalRepository.findById(item.getId());
            if (optional.isPresent()) {
                Subclinical subclinicalResult = optional.get();
                subclinicalResult.setStatus(Constants.NOTIFICATION_STATUS.NOTIFIED);
                subclinicalRepository.save(subclinicalResult);
                listEntity.add(subclinicalResult);
            }
        }
        return convertToDTO(listEntity);

    }

    @Override
    public ByteArrayInputStream exportToExcel(List<SubclinicalDTO> list, InputStream file) {
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 4;
            CellStyle style = workbook.createCellStyle();
            XSSFFont font = workbook.createFont();
            font.setFontHeight(14);
            style.setFont(font);

            for (SubclinicalDTO subclinicalDTO : list) {
                Row row = sheet.createRow(rowCount);
                row.createCell(0).setCellValue(subclinicalDTO.getDoctorAppointmentCode());
                row.createCell(1).setCellValue(subclinicalDTO.getPatientRecordCode());
                row.createCell(2).setCellValue(subclinicalDTO.getPatientRecordName());
                row.createCell(3).setCellValue(subclinicalDTO.getCode());
                row.createCell(4).setCellValue(subclinicalDTO.getName());
                row.createCell(5).setCellValue(subclinicalDTO.getTechnician());
                row.createCell(6).setCellValue(subclinicalDTO.getRoom());
                row.createCell(7).setCellValue(convertStatus(subclinicalDTO.getStatus()));
                rowCount++;
            }
            workbook.write(outputStream);
            workbook.close();
            return new ByteArrayInputStream(outputStream.toByteArray());
        } catch (IOException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    @Override
    public SubclinicalDTO save(SubclinicalDTO subclinicalDTO) {
        Subclinical subclinical = subclinicalMapper.toEntity(subclinicalDTO);
        return subclinicalMapper.toDto(subclinicalRepository.save(subclinical));
    }

    @Override
    public boolean existsByDoctorAppointmentCode(String doctorAppointmentCode) {
        return subclinicalRepository.existsByDoctorAppointmentCode(doctorAppointmentCode);
    }

    @Override
    public List<SubclinicalDTO> findAllByIds(List<Long> ids) {
        return subclinicalMapper.toDto(subclinicalRepository.findAllById(ids));
    }

    private static String convertStatus(Integer status) {
        if (status.equals(Constants.ENTITY_STATUS.ACTIVE)) {
            return "Chưa thông báo";
        }
        if (status.equals(Constants.ENTITY_STATUS.DEACTIVATE)) {
            return "Đã thông báo";
        }
        return null;
    }

    private List<SubclinicalDTO> convertToDTO(List<Subclinical> listEntity){
        List<SubclinicalDTO> listDTO = new ArrayList<>();
        for(Subclinical entity: listEntity){
            SubclinicalDTO dto = new SubclinicalDTO();
            dto.setId(entity.getId());
            dto.setDoctorAppointmentCode(entity.getDoctorAppointmentCode());
            dto.setCode(entity.getCode());
            dto.setName(entity.getName());
            dto.setStatus(entity.getStatus());
            dto.setTechnician(entity.getTechnician());
            dto.setRoom(entity.getRoom());

            DoctorAppointment doctorAppointment = doctorAppointmentRepository.findByAppointmentCode(dto.getDoctorAppointmentCode());
            if (doctorAppointment != null) {
                PatientRecord patient = doctorAppointment.getPatientRecord();
                dto.setPatientRecordId(patient.getId());
                dto.setPatientRecordCode(patient.getPatientRecordCode());
                dto.setPatientRecordName(patient.getName());
            }
            listDTO.add(dto);
        }
        return listDTO;
    }
}
