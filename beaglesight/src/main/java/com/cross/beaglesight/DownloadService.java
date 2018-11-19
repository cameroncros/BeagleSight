package com.cross.beaglesight;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    public static final String PATH = "path";
    public static final String PROGRESS = "progress";
    public static final String RESULT = "result";
    public static final String URL = "url";
    public static final String RECEIVER = "receiver";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String urlToDownload = intent.getStringExtra(URL);
        String outputFilePath = intent.getStringExtra(PATH);
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
        URLConnection connection = null;
        InputStream input = null;
        OutputStream output = null;
        boolean success = true;
        try {
            URL url = new URL(urlToDownload);
            connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            // download the file
            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(outputFilePath);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data, 0, 1024)) != -1) {
                total += count;
                // publishing the progress....
                Bundle resultData = new Bundle();
                resultData.putInt(PROGRESS, (int) (total * 100 / fileLength));
                resultData.putString(PATH, outputFilePath);
                receiver.send(UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            File file = new File(outputFilePath);
            file.deleteOnExit();
            Log.e("BeagleSight", e.getLocalizedMessage());
            success = false;
        }

        Bundle resultData = new Bundle();
        resultData.putInt("progress", 100);
        resultData.putString(PATH, outputFilePath);
        resultData.putBoolean(RESULT, success);
        receiver.send(UPDATE_PROGRESS, resultData);
    }
}
