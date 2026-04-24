package com.sky.Text;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @Description POIText
 * @Author kight-tom
 * @Date 2026-04-24  11:59
 */
public class POIText {

    public static void writer() throws Exception{
        //在内存中创建一个excel
        XSSFWorkbook excel = new XSSFWorkbook();
        //创建一个sheet
        XSSFSheet sheet = excel.createSheet("itcast");
        //创建一行
        XSSFRow row = sheet.createRow(1);
        //创建单元格并写入数据
        row.createCell(1).setCellValue("姓名");
        row.createCell(2).setCellValue("年龄");

        row =sheet.createRow(2);
        row.createCell(1).setCellValue("李三");
        row.createCell(2).setCellValue("18");

        row =sheet.createRow(3);
        row.createCell(1).setCellValue("李三");
        row.createCell(2).setCellValue("18");

        FileOutputStream fileOutputStream = new FileOutputStream("D:\\itcast.xlsx");
        excel.write(fileOutputStream);

        //关闭流
        fileOutputStream.close();
        excel.close();
    }

    public static void reader() throws Exception{

        FileInputStream fileInputStream = new FileInputStream("D:\\itcast.xlsx");
        XSSFWorkbook excel = new XSSFWorkbook(fileInputStream);
        XSSFSheet sheet = excel.getSheetAt(0);

        //获取最后一行的行号
        int lastRowNum = sheet.getLastRowNum();
        for (int i = 1; i <= lastRowNum; i++) {
            //获取每一行
            XSSFRow row = sheet.getRow(i);
            //获取每一行的单元格
            String stringCellValue1 = row.getCell(1).getStringCellValue();
            String stringCellValue2 = row.getCell(2).getStringCellValue();
            System.out.println(stringCellValue1+ "  " + stringCellValue2);
        }

        fileInputStream.close();
        excel.close();

    }

    public static void main(String[] args) throws  Exception{
        //writer();
        reader();
    }
}
