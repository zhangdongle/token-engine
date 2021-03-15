package com.mchain.tokenengine.utils.eth.utils;

import com.alibaba.fastjson.JSON;
import com.mchain.tokenengine.utils.eth.params.BalanceParams;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 以太坊 余额 工具类
 */
@Component
@Slf4j
public class EthBalanceUtil extends EthUtil {

	/**
	 * 获取余额
	 * @param params 获取余额参数
	 */
	public BigDecimal getBalance(Web3j web3j, BalanceParams params) {
		return getBalance(web3j, params, null);
	}

	/**
	 * 获取Eth余额
	 */
	public BigDecimal getEthBalance(Web3j web3j, BalanceParams params) {
		return getEthBalance(web3j, params, null);
	}

	/**
	 * 获取代币余额
	 */
	public BigDecimal getTokenBalance(Web3j web3j, BalanceParams params) {
		return getTokenBalance(web3j, params, null);
	}

	/**
	 * 获取余额
	 * @param params 获取余额参数
	 */
	public BigDecimal getBalance(Web3j web3j, BalanceParams params, DefaultBlockParameterName parameter) {
		BigDecimal balance;
		if (StringUtil.isBlank(params.getContractAddress())) {
			balance = getEthBalance(web3j, params, parameter);
		} else {
			balance = getTokenBalance(web3j, params, parameter);
		}
		return balance;
	}

	/**
	 * 获取Eth余额
	 */
	public BigDecimal getEthBalance(Web3j web3j, BalanceParams params, DefaultBlockParameterName parameter) {
		try {
			String address = params.getAddress();

			if (null == parameter) {
				parameter = DefaultBlockParameterName.LATEST;
			}
			EthGetBalance ethGetBalance = web3j.ethGetBalance(address, parameter).send();
			if (null != ethGetBalance.getError()) {
				String content = String.format("获取ETH余额失败! error=%s", JSON.toJSONString(ethGetBalance.getError()));
				throw new RuntimeException(content);
			}
			BigInteger balance = ethGetBalance.getBalance();
			return Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("获取ETH余额失败!");
		}
	}

	/**
	 * 获取代币余额
	 */
	public BigDecimal getTokenBalance(Web3j web3j, BalanceParams params, DefaultBlockParameterName parameter) {
		try {
			String methodName = "balanceOf";

			String address = params.getAddress();
			String contractAddress = params.getContractAddress();
			BigDecimal unit = params.getUnit();

			List<TypeReference<?>> outputParameters = new ArrayList<>();
			outputParameters.add(new TypeReference<Uint256>() {
			});

			List<Type> inputParameters = new ArrayList<>();
			inputParameters.add(new Address(address));

			Function function = new Function(methodName, inputParameters, outputParameters);
			String data = FunctionEncoder.encode(function);

			Transaction transaction = Transaction.createEthCallTransaction(address, contractAddress, data);

			if (null == parameter) {
				parameter = DefaultBlockParameterName.LATEST;
			}
			EthCall ethCall = web3j.ethCall(transaction, parameter).send();
			if (null != ethCall.getError()) {
				String content = String.format("获取Token余额失败! error=%s", JSON.toJSONString(ethCall.getError()));
				throw new RuntimeException(content);
			}
			List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
			BigInteger balanceValue = (BigInteger) results.get(0).getValue();

			if (null == params.getUnit() || BigDecimal.ZERO.compareTo(unit) <= 0) {
				unit = getTokenUnit(web3j, contractAddress);
			}

			return new BigDecimal(balanceValue).divide(unit);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("获取Token余额失败!");
		}
	}
}
