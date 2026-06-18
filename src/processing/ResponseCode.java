package processing;

public final class ResponseCode {
    public static final String APPROVED = "00";
    public static final String INVALID_TRANSACTION = "12";
    public static final String INVALID_CARD = "14";
    public static final String UNABLE_TO_LOCATE_RECORD = "25";
    public static final String INSUFFICIENT_FUNDS = "51";
    public static final String SYSTEM_MALFUNCTION = "96";

    private ResponseCode() {
    }
}
