package server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import server.domain.TopItem;
import server.domain.UserProfile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TopRequestService {

    private volatile List<TopItem> topList = new ArrayList<>();

    @Resource
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Value("${numberOfTopPlayers}")
    Integer NUMBER_OF_TOP_PLAYERS;

    public void onRatingChange(UserProfile user) {
        TopItem newItem = new TopItem(user.id(), user.getName(), user.getRating());

        if(topList.isEmpty()){
            topList.add(newItem);
            return;
        }

        for (TopItem item : topList) {
            if (item.profileId == newItem.profileId) {

                item.profileName = newItem.profileName;
                item.rating = newItem.rating;
                return;
            }
        }
        topList.add(newItem);
    }

    public List<TopItem> getTopList() {
        return topList.stream().sorted(Comparator.comparingInt(item -> item.rating)).limit(NUMBER_OF_TOP_PLAYERS).collect(Collectors.toList());
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

    private List<TopItem> convertToTopItemList(List<Map<String, Object>> mapList) {
        return mapList.stream()
                .map(map -> new TopItem((int) map.get("id"), (String) map.get("name"), (int) map.get("rating")))
                .collect(Collectors.toList());
    }
}