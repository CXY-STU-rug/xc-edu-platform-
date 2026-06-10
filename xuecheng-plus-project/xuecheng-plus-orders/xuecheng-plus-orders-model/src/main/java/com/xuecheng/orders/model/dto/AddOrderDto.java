package com.xuecheng.orders.model.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @version 1.0
 * @description 创建商品订单
 */
@Data
@ToString
public class AddOrderDto  {

    /**
     * 总价
     */
    @NotNull(message = "订单总价不能为空")
    @DecimalMin(value = "0", message = "订单总价不能为负数") //免费课价格为 0，故下限取 0
    private Float totalPrice;

    /**
     * 订单类型
     */
    @NotEmpty(message = "订单类型不能为空")
    private String orderType;

    /**
     * 订单名称
     */
    @NotEmpty(message = "订单名称不能为空")
    private String orderName;
    /**
     * 订单描述
     */
    private String orderDescrip;

    /**
     * 订单明细json，不可为空
     * [{"goodsId":"","goodsType":"","goodsName":"","goodsPrice":"","goodsDetail":""},{...}]
     */
    @NotEmpty(message = "订单明细不能为空") //xc_orders.order_detail 列 NOT NULL，缺失会落库失败（README 缺陷9）
    private String orderDetail;

    /**
     * 外部系统业务id
     */
    @NotEmpty(message = "外部业务id不能为空") //选课记录id，幂等去重的依据
    private String outBusinessId;

}
