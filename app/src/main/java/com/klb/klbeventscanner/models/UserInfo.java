package com.klb.klbeventscanner.models;

import java.util.Objects;

public class UserInfo {
    String name;
    String position;
    String branch;
    String sex;
    String portraitId;

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
        if(Objects.equals(sex, "Male")){
            return "anh";
        } else {
            return "chị";
        }
    }

    public String getPortraitId() {
        return portraitId;
    }
}