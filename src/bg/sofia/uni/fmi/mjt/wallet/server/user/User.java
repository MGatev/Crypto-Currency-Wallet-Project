package bg.sofia.uni.fmi.mjt.wallet.server.user;

import bg.sofia.uni.fmi.mjt.wallet.server.api.dto.Asset;
import bg.sofia.uni.fmi.mjt.wallet.server.user.account.Order;
import bg.sofia.uni.fmi.mjt.wallet.server.user.account.Wallet;
import bg.sofia.uni.fmi.mjt.wallet.server.utils.Cache;
import bg.sofia.uni.fmi.mjt.wallet.server.utils.TimedCache;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User implements Serializable {

    private static final int MAX_MINUTES_CACHE = 30;

    private final String username;
    private final Cache<String, Asset> cache;
    private final Wallet wallet;
    private final Map<String, Collection<Order>> currentOrders;
    private final Map<String, Collection<Order>> pastOrders;

    public User(String username) {
        this.username = username;
        cache = new TimedCache<>(MAX_MINUTES_CACHE);
        wallet = new Wallet(0.0);
        currentOrders = new HashMap<>();
        pastOrders = new HashMap<>();
    }

    public Cache<String, Asset> getCache() {
        return cache;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Map<String, Collection<Order>> getCurrentOrders() {
        return currentOrders;
    }

    public Map<String, Collection<Order>> getPastOrders() {
        return pastOrders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
