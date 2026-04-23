package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Description OrderTsk
 * @Author kight-tom
 * @Date 2026-04-23  18:00
 */
@Component
@Slf4j
public class OrderTsk {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时处理超时订单
     */
    @Scheduled(cron = "0 * * * * * ") // 每分钟执行一次
    public void processTimeOutOrder(){
        log.info("定时处理超时订单{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, LocalDateTime.now().plusMinutes(-15));
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orderMapper.update(Orders.builder()
                        .id(orders.getId())
                        .status(Orders.CANCELLED)
                        .cancelReason("订单超时，自动取消")
                        .cancelTime(LocalDateTime.now())
                        .build());
            }
        }
    }

    /**
     * 定时处理派送订单
     */
    @Scheduled(cron = "0 0 1 * * * ") // 每天凌晨1点执行一次
    public void processCompletedOrder(){
        log.info("定时处理派送订单{}", LocalDateTime.now());
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().plusMinutes(-60));
        if (ordersList != null && ordersList.size() > 0) {
            for (Orders orders : ordersList) {
                orderMapper.update(Orders.builder()
                        .id(orders.getId())
                        .status(Orders.COMPLETED)
                        .deliveryTime(LocalDateTime.now())
                        .build());
            }
        }
    }
}
