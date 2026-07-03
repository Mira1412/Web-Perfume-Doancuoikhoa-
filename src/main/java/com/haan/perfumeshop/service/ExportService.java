package com.haan.perfumeshop.service;

import com.haan.perfumeshop.model.Order;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExportService {

    public byte[] exportOrdersToExcel(List<Order> orders) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Báo cáo Đơn hàng");

            // 1. TẠO FONT VÀ STYLE
            // Font tiêu đề cột
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            // Style tiêu đề cột
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

            // Style dữ liệu
            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Style tiền tệ
            CellStyle numberCellStyle = workbook.createCellStyle();
            numberCellStyle.cloneStyleFrom(dataCellStyle);
            DataFormat format = workbook.createDataFormat();
            numberCellStyle.setDataFormat(format.getFormat("#,##0"));

            // 2. GHI BẢNG DANH SÁCH ĐƠN HÀNG CHI TIẾT (Cột A -> H)
            String[] columns = {"Mã ĐH", "Ngày Đặt", "Người Nhận", "SĐT", "Địa Chỉ", "Tổng Tiền (VNĐ)", "Trạng Thái", "Thanh Toán"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

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
                cell6.setCellValue(getFriendlyStatus(order.getTrang_thai()));
                cell6.setCellStyle(dataCellStyle);

                Cell cell7 = row.createCell(7);
                String paymentMethod = order.getPhuong_thuc_thanh_toan() != null ? order.getPhuong_thuc_thanh_toan() : "COD";
                if ("VNPay".equalsIgnoreCase(paymentMethod)) {
                    paymentMethod = "VNPay (" + (order.getMa_giao_dich() != null ? order.getMa_giao_dich() : "") + ")";
                }
                cell7.setCellValue(paymentMethod);
                cell7.setCellStyle(dataCellStyle);
            }

            // 3. THỐNG KÊ DỮ LIỆU ĐỂ VẼ BIỂU ĐỒ (Đặt tại cột K -> P để hiển thị song song)
            // Thống kê Doanh thu theo Ngày
            Map<LocalDate, Double> revenueByDate = new TreeMap<>();
            for (Order order : orders) {
                if (order.getNgay_dat() != null && !"Cancelled".equalsIgnoreCase(order.getTrang_thai())) {
                    LocalDate date = order.getNgay_dat().toLocalDate();
                    double total = order.getTong_tien() != null ? order.getTong_tien() : 0;
                    revenueByDate.put(date, revenueByDate.getOrDefault(date, 0.0) + total);
                }
            }

            // Thống kê Trạng thái đơn hàng
            Map<String, Integer> statusCount = new HashMap<>();
            for (Order order : orders) {
                String status = getFriendlyStatus(order.getTrang_thai());
                statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
            }

            // Tạo styles cho bảng Dashboard phụ
            CellStyle dashboardTitleStyle = workbook.createCellStyle();
            Font dbTitleFont = workbook.createFont();
            dbTitleFont.setBold(true);
            dbTitleFont.setColor(IndexedColors.DARK_TEAL.getIndex());
            dashboardTitleStyle.setFont(dbTitleFont);

            CellStyle dashboardHeaderStyle = workbook.createCellStyle();
            Font dbHeaderFont = workbook.createFont();
            dbHeaderFont.setBold(true);
            dashboardHeaderStyle.setFont(dbHeaderFont);
            dashboardHeaderStyle.setBorderBottom(BorderStyle.MEDIUM);

            // Ghi bảng Thống kê Doanh thu (Cột K, L)
            Row dbTitleRow = sheet.getRow(0);
            Cell dbTitle1 = dbTitleRow.createCell(10);
            dbTitle1.setCellValue("THỐNG KÊ DOANH THU");
            dbTitle1.setCellStyle(dashboardTitleStyle);

            Row dbHeaderRow = sheet.getRow(1) != null ? sheet.getRow(1) : sheet.createRow(1);
            Cell dbH1 = dbHeaderRow.createCell(10); dbH1.setCellValue("Ngày"); dbH1.setCellStyle(dashboardHeaderStyle);
            Cell dbH2 = dbHeaderRow.createCell(11); dbH2.setCellValue("Doanh thu"); dbH2.setCellStyle(dashboardHeaderStyle);

            int dbRowIdx = 2;
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM");
            int startRevenueRow = dbRowIdx + 1;
            for (Map.Entry<LocalDate, Double> entry : revenueByDate.entrySet()) {
                Row r = sheet.getRow(dbRowIdx) != null ? sheet.getRow(dbRowIdx) : sheet.createRow(dbRowIdx);
                dbRowIdx++;
                
                Cell c1 = r.createCell(10);
                c1.setCellValue(entry.getKey().format(df));
                c1.setCellStyle(dataCellStyle);
                
                Cell c2 = r.createCell(11);
                c2.setCellValue(entry.getValue());
                c2.setCellStyle(numberCellStyle);
            }
            int endRevenueRow = dbRowIdx;

            // Ghi bảng Thống kê Trạng thái (Cột N, O)
            Cell dbTitle2 = dbTitleRow.createCell(13);
            dbTitle2.setCellValue("TRẠNG THÁI ĐƠN");
            dbTitle2.setCellStyle(dashboardTitleStyle);

            Cell dbH3 = dbHeaderRow.createCell(13); dbH3.setCellValue("Trạng thái"); dbH3.setCellStyle(dashboardHeaderStyle);
            Cell dbH4 = dbHeaderRow.createCell(14); dbH4.setCellValue("Số đơn"); dbH4.setCellStyle(dashboardHeaderStyle);

            int dbStatusRowIdx = 2;
            int startStatusRow = dbStatusRowIdx + 1;
            for (Map.Entry<String, Integer> entry : statusCount.entrySet()) {
                Row r = sheet.getRow(dbStatusRowIdx) != null ? sheet.getRow(dbStatusRowIdx) : sheet.createRow(dbStatusRowIdx);
                dbStatusRowIdx++;
                
                Cell c1 = r.createCell(13);
                c1.setCellValue(entry.getKey());
                c1.setCellStyle(dataCellStyle);
                
                Cell c2 = r.createCell(14);
                c2.setCellValue(entry.getValue());
                c2.setCellStyle(dataCellStyle);
            }
            int endStatusRow = dbStatusRowIdx;

            // 4. VẼ BIỂU ĐỒ 1: DOANH THU THEO NGÀY (Cột Q -> Y, từ dòng 1 -> 15)
            if (!revenueByDate.isEmpty()) {
                XSSFDrawing drawing = sheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 16, 0, 24, 14);
                
                XSSFChart chart = drawing.createChart(anchor);
                chart.setTitleText("Biểu đồ Doanh thu");
                chart.setTitleOverlay(false);
                
                XDDFChartLegend legend = chart.getOrAddLegend();
                legend.setPosition(LegendPosition.BOTTOM);

                XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxis.setTitle("Ngày");
                XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
                leftAxis.setTitle("Số tiền (VNĐ)");

                XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(startRevenueRow - 1, endRevenueRow - 1, 10, 10));
                XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(startRevenueRow - 1, endRevenueRow - 1, 11, 11));

                XDDFLineChartData data = (XDDFLineChartData) chart.createData(ChartTypes.LINE, bottomAxis, leftAxis);
                XDDFLineChartData.Series series = (XDDFLineChartData.Series) data.addSeries(xs, ys);
                series.setTitle("Doanh thu thực tế", null);
                series.setSmooth(true);
                chart.plot(data);
            }

            // 5. VẼ BIỂU ĐỒ 2: TRẠNG THÁI ĐƠN HÀNG (Cột Q -> Y, từ dòng 16 -> 30)
            if (!statusCount.isEmpty()) {
                XSSFDrawing drawing = sheet.createDrawingPatriarch();
                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 16, 15, 24, 29);
                
                XSSFChart chart = drawing.createChart(anchor);
                chart.setTitleText("Tỷ lệ Trạng thái Đơn");
                chart.setTitleOverlay(false);

                XDDFChartLegend legend = chart.getOrAddLegend();
                legend.setPosition(LegendPosition.RIGHT);

                XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(sheet, new CellRangeAddress(startStatusRow - 1, endStatusRow - 1, 13, 13));
                XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(startStatusRow - 1, endStatusRow - 1, 14, 14));

                XDDFPieChartData data = (XDDFPieChartData) chart.createData(ChartTypes.PIE, null, null);
                XDDFPieChartData.Series series = (XDDFPieChartData.Series) data.addSeries(xs, ys);
                series.setTitle("Đơn hàng", null);
                chart.plot(data);
            }

            // Căn chỉnh độ rộng cột
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }
            sheet.autoSizeColumn(10);
            sheet.autoSizeColumn(11);
            sheet.autoSizeColumn(13);
            sheet.autoSizeColumn(14);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String getFriendlyStatus(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status) {
            case "Pending": return "Chờ xử lý";
            case "Packing": return "Đang đóng gói";
            case "Shipping": return "Đang vận chuyển";
            case "Delivered": return "Đã giao hàng";
            case "Cancelled": return "Đã hủy";
            default: return status;
        }
    }
}
