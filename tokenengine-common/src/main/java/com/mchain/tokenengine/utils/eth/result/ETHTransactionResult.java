package com.mchain.tokenengine.utils.eth.result;

import org.apache.commons.lang3.StringUtils;
import org.web3j.abi.TypeDecoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * 以太坊交易信息返回类
 */
public class ETHTransactionResult extends BlockchainETHResult {

    private boolean pending;

    private boolean success;

    private boolean token;

    /**
     * 该事务所在的块的hash
     */
    private String blockHash;

    /**
     * 该事物所在块的下标
     * 字符串
     */
    private BigInteger blockNumber;

    /**
     * 该事务的完成时间
     */
    private Date transferTime;

    /**
     * 块中交易指标位置的整数
     * 字符串
     */
    private BigInteger transactionIndex;

    /**
     * 交易Hash
     */
    private String hash;

    /**
     * 发送人在此之前的交易次数
     * 字符串
     */
    private BigInteger nonce;

    /**
     * 发送人地址
     */
    private String from;

    /**
     * 接收人地址
     */
    private String to;

    /**
     * 值
     */
    private BigDecimal value;

    /**
     * GasPrice
     */
    private BigInteger gasPrice;

    /**
     * GasLimit
     */
    private BigInteger gas;

    /**
     * UseGas
     */
    private BigInteger useGas;

    /**
     *  发起交易时附带的数据
     */
    private String input;

    /**
     *  分解input数据-在logs里
     */
    private Data inputData;

    /**
     * EventLog 记录合约的操作
     */
    private List<Log> logs;

    /**
     * 获取已使用的手续费
     */
    public BigDecimal getFee() {
        if(null != getGasPrice() && null != getUseGas()) {
            BigInteger weiFee = getGasPrice().multiply(getUseGas());
            return Convert.fromWei(weiFee.toString(), Convert.Unit.ETHER);
        }
        return BigDecimal.ZERO;
    }

    /**
     * 获取开始设置的手续费
     */
    public BigDecimal getOriginalFee() {
        if(null != getGasPrice() && null != getGas()) {
            BigInteger weiFee = getGasPrice().multiply(getGas());
            return Convert.fromWei(weiFee.toString(), Convert.Unit.ETHER);
        }
        return BigDecimal.ZERO;
    }

    public ETHTransactionResult(Transaction transaction, TransactionReceipt transactionReceipt) {
        this.pending = false;
        this.success = false;
        if(null != transaction) {
            this.token = false;
            if(StringUtils.isNotBlank(transaction.getInput()) && transaction.getInput().length() == 138) {
                String method = transaction.getInput().substring(0, 10);
                if("0xa9059cbb".equals(method)){
                    this.token = true;
                }
            }
            if(StringUtils.isNotBlank(transaction.getBlockHash().replaceFirst("^(0x)?0*", ""))) {
                this.transactionIndex = transaction.getTransactionIndex();
                this.blockHash = transaction.getBlockHash();
                this.blockNumber = transaction.getBlockNumber();
                this.hash = transaction.getHash();
                this.nonce = transaction.getNonce();
                this.from = transaction.getFrom();
                this.to = transaction.getTo();
                this.value = Convert.fromWei(transaction.getValue().toString(), Convert.Unit.ETHER);
                this.gas = transaction.getGas();
                this.gasPrice = transaction.getGasPrice();
                this.input = transaction.getInput();
                if(null != transactionReceipt) {
                    this.success = transactionReceipt.isStatusOK();
                    this.useGas = transactionReceipt.getGasUsed();
                    this.logs = transactionReceipt.getLogs();
                    if(isToken()) {
                        this.success = transactionReceipt.getLogs() != null && transactionReceipt.getLogs().size() > 0;
                        Data data = new Data();
                        String input = transaction.getInput();
                        String toStr = input.substring(10,74);
                        String valueStr = input.substring(74);
                        try {
                            Method refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
                            refMethod.setAccessible(true);

                            Address address = (Address)refMethod.invoke(null, toStr, 0, Address.class);
                            Uint256 unitValue = (Uint256) refMethod.invoke(null, valueStr, 0, Uint256.class);

                            String toAddress =address.toString();
                            BigInteger weiValue = unitValue.getValue();

                            data.setContractAddress(getTo());
                            data.setToAddress(toAddress);
                            data.setWeiValue(weiValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        this.inputData = data;
                    }
                } else {
                    this.pending = true;
                }
            } else {
                this.pending = true;
                this.success = false;
            }
        }
    }

    public class Data {

        private String contractAddress;

        private String toAddress;

        private BigInteger weiValue;

        public Data() {
        }

        public Data(Log log) {
            if(null != log) {
                this.contractAddress = log.getAddress();
                List<String> topics = log.getTopics();
                if(topics.size() == 3) {
                    this.toAddress = log.getTopics().get(2);
                    this.weiValue = Numeric.decodeQuantity(log.getData());
                }
            }
        }

        public String getContractAddress() {
            return contractAddress;
        }

        public void setContractAddress(String contractAddress) {
            this.contractAddress = contractAddress;
        }

        public String getToAddress() {
            return toAddress;
        }

        public void setToAddress(String toAddress) {
            this.toAddress = toAddress;
        }

        public BigInteger getWeiValue() {
            return weiValue;
        }

        public void setWeiValue(BigInteger weiValue) {
            this.weiValue = weiValue;
        }
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isToken() {
        return token;
    }

    public void setToken(boolean token) {
        this.token = token;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Date getTransferTime() {
        return transferTime;
    }

    public void setTransferTime(Date transferTime) {
        this.transferTime = transferTime;
    }

    public BigInteger getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(BigInteger transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigInteger getGas() {
        return gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    public BigInteger getUseGas() {
        return useGas;
    }

    public void setUseGas(BigInteger useGas) {
        this.useGas = useGas;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Data getInputData() {
        return inputData;
    }

    public void setInputData(Data inputData) {
        this.inputData = inputData;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }
}
