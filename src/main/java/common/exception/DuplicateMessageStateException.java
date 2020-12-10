package common.exception;

public class DuplicateMessageStateException extends RuntimeException {

    private String errorMessage;

    Object wrongRequestInOrder;

    public DuplicateMessageStateException(String errorMessage, Object wrongOrderRequest) {
        this.errorMessage = errorMessage;
        this.wrongRequestInOrder = wrongOrderRequest;
    }

    @Override
    public String toString() {
        return String.format(errorMessage, wrongRequestInOrder);
    }

}
