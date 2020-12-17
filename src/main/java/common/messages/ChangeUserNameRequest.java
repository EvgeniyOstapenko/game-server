package common.messages;

public class ChangeUserNameRequest {

    private String newUserName;

    @Override
    public String toString() {
        return "ChangeNameRequest{}";
    }

    public String getNewUserName() {
        return newUserName;
    }

    public void setNewUserName(String newUserName) {
        this.newUserName = newUserName;
    }
}
