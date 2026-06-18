package processing;

import domain.Account;
import domain.AccountRepository;
import iso.ISOMessage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class TransactionProcessor {
    private final AccountRepository repository;
    private final ConcurrentHashMap<String, Long> reversalLog = new ConcurrentHashMap<>();

    private final AtomicLong authSequence = new AtomicLong(0);

    public TransactionProcessor(AccountRepository repository) {
        this.repository = repository;
    }
    public ISOMessage process(ISOMessage req) {
        try {
            switch (req.mti()) {
                case "0200":
                    return handlePurchase(req);
                case "0100":
                    return handleBalanceInquiry(req);
                case "0400":
                    return handleReversal(req);
                default:
                    return decline(req, ResponseCode.INVALID_TRANSACTION);
            }
        } catch (RuntimeException e) {
            // Any unexpected fault becomes a clean "system malfunction" response
            // rather than dropping the cardholder's connection.
            System.err.println("[processor] error handling " + req.mti() + ": " + e);
            return decline(req, ResponseCode.SYSTEM_MALFUNCTION);
        }
    }

    private ISOMessage handlePurchase(ISOMessage req) {
        ISOMessage resp = baseResponse(req);

        if (!ProcessingCode.PURCHASE.equals(req.get(3))) {
            resp.set(39, ResponseCode.INVALID_TRANSACTION);
            return resp;
        }

        String pan = req.get(2);
        Account account = repository.find(pan);
        if (account == null) {
            resp.set(39, ResponseCode.INVALID_CARD);
            return resp;
        }

        long amount = parseAmount(req.get(4));
        if (account.debit(amount)) {
            resp.set(38, nextAuthId());
            resp.set(39, ResponseCode.APPROVED);
            reversalLog.put(reversalKey(req), amount);
        } else {
            resp.set(39, ResponseCode.INSUFFICIENT_FUNDS);
        }
        return resp;
    }

    private ISOMessage handleBalanceInquiry(ISOMessage req) {
        ISOMessage resp = baseResponse(req);

        if (!ProcessingCode.BALANCE_INQUIRY.equals(req.get(3))) {
            resp.set(39, ResponseCode.INVALID_TRANSACTION);
            return resp;
        }

        String pan = req.get(2);
        Account account = repository.find(pan);
        if (account == null) {
            resp.set(39, ResponseCode.INVALID_CARD);
            return resp;
        }

        resp.set(38, nextAuthId());
        resp.set(39, ResponseCode.APPROVED);
        resp.set(54, buildAdditionalAmounts(account));
        return resp;
    }

    private ISOMessage handleReversal(ISOMessage req) {
        ISOMessage resp = baseResponse(req);

        String pan = req.get(2);
        Long debited = reversalLog.remove(reversalKey(req));
        if (debited == null) {
            // Nothing to reverse: either never approved, or already reversed.
            resp.set(39, ResponseCode.UNABLE_TO_LOCATE_RECORD);
            return resp;
        }

        Account account = repository.find(pan);
        if (account == null) {
            resp.set(39, ResponseCode.INVALID_CARD);
            return resp;
        }

        account.credit(debited);
        resp.set(39, ResponseCode.APPROVED);
        return resp;
    }

    private ISOMessage baseResponse(ISOMessage req) {
        ISOMessage resp = new ISOMessage(responseMti(req.mti()));
        for (int id : new int[] {2, 3, 4, 7, 11, 12, 13, 37, 41, 49}) {
            resp.echo(req, id);
        }
        return resp;
    }

    private ISOMessage decline(ISOMessage req, String code) {
        ISOMessage resp = baseResponse(req);
        resp.set(39, code);
        return resp;
    }

    private String responseMti(String requestMti) {
        int value = Integer.parseInt(requestMti) + 10;
        return String.format("%04d", value);
    }

    private String reversalKey(ISOMessage req) {
        return req.get(11) + "|" + req.get(2);
    }

    private long parseAmount(String amountField) {
        return Long.parseLong(amountField);
    }

    private String nextAuthId() {
        long n = authSequence.incrementAndGet() % 1_000_000L;
        return String.format("%06d", n);
    }

    private String buildAdditionalAmounts(Account account) {
        long balance = account.balanceMinor();
        char sign = balance < 0 ? 'D' : 'C';
        long magnitude = Math.abs(balance);
        return "00"                                   // account type: default
                + "02"                                // amount type: available balance
                + account.currencyCode()              // 3-char ISO currency
                + sign                                 // C or D
                + String.format("%012d", magnitude);  // 12-digit amount
    }
}
