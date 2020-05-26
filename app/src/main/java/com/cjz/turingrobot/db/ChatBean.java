package com.cjz.turingrobot.db;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class ChatBean extends LitePalSupport implements Serializable {
    @Column(ignore = true)
    public static final int SEND = 1;     //发送消息
    @Column(ignore = true)
    public static final int RECEIVE = 2; //接收到的消息
    @Column(ignore = true)
    public boolean isCheck = false;
    private int id;
    private int state;       //消息的状态（是接收还是发送）
    private String message; //消息的内容

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
