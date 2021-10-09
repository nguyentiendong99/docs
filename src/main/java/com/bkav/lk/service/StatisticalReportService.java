package com.bkav.lk.service;

import org.springframework.util.MultiValueMap;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public interface StatisticalReportService {

    List<?> search(MultiValueMap<String, String> queryParams);

    void storeZipData(Map<String, byte[]> inputs, OutputStream outputStream);

    <T> Map<String, byte[]> generateExportData(List<T> reportContents, String[] fileFormats, Integer contentType);
}
