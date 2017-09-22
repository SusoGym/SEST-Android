package de.konstanz.schulen.suso.activities.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;

import de.konstanz.schulen.suso.R;
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

        //TODO display no entries
    }

    public void displayContent(ArrayList<Blog.Post> posts) {
//TODO display Content

    }

}
