package dartmouth.cs.ploomis.dartfan;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kristenvondrak on 2/28/15.
 */
public class ForumTeamDataSource {

    private static final String TAG = "DataSource";

    public static final int ID = 0;
    public static final int TEAM = 1;
    public static final int POSTS = 2;
    public static final int POST_INDEX = 3;
    public static final String EMPTY_STRING = "";

    // Database fields
    private SQLiteDatabase database;
    private ForumTeamDbHelper dbHelper;
    private String[] allColumns = { ForumTeamDbHelper.COLUMN_ID,
            ForumTeamDbHelper.COLUMN_TEAM,
            ForumTeamDbHelper.COLUMN_POSTS,
            ForumTeamDbHelper.COLUMN_POST_INDEX,

    };


    public ForumTeamDataSource(Context context) {
        dbHelper = new ForumTeamDbHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertTeam(ForumTeam entry) {
        Log.d(TAG, "insertEntry()");

        ContentValues values = new ContentValues();
        values.put(ForumTeamDbHelper.COLUMN_TEAM, entry.getTeamId());
        if (entry.getPostList() != null)
            values.put(ForumTeamDbHelper.COLUMN_POSTS, postsToString(entry.getPostList()));
        else {
            values.put(ForumTeamDbHelper.COLUMN_POSTS, EMPTY_STRING);
        }
        values.put(ForumTeamDbHelper.COLUMN_POST_INDEX, entry.getPostIndex());

        long insertId = database.insert(ForumTeamDbHelper.TABLE_FORUM_TEAM, null, values);

        Cursor cursor = database.query(ForumTeamDbHelper.TABLE_FORUM_TEAM,
                allColumns, ForumTeamDbHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);

        cursor.moveToFirst();

        // Log the comment stored
        Log.d(TAG, "insert ID = " + insertId);

        cursor.close();

        return insertId;
    }

    // Query a specific entry by its index.
    public ForumTeam fetchEntryByIndex(long rowId) {

        Log.d(TAG, "fetchEntryByIndex()");

        Cursor cursor = database.query(ForumTeamDbHelper.TABLE_FORUM_TEAM,
                allColumns, null, null, null, null, null);

        cursor.moveToPosition((int)rowId);
        ForumTeam entry = new ForumTeam();
        entry.setId(cursor.getLong(ID));
        entry.setTeamId(cursor.getInt(TEAM));
        String posts = cursor.getString(POSTS);
        if (!posts.equals(EMPTY_STRING))
            entry.setPostList(stringToPostList(posts));
        entry.setPostIndex(cursor.getInt(POST_INDEX));
        cursor.close();
        return entry;
    }
    public ForumTeam fetchEntryById(int teamId) {

        Log.d(TAG, "fetchEntryById()");

        Cursor cursor = database.query(ForumTeamDbHelper.TABLE_FORUM_TEAM,
                allColumns, null, null, null, null, null);


        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            if (cursor.getInt(TEAM) == teamId) {
                ForumTeam entry = new ForumTeam();
                entry.setId(cursor.getLong(ID));
                entry.setTeamId(cursor.getInt(TEAM));
                String posts = cursor.getString(POSTS);
                if (!posts.equals(EMPTY_STRING))
                    entry.setPostList(stringToPostList(posts));
                entry.setPostIndex(cursor.getInt(POST_INDEX));
                cursor.close();
                return entry;
            }
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return null;

    }


    // Remove an entry by giving its index
    public void removeEntry(long id) {
        Log.d(TAG, "delete entry by index = " + id);
        System.out.println("entry deleted with id: " + id);
        database.delete(ForumTeamDbHelper.TABLE_FORUM_TEAM, ForumTeamDbHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void removeEntryByTeamId(int id) {
        Log.d(TAG, "delete entry by id = " + id);
        System.out.println("entry deleted with id: " + id);
        database.delete(ForumTeamDbHelper.TABLE_FORUM_TEAM, ForumTeamDbHelper.COLUMN_TEAM
                + " = " + id, null);
    }

    public void deleteAllTeamEntries() {
        System.out.println("Entries deleted all");
        Log.d(TAG, "delete all = ");
        database.delete(ForumTeamDbHelper.TABLE_FORUM_TEAM, null, null);
    }

    // Query the entire table, return all rows
    public ArrayList<ForumTeam> fetchEntries() {
        Log.d(TAG, "fetchEntries()");

        ArrayList<ForumTeam> entries = new ArrayList<ForumTeam>();

        Cursor cursor = database.query(ForumTeamDbHelper.TABLE_FORUM_TEAM,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ForumTeam entry = new ForumTeam();
            entry.setId(cursor.getLong(ID));
            entry.setTeamId(cursor.getInt(TEAM));
            String posts = cursor.getString(POSTS);
            if (!posts.equals(EMPTY_STRING))
                entry.setPostList(stringToPostList(posts));
            entry.setPostIndex(cursor.getInt(POST_INDEX));

            entries.add(entry);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return entries;
    }

    // ----------------------------------------------------------------------- JSON static methods

    public static ForumTeam stringToForumTeam(String str) {
        Log.d(TAG, "stringToForumTeam, str: " + str);
        ForumTeam team = new ForumTeam();
        JSONObject obj;
        try {
            obj = new JSONObject(str);
            team.setId(obj.getLong("team_id"));
            team.setTeamId(obj.getInt("team_teamid"));
            String posts = obj.getString("team_posts");
            if (!posts.equals(EMPTY_STRING)) {
                Log.d(TAG, "string not empty");
                team.setPostList(stringToPostList(posts));
            }
            else {
                Log.d(TAG, "string empty");
            }
            team.setPostIndex(obj.getInt("team_postindex"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return team;
    }

    public static String forumTeamToString(ForumTeam team) {
        Log.d(TAG, "forumTeamToString");
        JSONObject json = new JSONObject();
        try {
            json.put("team_id", team.getId());
            json.put("team_teamid", team.getTeamId());
            if (team.getPostList() != null) {
                Log.d(TAG, "string not empty");
                json.put("team_posts", postsToString(team.getPostList()));
            }
            else {
                Log.d(TAG, "string empty");
                json.put("team_posts", EMPTY_STRING);
            }
            json.put("team_postindex", team.getPostIndex());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

    public static ArrayList<ForumPost> stringToPostList(String str) {
        Log.d(TAG, "stringToPostList, str: " + str);
        ArrayList<ForumPost> post_list = new ArrayList<ForumPost>();
        JSONArray ja, rja;

        try {
            ja = new JSONArray(str);

            for (int i = 0; i < ja.length(); i++) {
                Log.d(TAG, "in first for loop");
                ForumPost post = new ForumPost();
                JSONObject post_obj = ja.getJSONObject(i);
                post.setId(post_obj.getInt("post_id"));
                post.setName(post_obj.getString("post_name"));
                post.setComment(post_obj.getString("post_comment"));
                post.setDate(post_obj.getString("post_date"));

                String rstr = post_obj.getString("post_replies");
                if (!rstr.equals(EMPTY_STRING)) {
                    try {
                        rja = new JSONArray(rstr);
                        for (int j = 0; j < rja.length(); j++) {
                            ForumReply reply = new ForumReply();
                            JSONObject reply_obj = rja.getJSONObject(j);
                            reply.setName(reply_obj.getString("reply_name"));
                            reply.setReply(reply_obj.getString("reply_comment"));
                            post.addReply(reply);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                post_list.add(post);
            }

        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        return post_list;
    }

    public static JSONObject postToJSON(ForumPost entry) {
        Log.d(TAG, "postToJSON");
        try {
            JSONObject json = new JSONObject();
            json.put("post_id", entry.getId());
            json.put("post_comment", entry.getComment());
            json.put("post_name", entry.getName());
            json.put("post_date", entry.getDate());
            if (entry.getReplyList() != null)
                json.put("post_replies", repliesToString(entry.getReplyList()));
            else {
                json.put("post_replies", EMPTY_STRING);
            }
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }
    public static String postsToString(ArrayList<ForumPost> posts) {
        Log.d(TAG, "postsToString");
        JSONArray jsonArr = new JSONArray();
        if (posts != null) {
            for (ForumPost post : posts) {
                JSONObject obj = postToJSON(post);
                jsonArr.put(obj);
            }
            return jsonArr.toString();
        }
        else {
            return EMPTY_STRING;
        }
    }


    public static String repliesToString(ArrayList<ForumReply> replies) {
        Log.d(TAG, "repliesToString");
        JSONArray jsonArr = new JSONArray();
        if (replies != null) {
            for (ForumReply reply : replies) {
                JSONObject obj = replyToJSON(reply);
                jsonArr.put(obj);
            }
            return jsonArr.toString();
        }
        else {
            return EMPTY_STRING;
        }
    }

    public static JSONObject replyToJSON(ForumReply entry) {
        Log.d(TAG, "replyToJSON");
        try {
            JSONObject json = new JSONObject();
            json.put("reply_name", entry.getName());
            json.put("reply_comment", entry.getReply());
            return json;
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}

