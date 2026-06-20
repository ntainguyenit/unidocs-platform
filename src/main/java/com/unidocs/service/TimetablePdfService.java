package com.unidocs.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class TimetablePdfService {

    public byte[] generateTimetablePdf(List<String> data) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Load Fonts
        BaseFont bfRegular = BaseFont.createFont(new ClassPathResource("fonts/Roboto-Regular.ttf").getURL().toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        BaseFont bfBold = BaseFont.createFont(new ClassPathResource("fonts/Roboto-Bold.ttf").getURL().toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        
        Font titleFont = new Font(bfBold, 18, Font.NORMAL, new Color(30, 58, 138));
        Font headerFont = new Font(bfBold, 10, Font.NORMAL, Color.BLACK);
        Font cellFont = new Font(bfRegular, 10, Font.NORMAL, Color.BLACK);
        Font sectionFont = new Font(bfBold, 12, Font.NORMAL, new Color(153, 27, 27));

        // Title
        Paragraph title = new Paragraph("THỜI KHÓA BIỂU", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Table
        PdfPTable table = new PdfPTable(10);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.5f, 1.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f, 2.5f});

        String[] headers = {"Tiết học", "Từ", "Đến", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(new Color(243, 244, 246));
            cell.setPadding(8);
            table.addCell(cell);
        }

        String[][] times = {
                {"Tiết 1", "07:00", "07:50"}, {"Tiết 2", "07:55", "08:45"}, {"Tiết 3", "08:50", "09:40"},
                {"Tiết 4", "09:50", "10:40"}, {"Tiết 5", "10:45", "11:35"},
                {"Tiết 6", "13:00", "13:50"}, {"Tiết 7", "13:55", "14:45"}, {"Tiết 8", "14:50", "15:40"},
                {"Tiết 9", "15:50", "16:40"}, {"Tiết 10", "16:45", "17:35"},
                {"Tiết 11", "18:00", "18:50"}, {"Tiết 12", "18:55", "19:45"}, {"Tiết 13", "19:50", "20:40"}
        };

        int dataIndex = 0;
        for (int i = 0; i < 13; i++) {
            if (i == 0) addSectionHeader(table, "BUỔI SÁNG", sectionFont, new Color(255, 237, 213));
            if (i == 5) addSectionHeader(table, "BUỔI CHIỀU", sectionFont, new Color(219, 234, 254));
            if (i == 10) addSectionHeader(table, "BUỔI TỐI", sectionFont, new Color(243, 232, 255));

            // Time columns
            for (int j = 0; j < 3; j++) {
                PdfPCell cell = new PdfPCell(new Phrase(times[i][j], j == 0 ? headerFont : cellFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Data columns
            for (int d = 0; d < 7; d++) {
                String val = (dataIndex < data.size()) ? data.get(dataIndex++) : "";
                PdfPCell cell = new PdfPCell(new Phrase(val, cellFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);
            }
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private void addSectionHeader(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setColspan(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        table.addCell(cell);
    }
}
