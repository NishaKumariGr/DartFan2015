package dartmouth.cs.ploomis.dartfan.data;

/**
 * Stores information about a team
 */
public class Team {

    private String mName; // team name
    private int mIconID; // id of drawable team icon
    private boolean mIsFollowed; // indicates whether the team is being followed by the user


    public Team(String name, int iconID) {
        mName = name;
        mIconID = iconID;
        mIsFollowed = false;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public int getIconID() {
        return mIconID;
    }

    public void setIconID(int mIconID) {
        this.mIconID = mIconID;
    }

    public boolean isFollowed() {
        return mIsFollowed;
    }

    public void setFollowed(boolean mIsFollowed) {
        this.mIsFollowed = mIsFollowed;
    }
}
