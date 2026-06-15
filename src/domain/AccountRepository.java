package domain;

import java.util.concurrent.ConcurrentHashMap;

public final class AccountRepository {
    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
    public AccountRepository() {
        seed();
    }
    private void seed() {
        save(new Account("4111111111111111", "840", 100_000));
        save(new Account("4222222222222222", "840", 5_000));
        save(new Account("4000000000000002", "840", 0));
    }
    public void save(Account account) {
        accounts.put(account.pan(),account);
    }
    public Account find(String pan) {
        return accounts.get(pan);
    }
    public int size() {
        return accounts.size();
    }
}
