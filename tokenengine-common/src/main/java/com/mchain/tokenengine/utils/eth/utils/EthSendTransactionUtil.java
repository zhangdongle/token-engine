package com.mchain.tokenengine.utils.eth.utils;

import com.alibaba.fastjson.JSON;
import com.mchain.tokenengine.utils.eth.exception.AmountTooSmallException;
import com.mchain.tokenengine.utils.eth.params.SendTransactionParams;
import com.mchain.tokenengine.utils.eth.result.ETHFeeResult;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 以太坊发送交易 工具类
 */
@Component
@Slf4j
public class EthSendTransactionUtil extends EthUtil {

	/**
	 * 发送全部ETH交易
	 */
	public EthSendTransaction sendAllEth(Web3j web3j, SendTransactionParams params) throws AmountTooSmallException {
		try {
			// 获取预计手续费信息
			ETHFeeResult feeResult = getTransactionFee(web3j, params);

			// 获取余额
			EthGetBalance ethGetBalance = web3j.ethGetBalance(params.getFromAddress(), DefaultBlockParameterName.LATEST)
					.send();
			if (null != ethGetBalance.getError()) {
				String content = String.format("获取ETH余额失败! error=%s", JSON.toJSONString(ethGetBalance.getError()));
				log.error(content);
				throw new RuntimeException(content);
			}
			BigInteger weiBalance = ethGetBalance.getBalance();
			BigDecimal balance = Convert.fromWei(new BigDecimal(weiBalance), Convert.Unit.ETHER);

			// 实际转账的数量
			BigDecimal amount = balance.subtract(feeResult.getTransactionFee());
			if (amount.compareTo(BigDecimal.ZERO) < 1) {
				String content = String
						.format("地址 %s 余额不足手续费消耗! balance=%g, feeResult=%s", params.getFromAddress(), balance,
								JSON.toJSON(feeResult));
				log.error(content);
				throw new AmountTooSmallException(content);
			}

			params.setAmount(amount);
			params.setGasPrice(params.getGasPrice());
			params.setGasLimit(params.getGasLimit());
			return sendEthTransaction(web3j, params);
		} catch (Exception e) {
			log.error("发送全部ETH交易失败!");
			e.printStackTrace();
			throw new RuntimeException("发送全部ETH交易失败!");
		}
	}

	/**
	 * 发送交易
	 */
	public EthSendTransaction sendTransaction(Web3j web3j, SendTransactionParams transactionParams) {
		try {
			SendTransactionParams params = (SendTransactionParams) BeanUtils.cloneBean(transactionParams);

			EthSendTransaction ethSendTransaction;
			if (StringUtils.isBlank(params.getContractAddress())) {
				ethSendTransaction = sendEthTransaction(web3j, params);
			} else {
				ethSendTransaction = sendTokenTransaction(web3j, params);
			}
			return ethSendTransaction;
		} catch (Exception e) {
			log.error("发送交易失败!");
			e.printStackTrace();
			throw new RuntimeException("发送交易失败!");
		}
	}

	/**
	 * 发送Eth交易
	 */
	public EthSendTransaction sendEthTransaction(Web3j web3j, SendTransactionParams params) {
		try {
			String signedData = signTransaction(web3j, params, chainId);
			return sendRawTransaction(web3j, signedData);
		} catch (Exception e) {
			log.error("发送ETH交易失败!");
			e.printStackTrace();
			throw new RuntimeException("发送ETH交易失败!");
		}
	}

	/**
	 * 发送Token交易
	 */
	public EthSendTransaction sendTokenTransaction(Web3j web3j, SendTransactionParams transactionParams) {
		try {
			SendTransactionParams params = (SendTransactionParams) BeanUtils.cloneBean(transactionParams);
			String data = getTransactionData(web3j, params);
			params.setData(data);
			params.setAmount(BigDecimal.ZERO);
			params.setToAddress(params.getContractAddress());
			String signedData = signTransaction(web3j, params, chainId);
			transactionParams.setGasLimit(params.getGasLimit());
			transactionParams.setGasPrice(params.getGasPrice());
			return sendRawTransaction(web3j, signedData);
		} catch (Exception e) {
			log.error("发送Token交易失败!");
			e.printStackTrace();
			throw new RuntimeException("发送Token交易失败!");
		}
	}

