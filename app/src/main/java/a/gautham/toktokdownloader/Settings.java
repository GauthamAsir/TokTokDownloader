package a.gautham.toktokdownloader;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LinearLayout how_to_use = findViewById(R.id.how_to_use);
        how_to_use.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.guide);
            builder.setMessage(R.string.guide_explained);
            builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        LinearLayout shareApp = findViewById(R.id.shareApp);
        shareApp.setOnClickListener(v -> {
            Intent shareIntent =   new Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String app_url = "https://github.com/GauthamAsir/TokTokDownloader/releases";
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Download Tik-Tok videos without any watermark\nNo worries about privacy, im not requesting any permission in app \n\n" + app_url);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,"Tik-Tok Downloader without watermark");
            startActivity(Intent.createChooser(shareIntent, "Share via"));
        });

        LinearLayout contact_us = findViewById(R.id.contact_us);
        contact_us.setOnClickListener(v -> {

        });

        LinearLayout about = findViewById(R.id.about);
        about.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.about);

            PackageInfo pInfo = null;
            try {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String version = pInfo != null ? pInfo.versionName : "1.0";

            builder.setMessage(String.format(Locale.getDefault(),"App version: %s",version));
            builder.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

    }
}