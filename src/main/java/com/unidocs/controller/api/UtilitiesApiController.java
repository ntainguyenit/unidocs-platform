package com.unidocs.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unidocs.service.TimetablePdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/utilities")
public class UtilitiesApiController {

    @Autowired
    private TimetablePdfService timetablePdfService;

    @PostMapping("/timetable/export")
    public ResponseEntity<byte[]> exportTimetable(@RequestParam("data") String dataJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<String> data = objectMapper.readValue(dataJson, new TypeReference<List<String>>(){});
            byte[] pdfBytes = timetablePdfService.generateTimetablePdf(data);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ThoiKhoaBieu.pdf");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
