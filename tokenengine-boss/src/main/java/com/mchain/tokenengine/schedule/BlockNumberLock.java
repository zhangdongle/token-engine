package com.mchain.tokenengine.schedule;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockNumberLock {

	public static volatile Integer lock = 0;

	// 最小区块
	private static Integer minBlock = -1;

	// 当前队列中最大的区块高度
	private static volatile Integer maxBlock = -1;

	// 已同步完成最大的区块高度
	private static volatile Integer saveBlock = -1;

	// 等待同步的区块
	public static final Queue<Integer> syncBlockList = new ConcurrentLinkedQueue();

	// 已同步的区块
	public static final Queue<Integer> saveBlockList = new ConcurrentLinkedQueue();

	public static void push(List<Integer> list) {
		if (list.size() > 0) {
			Integer maxBlockNumber = list.stream().max(Integer::compareTo).get();
			if (maxBlockNumber > maxBlock) {
				maxBlock = maxBlockNumber;
			}
		}
		// 添加待同步区块队列
		syncBlockList.addAll(list);
	}

	public static Integer poll() {
		return syncBlockList.poll();
	}

	//	public static List<Integer> getSave() {
	//		return saveBlockList;
	//	}

	public static Integer syncSize() {
		return syncBlockList.size();
	}

	public static Integer saveSize() {
		return saveBlockList.size();
	}

	public static void save(Integer number) {
		synchronized (saveBlock) {
			if (saveBlock == -1) {
				saveBlock = number;
			} else if (number > saveBlock) {
				saveBlock = number;
			}
			// 从待同步区块列表移除，并保存到已同步区块队列中
			syncBlockList.remove(number);
			saveBlockList.add(number);
		}
	}

	public static Integer getSaveNumber() {
		return saveBlock;
	}

	public static Integer getMaxNumber() {
		return maxBlock;
	}
}
