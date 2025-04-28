package com.chat.im.interfaces;

import com.chat.im.entity.WKUIConversationMsg;

import java.util.List;

public interface IRefreshConversationMsgList {
    void onRefresh(List<WKUIConversationMsg> list);
}
