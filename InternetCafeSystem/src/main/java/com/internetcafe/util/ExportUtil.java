package com.internetcafe.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Excel导出工具类
 * 使用Apache POI将JTable数据导出为Excel文件
 */
public class ExportUtil {

    /**
     * 将JTable数据导出为Excel文件
     *
     * @param table    要导出的JTable
     * @param fileName 导出的文件名（不含路径）
     */
    public static void exportTableToExcel(JTable table, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("数据报表");

        // 创建标题行样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // 创建数据行样式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);

        TableModel model = table.getModel();

        // 写入表头
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < model.getColumnCount(); col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(model.getColumnName(col));
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(col);
        }

        // 写入数据行
        for (int row = 0; row < model.getRowCount(); row++) {
            Row dataRow = sheet.createRow(row + 1);
            for (int col = 0; col < model.getColumnCount(); col++) {
                Cell cell = dataRow.createCell(col);
                Object value = model.getValueAt(row, col);
                if (value != null) {
                    cell.setCellValue(value.toString());
                } else {
                    cell.setCellValue("");
                }
                cell.setCellStyle(dataStyle);
            }
        }

        // 自动调整列宽
        for (int col = 0; col < model.getColumnCount(); col++) {
            sheet.autoSizeColumn(col);
            // 设置最小列宽
            if (sheet.getColumnWidth(col) < 3000) {
                sheet.setColumnWidth(col, 3000);
            }
        }

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            workbook.write(fos);
            JOptionPane.showMessageDialog(null,
                    "数据已成功导出到：" + fileName,
                    "导出成功",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "导出失败：" + e.getMessage(),
                    "导出错误",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}