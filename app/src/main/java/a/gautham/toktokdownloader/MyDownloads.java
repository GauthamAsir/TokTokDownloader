package a.gautham.toktokdownloader;

import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class MyDownloads extends AppCompatActivity {

    private List<File> videoList = new ArrayList<>();
    private File path;
    private TextView error;
    private ProgressBar progress_circular;
    private final Handler handler = new Handler();
    private GridView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_downloads);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        path = this.getExternalFilesDir(DIRECTORY_DOWNLOADS);

        listView = findViewById(R.id.listView);
        error = findViewById(R.id.error);
        progress_circular = findViewById(R.id.progress_circular);

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(this::getFiles,700);
    }

    private void getFiles() {

        new Thread(() -> {

            if (path.exists()){

                runOnUiThread(() -> {
                    error.setVisibility(View.GONE);

                    File[] savedFiles = path.listFiles();

                    if (savedFiles != null){

                        videoList.clear();

                        Arrays.sort(savedFiles);

                        for (File file : savedFiles){
                            if (file.getName().endsWith(".mp4")){
                                videoList.add(file);
                            }
                        }

                        handler.post(() -> {
                            Adapter adapter = new Adapter(this, videoList);
                            listView.setAdapter(adapter);
                            progress_circular.setVisibility(View.GONE);
                        });
                        
                    }else {
                        runOnUiThread(() -> {
                            error.setVisibility(View.VISIBLE);
                            error.setText(R.string.no_files);
                        });
                    }

                });

            }else {
                runOnUiThread(() -> {
                    error.setVisibility(View.VISIBLE);
                    error.setText(R.string.directory_not_found);
                });
            }

        }).start();

    }

}