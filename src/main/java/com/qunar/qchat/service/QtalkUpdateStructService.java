package com.qunar.qchat.service;


import com.qunar.qchat.dao.IUserInfo;
import com.qunar.qchat.dao.model.UserInfoQtalk;
import com.qunar.qchat.model.JsonResult;
import com.qunar.qchat.model.UpdateStructResultMode;
import com.qunar.qchat.utils.JsonResultUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * QtalkUpdateStructService
 *
 * @author binz.zhang
 * @date 2019/1/29
 */
@Repository
public class QtalkUpdateStructService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QtalkUpdateStructService.class);

    @Autowired
    private IUserInfo iUserInfo;

    public JsonResult<?> getQtalk(Integer version, String domian) {
        UpdateStructResultMode updateStructResultMode = new UpdateStructResultMode();
        try {
            Integer hostId = iUserInfo.getHostInfo(domian);
            Integer curVersion = iUserInfo.selectMaxVersion(hostId);
            if (curVersion <= version) {
                updateStructResultMode.setVersion(curVersion);
                updateStructResultMode.setUpdate(new LinkedList<>());
                updateStructResultMode.setDelete(new LinkedList<>());
                return JsonResultUtils.success(updateStructResultMode);
            }
            List<UserInfoQtalk> users = iUserInfo.getQtalkUsersByVersion(version, hostId);
            if (users == null) {
                users = new ArrayList<>();
            }
            List<UserInfoQtalk> onJobUsers = users.stream().filter(QtalkUpdateStructService::filterOutLeaveUser).collect(Collectors.toList());
            List<UserInfoQtalk> delete = new ArrayList<>();
            List<UserInfoQtalk> add = new ArrayList<>();
            add.addAll(onJobUsers);
            users.removeAll(onJobUsers);
            delete = users;
            updateStructResultMode.setDelete(delete);
            updateStructResultMode.setUpdate(add);
            updateStructResultMode.setVersion(curVersion);
        } catch (Exception e) {
            LOGGER.error("get qtalk qunar struct fail", e);
            return JsonResultUtils.fail(500, "服务器内部错误");
        }
        return JsonResultUtils.success(updateStructResultMode);
    }

    public static boolean filterOutLeaveUser(UserInfoQtalk userInfoQtalk) {
        if (userInfoQtalk.getHire_flag() != 0) {
            return true;
        }
        return false;
    }
}
