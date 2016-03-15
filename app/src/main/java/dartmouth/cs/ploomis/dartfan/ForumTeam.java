package dartmouth.cs.ploomis.dartfan;

import java.util.ArrayList;

/**
 * Created by kristenvondrak on 2/28/15.
 */
public class ForumTeam {
    private long id;
    private int mTeamId;
    private ArrayList<ForumPost> mPostList;
    private int postIndex;

    public ForumTeam() {
        mPostList = new ArrayList<ForumPost>();
        postIndex = 0;
    }

    public void setPostIndex(int i) {
        postIndex = i;
    }

    public int getPostIndex() {
        return postIndex;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setTeamId(int team) {
        this.mTeamId = team;
    }

    public int getTeamId() {
        return mTeamId;
    }

    public void addReplyToPost(int post_id, ForumReply reply) {
        mPostList.get(post_id).addReply(reply);
    }

    public void addPost(ForumPost post) {
        post.setId(postIndex);
        postIndex++;
        mPostList.add(0, post);
    }

    public ArrayList<ForumPost> getPostList() {
        return mPostList;
    }

    public void setPostList(ArrayList<ForumPost> post) {
        this.mPostList = post;
    }
}
