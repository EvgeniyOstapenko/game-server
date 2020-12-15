package common.messages;

import server.domain.TopItem;

import java.util.*;

public class TopResponse extends AbstractResponse {

    public List<TopItem> topList;

    public TopResponse() {
    }

    public TopResponse(List<TopItem> topList) {
        this.topList = topList;
    }
}
