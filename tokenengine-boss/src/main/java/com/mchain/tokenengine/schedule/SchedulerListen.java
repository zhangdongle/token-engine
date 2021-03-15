package com.mchain.tokenengine.schedule;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 执行定时任务
 * @author GWELL
 */
@Component
@Slf4j
public class SchedulerListen implements ApplicationListener<ApplicationReadyEvent> {
	@Resource
	private Scheduler scheduler;

	@Value("${server.port}")
	private Integer port;

	@Override
	public void onApplicationEvent(final ApplicationReadyEvent event) {
		try {
			// 端口号8888为主任务
//			if (port != 8888) {
//				log.info("当前服务非主任务");
//				return;
//			}
			/*
			 * 添加一个定时任务进入队列:
			 * createTask({String-任务名}, {String-cron表达式}, {Class<? extends QuartzJobBean.class>-任务类}, scheduler);
			 * 注:
			 * 如果该任务不需要并发(即,该任务的下一次执行时间为上一次执行结束后, 反之,该任务到达指定cron规则就会执行,不管上一次是否结束)
			 * 需要在 任务类上加 @DisallowConcurrentExecution
			 */
			// 检测转入 ==> 5秒
			createTask("syncBlock", "*/5 * * * * ?", SyncBlockTask.class, scheduler);
			//            // 转入冷钱包 ==> 1 分钟
			//            createTask("transferCold", "0 */1 * * * ?", TransferColdTask.class, scheduler);
			//            // 退币 ==> 5秒
			//            createTask("refund", "*/5 * * * * ?", RefundTask.class, scheduler);
			//            // 提币 ==> 5秒
			//            createTask("extract", "*/5 * * * * ?", ExtractTask.class, scheduler);
			//            // 检测等待中的交易 ==> 5秒
			//            createTask("checkPending", "*/5 * * * * ?", CheckPendingTask.class, scheduler);
			//            // 对失败的交易进行重试操作(提币/退币) ==> 5秒
			////            createTask("openFail", "*/5 * * * * ?", OpenFailTransactionTask.class, scheduler);
			//            // 对用户成功转入的交易进行操作 ==> 5秒
			//            createTask("openUseTransaction", "*/5 * * * * ?", OpenUserTransactionTask.class, scheduler);
			//            // 释放期数 => 每小时
			//            createTask("release","0 */1 * * * ?", ReleaseTask.class, scheduler);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建任务公共方法
	 * @param name 任务名
	 * @param cron cron表达式
	 * @param jobClass 实现类 (该类继承org.quartz.Job 如果不希望该任务并发需要在该类上添加 @DisallowConcurrentExecution)
	 * @param scheduler 任务队列
	 */
	private void createTask(String name, String cron, Class<? extends Job> jobClass, Scheduler scheduler)
			throws SchedulerException {
		// 任务
		JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(String.format("%s-TASK", name.toUpperCase()),
				String.format("%s-TASK-GROUP", name.toUpperCase())).build();

		//cron表达式 表示每隔5秒执行
		CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cron)
				// 不立即触发, 等待下次Cron触发频率到达时刻开始
				.withMisfireHandlingInstructionDoNothing();

		// 定时任务触发器
		CronTrigger cronTrigger = TriggerBuilder.newTrigger()
				.withIdentity(String.format("%s-TRIGGER", name.toUpperCase()),
						String.format("%s-TRIGGER-GROUP", name.toUpperCase())).withSchedule(scheduleBuilder).startNow()
				.build();

		// 添加到任务队列
		scheduler.scheduleJob(jobDetail, cronTrigger);
	}

}
