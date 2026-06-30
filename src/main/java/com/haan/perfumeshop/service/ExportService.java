package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Order;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    public byte[] exportOrdersToExcel(List<Order> orders) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách Đơn hàng");

            // Header Font
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            // Header Style
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);

            // Data Style
            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Number/Currency Style
            CellStyle numberCellStyle = workbook.createCellStyle();
            numberCellStyle.cloneStyleFrom(dataCellStyle);
            DataFormat format = workbook.createDataFormat();
            numberCellStyle.setDataFormat(format.getFormat("#,##0"));

            // Columns setup
            String[] columns = {"Mã ĐH", "Ngày Đặt", "Người Nhận", "SĐT", "Địa Chỉ", "Tổng Tiền (VNĐ)", "Trạng Thái", "Thanh Toán"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Fill Data
            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Order order : orders) {
                Row row = sheet.createRow(rowIdx++);
                
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(order.getId());
                cell0.setCellStyle(dataCellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(order.getNgay_dat() != null ? order.getNgay_dat().format(formatter) : "");
                cell1.setCellStyle(dataCellStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(order.getUser() != null ? order.getUser().getFullName() : "");
                cell2.setCellStyle(dataCellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(order.getUser() != null ? order.getUser().getPhone() : "");
                cell3.setCellStyle(dataCellStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(order.getUser() != null ? order.getUser().getAddress() : "");
                cell4.setCellStyle(dataCellStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(order.getTong_tien() != null ? order.getTong_tien() : 0);
                cell5.setCellStyle(numberCellStyle);

                Cell cell6 = row.createCell(6);
                cell6.setCellValue(order.getTrang_thai());
                cell6.setCellStyle(dataCellStyle);

                Cell cell7 = row.createCell(7);
                String paymentMethod = order.getPhuong_thuc_thanh_toan() != null ? order.getPhuong_thuc_thanh_toan() : "COD";
                if ("VNPay".equalsIgnoreCase(paymentMethod)) {
                    paymentMethod = "VNPay (" + (order.getMa_giao_dich() != null ? order.getMa_giao_dich() : "") + ")";
                }
                cell7.setCellValue(paymentMethod);
                cell7.setCellStyle(dataCellStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                // Add some extra padding
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