	/**
	 * 发送已签名的交易
	 */
	public EthSendTransaction sendRawTransaction(Web3j web3j, String signedData) {
		try {
			if (StringUtil.isNotBlank(signedData)) {
				return web3j.ethSendRawTransaction(signedData).send();
			} else {
				log.error("交易签名为空!");
				throw new RuntimeException("交易签名为空!");
			}
		} catch (Exception e) {
			log.error("发送签名交易失败!");
			e.printStackTrace();
			throw new RuntimeException("发送签名交易失败!");
		}
	}

	/**
	 * 签名交易
	 */
	public String signTransaction(Web3j web3j, SendTransactionParams params, byte chainId) {
		try {
			byte[] signedMessage;
			Credentials credentials = getCredentials(params.getFromAddress(), params.getPassword());
			BigInteger value = Convert.toWei(params.getAmount(), Convert.Unit.ETHER).toBigInteger();
			BigInteger nonce = getNonce(web3j, params.getFromAddress());

			BigInteger gasPrice;
			BigInteger gasLimit;
			if (null == params.getGasPrice()) {
				gasPrice = getGasPrice(web3j);
			} else {
				gasPrice = params.getGasPrice();
			}
			if (null == params.getGasLimit()) {
				gasLimit = getGasLimit(web3j, params);
			} else {
				gasLimit = params.getGasLimit();
			}
			params.setGasLimit(gasLimit);
			params.setGasPrice(gasPrice);
			RawTransaction rawTransaction = RawTransaction
					.createTransaction(nonce, gasPrice, gasLimit, params.getToAddress(), value, params.getData());
			if (chainId > ChainId.MAINNET) {
				// 测试网络
				signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
			} else {
				signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
			}
			return Numeric.toHexString(signedMessage);
		} catch (Exception e) {
			log.error("交易签名失败!");
			e.printStackTrace();
			throw new RuntimeException("交易签名失败!");
		}
	}

	/**
	 * 从钱包文件加载凭据(离线交易签名)
	 */
	public Credentials getCredentials(String address, String password) {
		try {
			File dir = ResourceUtils.getFile(getKeyStorePath());
			if (!dir.exists()) {
				log.error("keystore文件夹不存在!");
				throw new RuntimeException("keystore文件夹不存在!");
			}
			File[] files = dir.listFiles((file, name) -> {
				String fileName = name.toLowerCase();
				String addressStr = address.toLowerCase();
				return fileName.contains(addressStr);
			});
			if (null == files || files.length < 1) {
				log.error("keystore不存在!");
				throw new RuntimeException("keystore不存在!");
			}
			return WalletUtils.loadCredentials(password, files[0]);
		} catch (Exception e) {
			log.error("从keystore加载凭据失败!");
			e.printStackTrace();
			throw new RuntimeException("从keystore加载凭据失败!");
		}
	}

	/**
	 * 格式化发送合约交易的 data
	 */
	public String getTransactionData(Web3j web3j, SendTransactionParams params) {
		try {
			String methodName = "transfer";
			if (StringUtils.isNotBlank(params.getMethod())) {
				methodName = params.getMethod();
			}

			List<TypeReference<?>> outputParameters = new ArrayList<>();
			TypeReference<Bool> typeReference = new TypeReference<Bool>() {
			};
			outputParameters.add(typeReference);

			BigDecimal amount = params.getAmount();
			BigDecimal unit = params.getUnit();
			if (null == params.getUnit() || BigDecimal.ZERO.compareTo(unit) <= 0) {
				unit = getTokenUnit(web3j, params.getContractAddress());
			}
			Address toAddress = new Address(params.getToAddress());
			Uint256 tokenValue = new Uint256(amount.multiply(unit).toBigInteger());

			List<Type> inputParameters = new ArrayList<>();
			inputParameters.add(toAddress);
			inputParameters.add(tokenValue);

			Function function = new Function(methodName, inputParameters, outputParameters);
			return FunctionEncoder.encode(function);
		} catch (Exception e) {
			log.error("生成交易DATA失败!");
			e.printStackTrace();
			throw new RuntimeException("生成交易DATA失败!");
		}
	}

