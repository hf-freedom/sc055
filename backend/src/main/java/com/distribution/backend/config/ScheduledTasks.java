package com.distribution.backend.config;

import com.distribution.backend.service.CommissionService;
import com.distribution.backend.service.WithdrawService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    @Autowired
    private CommissionService commissionService;

    @Autowired
    private WithdrawService withdrawService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void settlePendingCommissions() {
        logger.info("开始执行佣金结算定时任务 - {}", LocalDateTime.now());
        try {
            commissionService.settlePendingCommissions();
            logger.info("佣金结算定时任务执行完成 - {}", LocalDateTime.now());
        } catch (Exception e) {
            logger.error("佣金结算定时任务执行失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void compensateFailedWithdraws() {
        logger.info("开始执行提现失败补偿定时任务 - {}", LocalDateTime.now());
        try {
            withdrawService.compensateAllFailedWithdraws();
            logger.info("提现失败补偿定时任务执行完成 - {}", LocalDateTime.now());
        } catch (Exception e) {
            logger.error("提现失败补偿定时任务执行失败", e);
        }
    }
}
