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
        if(Objects.equals(sex, "Male")){
            return "anh";
        } else {
            return "chị";
        }
    }

    public String getPortraitId() {
        return portraitId;
    }
//
//    public int getImageBg(){
//        if(idImage == 0){
//            return R.id.imageBg;
//        }
//
//        if(idImage == 1){
//            return R.id.imageBg;
//        }
//
//        if(idImage == 2){
//            return R.id.imageBg;
//        }
//
//        return R.id.imageBg;
//    }
}