package server.domain;

import java.util.*;

public class TopItem {

    public int profileId;

    public String profileName;

    public int rating;


    public TopItem(int profileId, String profileName, int rating) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.rating = rating;
    }
}
