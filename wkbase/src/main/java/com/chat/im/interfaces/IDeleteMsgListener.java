package com.chat.im.interfaces;


import com.chat.im.entity.WKMsg;

/**
 * 2020-08-19 21:42
 * 删除消息监听
 */
public interface IDeleteMsgListener {
    void onDeleteMsg(WKMsg msg);
}
