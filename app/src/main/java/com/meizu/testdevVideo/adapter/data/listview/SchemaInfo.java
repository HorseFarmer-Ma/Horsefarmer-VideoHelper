package com.meizu.testdevVideo.adapter.data.listview;

/**
 * Schema值存储
 * Created by maxueming on 2017/7/3.
 */

public class SchemaInfo {
    private String description;
    private int jumpType;
    private String address;

    @Override
    public String toString() {
        return "SchemaInfo{" +
                "description='" + description + '\'' +
                ", jumpType=" + jumpType +
                ", address='" + address + '\'' +
                '}';
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getJumpType() {
        return jumpType;
    }

    public void setJumpType(int jumpType) {
        this.jumpType = jumpType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
