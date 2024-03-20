package com.klb.klbeventscanner.models;

import com.klb.klbeventscanner.R;

import java.util.Objects;

public class UserInfo {
    String name;
    String position;
    String branch;
    String sex;
    String portraitId;
    boolean isManager;

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public String getBranch() {
        return branch;
    }

    public String getGender() {
        if (Objects.equals(sex, "Male")) {
            return "Anh";
        } else {
            return "Chị";
        }
    }

    public String getPortraitId() {
        return portraitId;
    }

    public boolean isManager() {
        return isManager;
    }
}