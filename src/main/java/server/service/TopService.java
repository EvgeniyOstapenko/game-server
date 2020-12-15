package server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import server.domain.TopItem;
import server.domain.UserProfile;

import javax.annotation.Resource;
import java.util.*;

@Service
public class TopService {

    private List<TopItem> topList;

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${numberOfTopPlayers}")
    Integer NUMBER_OF_TOP_PLAYERS;


    public void onRatingChange(UserProfile profile) {
        //toto update topList
        List<Map<String, Object>> topPlayers = namedParameterJdbcTemplate
                .queryForList("select user_profile id," +
                                " user_profile.\"name\" ," +
                                " user_profile.rating " +
                                "from user_profile" +
                                " order by rating desc limit :NUMBER_OF_TOP_PLAYERS",
                        Map.of("NUMBER_OF_TOP_PLAYERS", NUMBER_OF_TOP_PLAYERS));
    }

    public List<TopItem> getTopList() {
        //todo return TOP 10 items

        return topList;
    }

    public List<Map<String, Object>> get() {
        List<Map<String, Object>> topPlayers = namedParameterJdbcTemplate
                .queryForList("select user_profile id," +
                                " user_profile.\"name\" ," +
                                " user_profile.rating " +
                                "from user_profile" +
                                " order by rating desc limit :NUMBER_OF_TOP_PLAYERS",
                        Map.of("NUMBER_OF_TOP_PLAYERS", NUMBER_OF_TOP_PLAYERS));
        return topPlayers;
    }

}