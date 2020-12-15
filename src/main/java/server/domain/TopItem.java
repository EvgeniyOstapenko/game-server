package server.domain;

public class TopItem {

    public int profileId;

    public String profileName;

    public int rating;

    public TopItem() {
    }

    public TopItem(int profileId, String profileName, int rating) {
        this.profileId = profileId;
        this.profileName = profileName;
        this.rating = rating;
    }
}
