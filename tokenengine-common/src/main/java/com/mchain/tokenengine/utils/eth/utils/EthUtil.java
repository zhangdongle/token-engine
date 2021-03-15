package com.mchain.tokenengine.utils.eth.utils;

import com.alibaba.fastjson.JSON;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ChainId;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 以太坊 基础 工具类
 * 需要在配置文件定义
 * - keystore存放路径   eth.keystore.path={PATH} 默认: /keystore
 * - 以太坊网络  eth.network.chainId={RINKEBY:4} 默认: 无(主网)
 * - Infura服务器地址    eth.client.infura={URL} 默认: https://rinkeby.infura.io/cBlY1kPP7L4EGm13e6tX(RINKEBY网络)
 */
@Component
@Slf4j
public class EthUtil {
	private final String DEFAULT_PATH = "/keystore";
	private final String DEFAULT_INFURA = "https://mainnet.infura.io/cBlY1kPP7L4EGm13e6tX";
	private final String DEFAULT_RINKEBY_INFURA = "https://rinkeby.infura.io/cBlY1kPP7L4EGm13e6tX";

	@Value("${eth.keystore.path}")
	String KEYSTOREPATH;
	@Value("${eth.network.chainId}")
	Byte chainId;
	@Value("${eth.client.infura}")
	String infuraUrl;

	/**
	 * 获取 WEB3J 实例
	 */
	public Web3j getWeb3j() {
		if (StringUtil.isBlank(infuraUrl)) {
			if (chainId > ChainId.MAINNET) {
				infuraUrl = DEFAULT_RINKEBY_INFURA;
			} else {
				infuraUrl = DEFAULT_INFURA;
			}
		}
		Web3j web3j = Web3j.build(new HttpService(infuraUrl));
		return web3j;
	}

	/**
	 * 获取 keystore 存放路径
	 */
	public String getKeyStorePath() {
		if (StringUtil.isBlank(KEYSTOREPATH)) {
			KEYSTOREPATH = DEFAULT_PATH;
		}
		return KEYSTOREPATH;
	}

	/**
	 * 返回当前最高区块
	 */
	public BigInteger getBlockNumber(Web3j web3j) {
		try {
			EthBlockNumber result = web3j.ethBlockNumber().send();
			if (null != result.getError()) {
				String content = String.format("获取最高区块高度失败! error=%s", JSON.toJSONString(result.getError()));
				throw new RuntimeException(content);
			}
			return result.getBlockNumber();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("获取最高区块高度失败!");
		}
	}

	/**
	 * 获取代币精度
	 */
	public BigDecimal getTokenUnit(Web3j web3j, String contractAddress) {
		try {
			String methodName = "decimals";

			List<Type> inputParameters = new ArrayList<>();
			List<TypeReference<?>> outputParameters = new ArrayList<>();
			outputParameters.add(new TypeReference<Uint8>() {
			});

			Function function = new Function(methodName, inputParameters, outputParameters);
			String data = FunctionEncoder.encode(function);

			Transaction transaction = Transaction.createEthCallTransaction(contractAddress, contractAddress, data);

			EthCall ethCall = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST).sendAsync().get();
			if (null != ethCall.getError()) {
				String content = String.format("获取Token精度失败! error=%s", JSON.toJSONString(ethCall.getError()));
				throw new RuntimeException(content);
			}
			List<Type> results = FunctionReturnDecoder.decode(ethCall.getValue(), function.getOutputParameters());
			if (results.size() < 1) {
				throw new RuntimeException("合约不存在!");
			}
			Integer decimal = Integer.parseInt(results.get(0).getValue().toString());

			BigDecimal unit = BigDecimal.TEN.pow(decimal);
			return unit;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("获取Token精度失败!");
		}
	}

}
