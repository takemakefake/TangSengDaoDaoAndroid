package com.chat.im.protocol;


import com.chat.im.message.type.WKMsgType;

/**
 * 2020-01-30 17:34
 * 断开连接消息
 */
public class WKDisconnectMsg extends WKBaseMsg {
    public byte reasonCode;
    public String reason;

    public WKDisconnectMsg() {
        packetType = WKMsgType.DISCONNECT;
    }
}
