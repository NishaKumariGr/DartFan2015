package dartmouth.cs.ploomis.dartfan;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kristenvondrak on 3/1/15.
 */
public class ForumTeamListAdapter<ForumTeam> extends ArrayAdapter<dartmouth.cs.ploomis.dartfan.ForumTeam> {

    private final Context context;
    private final List<dartmouth.cs.ploomis.dartfan.ForumTeam> values;

    public ForumTeamListAdapter(Context context, List<dartmouth.cs.ploomis.dartfan.ForumTeam> values) {
        super(context, R.layout.forum_post_item, (List<dartmouth.cs.ploomis.dartfan.ForumTeam>) values);
        this.context = context;
        this.values = values;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        dartmouth.cs.ploomis.dartfan.ForumTeam entry = values.get(position);

        View rowView = inflater.inflate(R.layout.forum_list_item, parent, false);

        // Set the team name
        TextView textViewTeam = (TextView) rowView.findViewById(R.id.forum_list_team);
        Resources res = context.getResources();
        String[] array = res.getStringArray(R.array.forum_team_names);
        textViewTeam.setText(array[entry.getTeamId()]);

        // Set the team icon
        ImageView imageView = (ImageView) rowView.findViewById(R.id.forum_list_icon);
        TypedArray imgs = context.getResources().obtainTypedArray(R.array.forum_team_icons);
        imageView.setImageResource(imgs.getResourceId(entry.getTeamId(), -1));

        return rowView;
    }
}