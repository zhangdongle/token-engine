package com.mchain.tokenengine.utils.eth.utils;

import com.alibaba.fastjson.JSON;
import com.mchain.tokenengine.utils.eth.result.ETHTransactionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.IntStream;

/**
 * 以太坊获取交易信息 工具类
 */
@Component
@Slf4j
public class EthTransactionInfoUtil extends EthUtil {

	/**
	 * 已确认的交易详情
	 */
	public TransactionReceipt getTransactionReceipt(Web3j web3j, String transactionHash) {
		try {
			EthGetTransactionReceipt result = web3j.ethGetTransactionReceipt(transactionHash).send();
			if (null != result.getError()) {
				String content = String.format("获取交易详情Receipt失败! error=%s", JSON.toJSONString(result.getError()));
				throw new RuntimeException(content);
			}
			return result.getTransactionReceipt().orElse(null);
		} catch (NoSuchElementException e) {
			// 该交易在pending中
			return new TransactionReceipt();
		} catch (Exception e) {
//			log.error("获取交易详情Receipt失败!", e);
			throw new RuntimeException("获取交易详情Receipt失败!");
		}
	}

	/**
	 * 根据Hash获取交易
	 */
	public Transaction getTransactionByHash(Web3j web3j, String transactionHash) {
		try {
			EthTransaction result = web3j.ethGetTransactionByHash(transactionHash).send();
			if (null != result.getError()) {
				String content = String.format("根据Hash获取交易失败! error=%s", JSON.toJSONString(result.getError()));
				throw new RuntimeException(content);
			}
			return result.getTransaction().orElse(null);
		} catch (Exception e) {
//			e.printStackTrace();
			throw new RuntimeException("根据Hash获取交易失败!");
		}
	}

	/**
	 * 获取区块信息
	 */
	public EthBlock getBlockByBlockNumber(Web3j web3j, BigInteger blockNumber, boolean returnFullTransaction) {
		try {
			return web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), returnFullTransaction).send();
		} catch (Exception e) {
			String content = String.format("获取区块信息失败! blockNumber = %d", blockNumber);
			//			e.printStackTrace();
			throw new RuntimeException(content);
		}
	}

	/**
	 * 获取区块中的所有交易
	 */
	public List<Transaction> getTransactionByBlockNumber(Web3j web3j, BigInteger blockNumber) {
		try {
			List<Transaction> transactions = new ArrayList<>();
			BigInteger transactionCount = getBlockTransactionCountByNumber(web3j, blockNumber);
			IntStream.rangeClosed(BigInteger.ZERO.intValue(), transactionCount.intValue()).forEach(i -> {
				transactions.add(getTransactionByBlockNumberAndIndex(web3j, blockNumber, i));
			});
			return transactions;
		} catch (Exception e) {
//			e.printStackTrace();
			throw new RuntimeException("获取区块中的所有交易失败!");
		}
	}

	/**
	 * 根据区块高度和交易索引获取交易详情
	 */
	public Transaction getTransactionByBlockNumberAndIndex(Web3j web3j, BigInteger blockNumber, int index) {
		try {
			EthTransaction result = web3j
					.ethGetTransactionByBlockNumberAndIndex(DefaultBlockParameter.valueOf(blockNumber),
							BigInteger.valueOf(index)).send();
			if (null != result.getError()) {
				String content = String.format("根据区块高度和交易索引获取交易详情失败! error=%s", JSON.toJSONString(result.getError()));
				throw new RuntimeException(content);
			}
			return result.getTransaction().orElse(new Transaction());
		} catch (Exception e) {
//			e.printStackTrace();
			throw new RuntimeException("根据区块高度和交易索引获取交易详情失败!");
		}
	}

	/**
	 * 获取区块中交易的数量
	 */
	public BigInteger getBlockTransactionCountByNumber(Web3j web3j, BigInteger blockNumber) {
		try {
			EthGetBlockTransactionCountByNumber result = web3j
					.ethGetBlockTransactionCountByNumber(DefaultBlockParameter.valueOf(blockNumber)).send();
			if (null != result.getError()) {
				String content = String.format("获取区块中交易数量失败! error=%s", JSON.toJSONString(result.getError()));
				throw new RuntimeException(content);
			}
			return result.getTransactionCount();
		} catch (Exception e) {
//			e.printStackTrace();
			throw new RuntimeException("获取区块中交易数量失败!");
		}
	}

	/**
	 * 获取交易详情
	 */
	public ETHTransactionResult getTransactionAllInfo(Web3j web3j, String transactionHash) {
		try {
			ETHTransactionResult result = new ETHTransactionResult(getTransactionByHash(web3j, transactionHash),
					getTransactionReceipt(web3j, transactionHash));
			if (result.isSuccess()) {
				EthBlock block = getBlockByBlockNumber(web3j, result.getBlockNumber(), false);
				if (null != block.getError()) {
					String content = String.format("根据获取交易详情失败! error=%s", JSON.toJSONString(block.getError()));
					throw new RuntimeException(content);
				}
				try {
					// 完成交易时间
					Date transferTime = new Date(
							block.getBlock().getTimestamp().multiply(BigInteger.valueOf(1000)).longValue());
					result.setTransferTime(transferTime);
				} catch (NullPointerException e) {
					log.error("交易时间为空", e);
					result.setTransferTime(new Date());
				}
			}
			return result;
		} catch (Exception e) {
//			log.error("获取交易详情异常", e);
			throw new RuntimeException("根据获取交易详情失败!");
		}
	}

}
