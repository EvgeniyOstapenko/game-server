package common.messages;

import server.common.GameResult;

public class FinishGameRequest {

    public GameResult result;

    @Override
    public String toString() {
        return "FinishGameRequest{}";
    }

    public GameResult getResult() {
        return result;
    }

    public void setResult(GameResult result) {
        this.result = result;
    }
}
