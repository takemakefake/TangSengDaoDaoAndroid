package com.chat.im.interfaces;


import com.chat.im.entity.WKChannelMember;

import java.util.List;

/**
 * 2020-02-01 16:39
 * 添加频道成员
 */
public interface IAddChannelMemberListener {
    void onAddMembers(List<WKChannelMember> list);
}
