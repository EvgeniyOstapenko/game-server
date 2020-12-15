package server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import server.domain.TopItem;
import server.domain.UserProfile;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopRequestService {

    private List<TopItem> topList;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${numberOfTopPlayers}")
    Integer NUMBER_OF_TOP_PLAYERS;


    public void onRatingChange() {
        topList = getTopUserListFromDB();
    }

    public List<TopItem> getTopList() {
        onRatingChange();
        return topList;
    }

    private List<TopItem> getTopUserListFromDB() {
        List<Map<String, Object>> topPlayers = namedParameterJdbcTemplate
                .queryForList("select user_profile.id," +
                                " user_profile.\"name\" ," +
                                " user_profile.rating " +
                                "from user_profile" +
                                " order by rating desc limit :NUMBER_OF_TOP_PLAYERS",
                        Map.of("NUMBER_OF_TOP_PLAYERS", NUMBER_OF_TOP_PLAYERS));

        return convertToTopItemList(topPlayers);
    }

    private List<TopItem> convertToTopItemList(List<Map<String, Object>> mapList){
        return mapList.stream()
                .map(map -> new TopItem((int) map.get("id"), (String) map.get("name"), (int) map.get("rating")))
                .collect(Collectors.toList());
    }
}