package com.chat.base.endpoint.entity

import android.widget.FrameLayout
import com.chat.base.ui.components.AvatarView
import com.chat.im.entity.WKChannel

class AvatarOtherViewMenu(
    val otherView: FrameLayout,
    val channel: WKChannel,
    val avatarView: AvatarView,
    val showUpdateDialog: Boolean
) {
}