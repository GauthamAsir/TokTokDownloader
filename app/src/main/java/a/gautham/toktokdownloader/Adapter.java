package a.gautham.toktokdownloader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;
import java.util.Objects;

import a.gautham.toktokdownloader.helpers.ThumbnailUtils;

public class Adapter extends ArrayAdapter<File> {

    public Adapter(@NonNull Context context, List<File> files) {
        super(context,R.layout.items_layout, files);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        File file = getItem(position);

        ViewHolder viewHolder;

        if (convertView==null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.items_layout, parent, false);
            viewHolder.thumbnail = convertView.findViewById(R.id.thumbnail);
            viewHolder.share_bt = convertView.findViewById(R.id.share_bt);
            viewHolder.delete_bt = convertView.findViewById(R.id.delete_bt);

            viewHolder.progressBar = convertView.findViewById(R.id.progressBar);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(Objects.requireNonNull(file).getAbsolutePath(), 3);
        viewHolder.thumbnail.setImageBitmap(bitmap);
        viewHolder.progressBar.setVisibility(View.GONE);

        viewHolder.share_bt.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/mp4");
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getAbsolutePath()));
            parent.getContext().startActivity(Intent.createChooser(shareIntent, "Share Video"));
        });

        viewHolder.delete_bt.setOnClickListener(v -> {
            if (file.delete()){
                remove(file);
                Toast.makeText(parent.getContext(), "File Deleted", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    static class ViewHolder{
        ImageView thumbnail, share_bt, delete_bt;
        ProgressBar progressBar;
    }

}
