package com.chat.im.manager;

import android.text.TextUtils;

import com.chat.im.db.RobotDBManager;
import com.chat.im.entity.WKRobot;
import com.chat.im.entity.WKRobotMenu;
import com.chat.im.interfaces.IRefreshRobotMenu;
import com.chat.im.utils.WKCommonUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RobotManager extends BaseManager {

    private RobotManager() {
    }

    private static class RobotManagerBinder {
        final static RobotManager manager = new RobotManager();
    }

    public static RobotManager getInstance() {
        return RobotManagerBinder.manager;
    }

    private ConcurrentHashMap<String, IRefreshRobotMenu> refreshRobotMenu;

    public WKRobot getWithRobotID(String robotID) {
        return RobotDBManager.getInstance().query(robotID);
    }

    public WKRobot getWithUsername(String username) {
        return RobotDBManager.getInstance().queryWithUsername(username);
    }

    public List<WKRobot> getWithRobotIds(List<String> robotIds) {
        return RobotDBManager.getInstance().queryRobots(robotIds);
    }

    public List<WKRobotMenu> getRobotMenus(String robotID) {
        return RobotDBManager.getInstance().queryRobotMenus(robotID);
    }

    public List<WKRobotMenu> getRobotMenus(List<String> robotIds) {
        return RobotDBManager.getInstance().queryRobotMenus(robotIds);
    }

    public void saveOrUpdateRobots(List<WKRobot> list) {
        if (WKCommonUtils.isNotEmpty(list)) {
            RobotDBManager.getInstance().insertOrUpdateRobots(list);
        }
    }

    public void saveOrUpdateRobotMenus(List<WKRobotMenu> list) {
        if (WKCommonUtils.isNotEmpty(list)) {
            RobotDBManager.getInstance().insertOrUpdateMenus(list);
        }
        setRefreshRobotMenu();
    }

    public void addOnRefreshRobotMenu(String key, IRefreshRobotMenu iRefreshRobotMenu) {
        if (TextUtils.isEmpty(key) || iRefreshRobotMenu == null) return;
        if (refreshRobotMenu == null) refreshRobotMenu = new ConcurrentHashMap<>();
        refreshRobotMenu.put(key, iRefreshRobotMenu);
    }

    public void removeRefreshRobotMenu(String key) {
        if (TextUtils.isEmpty(key) || refreshRobotMenu == null) return;
        refreshRobotMenu.remove(key);
    }

    private void setRefreshRobotMenu() {
        runOnMainThread(() -> {
            for (Map.Entry<String, IRefreshRobotMenu> entry : refreshRobotMenu.entrySet()) {
                entry.getValue().onRefreshRobotMenu();
            }
        });
    }
}
