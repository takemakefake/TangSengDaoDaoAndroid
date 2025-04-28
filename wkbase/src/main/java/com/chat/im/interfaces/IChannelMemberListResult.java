package com.chat.im.interfaces;


import com.chat.im.entity.WKChannelMember;

import java.util.List;

public interface IChannelMemberListResult {
    void onResult(List<WKChannelMember> list);
}
