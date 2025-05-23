package com.chat.im.interfaces;


import com.chat.im.msgmodel.WKMessageContent;

/**
 * 2019-11-10 16:12
 * 上传附件回掉
 */
public interface IUploadAttacResultListener {
    /**
     * 上传附件返回结果
     **/
    void onUploadResult(boolean isSuccess, WKMessageContent messageContent);
}
