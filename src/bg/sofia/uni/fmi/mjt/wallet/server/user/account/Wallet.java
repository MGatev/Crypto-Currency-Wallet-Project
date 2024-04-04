package bg.sofia.uni.fmi.mjt.wallet.server.user.account;

import bg.sofia.uni.fmi.mjt.wallet.server.Response;

import java.io.Serializable;

public class Wallet implements Serializable {
    private double balance;

    public Wallet(double balance) {
        this.balance = balance;
    }

    public Response addMoney(double money) {
        if (money < 0.0) {
            return Response.decline("You cannot add negative amount of money.");
        }

        balance += money;
        return Response.ok("You have successfully added " + money + "$ to your wallet.");
    }

    public void withdrawMoney(double money) {
        if (money < 0.0) {
            return;
        }

        balance -= money;
    }

    public double getBalance() {
        return balance;
    }
}
