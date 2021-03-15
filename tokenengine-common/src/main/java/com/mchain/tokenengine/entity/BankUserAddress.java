package com.mchain.tokenengine.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.mchain.tokenengine.common.SuperModelU;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author zhl
 * @since 2019-10-23
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("tb_bank_user_address")
public class BankUserAddress extends SuperModelU<BankUserAddress> {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private String userId;
    /**
     * 账户ID
     */
    @TableField("account_id")
    private String accountId;
    /**
     * 链上地址
     */
    private String address;
    /**
     * 0.已通知，1.已创建，2.已通知
     */
    private Integer status;

	/**
	 * 币种ID
	 */
	@TableField("coin_id")
	private Integer coinId;


    public static final String USER_ID = "user_id";

    public static final String ACCOUNT_ID = "account_id";

    public static final String ADDRESS = "address";

    public static final String STATUS = "status";

    public static final String COIN_ID = "coin_id";

}
