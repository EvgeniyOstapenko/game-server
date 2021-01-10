package server.controller;

import common.messages.TopRequest;
import common.messages.TopResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import platform.service.MessageController;
import server.domain.UserProfile;
import server.service.TopRequestService;

@Service
public class TopRequestController implements MessageController<TopRequest, UserProfile> {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    TopRequestService topRequestService;

    @Autowired
    public TopRequestController(TopRequestService topRequestService) {
        this.topRequestService = topRequestService;
    }

    @Override
    public Object onMessage(TopRequest startGameRequest, UserProfile userProfile) {
        topRequestService.onRatingChange(userProfile);
        return new TopResponse(topRequestService.getTopList());

    }

    @Override
    public Class<TopRequest> messageClass() {
        return TopRequest.class;
    }

}