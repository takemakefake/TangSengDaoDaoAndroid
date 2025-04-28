package com.chat.base.endpoint.entity

import android.content.Context
import com.chat.base.ui.components.AvatarView
import com.chat.im.entity.WKChannel

class ShowCommunityAvatarMenu(
    val context: Context,
    val avatarView: AvatarView,
    val channel: WKChannel?
)