package com.chat.uikit.chat.manager;

import com.chat.im.entity.WKChannel;
import com.chat.im.entity.WKMsgSetting;
import com.chat.im.entity.WKSendOptions;
import com.chat.im.msgmodel.WKMessageContent;

public class SendMsgEntity {
    public WKMessageContent messageContent;
    public WKChannel wkChannel;
    public WKSendOptions options;

    public SendMsgEntity(WKMessageContent messageContent, WKChannel channel, WKSendOptions options) {
        this.wkChannel = channel;
        this.messageContent = messageContent;
        this.options = options;
    }
}