	/**
	 * 获取GasLimit
	 */
	public BigInteger getGasLimit(Web3j web3j, SendTransactionParams transactionParams) {
		try {
			SendTransactionParams params = (SendTransactionParams) BeanUtils.cloneBean(transactionParams);
			params.setAmount(BigDecimal.ZERO);
			Transaction transaction;
			if (StringUtils.isBlank(params.getContractAddress())) {
				BigInteger value = Convert.toWei(params.getAmount(), Convert.Unit.ETHER).toBigInteger();
				transaction = Transaction
						.createEtherTransaction(params.getFromAddress(), null, null, null, params.getToAddress(),
								value);
			} else {
				String data;
				if (StringUtils.isNotBlank(params.getData())) {
					data = params.getData();
				} else {
					data = getTransactionData(web3j, params);
				}
				transaction = Transaction.createFunctionCallTransaction(params.getFromAddress(), null, null, null,
						params.getContractAddress(), data);
			}
			EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
			if (null != ethEstimateGas.getError()) {
				String content = String
						.format("计算交易的GasLimit失败! error=%s", JSON.toJSONString(ethEstimateGas.getError()));
				log.error(content);
				throw new RuntimeException(content);
			}
			return ethEstimateGas.getAmountUsed();
		} catch (Exception e) {
			log.error("计算交易的GasLimit失败!");
			e.printStackTrace();
			throw new RuntimeException("计算交易的GasLimit失败!");
		}
	}

	/**
	 * 获取gasPrice 单位:wei
	 */
	public BigInteger getGasPrice(Web3j web3j) {
		try {
			EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
			if (null != ethGasPrice.getError()) {
				String content = String.format("获取GasPrice失败! error=%s", JSON.toJSONString(ethGasPrice.getError()));
				log.error(content);
				throw new RuntimeException(content);
			}
			return ethGasPrice.getGasPrice();
		} catch (Exception e) {
			log.error("获取GasPrice失败!");
			e.printStackTrace();
			throw new RuntimeException("获取GasPrice失败!");
		}
	}

	/**
	 * if=> 最小值 <= GasPrice <= 最大值
	 *   => GasPrice = GasPrice * multiple
	 * @param min 最小值
	 * @param max 最大值
	 * @param multiple 倍数
	 */
	public BigInteger getGasPrice(Web3j web3j, BigInteger min, BigInteger max, BigDecimal multiple) {
		try {
			BigInteger gasPrice = getGasPrice(web3j);
			if (gasPrice.compareTo(min) > -1 && gasPrice.compareTo(max) < 1) {
				gasPrice = multiple.multiply(BigDecimal.valueOf(gasPrice.longValue())).toBigInteger();
			}
			return gasPrice;
		} catch (Exception e) {
			log.error("获取GasPrice失败!");
			e.printStackTrace();
			throw new RuntimeException("获取GasPrice失败!");
		}
	}

	/**
	 * 获取交易预计手续费
	 * @param transactionParams 发送交易的参数
	 */
	public ETHFeeResult getTransactionFee(Web3j web3j, SendTransactionParams transactionParams) {
		try {
			SendTransactionParams params = (SendTransactionParams) BeanUtils.cloneBean(transactionParams);
			BigInteger gasPrice = getGasPrice(web3j);
			BigInteger gasLimit = getGasLimit(web3j, params);
			String weiFeeStr = gasPrice.multiply(gasLimit).toString();
			BigDecimal fee = Convert.fromWei(weiFeeStr, Convert.Unit.ETHER);
			return new ETHFeeResult(gasLimit, gasPrice, fee);
		} catch (Exception e) {
			log.error("获取交易预计使用手续费失败!");
			e.printStackTrace();
			throw new RuntimeException("获取交易预计使用手续费失败!");
		}
	}

	/**
	 * 获取地址的 noce
	 */
	public BigInteger getNonce(Web3j web3j, String address) {
		try {
			EthGetTransactionCount ethGetTransactionCount = web3j
					.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).sendAsync().get();
			if (null != ethGetTransactionCount.getError()) {
				String content = String
						.format("获取Nonce失败! error=%s", JSON.toJSONString(ethGetTransactionCount.getError()));
				log.error(content);
				throw new RuntimeException(content);
			}
			return ethGetTransactionCount.getTransactionCount();
		} catch (Exception e) {
			log.error("获取Nonce失败!");
			e.printStackTrace();
			throw new RuntimeException("获取Nonce失败!");
		}
	}

}
