package com.mchain.tokenengine.consumer;

import com.alibaba.fastjson.JSON;
import com.mchain.tokenengine.constants.RabbitConstant;
import com.mchain.tokenengine.dto.SyncBlockInfo;
import com.mchain.tokenengine.schedule.BlockNumberLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RabbitListener(bindings = @QueueBinding(exchange = @Exchange(value = RabbitConstant.EXCHANGE_SYNCNUMBER, type = "fanout"), value = @Queue(RabbitConstant.QUEUE_MAIN)))
public class SyncBlockNumberConsumer {

	@Value("${server.port}")
	private Integer port;

	private static AtomicInteger i = new AtomicInteger(0);

	@RabbitHandler
	public void handle(String body) {
		// 端口号8888为主任务
		i.getAndIncrement();
//		if (port != 8888) {
//			log.info("处理任务数：{}", i);
//			return;
//		}
		log.info("处理任务数：{}", i);
		SyncBlockInfo syncBlock;
		try {
			syncBlock = JSON.parseObject(body, SyncBlockInfo.class);
			log.info(" ====> 已同步消息区块，{}", syncBlock.getNumber());
			BlockNumberLock.save(syncBlock.getNumber());
		} catch (Exception e) {
			log.error("解析区块信息异常，{}", body);
			return;
		}
	}
}
