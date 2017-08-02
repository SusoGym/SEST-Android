package de.konstanz.schulen.suso.util;

import android.content.Context;
import android.os.Handler;

public class ThreadHandler
{

    public static void runOnMainThread(Runnable runnable, Context ctx)
    {

        Handler handler = new Handler(ctx.getMainLooper());
        handler.post(runnable);

    }


}
