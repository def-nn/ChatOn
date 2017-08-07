package com.app.chaton.adapter;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private Cursor cursor;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messBody;
        public ImageView messViewed, icError, icProcess;

        public ViewHolder(View itemView) {
            super(itemView);
            messBody = (TextView) itemView.findViewById(R.id.messBody);
            messViewed = (ImageView) itemView.findViewById(R.id.icNotViewed);
            icError = (ImageView) itemView.findViewById(R.id.icError);
            icProcess = (ImageView) itemView.findViewById(R.id.icProcess);
        }
    }

    public ChatAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    public void setCursor(Cursor cursor) {
        Log.d("myLogs", "cursor " + cursor.getCount());
        this.cursor = cursor;
    }

    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = (viewType == Message.TYPE_TO) ? LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_to, parent, false)
                : LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_from, parent, false);
        return  new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder message_view, int position) {
        cursor.moveToPosition(position);

        if (message_view.getItemViewType() == Message.TYPE_TO)
            switch (cursor.getInt(7)) {
                case Message.STATE_PROCESS:
                    message_view.icProcess.setVisibility(View.VISIBLE);
                    break;
                case Message.STATE_FAILURE:
                    message_view.icError.setVisibility(View.VISIBLE);
                    break;
                case Message.STATE_SUCCESS:
                    if (cursor.getInt(5) == 0) message_view.messViewed.setVisibility(View.VISIBLE);
        }

        message_view.messBody.setText(cursor.getString(4));
    }

    @Override
    public int getItemViewType(int position) {
        cursor.moveToPosition(position);
        return cursor.getInt(3);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }
}
