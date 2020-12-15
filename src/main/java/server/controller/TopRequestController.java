package server.controller;

import common.messages.Pong;
import common.messages.TopRequest;
import common.messages.TopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.domain.TopItem;
import server.domain.UserProfile;
import server.service.TopService;

import java.util.List;

@Service
public class TopRequestController implements MessageController<TopRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    TopService topService;

    @Autowired
    public TopRequestController(TopService topService) {
        this.topService = topService;
    }

    @Override
    public Object onMessage(TopRequest startGameRequest, UserProfile userProfile) {
        topService.onRatingChange(userProfile);
        return new TopResponse(topService.getTopList());

    }

    @Override
    public Class<TopRequest> messageClass() {
        return TopRequest.class;
    }

}