package de.konstanz.schulen.suso.data;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

public class Blog {

    @Data
    @AllArgsConstructor
    public static class Post {
        @NonNull
        private int id, authorId;
        @NonNull
        private String subject, body;
        @NonNull
        private Date releaseDate;
        @NonNull
        private Author author;
    }

    @Data
    @AllArgsConstructor
    public static class Author
    {
        @NonNull
        private int id, permission;
        @NonNull
        private String username, displayName;

    }

}