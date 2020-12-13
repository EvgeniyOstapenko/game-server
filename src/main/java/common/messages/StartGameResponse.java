package common.messages;

public class StartGameResponse extends AbstractResponse {

    public StartGameResponse() {
    }

    public StartGameResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    @Override
    public String toString() {
        return "StartGameResponse{}";
    }
}
