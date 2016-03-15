package dartmouth.cs.ploomis.dartfan;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kristenvondrak on 2/28/15.
 */

public class ForumPostsAdapter<ForumPosts> extends
        ArrayAdapter<ForumPost> {

    private final Context context;
    private final List<ForumPost> values;

    public ForumPostsAdapter(Context context, List<ForumPost> values)  {
        super(context, R.layout.forum_post_item, (List<ForumPost>) values);
        this.context = context;
        this.values = values;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.d("ForumPostsAdapter", "getView");
        ForumPost entry = values.get(position);

        View rowView = inflater.inflate(R.layout.forum_post_item, parent, false);

        TextView textViewName = (TextView) rowView.findViewById(R.id.forum_post_name);
        textViewName.setText(entry.getName());

        TextView textViewComment = (TextView) rowView.findViewById(R.id.forum_post_comment);
        textViewComment.setText(entry.getComment());

        TextView textViewDate = (TextView) rowView.findViewById(R.id.forum_post_date);
        textViewDate.setText(entry.getDate());

        // Add the replies to the post
        if (entry.getReplyList() != null) {
            for (ForumReply reply : entry.getReplyList()) {
                addReplyItemToView(rowView, reply);
            }
        }

        Button btn_reply = (Button) rowView.findViewById(R.id.button_reply);
        btn_reply.setTag(position);

        return rowView;
    }

    public void addReplyItemToView(View rowView, ForumReply reply) {

        // Find the overall layout
        LinearLayout rl = (LinearLayout) rowView.findViewById(R.id.forum_replies);

        // Create a new reply item
        LinearLayout replies = new LinearLayout(this.context);
        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        lparams.setMargins(10,10,10,10);
        replies.setOrientation(LinearLayout.HORIZONTAL);
        rl.addView(replies, lparams);

        LinearLayout.LayoutParams tparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        TextView name_textview = new TextView(this.context);
        name_textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        name_textview.setTextColor(this.context.getResources().getColor(R.color.dark_gray));
        name_textview.setTypeface(name_textview.getTypeface(), Typeface.BOLD);
        name_textview.setText(reply.getName());
        name_textview.setPadding(30,0,0,0);

        TextView comment_textview = new TextView(this.context);
        comment_textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        comment_textview.setTextColor(this.context.getResources().getColor(R.color.dark_gray));
        comment_textview.setText(reply.getReply());
        comment_textview.setPadding(30,0,0,30);

        LinearLayout.LayoutParams sparams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);

        TextView line = new TextView(this.context);
        line.setBackgroundColor(this.context.getResources().getColor(R.color.white));


        replies.addView(name_textview, tparams);
        replies.addView(comment_textview, tparams);
        rl.addView(line, sparams);

    }




}

