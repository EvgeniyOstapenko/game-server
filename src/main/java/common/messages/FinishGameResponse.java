package common.messages;

import common.dto.AwardStructure;

import java.util.*;

public class FinishGameResponse extends AbstractResponse{

    public AwardStructure award;

    public FinishGameResponse() {
    }

    public FinishGameResponse(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
//        this.award = award;
    }

    @Override
    public String toString() {
        return "FinishGameResponse{}";
    }

}
