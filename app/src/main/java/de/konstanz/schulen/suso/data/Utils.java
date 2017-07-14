package de.konstanz.schulen.suso.data;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class Utils {
    public static String readFromURL(URL url) throws IOException {
        InputStream is;
        try {
            is = url.openStream();
        }catch(IOException e){
            e.printStackTrace();
            return "";
        }
        int temp;
        StringBuilder data = new StringBuilder();
        while((temp = is.read()) !=-1){
            data.append((char) temp);
        }
        return data.toString();
    }
}
