package com.chat.im.interfaces;


import com.chat.im.entity.WKCMD;

/**
 * 2/3/21 2:23 PM
 * cmd监听
 */
public interface ICMDListener {
    void onMsg(WKCMD wkcmd);
}
