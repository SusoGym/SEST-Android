package de.konstanz.schulen.suso.activities;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import de.konstanz.schulen.suso.R;
import de.konstanz.schulen.suso.data.Blog;


public class BlogPostActivity extends AppCompatActivity {
    public static final String POST_EXTRA = "post";

    private Blog.Post post;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blog_large_post);

        post = getIntent().getParcelableExtra(POST_EXTRA);


        TextView title = (TextView) findViewById(R.id.post_activity_title);
        TextView author = (TextView) findViewById(R.id.post_activity_author);
        TextView date = (TextView) findViewById(R.id.post_activity_date);
        WebView body = (WebView) findViewById(R.id.post_activity_body);


        title.setText(post.getSubject());
        author.setText(post.getAuthor().getDisplayName());
        date.setText(SimpleDateFormat.getDateInstance().format(post.getReleaseDate()));
        //Use of deprecated function necessary, as the newer functions require a higher API level
        body.getSettings().setLoadWithOverviewMode(true);
        body.getSettings().setUseWideViewPort(true);
        body.getSettings().setJavaScriptEnabled(true);
        body.setWebChromeClient(new WebChromeClient());

        body.setBackgroundColor(Color.TRANSPARENT);

        String bodyHTML = "<head><meta name=\"viewport\" content=\"width=device-width\"/></head>" + post.getBody() + "</body></html>";
        body.loadDataWithBaseURL(null, bodyHTML, "text/html", "UTF-8", null);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay_fixed, R.anim.slide_out_to_right);
    }


}
