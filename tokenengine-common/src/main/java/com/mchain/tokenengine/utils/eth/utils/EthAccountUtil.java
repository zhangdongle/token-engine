package com.mchain.tokenengine.utils.eth.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mchain.tokenengine.utils.eth.exception.AddressMismatchException;
import com.mchain.tokenengine.utils.eth.exception.PrivateKeyErrorException;
import com.mchain.tokenengine.utils.eth.exception.PrivateKeyFormatException;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 以太坊 账号 工具类
 * 需要在配置文件定义 eth.keystore.path={PATH} 默认: /keystore
 * 设置keystore存放路径
 */
@Component
@Slf4j
public class EthAccountUtil {
	private final String DEFAULT_PATH = "/keystore";
	@Value("${eth.keystore.path}")
	private String KEYSTORE_PATH;
	@Value("${eth.keystore.password}")
	private String PASSWORD;

	public String defaultNewAccount() {
		return newAccountToKeystore(PASSWORD);
	}

	/**
	 * 创建地址 - keystore
	 */
	public String newAccountToKeystore(String password) {
		try {
			if (StringUtil.isBlank(KEYSTORE_PATH)) {
				KEYSTORE_PATH = DEFAULT_PATH;
			}
			File dir = ResourceUtils.getFile(KEYSTORE_PATH);
			if (!dir.exists()) {
				// 文件夹不存在, 创建
				boolean result = dir.mkdir();
				if (!result) {
					throw new RuntimeException("创建Keystore文件夹失败!");
				}
			}
			// 生成 publicKey 和 privateKey
			ECKeyPair ecKeyPair = Keys.createEcKeyPair();
			// 生成钱包
			WalletFile walletFile = Wallet.createLight(password, ecKeyPair);
			String address = new StringBuffer(walletFile.getAddress()).insert(0, "0x").toString().toLowerCase();
			// 生成 keystore 文件名
			DateTimeFormatter format = DateTimeFormatter.ofPattern("'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
			ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
			String fileName = now.format(format) + address + ".json";
			// 生成文件
			File destination = new File(dir, fileName);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(destination, walletFile);
			return address;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("创建地址并生成keystore失败!");
		}
	}

	/**
	 * 所有信息(私钥, 地址) - keystore
	 * @return {"privateKey" : "", "address" : ""}
	 */
	public Map<String, String> newAccountFull(String password) {
		try {
			if (StringUtil.isBlank(KEYSTORE_PATH)) {
				KEYSTORE_PATH = DEFAULT_PATH;
			}
			File dir = ResourceUtils.getFile(KEYSTORE_PATH);
			if (!dir.exists()) {
				// 文件夹不存在, 创建
				boolean result = dir.mkdir();
				if (!result) {
					throw new RuntimeException("创建Keystore文件夹失败!");
				}
			}
			// 生成 publicKey 和 privateKey
			ECKeyPair ecKeyPair = Keys.createEcKeyPair();
			// 生成钱包
			WalletFile walletFile = Wallet.createLight(password, ecKeyPair);
			String address = new StringBuffer(walletFile.getAddress()).insert(0, "0x").toString().toLowerCase();
			// 生成 keystore 文件名
			DateTimeFormatter format = DateTimeFormatter.ofPattern("'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
			ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
			String fileName = now.format(format) + address + ".json";
			// 生成文件
			File destination = new File(dir, fileName);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(destination, walletFile);

			Map<String, String> result = new HashMap<>();
			result.put("privateKey", ecKeyPair.getPrivateKey().toString(16));
			result.put("address", address);
			return result;
		} catch (Exception e) {
			log.error("创建地址并生成keystore失败!");
			e.printStackTrace();
			throw new RuntimeException("创建地址并生成keystore失败!");
		}
	}

	/**
	 * 导入私钥
	 */
	public boolean importPrivateKey(String privateKey, String address, String password)
			throws PrivateKeyFormatException, PrivateKeyErrorException, AddressMismatchException, IOException {
		File dir = ResourceUtils.getFile(KEYSTORE_PATH);
		if (!dir.exists()) {
			// 文件夹不存在, 创建
			boolean result = dir.mkdir();
			if (!result) {
				throw new RuntimeException("创建Keystore文件夹失败!");
			}
		}
		privateKey = privateKey.replaceFirst("0x", "");
		BigInteger key;
		try {
			key = new BigInteger(privateKey, 16);
		} catch (Exception e) {
			throw new PrivateKeyFormatException("私钥格式错误! privateKey=" + privateKey);
		}
		ECKeyPair ecKeyPair = ECKeyPair.create(key);
		// 生成钱包
		WalletFile walletFile;
		try {
			walletFile = Wallet.createStandard(password, ecKeyPair);
		} catch (Exception e) {
			throw new PrivateKeyErrorException("私钥错误, 生成钱包失败! privateKey=" + privateKey);
		}
		String generateAddress = new StringBuffer(walletFile.getAddress()).insert(0, "0x").toString();
		if (!address.equalsIgnoreCase(generateAddress)) {
			throw new AddressMismatchException("地址与私钥不匹配! 私钥中的地址为:" + generateAddress);
		}
		File[] files = dir.listFiles((file, name) -> {
			String fileName = name.toLowerCase();
			String addressStr = address.toLowerCase();
			return fileName.contains(addressStr);
		});
		if (files != null && files.length > 0) {
			log.info("keystore 已存在无需创建!");
			return true;
		}
		// 生成 keystore 文件名
		DateTimeFormatter format = DateTimeFormatter.ofPattern("'UTC--'yyyy-MM-dd'T'HH-mm-ss.nVV'--'");
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		String fileName = now.format(format) + address + ".json";
		// 生成文件
		File destination = new File(dir, fileName);
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.writeValue(destination, walletFile);
		return true;
	}

}
