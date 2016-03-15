package dartmouth.cs.ploomis.dartfan;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;

import dartmouth.cs.ploomis.dartfan.data.Team;


/**
 * Displays sports teams in a grid view. Allows the user to select which teams to follow.
 */
public class HomeFragment extends Fragment {

    public static final String SHARED_PREFERENCES_KEY = "saved_preferences";
    public static ArrayList<Team> MENS_TEAMS;
    public static ArrayList<Team> WOMENS_TEAMS;


    public void createTeamArray() {

        Resources res = this.getResources();
        String[] m_name = res.getStringArray(R.array.men_team_names);
        String[] w_name = res.getStringArray(R.array.women_team_names);
        TypedArray imgs = this.getResources().obtainTypedArray(R.array.forum_team_icons);
        int i = 0;

        MENS_TEAMS = new ArrayList<>();
        for(String m: m_name) {
            Team team = new Team(m, imgs.getResourceId(i, -1));
            MENS_TEAMS.add(team);
            i++;
        }
        WOMENS_TEAMS = new ArrayList<>();
        for(String w: w_name) {
            Team team = new Team(w, imgs.getResourceId(i, -1));
            WOMENS_TEAMS.add(team);
            i++;
        }


    }

    private ArrayList<Team> mCurrentTeams; // changes based on radio button clicks
    private TeamAdapter mMensTeamsAdapter;
    private TeamAdapter mWomensTeamsAdapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    public  ArrayList<Team> getMCurrentTeams(){
        return mCurrentTeams;
    }

    public ArrayList<Team> getAllTeams() {
        ArrayList<Team> followed = new ArrayList<>();
        for (Team team : MENS_TEAMS) {
            followed.add(team);
        }

        for (Team team : WOMENS_TEAMS) {
            followed.add(team);
        }
        return followed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        createTeamArray();
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Define adapters for each set of teams
        mMensTeamsAdapter = new TeamAdapter(view.getContext(), MENS_TEAMS);
        mWomensTeamsAdapter = new TeamAdapter(view.getContext(), WOMENS_TEAMS);

        // Set up grid view
        final GridView gridView = (GridView) view.findViewById(R.id.gridview);
        gridView.setAdapter(mMensTeamsAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onTeamSelected(position);
            }
        });

        // Set up radio group to choose men's/women's/coed teams
        // change the data set of the adapter depending on which type is selected
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.radioTeams);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.mensButton) {
                    mCurrentTeams = MENS_TEAMS;
                    gridView.setAdapter(mMensTeamsAdapter);
                } else if (checkedId == R.id.womensButton) {
                    mCurrentTeams = WOMENS_TEAMS;
                    gridView.setAdapter(mWomensTeamsAdapter);
                }
            }
        });

        int selectedRadioBtnID = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioBtnID == R.id.mensButton) {
            mCurrentTeams = MENS_TEAMS;
        } else if (selectedRadioBtnID == R.id.womensButton) {
            mCurrentTeams = WOMENS_TEAMS;
        }

        // Load which mTeams are followed from shared preferences
        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

        for (Team team : MENS_TEAMS) {
            boolean isFollowed = prefs.getBoolean(team.getName(), false);
            team.setFollowed(isFollowed);
        }

        for (Team team : WOMENS_TEAMS) {
            boolean isFollowed = prefs.getBoolean(team.getName(), false);
            team.setFollowed(isFollowed);
        }

        return view;
    }

    /**
     * Stores a boolean value to shared preferences to indicate whether a team is being followed or not
     * When a team is selected, either follow or unfollow the selected team.
     *
     * @param position the position of the selected team in the grid view
     */
    private void onTeamSelected(int position) {

        Team selectedTeam = mCurrentTeams.get(position);
        selectedTeam.setFollowed(!selectedTeam.isFollowed());
        Log.d("onTeamSelected", Integer.toString(position) + " " + selectedTeam.getName());

        mMensTeamsAdapter.notifyDataSetChanged();
        mWomensTeamsAdapter.notifyDataSetChanged();

        SharedPreferences prefs = getActivity().getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(selectedTeam.getName(), selectedTeam.isFollowed());
        editor.apply();
    }

    /**
     * Custom ArrayAdapter for displaying team names and icons
     */
    private class TeamAdapter extends ArrayAdapter<Team> {

        private ArrayList<Team> teams;

        public TeamAdapter(Context context, ArrayList<Team> teams) {
            super(context, android.R.layout.simple_list_item_1, teams);
            this.teams = teams;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Team team = teams.get(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_home, parent, false);
            }

            // load the corresponding entry image to the image view
            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            Drawable image = getResources().getDrawable(team.getIconID());
            imageView.setImageDrawable(image);

            // set text view to team name
            TextView nameText = (TextView) convertView.findViewById(R.id.teamNameText);
            nameText.setText(team.getName());

            // indicate whether the team is being followed
            ImageView followImage = (ImageView) convertView.findViewById(R.id.followImage);
            if (team.isFollowed()) {
                followImage.setVisibility(View.VISIBLE);
            } else {
                followImage.setVisibility(View.INVISIBLE);
            }

            return convertView.findViewById(R.id.teamLayout);
        }
    }
}
