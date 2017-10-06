package de.konstanz.schulen.suso.activities.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.CustomEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.activities.BlogPostActivity;
import de.konstanz.schulen.suso.data.Blog;
import de.konstanz.schulen.suso.data.BlogFetcher;
import de.konstanz.schulen.suso.util.AccountManager;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.FabricHandler;


public class BlogFragment extends AbstractFragment {

    private static final String TAG = BlogFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeContainer;


    public BlogFragment() {
        super(R.layout.fragment_blog, R.id.nav_blog, R.string.nav_blog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        swipeContainer = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeContainerSubstitutionplan);
    }

    @Override
    public void onPushToForeground() {
        refresh();
    }

    @Override
    public void refresh() {
        boolean success = true;

        BlogFetcher.getFetcher().setCredentials(AccountManager.getInstance().getCredentials()).setAction(BlogFetcher.Fetcher.Action.FETCH_POSTS).
                performAsync(new Callback<BlogFetcher.Response>() {
                    @Override
                    public void callback(BlogFetcher.Response response) {
                        swipeContainer.setRefreshing(false);


                        if (response instanceof BlogFetcher.Error) {
                            Log.e(TAG, "Error while trying to fetch blog posts: " + ((BlogFetcher.Error) response).getError());
                        } else if (response instanceof BlogFetcher.FetchPostsResult) {
                            displayContent(((BlogFetcher.FetchPostsResult) response).getPosts());
                            return;
                        }

                        displayNoEntries();


                    }
                }, getActivity());


        FabricHandler.logCustomEvent(new CustomEvent("Reloaded BlogFetcher").putCustomAttribute("success", success + ""));

    }

    public void displayNoEntries() {

        FrameLayout blogFrame = (FrameLayout) getActivity().findViewById(R.id.blog_frame);

        TextView infoView = new TextView(blogFrame.getContext());
        infoView.setText(R.string.no_blog_posts);
        infoView.setTextSize(25);
        infoView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llp.setMargins(0, 40, 0, 0);
        infoView.setLayoutParams(llp);
        blogFrame.addView(infoView);
    }

    public void displayContent(ArrayList<Blog.Post> posts) {
        FrameLayout blogFrame = (FrameLayout) getActivity().findViewById(R.id.blog_frame);

        blogFrame.removeAllViews();

        ListView postView = new ListView(getActivity());
        BlogPostAdapter adapter = new BlogPostAdapter(posts);

        postView.setAdapter(adapter);

        blogFrame.addView(postView);

    }



    public static class BlogPostAdapter implements ListAdapter{
        private List<Blog.Post> blogPosts;


        public BlogPostAdapter(List<Blog.Post> blogPosts){
            this.blogPosts = blogPosts;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getCount() {
            return blogPosts.size();
        }

        @Override
        public Object getItem(int i) {
            return blogPosts.get(i);
        }

        @Override
        public long getItemId(int i) {
            return blogPosts.get(i).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup parent) {
            if(view==null || !(view instanceof RelativeLayout) || !(view.getId()==R.id.list_item_blog_view))
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_blog, parent, false);

            final Blog.Post post = blogPosts.get(i);


            TextView titleView = (TextView) view.findViewById(R.id.blog_post_title);
            TextView authorView = (TextView) view.findViewById(R.id.blog_post_author);
            TextView dateView = (TextView) view.findViewById(R.id.blog_post_date);

            titleView.setText(post.getSubject());
            authorView.setText(post.getAuthor().getDisplayName());
            dateView.setText(new SimpleDateFormat("dd.MM.yyyy").format(post.getReleaseDate()));


            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Activity activity = (Activity) view.getContext();
                    Intent intent = new Intent(activity, BlogPostActivity.class);
                    intent.putExtra(BlogPostActivity.POST_EXTRA, post);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.stay_fixed);
                }
            });

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return blogPosts.size()==0;
        }
    }

}
