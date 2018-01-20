package de.konstanz.schulen.suso.activities.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.CustomEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.activities.BlogPostActivity;
import de.konstanz.schulen.suso.data.Blog;
import de.konstanz.schulen.suso.data.BlogFetcher;
import de.konstanz.schulen.suso.data.fetch.DownloadManager;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.FabricHandler;


public class BlogFragment extends AbstractFragment {

    private static final String TAG = BlogFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeContainer;


    public BlogFragment() {
        super(R.layout.blog_fragment, R.id.nav_blog, R.string.nav_blog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        swipeContainer = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipeRefreshContainer);
    }

    @Override
    public void onPushToForeground() {
        refresh();
    }

    @Override
    public void refresh() {

        BlogFetcher.getFetcher().setCredentials(DownloadManager.getInstance().getCredentials()).setAction(BlogFetcher.Fetcher.Action.FETCH_POSTS).
                performAsync(new Callback<BlogFetcher.Response>() {
                    @Override
                    public void callback(BlogFetcher.Response response) {
                        swipeContainer.setRefreshing(false);


                        boolean success = true;

                        if (response instanceof BlogFetcher.Error) {
                            Log.e(TAG, "Error while trying to fetch blog posts: " + ((BlogFetcher.Error) response).getError());
                            success = false;
                        } else if (response instanceof BlogFetcher.FetchPostsResult) {
                            displayContent(((BlogFetcher.FetchPostsResult) response).getPosts());
                            success = true;
                        }

                        FabricHandler.logCustomEvent(new CustomEvent("Reloaded BlogFetcher").putCustomAttribute("success", success + ""));

                        if (!success) {
                            displayNoEntries();
                        }

                    }
                }, getActivity());


    }

    public void displayNoEntries() {

        RecyclerView blogFrame = (RecyclerView) getActivity().findViewById(R.id.blog_frame);

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
        RecyclerView blogFrame = (RecyclerView) getActivity().findViewById(R.id.blog_frame);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        blogFrame.setLayoutManager(layoutManager);

        BlogPostAdapter adapter = new BlogPostAdapter(posts);

        blogFrame.setAdapter(adapter);
        blogFrame.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        protected TextView titleView, authorView, dateView;

        public BlogViewHolder(View v) {
            super(v);
            titleView = (TextView) v.findViewById(R.id.blog_post_title);
            authorView = (TextView) v.findViewById(R.id.blog_post_author);
            dateView = (TextView) v.findViewById(R.id.blog_post_date);
        }

        public void initialize(final Blog.Post p)
        {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity activity = (Activity)itemView.getContext();
                    Intent intent = new Intent(activity, BlogPostActivity.class);
                    intent.putExtra(BlogPostActivity.POST_EXTRA, p);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_in_from_right, R.anim.stay_fixed);
                }
            });

            authorView.setText(p.getAuthor().getDisplayName());
            dateView.setText(new SimpleDateFormat("dd.MM.yyyy").format(p.getReleaseDate()));
            titleView.setText(p.getSubject());
        }

    }

    public static class BlogPostAdapter extends RecyclerView.Adapter<BlogViewHolder> {
        private List<Blog.Post> blogPosts;


        public BlogPostAdapter(List<Blog.Post> blogPosts) {
            this.blogPosts = blogPosts;
        }

        @Override
        public void onBindViewHolder(BlogViewHolder holder, int position) {
            holder.initialize(blogPosts.get(position));
        }

        @Override
        public BlogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.blog_list_items, parent, false);
            return new BlogViewHolder(itemView);
        }

        @Override
        public int getItemCount() {
            return blogPosts == null ? 0 : blogPosts.size();
        }

    }

}
