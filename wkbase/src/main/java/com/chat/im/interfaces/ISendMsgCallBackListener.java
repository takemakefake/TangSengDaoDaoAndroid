package com.chat.im.interfaces;


import com.chat.im.entity.WKMsg;

/**
 * 2020-08-02 00:21
 * 发送消息监听
 */
public interface ISendMsgCallBackListener {
    void onInsertMsg(WKMsg msg);
}
