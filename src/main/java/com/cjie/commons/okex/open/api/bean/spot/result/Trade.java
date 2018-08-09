package com.cjie.commons.okex.open.api.bean.spot.result;

public class Trade {

    /**
     * 妙计时间
     */
    //private Long date;
    /**
     * 毫秒级时间
     */
    private String time;
    /**
     * 价格信息
     */
    private String price;
    /**
     * 数量
     */
    private String size;
    /**
     * id 交易id
     */
    private Integer trade_id;
    /**
     * 买卖信息 buy sell
     */
    private String side;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public Integer getTrade_id() {
        return trade_id;
    }

    public void setTrade_id(Integer trade_id) {
        this.trade_id = trade_id;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }
}
