package bg.sofia.uni.fmi.mjt.wallet.server.user.account;

import java.io.Serializable;

public class Order implements Serializable {
    private final String id;
    private final double amount;
    private final double boughtPrice;
    private double soldPrice;

    public Order(String id, double amount, double boughtPrice, double soldPrice) {
        this.id = id;
        this.amount = amount;
        this.boughtPrice = boughtPrice;
        this.soldPrice = soldPrice;
    }

    public double getAmount() {
        return amount;
    }

    public double getBoughtPrice() {
        return boughtPrice;
    }

    public double getSoldPrice() {
        return soldPrice;
    }

    public void setSoldPrice(double soldPrice) {
        this.soldPrice = soldPrice;
    }

    @Override
    public String toString() {
        return "Order=[" + "id=" + id + ", amount=" + amount + ", boughtPrice=" + boughtPrice +
            "]";
    }
}
