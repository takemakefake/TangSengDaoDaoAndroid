package com.chat.uikit.enity

import com.chat.im.entity.WKChannelMember

class AllGroupMemberEntity(
    val channelMember: WKChannelMember,
    val onLine: Int,
    val lastOfflineTime: String,
    val lastOnlineTime: String,
) {
}