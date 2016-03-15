package dartmouth.cs.ploomis.dartfan;

/**
 * Created by kristenvondrak on 2/28/15.
 */
public class ForumReply {
    private String mName;
    private String mReply;

    public ForumReply() {}

    public ForumReply(String name, String reply) {
        setName(name);
        setReply(reply);
    }

    public void setReply(String comment) {
        this.mReply = comment;
    }
    public String getReply() {
        return mReply;
    }

    public void setName(String name) {
        this.mName = name;
    }
    public String getName() {
        return mName;
    }
}
