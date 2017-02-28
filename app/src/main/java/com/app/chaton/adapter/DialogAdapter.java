package com.app.chaton.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.DialogActivity;
import com.app.chaton.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder>{

    private final static int DAY_MILLS = 86400000;

    private DialogActivity.DialogListenerFactory dialogListenerFactory;

    private Context context;
    private List<Message> message_list;

    private SimpleDateFormat timePattern, datePattern;
    {
        timePattern = new SimpleDateFormat("HH:mm");
        datePattern = new SimpleDateFormat("MMM d");
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewGroup dialogLayout;
        public TextView name, text, date;
        public ImageView status;
        public CircleImageView friendImage, ownerImage;

        public ViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.friendName);
            this.text = (TextView) itemView.findViewById(R.id.friendMess);
            this.date = (TextView) itemView.findViewById(R.id.friendDate);
            this.friendImage = (CircleImageView) itemView.findViewById(R.id.friendImage);
            this.ownerImage = (CircleImageView) itemView.findViewById(R.id.ownerImage);
            this.status = (ImageView) itemView.findViewById(R.id.friendStatus);
            this.dialogLayout = (ConstraintLayout) itemView.findViewById(R.id.dialogItem);
        }
    }

    public DialogAdapter(Context context, List<Message> message_list,
                         DialogActivity.DialogListenerFactory dialogListenerFactory) {
        this.context = context;
        this.message_list = message_list;
        this.dialogListenerFactory = dialogListenerFactory;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (viewType == Message.TYPE_TO) ? LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.dialog_item_to, parent, false)
                                          : LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.dialog_item_from, parent, false);
        return  new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder dialog, int position) {
        final Message message = this.message_list.get(position);

        dialog.name.setText(message.getCompanion().getName());
        dialog.date.setText(getDateCreation(message.createdAt()));

        if (message.isViewed())
            dialog.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_not_active));
        else
            dialog.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_active));

        Typeface myriad = Typeface.createFromAsset(context.getAssets(), "fonts/MyriadPro.ttf");
        dialog.text.setTypeface(myriad);
        dialog.name.setTypeface(myriad);
        dialog.date.setTypeface(myriad);

        if (dialog.getItemViewType() == Message.TYPE_TO) {
            if (message.getBody().length() > 25)
                dialog.text.setText(message.getBody().substring(0, 23).concat("..."));
            else
                dialog.text.setText(message.getBody());
            // TODO bind user porfile image
        } else {
            if (message.getBody().length() > 30)
                dialog.text.setText(message.getBody().substring(0, 28).concat("..."));
            else
                dialog.text.setText(message.getBody());
        }

        if (!message.getCompanion().getAvatar().equals("avatar.png"))
            new BitmapDownloader(dialog.friendImage, message.getCompanion().getAvatar(), 45).execute();

        dialog.dialogLayout.setOnClickListener(
                dialogListenerFactory.createListener(message.getCompanion().getId(),
                                                     message.getCompanion().getName()));
    }

    private class BitmapDownloader extends AsyncTask<Void, Void, Bitmap> {
        private static final String DOWNLOAD_URL = "https://chaton.ga/uploads/avatars/";

        CircleImageView imageView;
        String image_url;
        int reqSide;

        BitmapDownloader(CircleImageView imageView, String image_url, int reqSide) {
            this.imageView = imageView;
            this.image_url = image_url;
            this.reqSide = reqSide;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            try {
                URL url = new URL(DOWNLOAD_URL + image_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream inputStream = connection.getInputStream();

                Bitmap bm = BitmapFactory.decodeStream(inputStream);

                int width = bm.getWidth();
                int height = bm.getHeight();
                float scaleWidth = ((float) reqSide) / width;
                float scaleHeight = ((float) reqSide) / height;

                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);

                return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
            } catch (IOException e) {
                Log.d("myLogs", "stub");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
                Log.d("myLogs", "" + bitmap.getWidth());
                imageView.setImageBitmap(bitmap);
        }
    }

    private String getDateCreation(Long created_at) {
        Date creation_date = new Date(created_at);
        long time_diff = new Date().getTime() - creation_date.getTime();

        if (time_diff < DAY_MILLS)
            return timePattern.format(creation_date);
        else if (time_diff < DAY_MILLS * 2)
            return context.getResources().getString(R.string.yesterday);
        else
            return datePattern.format(creation_date);
    }

    @Override
    public int getItemViewType(int position) {
        return this.message_list.get(position).getType();

    }

    @Override
    public int getItemCount() { return this.message_list.size(); }

    public List<Message> getMessageList() { return this.message_list; }
}
