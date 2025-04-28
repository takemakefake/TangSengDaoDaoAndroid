package com.chat.im.interfaces;

import com.chat.im.entity.WKReminder;

import java.util.List;

public interface INewReminderListener {
    void newReminder(List<WKReminder> list);
}
