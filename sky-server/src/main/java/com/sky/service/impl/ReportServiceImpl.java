package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description ReportService
 * @Author kight-tom
 * @Date 2026-04-23  20:42
 */

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("营业额统计：{}到{}", begin, end);
        List<LocalDate> dataList = new ArrayList<>();
        dataList.add(begin);
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dataList.add(begin);
        }

        List<Double> turnoverList = new ArrayList();
        for (LocalDate date : dataList){
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map =new HashMap();
            map.put("begin", beginTime);
            map.put("end", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnoverList.add(turnover == null ? 0.0 : turnover);
        }
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .dateList(StringUtils.join(dataList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();

        return turnoverReportVO;
    }


    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        String nameList = StringUtils.join(
                salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList()),
                ","
        );
        String numberList = StringUtils.join(
                salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList()),
                ","
        );

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        log.info("导出营业数据");
        //1. 查询概览数据 查最近30天的营业数据
        LocalDate dataBegin =LocalDate.now().minusDays(30);
        LocalDate dataEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dataBegin, LocalTime.MIN), LocalDateTime.of(dataEnd, LocalTime.MAX));

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/sky_data.xlsx");
        //2. 通过POI将数据写入到Excel中
        try {
            //基于inputStream创建Excel
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //填充概览数据
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间：" + dataBegin + "至" + dataEnd);
            sheet.getRow(3).getCell(2).setCellValue(businessDataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessDataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充明细数据
            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = dataBegin.plusDays(i);
                BusinessDataVO dataVO = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dataVO.getTurnover());
                row.getCell(3).setCellValue(dataVO.getValidOrderCount());
                row.getCell(4).setCellValue(dataVO.getOrderCompletionRate());
                row.getCell(5).setCellValue(dataVO.getUnitPrice());
                row.getCell(6).setCellValue(dataVO.getNewUsers());
            }

            //3. 使用输出流将Excel下载到客户端
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            //关闭资源
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
