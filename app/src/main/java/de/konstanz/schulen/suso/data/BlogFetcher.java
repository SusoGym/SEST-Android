package de.konstanz.schulen.suso.data;

import android.content.Context;

import com.google.android.gms.auth.api.credentials.Credential;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.konstanz.schulen.suso.SusoApplication;
import de.konstanz.schulen.suso.util.Callback;
import de.konstanz.schulen.suso.util.IOUtility;
import de.konstanz.schulen.suso.util.ThreadHandler;
import de.konstanz.schulen.suso.util.exceptions.UnhandledActionException;
import lombok.Data;

public class BlogFetcher {

    private static final String TAG = BlogFetcher.class.getSimpleName();
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);

    public static Fetcher getFetcher() {
        return new Fetcher();
    }

    public static class Fetcher {
        private static final String TAG = Fetcher.class.getSimpleName();

        private static final String API_ENDPOINT = SusoApplication.API_ENDPOINT;

        private String username;
        private String password;
        private Action action;
        private Date startDate, endDate, releaseDate;

        public Fetcher setUserName(String username) {
            this.username = username;
            return this;
        }

        public Fetcher setPassword(String password) {
            this.password = password;
            return this;
        }

        public Fetcher setCredentials(Credential cred) {
            this.setUserName(cred.getId()).setPassword(cred.getPassword());
            return this;
        }

        public Fetcher setAction(Action action) {
            this.action = action;
            return this;
        }

        public Fetcher setStartDate(Date startDate) {
            this.startDate = startDate;
            return this;
        }

        public Fetcher setEndDate(Date endDate) {
            this.startDate = endDate;
            return this;
        }

        public Fetcher setReleaseDate(Date releaseDate) {
            this.startDate = releaseDate;
            return this;
        }

        public void performAsync(final Callback<Response> callback, final Context ctx) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Response response = performSync();

                    ThreadHandler.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.callback(response);
                        }
                    }, ctx);

                }
            }).start();
        }

        public Response performSync() {

            if (this.action == null) {
                throw new IllegalStateException("Action cannot be null!");
            }

            String url = API_ENDPOINT + "/blog/index.php?console";

            url += "&action=" + action.getUrlAction();


            try {

                switch (this.action) {
                    case FETCH_POSTS:
                        url = addToUrl(url, "startDate", dateToString(startDate));
                        url = addToUrl(url, "endDate", dateToString(endDate));
                        break;
                    //TODO: add other posts
                    default:
                        throw new UnhandledActionException("Unknown action: " + this.action);
                }

                String data = IOUtility.readFromURL(new URL(url))
                        .replace("ï»¿", "");
                JSONObject object = new JSONObject(data);

                int responseCode = object.getInt("code");

                if (responseCode != 200) {
                    return new Error().handle(object);
                } else {
                    Response resp;

                    if (action.getResultClass() == null) {
                        throw new UnhandledActionException("Unknown result class for action: " + action);
                    }

                    resp = action.getResultClass().newInstance();

                    resp.handle(object);

                    return resp;
                }

            } catch (UnsupportedEncodingException | MalformedURLException e) {
                e.printStackTrace();
                return new Error(e.getMessage(), 500);
            } catch (IOException e) {
                e.printStackTrace();
                return new Error(e.getMessage(), 502);
            } catch (JSONException e) {
                e.printStackTrace();
                return new Error(e.getMessage(), 500);
            } catch (InstantiationException e) {
                e.printStackTrace();
                return new Error(e.getMessage(), 500);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return new Error(e.getMessage(), 500);

            }


        }

        private String addToUrl(String url, String key, String value) throws UnsupportedEncodingException {
            if (value != null) {

                if (!url.endsWith("?")) {
                    url += "&";
                }

                url += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
            }

            return url;
        }

        private String dateToString(Date d) {
            if (d == null) {
                return null;
            }
            return dateFormat.format(d);
        }

        public enum Action {
            FETCH_POSTS("fetchposts", FetchPostsResult.class),
            ADD_POST("addpost"),
            EDIT_POST("editpost"),
            DELETE_POST("deletepost"),
            GET_USER_INFO("getuserinfo"),
            CREATE_TOKEN_FROM_SESSION("createtokenfromsession"),
            CREATE_TOKEN("createtoken"),
            GET_PERMISSIONS("getpermissions"),
            HAS_PERMISSION("haspermission"),
            CHANGE_PERMISSION("changepermission"),
            CHANGE_DISPLAYNAME("changedisplayname");

            private String urlAction;
            private Class<? extends Response> resultClass;

            Action(String urlAction) {

                this.urlAction = urlAction;

            }

            Action(String urlAction, Class<? extends Response> resultClass) {
                this(urlAction);

                this.resultClass = resultClass;
            }

            public String getUrlAction() {
                return urlAction;
            }

            public Class<? extends Response> getResultClass() {
                return resultClass;
            }
        }

    }


    @Data
    public static class Error extends Response {

        private String error;
        private int errorCode;

        public Error() {
        }

        public Error(String message, int errorCode) {
            this.error = message;
            this.errorCode = errorCode;
        }

        @Override
        public Error handle(JSONObject obj) throws JSONException {

            return this;

        }
    }

    @Data
    public static class FetchPostsResult extends Response {

        private ArrayList<Blog.Post> posts = new ArrayList<>();

        public ArrayList<Blog.Post> getPosts() {
            return posts;
        }

        @Override
        public FetchPostsResult handle(JSONObject obj) throws JSONException {
            JSONArray arr = obj.getJSONArray("payload");

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                JSONObject authorObj = o.getJSONObject("authorObject");
                Blog.Author author = new Blog.Author(authorObj.getInt("id"), authorObj.getInt("permission"), authorObj.getString("username"), authorObj.getString("displayName"));
                try {
                    posts.add(new Blog.Post(o.getInt("id"), o.getInt("authorId"), o.getString("subject"), o.getString("body"), dateFormat.parse(o.getString("releaseDate")), author));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            return this;
        }
    }

    @Data
    public static abstract class Response {


        public abstract Response handle(JSONObject obj) throws JSONException;

    }

}
