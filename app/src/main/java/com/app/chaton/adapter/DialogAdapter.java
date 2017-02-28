package com.app.chaton.adapter;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
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
import com.app.chaton.Utils.ImageDownloader;
import com.app.chaton.Utils.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder>{

    private final static int DAY_MILLS = 86400000;

    private DialogActivity.DialogListenerFactory dialogListenerFactory;

    private Activity activity;
    private List<Message> message_list;
    private PreferenceHelper preferenceHelper;

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

    public DialogAdapter(Activity activity, List<Message> message_list,
                         DialogActivity.DialogListenerFactory dialogListenerFactory,
                         PreferenceHelper preferenceHelper) {
        this.activity = activity;
        this.message_list = message_list;
        this.dialogListenerFactory = dialogListenerFactory;
        this.preferenceHelper = preferenceHelper;
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
            dialog.status.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.circle_not_active));
        else {
            dialog.status.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.circle_active));
            dialog.friendImage.setBorderWidth(6);
            dialog.friendImage.setBorderColor(ContextCompat.getColor(activity, R.color.colorAccent));
        }

        Typeface myriad = Typeface.createFromAsset(activity.getAssets(), "fonts/MyriadPro.ttf");
        dialog.text.setTypeface(myriad);
        dialog.name.setTypeface(myriad);
        dialog.date.setTypeface(myriad);

        if (dialog.getItemViewType() == Message.TYPE_TO) {
            if (message.getBody().length() > 25)
                dialog.text.setText(message.getBody().substring(0, 23).concat("..."));
            else
                dialog.text.setText(message.getBody());

            new ImageDownloader(activity, preferenceHelper.getAvatar()) {
                @Override
                protected void onPostExecute(byte[] bm_data) {
                    super.onPostExecute(bm_data);
                    dialog.ownerImage.setImageBitmap(BitmapFactory.decodeByteArray(bm_data, 0, bm_data.length));
                }
            }.execute();

        } else {
            if (message.getBody().length() > 30)
                dialog.text.setText(message.getBody().substring(0, 28).concat("..."));
            else
                dialog.text.setText(message.getBody());
        }

        if (!message.getCompanion().getAvatar().equals("avatar.png"))
            new ImageDownloader(activity, message.getCompanion().getAvatar()) {
                @Override
                protected void onPostExecute(byte[] bm_data) {
                    super.onPostExecute(bm_data);
                    dialog.friendImage.setImageBitmap(BitmapFactory.decodeByteArray(bm_data, 0, bm_data.length));
                }
            }.execute();

        dialog.dialogLayout.setOnClickListener(
                dialogListenerFactory.createListener(message.getCompanion().getId(),
                                                     message.getCompanion().getName(),
                                                     message.getCompanion().getAvatar()));
    }

    private String getDateCreation(Long created_at) {
        Date creation_date = new Date(created_at * 1000);
        long time_diff = new Date().getTime() - creation_date.getTime();

        Log.d("myLogs", "date " + new Date().getTime() + " " + creation_date.getTime());

        if (time_diff < DAY_MILLS) {
            Log.d("myLogs", "date1 " + timePattern.format(creation_date));
            return timePattern.format(creation_date);
        }
        else if (time_diff < DAY_MILLS * 2) {
            Log.d("myLogs", "date2 " + activity.getResources().getString(R.string.yesterday));
            return activity.getResources().getString(R.string.yesterday);
        }
        else {
            Log.d("myLogs", "date3 " + datePattern.format(creation_date));
            return datePattern.format(creation_date);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.message_list.get(position).getType();

    }

    @Override
    public int getItemCount() { return this.message_list.size(); }

    public List<Message> getMessageList() { return this.message_list; }
}
