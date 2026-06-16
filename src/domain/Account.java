package domain;

import java.util.concurrent.atomic.AtomicLong;

public final class Account {
    private final String pan;
    private final String currencyCode;
    private final AtomicLong balanceMinor;

    public Account(String pan, String currencyCode, AtomicLong balanceMinor) {
        this.pan = pan;
        this.currencyCode = currencyCode;
        this.balanceMinor = new AtomicLong(openingBalanceMinor);
    }
    public String getPan() {
        return pan;
    }
    public String getCurrencyCode() {
        return currencyCode;
    }
    public AtomicLong getBalanceMinor() {
        return balanceMinor.get();
    }
    public boolean debit(long amount) {
        if(amount<0){
            throw new IllegalArgumentException("Debit amount must be non-negative: "+amount);
        }
        for(;;){
            long current=balanceMinor.get();
            if(current<amount){
                return false;
            }
            if(balanceMinor.compareAndSet(current, current-amount)){
                return true;
            }
        }
    }
    public void credit(long amount) {
        if(amount<0){
            throw new IllegalArgumentException("Credit amount must be non-negative: "+amount);
        }
        balanceMinor.addAndGet(amount);
    }
}
