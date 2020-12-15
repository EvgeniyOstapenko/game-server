package common.messages;

import server.domain.TopItem;

import java.util.*;

public class TopResponse {

    public List<TopItem> topList;


    public TopResponse(List<TopItem> topList) {
        this.topList = topList;
    }
}
