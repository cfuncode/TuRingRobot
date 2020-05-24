package com.cjz.turingrobot.adapter;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.cjz.turingrobot.R;
import com.cjz.turingrobot.db.ChatBean;

import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private List<ChatBean> chatBeanList; //聊天数据
    private LayoutInflater layoutInflater;
    public boolean flag = false;

    public ChatAdapter(List<ChatBean> chatBeanList, Context context) {
        this.chatBeanList = chatBeanList;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return chatBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View contentView, ViewGroup viewGroup) {
        final Holder holder = new Holder();
        //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
        if (chatBeanList.get(position).getState() == ChatBean.RECEIVE) {
            //加载左边布局，也就是机器人对应的布局信息
            contentView = layoutInflater.inflate(R.layout.chatting_left_item, null);
        } else {
            //加载右边布局，也就是用户对应的布局信息
            contentView = layoutInflater.inflate(R.layout.chatting_right_item, null);
        }
        holder.tv_chat_content=contentView.findViewById(R.id.tv_chat_content);
        holder.tv_chat_content.setText(chatBeanList.get(position).getMessage());
        holder.checkBox=contentView.findViewById(R.id.select);
        holder.tv_chat_content.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        // 根据isSelected来设置checkbox的显示状况
        if (flag) {
            holder.checkBox.setVisibility(View.VISIBLE);
            if (chatBeanList.get(position).isCheck) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatBeanList.get(position).isCheck) {
                    chatBeanList.get(position).isCheck = false;
                    holder.checkBox.setChecked(false);
                } else {
                    chatBeanList.get(position).isCheck = true;
                    holder.checkBox.setChecked(true);
                }
            }
        });
        return contentView;
    }

    class Holder {
        public TextView tv_chat_content; // 聊天内容
        public CheckBox checkBox; // 多选
    }
}

