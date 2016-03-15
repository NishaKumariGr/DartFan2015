package dartmouth.cs.ploomis.dartfan;

import java.util.ArrayList;

/**
 * Created by kristenvondrak on 2/28/15.
 */
public class ForumPost {
    private int id;
    private int mTeam;
    private String mComment;
    private String mName;
    private String mDate;
    private ArrayList<ForumReply> mReplyList;

    public ForumPost() {
        mReplyList = new ArrayList<ForumReply>();
    }

    public ForumPost(int team, String comment, String name, String date) {
        setTeam(team);
        setComment(comment);
        setName(name);
        setDate(date);
        mReplyList = new ArrayList<ForumReply>();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setTeam(int team) {
        this.mTeam = team;
    }

    public int getTeam() {
        return mTeam;
    }

    public void setDate(String d) {
        this.mDate = d;
    }

    public String getDate() {
        return mDate;
    }

    public void setComment(String comment) {
        this.mComment = comment;
    }

    public String getComment() {
        return mComment;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    public void addReply(ForumReply reply) {
        mReplyList.add(reply);
    }

    public ArrayList<ForumReply> getReplyList() {
        return mReplyList;
    }

    public void setReplyList(ArrayList<ForumReply> reply) {
        this.mReplyList = reply;
    }
}
