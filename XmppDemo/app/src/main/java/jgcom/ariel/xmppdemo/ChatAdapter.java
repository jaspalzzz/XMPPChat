package jgcom.ariel.xmppdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Ariel-17 on 11/6/2017.
 */

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    int SENDER_VIEW = 1;
    int RECIEVER_VIEW = 0;
    Context context;
    LayoutInflater inflater;
    List<ChatModel> chatList;

    public ChatAdapter(Context context, List<ChatModel> chatList) {
        this.context = context;
        this.chatList = chatList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {

        if (this.chatList.get(position).getIsMine().equals("true")) {
            return SENDER_VIEW;
        } else {
            return RECIEVER_VIEW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == SENDER_VIEW) {
            view = inflater.inflate(R.layout.msg_send_layout, parent, false);
            return new SendViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.msg_recieve_layout, parent, false);
            return new RecieveViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof SendViewHolder)
        {
            SendViewHolder sendViewHolder = (SendViewHolder) holder;
            sendViewHolder.mSendText.setText(chatList.get(position).getMsg());
        }
        else {
            RecieveViewHolder recieveViewHolder= (RecieveViewHolder) holder;
            recieveViewHolder.mSendText.setText(chatList.get(position).getMsg());
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    class SendViewHolder extends RecyclerView.ViewHolder {
        TextView mSendText;

        public SendViewHolder(View itemView) {
            super(itemView);
            mSendText = itemView.findViewById(R.id.send_textview);
        }
    }

    class RecieveViewHolder extends RecyclerView.ViewHolder {
        TextView mSendText;

        public RecieveViewHolder(View itemView) {
            super(itemView);
            mSendText = itemView.findViewById(R.id.recieve_textview);
        }
    }

    public void notifyAdapter(List<ChatModel> chatList) {
        this.chatList = chatList;
        notifyDataSetChanged();
    }
}
