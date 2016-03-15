package dartmouth.cs.ploomis.dartfan;

import android.app.Fragment;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import dartmouth.cs.ploomis.dartfan.data.NewsFragmentItem;
import dartmouth.cs.ploomis.dartfan.data.Team;


// Created by Nisha 02/28/15

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class NewsFragment extends ListFragment {

    private HomeFragment myHomeFragment;
    private ArrayList<String> linkOnClick;
    ProgressDialog mProgressDialog;

    public NewsFragment() {
        // Required empty public constructor
    }

    public void setHomeFragment(HomeFragment homeFragment) {
        myHomeFragment = homeFragment;
    }

    //Listening to click events of listview
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        FetchWebsiteData f= new FetchWebsiteData();
        //redirect each news to its corresponding website
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkOnClick.get(position)));
        startActivity(browserIntent);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        //Call the Async Task
        FetchWebsiteData fetchWebsiteData=new FetchWebsiteData();
        fetchWebsiteData.execute();

        return view;
    }




    private class FetchWebsiteData extends AsyncTask<Void, Void, Void> {

        //Arraylists to store various elements of feed

        ArrayList<String> titles = new ArrayList<String>();
        ArrayList<String> descriptions = new ArrayList<String>();
        ArrayList<String> dates = new ArrayList<String>();
        ArrayList<String> links = new ArrayList<String>();
        ArrayList<String> category=new ArrayList<String>();
        ArrayList<String> images=new ArrayList<String>();
        ArrayList<Bitmap> newsImage=new ArrayList<Bitmap>();

        //The final URL to be used concatenating the static and dynamic part
        private String mURL;
        // array to store the list of teams being followed
        ArrayList<String> mFollowedTeams=new ArrayList<String>();
        String mImageLocation;
        Bitmap mBitmap;



        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Displaying the ProgressDialog till the news feed is loaded
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            // Creating a HashMap object and storing values by calling mapGameToId().
            HashMap<String, String> mHashMap= mapGameToId();

            // Retrieving all the current teams from the HomeFragment
            //ArrayList<Team> mCurrentTeams = myHomeFragment.getMCurrentTeams();
            ArrayList<Team> mCurrentTeams = myHomeFragment.getAllTeams();
            String mteamName;

            for(int i=0;i<mCurrentTeams.size();i++)
            {
                Log.d("ouo",i+"th team is"+mCurrentTeams.get(i));

            }

            // count the number of teams being followed
            int counter=0;

            //Getting the names and number of followed team
            for (int i = 0; i < mCurrentTeams.size(); i++) {
                if (mCurrentTeams.get(i).isFollowed()) {
                    mteamName = mCurrentTeams.get(i).getName();
                    mURL = mHashMap.get(mteamName);
                    mFollowedTeams.add(counter,mURL);
                    counter++;
                }

            }


            // Display all news if none of the team is followed
            if(counter==0)
            {
                mURL = mHashMap.get("All News");
                mFollowedTeams.add(0, mURL);
            }



            // Reading the new feed for all the followed teams
            for(int i=0;i<mFollowedTeams.size();i++) {
                try {
                    // Connect to the website
                    Document document = Jsoup.connect(mFollowedTeams.get(i)).get();
                    String mFirstPartOfString = "http://www.dartmouthsports.com//ViewArticle.dbml?DB_OEM_ID=11600&";
                    // Retrieving each element from the feed and storing in an ArrayList
                    for (Element item : document.select("item")) {
                        int mThirdPartOfString = item.outerHtml().indexOf("ATCLID");
                        int mSecondPartOfString = item.outerHtml().indexOf("<guid>");
                        titles.add(item.select("title").text());
                        descriptions.add(item.select("description").text());
                        dates.add(item.select("pubDate").text());
                        links.add(mFirstPartOfString + item.outerHtml().substring(mThirdPartOfString, mSecondPartOfString));
                        category.add(item.select("category").text());
                        images.add(item.outerHtml().substring(item.outerHtml().indexOf("url") + 2, item.outerHtml().indexOf("length") - 2));

                        //extracting the link of image
                        mImageLocation=item.outerHtml().substring(item.outerHtml().indexOf("url") + 2, item.outerHtml().indexOf("length") - 2).substring(3);

                        //Storing images locally
                        try {
                            URL imageURL = new URL(mImageLocation);
                            mBitmap = BitmapFactory.decodeStream(imageURL.openStream());
                            newsImage.add(mBitmap);
                        }
                        catch (IOException e) {

                            Log.e("error", "Downloading Image Failed");
                            mBitmap = null;
                            newsImage.add(mBitmap);
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            linkOnClick=links;
            return null;
        }


        protected HashMap<String, String> mapGameToId() {
            HashMap<String, String> mapGame = new HashMap();

            // HashMap key= Game Name, Value= webaddress of the sports
            mapGame.put("All News","http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&media=news");

            mapGame.put("Baseball", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4699&media=news");
            mapGame.put("Football", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4719&media=news");
            mapGame.put("M Basketball", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4703&media=news");
            mapGame.put("M Soccer", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4697&media=news");
            mapGame.put("M Crew Lightweight", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4725&media=news");
            mapGame.put("M Lacrosse", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4694&media=news");
            mapGame.put("Men's Hockey", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4728&media=news");
            mapGame.put("M Squash", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4729&media=news");
            mapGame.put("M Tennis", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4709&media=news");
            mapGame.put("M Swimming & Diving", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4714&media=news");
            mapGame.put("M Cross Country", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4710&media=news");
            mapGame.put("Sailing", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4693&media=news");
            mapGame.put("M Track & Field", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4706&media=news");
            mapGame.put("M Crew Heavyweight", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4712&media=news");
            mapGame.put("Equestrian", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4727&media=news");
            mapGame.put("M Golf", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4708&media=news");
            mapGame.put("Skiing", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4723&media=news");

            mapGame.put("W Squash", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4696&media=news");
            mapGame.put("Softball", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4707&media=news");
            mapGame.put("W Soccer", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4705&media=news");
            mapGame.put("Women's Volleyball", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4702&media=news");
            mapGame.put("Women's Hockey", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4726&media=news");
            mapGame.put("W Crew", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4731&media=news");
            mapGame.put("W Basketball", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4704&media=news");
            mapGame.put("W Lacrosse", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4695&media=news");
            mapGame.put("W Tennis", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4700&media=news");
            mapGame.put("W Swimming & Diving", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4711&media=news");
            mapGame.put("W Cross Country", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4698&media=news");
            mapGame.put("W Track & Field", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4701&media=news");
            mapGame.put("Field Hockey", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4732&media=news");
            mapGame.put("Sailing", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4693&media=news");
            mapGame.put("Equestrian", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4727&media=news");
            mapGame.put("W Golf", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4720&media=news");
            mapGame.put("Skiing", "http://www.dartmouthsports.com/rss.dbml?db_oem_id=11600&RSS_SPORT_ID=4723&media=news");



            return mapGame;
        }


        @Override
        protected void onPostExecute(Void result) {

            // ListView items list
            List<NewsFragmentItem> mItems;
            // initialize the items list
            mItems = new ArrayList<NewsFragmentItem>();

            // Iterating to read all required components to be displayed and adding them to the array mItems
            for (int i = 0; i < titles.size(); i++) {
                mItems.add(new NewsFragmentItem(titles.get(i), dates.get(i),category.get(i),newsImage.get(i)));
            }

            // initialize and set the list adapter
            setListAdapter(new NewsFragmentAdapter(getActivity(), mItems));

            mProgressDialog.dismiss();

        }
    }
}

// Creating the custom adapter to display listview items
class NewsFragmentAdapter extends ArrayAdapter<NewsFragmentItem> {

    ViewHolder viewHolder;
    Drawable d;

    public NewsFragmentAdapter(Context context, List<NewsFragmentItem> items) {
        super(context, R.layout.newsfragment_adapter_row, items);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {

            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.newsfragment_adapter_row, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.newsPubDate = (TextView) convertView.findViewById(R.id.textViewPubDate);
            viewHolder.newsTitle = (TextView) convertView.findViewById(R.id.textViewTitle);

            //  viewHolder.newsDescription = (TextView) convertView.findViewById(R.id.textViewDescription);
            viewHolder.newsCategory = (TextView) convertView.findViewById(R.id.textViewCategory);
            viewHolder.newsImage = (ImageView) convertView.findViewById(R.id.imageView);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Setting the various elements to be displayed in the listview
        NewsFragmentItem item = getItem(position);
        viewHolder.newsTitle.setText(item.title);
        viewHolder.newsPubDate.setText(item.pubDate);
        viewHolder.newsCategory.setText(item.category);
        viewHolder.newsImage.setImageBitmap(item.image);

        return convertView;
    }

    //Class to store all the elements to be displayed in one row of the listview
    private static class ViewHolder {

        TextView newsTitle;
        TextView newsPubDate;
        TextView newsCategory;
        ImageView newsImage;
    }
}
