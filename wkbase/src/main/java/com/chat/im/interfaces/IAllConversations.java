package com.chat.im.interfaces;

import com.chat.im.entity.WKUIConversationMsg;

import java.util.List;

public interface IAllConversations {
    void onResult(List<WKUIConversationMsg> list);
}
