package com.mchain.tokenengine.config;

import com.mchain.tokenengine.utils.eth.utils.EthTransactionInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class Web3jPool {

	private static final int initCount = 5;
	private static final int minCount = 3;
	private static final int maxCount = 10;
	private static final int basecount = 5;
	private static final int timeoutcount = 100;// 超时次数

	private static Queue<Web3j> syncBlockList = new ConcurrentLinkedQueue<>();

	private static EthTransactionInfoUtil ethTransactionInfoUtil;

	@Autowired
	public void setDatastore(EthTransactionInfoUtil ethTransactionInfoUtil) {
		Web3jPool.ethTransactionInfoUtil = ethTransactionInfoUtil;
		init();
	}

	//	static {
	////		ethTransactionInfoUtil = SpringContextUtil.getBean(EthTransactionInfoUtil.class);
	//
	//	}

	/**
	 * 获取Web3j连接对象
	 * @return
	 */
	public static Web3j pool() {
		synchronized (syncBlockList) {
			final int size = syncBlockList.size();
			// 小于最小连接数，则再创建一定的连接
			if (size < minCount) {
				log.info("Web3j连接小于最低值，min-size：{}，size：{}", minCount, size);
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < basecount; i++) {
							Web3j web3j = ethTransactionInfoUtil.getWeb3j();
							syncBlockList.add(web3j);
						}
					}
				});
			}
		}
		int count = 0;
		while (true) {
			synchronized (syncBlockList) {
				if (syncBlockList.size() > 0) {
					return syncBlockList.poll();
				}
			}
			count++;
			try {
				Thread.sleep(200L);
			} catch (InterruptedException e) {
				log.error("Web3jPool 中断异常", e);
				return null;
			}

			if (count >= timeoutcount) {
				log.error("Web3jPool 获取连接超时", count);
				return null;
			}
		}
	}

	/**
	 * 初始化Web3j连接池
	 */
	private static void init() {
		log.info("初始化Web3j连接，size：{}", initCount);
		synchronized (syncBlockList) {
			for (int i = 0; i < initCount; i++) {
				try {
					Web3j web3j = ethTransactionInfoUtil.getWeb3j();
					syncBlockList.add(web3j);
				} catch (Exception e) {
					log.error("初始化Web3jPoll异常", e);
				}
			}
		}
	}

	/**
	 * 回收Web3j连接
	 * @param web3j
	 */
	public static void push(Web3j web3j) {
		log.info("回收Web3j连接");
		synchronized (syncBlockList) {
			if (syncBlockList.size() > maxCount) {
				web3j.shutdown();
			} else {
				syncBlockList.add(web3j);
			}
		}
	}
}
