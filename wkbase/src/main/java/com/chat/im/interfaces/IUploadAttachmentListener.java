package com.chat.im.interfaces;


import com.chat.im.entity.WKMsg;

/**
 * 2020-08-02 00:29
 * 上传聊天附件
 */
public interface IUploadAttachmentListener {
    void onUploadAttachmentListener(WKMsg msg, IUploadAttacResultListener attacResultListener);
}
