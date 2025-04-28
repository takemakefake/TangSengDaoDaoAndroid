package com.chat.im.interfaces;


import com.chat.im.entity.WKChannelMember;

/**
 * 2019-12-01 15:52
 * 获取频道成员
 */
public interface IGetChannelMemberInfo {
    WKChannelMember onResult(String channelId, byte channelType, String uid, IChannelMemberInfoListener iChannelMemberInfoListener);
}
