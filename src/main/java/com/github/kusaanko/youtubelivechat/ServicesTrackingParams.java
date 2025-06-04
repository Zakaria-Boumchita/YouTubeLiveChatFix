package com.github.kusaanko.youtubelivechat;

import java.util.ArrayList;
import java.util.Objects;

public class ServicesTrackingParams {

    private ArrayList<ServiceParam> serviceParams;

    public ArrayList<ServiceParam> getServiceParams() {
        return serviceParams;
    }

    public void setServiceParams(ArrayList<ServiceParam> serviceParams) {
        this.serviceParams = serviceParams;
    }

    public boolean isOnline() {
        Objects.requireNonNull(serviceParams);
        for (ServiceParam serviceParam : serviceParams) {
            if(serviceParam.getService().equals("GFEEDBACK")) {
                for (Param param : serviceParam.getParamList()) {
                    if(param.getKey().equals("is_viewed_live")) {
                        return Boolean.parseBoolean(param.getValue());
                    }
                }
            }
        }
        throw new IllegalStateException("possible api changed, but service GFEEDBACK or Param `is_viewed_live` not found");
    }
}
