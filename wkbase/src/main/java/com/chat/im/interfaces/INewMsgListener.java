package com.chat.im.interfaces;


import com.chat.im.entity.WKMsg;

import java.util.List;

/**
 * 2019-11-18 11:44
 * 新消息监听
 */
public interface INewMsgListener {
    void newMsg(List<WKMsg> msgs);
}
