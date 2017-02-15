package com.app.chaton.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.R;

import java.util.List;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder>{

    private static final int TYPE_TO = 0;
    private static final int TYPE_FROM = 1;

    private Context context;
    private List<Message> message_list;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, text;
        public ImageView friendImage, ownerImage, status;

        public ViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.friendName);
            this.text = (TextView) itemView.findViewById(R.id.friendMess);
            this.friendImage = (ImageView) itemView.findViewById(R.id.friendImage);
            this.ownerImage = (ImageView) itemView.findViewById(R.id.ownerImage);
            this.status = (ImageView) itemView.findViewById(R.id.friendStatus);
        }
    }

    public DialogAdapter(Context context, List<Message> message_list) {
        this.context = context;
        this.message_list = message_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("myLogs", " " + viewType);

        View view = (viewType == TYPE_TO) ? LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.dialog_item_to, parent, false)
                                          : LayoutInflater.from(parent.getContext())
                                              .inflate(R.layout.dialog_item_from, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder dialog, int position) {
        Message message = this.message_list.get(position);

        dialog.name.setText(message.getCompanion().getName());

        if (message.isViewed())
            dialog.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_not_active));
        else
            dialog.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_active));

        Typeface myriad = Typeface.createFromAsset(context.getAssets(), "fonts/MyriadPro.ttf");
        dialog.text.setTypeface(myriad);
        dialog.name.setTypeface(myriad);

        if (dialog.getItemViewType() == TYPE_TO) {
            if (message.getBody().length() > 25)
                dialog.text.setText(message.getBody().substring(0, 23).concat("..."));
            else
                dialog.text.setText(message.getBody());

            // TODO bind user porfile image
        }

        if (message.getBody().length() > 30)
            dialog.text.setText(message.getBody().substring(0, 28).concat("..."));
        else
            dialog.text.setText(message.getBody());

        // TODO bind friend's profile image
    }

    @Override
    public int getItemViewType(int position) {
        Log.d("myLogs", " " + (this.message_list.get(position).to().equals(this.message_list.get(position).getCompanion().getId()) ? TYPE_TO : TYPE_FROM));

        return ((this.message_list.get(position).to()
                .equals(this.message_list.get(position).getCompanion().getId())) ? TYPE_TO : TYPE_FROM);

    }

    @Override
    public int getItemCount() {
        return this.message_list.size();
    }
}
