package com.github.kusaanko.youtubelivechat;

import java.util.List;

public class ServiceParam {
    private String service;
    private List<Param> paramList;

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public List<Param> getParamList() {
        return paramList;
    }

    public void setParamList(List<Param> paramList) {
        this.paramList = paramList;
    }
}
