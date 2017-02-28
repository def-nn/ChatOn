package com.app.chaton.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.chaton.API_helpers.Message;
import com.app.chaton.R;
import com.app.chaton.Utils.ImageDownloader;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>{

    private Context context;
    private List<Message> message_list;
    private String companionName, myName, companionAvatar, myAvatar;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView messName, messBody;
        public ImageView messImage;

        public ViewHolder(View itemView) {
            super(itemView);
            messName = (TextView) itemView.findViewById(R.id.messName);
            messBody = (TextView) itemView.findViewById(R.id.messBody);
            messImage = (ImageView) itemView.findViewById(R.id.messImage);
        }
    }

    public ChatAdapter(Context context, List<Message> message_list, String myName,
                       String companion_name, String myAvatar, String companionAvatar) {
        this.context = context;
        this.message_list = message_list;
        this.companionName = companion_name;
        this.myName = myName;
        this.myAvatar = myAvatar;
        this.companionAvatar = companionAvatar;
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
    public void onBindViewHolder(final ChatAdapter.ViewHolder message_view, int position) {
        Message message = this.message_list.get(position);

        if (message_view.getItemViewType() == Message.TYPE_TO) {
            message_view.messName.setText(myName);
            new ImageDownloader(context, myAvatar) {
                @Override
                protected void onPostExecute(byte[] bm_data) {
                    super.onPostExecute(bm_data);
                    message_view.messImage.setImageBitmap(BitmapFactory.decodeByteArray(bm_data, 0, bm_data.length));
                }
            }.execute();
        } else {
            message_view.messName.setText(companionName);
            new ImageDownloader(context, companionAvatar) {
                @Override
                protected void onPostExecute(byte[] bm_data) {
                    super.onPostExecute(bm_data);
                    message_view.messImage.setImageBitmap(BitmapFactory.decodeByteArray(bm_data, 0, bm_data.length));
                }
            }.execute();
        }

        message_view.messBody.setText(message.getBody());
    }

    @Override
    public int getItemViewType(int position) {
        return this.message_list.get(position).getType();
    }

    @Override
    public int getItemCount() { return this.message_list.size(); }

    public List<Message> getMessageList() { return this.message_list; }
}
