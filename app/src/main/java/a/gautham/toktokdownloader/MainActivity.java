package a.gautham.toktokdownloader;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tapadoo.alerter.Alerter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import a.gautham.toktokdownloader.helpers.ConnectivityService;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MainActivity extends AppCompatActivity {

    private static String OPENED_FROM_OUTSIDE = null;
    private AlertDialog pgDialog;
    //private DownloadManager downloadManager;
    private ClipboardManager clipboardManager;
    private ProgressBar progressBar;
    private TextView percent_pg;
    private String playURL = "";
    private Button download_bt;
    private RelativeLayout loading_layout;
    private ProgressBar progress_circular;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        download_bt = findViewById(R.id.download_bt);
        loading_layout = findViewById(R.id.loading_layout);
        progress_circular = findViewById(R.id.progress_circular);
        text = findViewById(R.id.text);
        LinearLayout my_downloads_bt = findViewById(R.id.my_downloads_bt);
        LinearLayout settings_bt = findViewById(R.id.settings_bt);
        ImageView my_downloads_icon = findViewById(R.id.my_downloads_icon);
        ImageView settings_icon = findViewById(R.id.settings_icon);

        settings_bt.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), Settings.class)));

        my_downloads_bt.setOnClickListener(v -> {

            Intent intent = new Intent(getApplicationContext(), MyDownloads.class);

            String trans = getString(R.string.change_bounds);

            ActivityOptions activityOptions = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this, my_downloads_icon, trans);
            startActivity(intent, activityOptions.toBundle());
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(false);

        });

        settings_bt.setOnClickListener(v -> {

            Intent intent = new Intent(getApplicationContext(), Settings.class);

            String trans = getString(R.string.change_bounds);

            ActivityOptions activityOptions = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this, settings_icon, trans);
            startActivity(intent, activityOptions.toBundle());
            getWindow().setAllowEnterTransitionOverlap(false);
            getWindow().setAllowReturnTransitionOverlap(false);

        });

        LayoutInflater layoutInflater = getLayoutInflater();
        //downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View view = layoutInflater.inflate(R.layout.progress_bar, null);
        dialog.setView(view);
        dialog.setIcon(R.drawable.ic_round_cloud_download_24);
        dialog.setTitle(R.string.downloading_);
        dialog.setCancelable(false);

        progressBar = view.findViewById(R.id.progressBar);
        percent_pg = view.findViewById(R.id.percent_pg);

        pgDialog = dialog.create();

    }

    private void loading(boolean value){

        if (value){
            loading_layout.setVisibility(View.VISIBLE);
            progress_circular.setVisibility(View.VISIBLE);
            download_bt.setEnabled(false);
        }else {
            loading_layout.setVisibility(View.GONE);
            progress_circular.setVisibility(View.GONE);
            download_bt.setEnabled(true);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final Intent intent1 = getIntent();
        final String dataString;
        if (intent1 != null && (dataString = intent1.getDataString()) != null) {
            OPENED_FROM_OUTSIDE = dataString;
        }
    }

    public void download(View view) {

        final boolean isThereConnection = ConnectivityService.isConnected(this);

        if (!isThereConnection) {
            download_bt.setVisibility(View.GONE);
            text.setText(R.string.no_internet_expand);
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            return;
        }

        if (OPENED_FROM_OUTSIDE != null && OPENED_FROM_OUTSIDE.contains("vm.tiktok.com/")) {
            Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
        } else if (clipboardManager != null) {

            loading(true);

            final ClipDescription clipDescription = clipboardManager.getPrimaryClipDescription();

            if (clipboardManager.hasPrimaryClip() && clipDescription != null &&
                    clipDescription.hasMimeType(MIMETYPE_TEXT_PLAIN)) {
                final ClipData link = clipboardManager.getPrimaryClip();

                if (link != null) {
                    final ClipData.Item item = link.getItemAt(0);

                    final String clipboardUrl = item.getText().toString();

                    if (clipboardUrl.contains(getString(R.string.tick_tok__base_url))) {
                        Log.i("tiktok1", clipboardUrl);
                        ArrayList<String> trueLink = getURLS(clipboardUrl);
                        saveVideo(trueLink.get(0));
                        Toast.makeText(this, R.string.please_wait, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (clipboardUrl.isEmpty()) {
                        loading(false);
                        showAlert(getString(R.string.no_copy_title), getString(R.string.no_copy), R.color.red);
                    }else {
                        loading(false);
                        showAlert(getString(R.string.not_valid_link), getString(R.string.copy_valid_link), R.color.red);
                    }

                    return;
                }

            } else {
                loading(false);
                showAlert(getString(R.string.no_copy_title), getString(R.string.copy_valid_link), R.color.red);
                return;
            }

            loading(false);
            showAlert(getString(R.string.error_loading_clipboard_title), getString(R.string.error_loading_clipboard), R.color.red);

        }
    }

    public void showAlert(final String title, final String message, final int color) {
        Alerter.create(this)
                .setTitle(title)
                .setText(message)
                .setIcon(R.drawable.ic_round_error_outline_24)
                .setBackgroundColorRes(color)
                .enableSwipeToDismiss()
                .show();
    }

    public void showAlert(final String title, final String message, final int color, int drawable, View.OnClickListener onClickListener) {
        Alerter.create(this)
                .setTitle(title)
                .setOnClickListener(onClickListener)
                .setText(message)
                .setIcon(drawable)
                .setBackgroundColorRes(color)
                .enableSwipeToDismiss()
                .show();
    }

    @NonNull
    public final ArrayList<String> getURLS(final String str) {
        final ArrayList<String> arrayList = new ArrayList<>();
        final Matcher matcher = Pattern.compile("\\(?\\b(https?://|www[.]|ftp://)[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]").matcher(str);
        while (matcher.find()) {
            String group = matcher.group();
            if (group.startsWith("(") && group.endsWith(")"))
                group = group.substring(1, group.length() - 1);
            arrayList.add(group);
        }
        return arrayList;
    }

    public void saveVideo(final String str) {

        new Thread(() -> {
            try {

                final Document document = Jsoup.connect(str)
                        .userAgent("Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                        .get();

                for (Element element : document.select("script")) {
                    final String data = element.data();
                    if (data.contains("videoData")) {
                        final String substring = data.substring(data.lastIndexOf("urls"));
                        final String substring2 = substring.substring(substring.indexOf("[") + 1);
                        final String substring3 = substring2.substring(0, substring2.indexOf("]"));
                        playURL = substring3.substring(1, substring3.length() - 1);
                    }
                }

                //downloader(playURL);
                runOnUiThread(() -> new DownloadTask().execute(playURL));
            } catch (final Exception e) {
                Log.e("tiktok2", "", e);
            }
        }).start();
    }

    /*public void downloader(final String str) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), "Download Started", Toast.LENGTH_SHORT).show());

        final DownloadManager.Request request = new DownloadManager.Request(Uri.parse(withoutWatermark(str)));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setAllowedOverRoaming(true);
        request.setTitle(getString(R.string.app_name));
        request.setDescription("Downloading");
        request.setMimeType("application/mp4");
        request.setNotificationVisibility(1);
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(this, DIRECTORY_DOWNLOADS, ("tiktok__" + System.currentTimeMillis()) + "." + "mp4");

        downloadManager.enqueue(request);

    }*/

    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private PowerManager.WakeLock mWakeLock;
        private String fileName = "TikTokDownloader" + System.currentTimeMillis() +".mp4";

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;

            try {
                Uri uri = Uri.parse(withoutWatermark(sUrl[0]));
                URL url = new URL(uri.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(Objects.requireNonNull(MainActivity.this.getExternalFilesDir(DIRECTORY_DOWNLOADS))
                        .getAbsolutePath() + File.separator + fileName);

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);

                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setProgress(0);
            percent_pg.setText("0%");
            loading(false);
            pgDialog.show();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        getClass().getName());
            }
            mWakeLock.acquire(10*60*1000L /*10 minutes*/);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            percent_pg.setText(String.format(Locale.getDefault(),"%3d %%",progress[0]));

            // if we get here, length is known, now set indeterminate to false
            progressBar.setIndeterminate(false);
            progressBar.setMax(100);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            mWakeLock.release();
            pgDialog.dismiss();
            if (result != null){
                showAlert("Error",result,R.color.red);
                Log.e("Error Downloading: ", result);
            }
            else{

                File file = new File(Objects.requireNonNull(MainActivity.this.getExternalFilesDir(DIRECTORY_DOWNLOADS))
                        .getAbsolutePath() + File.separator + fileName);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/mp4");
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getAbsolutePath()));

                showAlert("File Downloaded", "Click to Share",
                        R.color.green, R.drawable.ic_round_done_24, v ->
                                startActivity(Intent.createChooser(shareIntent, "Share Video")));
            }
        }
    }

    /**
     * We call the TikTok Server to get a watermark free sample of the video
     * DO NOT TOUCH THIS SNIPPET OF CODE IS VERY CRITITCAL AND MAY BREAK THE NO WATERMARK DEAL
     */
    public String withoutWatermark(final String url) {
        try {
            final HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
            httpURLConnection.connect();
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                final StringBuilder stringBuffer = new StringBuilder();

                String readLine;
                while ((readLine = bufferedReader.readLine()) != null) {
                    stringBuffer.append(readLine);

                    if (stringBuffer.toString().contains("vid:")) {
                        try {
                            if (stringBuffer.substring(stringBuffer.indexOf("vid:")).substring(0, 4).equals("vid:")) {
                                final String substring = stringBuffer.substring(stringBuffer.indexOf("vid:"));
                                final String trim = substring.substring(4, substring.indexOf("%"))
                                        .replaceAll("[^A-Za-z0-9]", "").trim();
                                return "http://api2.musical.ly/aweme/v1/playwm/?video_id=" + trim;
                            }
                        } catch (final Exception e) {
                            Log.e("tiktok3", "", e);
                        }
                    }
                }
            }

        } catch (final Exception e) {
            Log.e("tiktok4", "", e);
        }

        return "";
    }

}