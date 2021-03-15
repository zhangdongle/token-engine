package com.mchain.tokenengine.utils.eth.result;

/**
 * 以太坊 RPC 接口统一返回类
 */
public class BlockchainETHResult  {

    private String jsonrpc;

    private Integer id;

    private String result;

    private Object error;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

}
