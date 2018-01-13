package de.konstanz.schulen.suso.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import de.konstanz.schulen.suso.util.DebugUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

public class Blog {

    @Data
    @AllArgsConstructor
    public static class Post implements Parcelable{
        @NonNull
        private int id, authorId;
        @NonNull
        private String subject, body;
        @NonNull
        private Date releaseDate;
        @NonNull
        private Author author;





        protected Post(Parcel in) {
            author = in.readParcelable(Author.class.getClassLoader());
            id = in.readInt();
            authorId = in.readInt();
            subject = in.readString();
            body = in.readString();
            releaseDate = new Date(in.readLong());

        }


        public static final Creator<Post> CREATOR = new Creator<Post>() {
            @Override
            public Post createFromParcel(Parcel in) {
                return new Post(in);
            }

            @Override
            public Post[] newArray(int size) {
                return new Post[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeParcelable(author, i);
            parcel.writeInt(id);
            parcel.writeInt(authorId);
            parcel.writeString(subject);
            parcel.writeString(body);
            parcel.writeLong(releaseDate.getTime());

        }
    }

    @Data
    @AllArgsConstructor
    public static class Author implements Parcelable
    {
        @NonNull
        private int id, permission;
        @NonNull
        private String username, displayName;






        protected Author(Parcel in) {
            id = in.readInt();
            permission = in.readInt();
            username = in.readString();
            displayName = in.readString();

        }

        public static final Creator<Author> CREATOR = new Creator<Author>() {
            @Override
            public Author createFromParcel(Parcel in) {
                return new Author(in);
            }

            @Override
            public Author[] newArray(int size) {
                return new Author[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(id);
            parcel.writeInt(permission);
            parcel.writeString(username);
            parcel.writeString(displayName);
        }
    }

}