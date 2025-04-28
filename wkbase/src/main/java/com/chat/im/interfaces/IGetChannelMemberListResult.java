package com.chat.im.interfaces;

import com.chat.im.entity.WKChannelMember;

import java.util.List;

public interface IGetChannelMemberListResult {
    public void onResult(List<WKChannelMember> list, boolean isRemote);
}
