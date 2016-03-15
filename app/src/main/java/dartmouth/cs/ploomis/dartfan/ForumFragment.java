package dartmouth.cs.ploomis.dartfan;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;

import dartmouth.cs.ploomis.dartfan.data.Team;


/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ForumFragment extends Fragment {

    private HomeFragment mHomeFragment;
    public static final String TEAM_ID_KEY = "team_id_key";
    public static ForumTeamDataSource datasource;
    public static ForumTeamListAdapter<ForumPost> adapter;
    public ArrayList<Team> mCurrentTeams;
    public boolean followedTeams[] = new boolean[TeamGlobals.NUM_TEAMS];

    public void setHomeFragment(HomeFragment homeFragment) {
        mHomeFragment = homeFragment;
    }

    public ForumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_forum, container, false);

        // Retrieve the all the teams that are being followed from database
        datasource = new ForumTeamDataSource(this.getActivity());
        datasource.open();
        retrieveFollowedTeams();
        ArrayList<ForumTeam> teams = datasource.fetchEntries();

        // Populate the list view with the teams that are being followed
        AbsListView listView= (AbsListView) view.findViewById(R.id.forum_main_listview);
        adapter = new ForumTeamListAdapter(this.getActivity(), teams);
        listView.setAdapter(adapter);

        // Define the listener interface
        AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // get id of selected team, start forum activity
                ForumTeam entry = adapter.getItem(position);
                startForumActivity(entry);
            }
        };

        // Get the ListView and wired the listener
        listView.setOnItemClickListener(mListener);
        datasource.close();

        return view;
    }

    public void retrieveFollowedTeams() {
        // Retrieving all the followed teams from the HomeFragment
        //mCurrentTeams = mHomeFragment.getMCurrentTeams();
        mCurrentTeams = mHomeFragment.getAllTeams();
        String name;
        int teamId;

        //Getting the names of followed team
        for (int i = 0; i < mCurrentTeams.size(); i++) {
            name = mCurrentTeams.get(i).getName();
            teamId = teamNameToTeamId(name);

            // Team is followed
            if (mCurrentTeams.get(i).isFollowed()) {
                followedTeams[teamId] = true;

                // If not in database, add dummy team so it shows up in listview
                if (datasource.fetchEntryById(teamId) == null) {
                    ForumTeam team = new ForumTeam();
                    team.setTeamId(teamId);
                    datasource.insertTeam(team);
                }
            }
            // Team is not followed
            else {
                followedTeams[teamId] = false;
                // If in database, remove
                if (datasource.fetchEntryById(teamId) != null)
                    datasource.removeEntryByTeamId(teamId);
            }
        }
    }

    public int teamNameToTeamId(String name) {
        Resources res = getResources();
        String[] mTeams = res.getStringArray(R.array.men_team_names);
        String[] wTeams = res.getStringArray(R.array.women_team_names);
        int i = 0;
        for (String m: mTeams) {
            if (m.equals(name))
                return i;
            i++;
        }
        for (String w: wTeams) {
            if (w.equals(name))
                return i;
            i++;
        }
        return -1;
    }

    public void startForumActivity(ForumTeam team) {
        Intent intent = new Intent(this.getActivity(), ForumActivity.class);
        intent.putExtra(TEAM_ID_KEY, team.getTeamId());
        startActivity(intent);
    }
}