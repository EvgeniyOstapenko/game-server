package common.messages;

import common.dto.AwardStructure;

import java.util.*;

public class FinishGameResponse extends AbstractResponse{

    public AwardStructure award;

    @Override
    public String toString() {
        return "FinishGameResponse{}";
    }

}
