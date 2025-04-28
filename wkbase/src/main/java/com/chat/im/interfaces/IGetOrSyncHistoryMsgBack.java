package com.chat.im.interfaces;


import com.chat.im.entity.WKMsg;

import java.util.List;

/**
 * 2020-10-10 13:40
 * 获取或同步消息返回
 */
public interface IGetOrSyncHistoryMsgBack {
    void onSyncing();
    void onResult(List<WKMsg> msgs);
}
