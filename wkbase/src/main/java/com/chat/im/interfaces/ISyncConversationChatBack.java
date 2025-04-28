package com.chat.im.interfaces;


import com.chat.im.entity.WKSyncChat;

/**
 * 2020-10-09 14:43
 * 同步消息返回
 */
public interface ISyncConversationChatBack {
    void onBack(WKSyncChat syncChat);
}
