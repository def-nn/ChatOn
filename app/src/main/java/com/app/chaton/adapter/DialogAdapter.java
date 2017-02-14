package com.app.chaton.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.R;

import java.util.List;

public class DialogAdapter extends RecyclerView.Adapter<DialogAdapter.ViewHolder>{
    private Context context;
    private List<Message> message_list;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, text;
        public ImageView image, status;

        public ViewHolder(View itemView) {
            super(itemView);
            this.name = (TextView) itemView.findViewById(R.id.friendName);
            this.text = (TextView) itemView.findViewById(R.id.friendMess);
            this.image = (ImageView) itemView.findViewById(R.id.friendImage);
            this.status = (ImageView) itemView.findViewById(R.id.friendStatus);
        }
    }

    public DialogAdapter(Context context, List<Message> message_list) {
        this.context = context;
        this.message_list = message_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                                        .inflate(R.layout.dialog_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder dialog, int position) {
        Message message = this.message_list.get(position);
        dialog.name.setText(message.getCompanion().getName());

        if (message.getBody().length() > 30)
            dialog.text.setText(message.getBody().substring(0, 28).concat("..."));
        else
            dialog.text.setText(message.getBody());

        if (message.isViewed())
            dialog.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_not_active));
        else
            dialog.status.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.circle_active));

        Typeface myriad = Typeface.createFromAsset(context.getAssets(), "fonts/MyriadPro.ttf");
        dialog.text.setTypeface(myriad);
        dialog.name.setTypeface(myriad);

        // TODO bind profile image
    }

    @Override
    public int getItemCount() {
        return this.message_list.size();
    }
}
