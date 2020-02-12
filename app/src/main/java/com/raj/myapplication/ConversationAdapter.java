package com.raj.myapplication;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    public static final int VIEW_TYPE_BOT = 0;
    public static final int VIEW_TYPE_USER = 1;


    private List<Message> messageArrayList = new ArrayList<>();
    private Context context;

    public ConversationAdapter(Context context) {
        this.context = context;
    }

    public void addMessage(String s, int type) {
        Message message = new Message();
        message.userType = type;
        message.msg = s;
        messageArrayList.add(message);
        notifyItemInserted(messageArrayList.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return messageArrayList.get(position).userType;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bot_bubble, parent, false);
        ConversationViewHolder conversationViewHolder = new ConversationViewHolder(view);
        if (viewType == VIEW_TYPE_BOT) {
            conversationViewHolder.textView.setBackground(ContextCompat.getDrawable(context,R.drawable.bot_bubble));
            conversationViewHolder.textView.setTextColor(ContextCompat.getColor(context,R.color.colorAccent));
            ((FrameLayout.LayoutParams) conversationViewHolder.textView.getLayoutParams()).gravity = Gravity.RIGHT;
        } else if (viewType == VIEW_TYPE_USER) {
            conversationViewHolder.textView.setBackground(ContextCompat.getDrawable(context,R.drawable.me_bubble));
            conversationViewHolder.textView.setTextColor(ContextCompat.getColor(context,R.color.colorPrimary));
            ((FrameLayout.LayoutParams) conversationViewHolder.textView.getLayoutParams()).gravity = Gravity.LEFT;
        }
        return conversationViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        holder.textView.setText(message.msg);
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    static class Message {
        int userType;
        String msg;
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView textView;


        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textMessage);
        }
    }
}
