package com.sharukhhasan.androidbluetoothscanner.util;

import java.util.UUID;

/**
 * Created by sharukhhasan on 6/1/16.
 */
public class DeviceItem {
    private String deviceName;
    private String nnname;
    private String address;
    private UUID uuid;
    private boolean connected;

    public String getDeviceName()
    {
        return deviceName;
    }

    public boolean getConnected()
    {
        return connected;
    }

    public String getAddress()
    {
        return address;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public DeviceItem(String name, String address, String connected)
    {
        this.deviceName = name;
        this.address = address;

        if(connected == "true")
        {
            this.connected = true;
        }
        else
        {
            this.connected = false;
        }
    }
}
