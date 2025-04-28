package com.chat.base.endpoint.entity

import com.chat.im.entity.WKMsg

class PrivacyMessageMenu(val iClick: IClick) {

    interface IClick {
        fun onDelete(mMsg: WKMsg)
        fun clearChannelMsg(channelID: String, channelType: Byte)
    }
}