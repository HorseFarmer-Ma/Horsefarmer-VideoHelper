package com.meizu.testdevVideo.util.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * 适用于Android 3.x,4.x修改静态ip
 * Created by maxueming on 2017/1/5.
 */
public class DnsSettingLollipopDown {

    private Context mContext;

    public DnsSettingLollipopDown(Context context){
        mContext = context;
    }


    /**
     * 设置静态ip地址的方法
     */
    public boolean setIpWithStaticIp(boolean isEnableStaticIp, String ipAddress, String gateway, String mask, String dns1, String dns2) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiConfiguration wifiConfig = null;
        WifiInfo connectionInfo = wifiManager.getConnectionInfo(); //得到连接的wifi网络

        List<WifiConfiguration> configuredNetworks = wifiManager
                .getConfiguredNetworks();
        for (WifiConfiguration conf : configuredNetworks) {
            if (conf.networkId == connectionInfo.getNetworkId()) {
                wifiConfig = conf;
                break;
            }
        }

        if (android.os.Build.VERSION.SDK_INT < 11) { // 如果是android2.x版本的话
            ContentResolver ctRes = mContext.getContentResolver();
            if(isEnableStaticIp){
                android.provider.Settings.System.putInt(ctRes, android.provider.Settings.System.WIFI_USE_STATIC_IP, 1);
            }else{
                android.provider.Settings.System.putInt(ctRes, android.provider.Settings.System.WIFI_USE_STATIC_IP, 0);
            }

            android.provider.Settings.System.putString(ctRes, android.provider.Settings.System.WIFI_STATIC_IP, ipAddress);

            if(null != mask){
                android.provider.Settings.System.putString(ctRes, android.provider.Settings.System.WIFI_STATIC_NETMASK, mask);
            }

            android.provider.Settings.System.putString(ctRes, android.provider.Settings.System.WIFI_STATIC_GATEWAY, gateway);
            android.provider.Settings.System.putString(ctRes, android.provider.Settings.System.WIFI_STATIC_DNS1, dns1);
            if(null != dns2){
                android.provider.Settings.System.putString(ctRes, android.provider.Settings.System.WIFI_STATIC_DNS2, dns2);
            }
            return true;
        } else {
            // 如果是android3.x版本及以上的话
            try {
                if(isEnableStaticIp){
                    setIpAssignment("STATIC", wifiConfig);
                    setIpAddress(InetAddress.getByName(ipAddress), 24, wifiConfig);
                    setGateway(InetAddress.getByName(gateway), wifiConfig);
                    if(null != dns2){
                        setDNS(InetAddress.getByName(dns1), InetAddress.getByName(dns2), wifiConfig);
                    } else{
                        setDNS(InetAddress.getByName(dns1), null, wifiConfig);
                    }
                }else{
                    setIpAssignment("DHCP", wifiConfig);
                }
                wifiManager.updateNetwork(wifiConfig);
                wifiManager.saveConfiguration();
                System.out.println("静态ip设置成功！");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("静态ip设置失败！");
                return false;
            }
        }
    }


    /**
     * android里设置静态Ip在android6.0的系统中出现NoSuchFieldExecption:ipAssignment的异常。
     * 原来在5.0+的版本后ipAssignment属性设置改成setIpAssignment的方法调用。
     * @param assign
     * @param wifiConf
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static void setIpAssignment(String assign, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        if (Build.VERSION.SDK_INT >= 21) {
            Object ipConfiguration = wifiConf.getClass()
                    .getMethod("getIpConfiguration").invoke(wifiConf);
            setEnumField(ipConfiguration, assign, "ipAssignment");
        } else {
            setEnumField(wifiConf, assign, "ipAssignment");
        }
    }


    /**
     * @param addr
     * @param prefixLength
     * @param wifiConf
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    @SuppressWarnings("unchecked")
    public static void setIpAddress(InetAddress addr, int prefixLength,
                                    WifiConfiguration wifiConf) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, NoSuchMethodException,
            ClassNotFoundException, InstantiationException,
            InvocationTargetException {
        Object linkProperties = getField(wifiConf, "linkProperties");
        if (linkProperties == null){
            return;
        }
        Class laClass = Class.forName("android.net.LinkAddress");
        Constructor laConstructor = laClass.getConstructor(new Class[] {
                InetAddress.class, int.class });
        Object linkAddress = laConstructor.newInstance(addr, prefixLength);

        ArrayList mLinkAddresses = (ArrayList) getDeclaredField(linkProperties, "mLinkAddresses");
        mLinkAddresses.clear();
        mLinkAddresses.add(linkAddress);
    }

    private static Object getField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        Object out = f.get(obj);
        return out;
    }

    private static Object getDeclaredField(Object obj, String name)
            throws SecurityException, NoSuchFieldException,IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        Object out = f.get(obj);
        return out;
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    public static void setEnumField(Object obj, String value, String name)
            throws SecurityException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Field f = obj.getClass().getField(name);
        f.set(obj, Enum.valueOf((Class) f.getType(), value));
    }

    /**
     * @param gateway
     * @param wifiConf
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static void setGateway(InetAddress gateway, WifiConfiguration wifiConf) throws SecurityException,
            IllegalArgumentException, NoSuchFieldException,
            IllegalAccessException, ClassNotFoundException,
            NoSuchMethodException, InstantiationException,
            InvocationTargetException {

        Object linkProperties = getField(wifiConf, "linkProperties");
        if(linkProperties == null)return;

        if(Build.VERSION.SDK_INT < 11){
            ArrayList mGateways = (ArrayList)getDeclaredField(linkProperties, "mGateways");
            mGateways.clear();
            mGateways.add(gateway);
        }else{
            Class routeInfoClass = Class.forName("android.net.RouteInfo");
            Constructor routeInfoConstructor = routeInfoClass
                    .getConstructor(new Class[] { InetAddress.class });
            Object routeInfo = routeInfoConstructor.newInstance(gateway);

            ArrayList mRoutes = (ArrayList) getDeclaredField(linkProperties,
                    "mRoutes");
            mRoutes.clear();
            mRoutes.add(routeInfo);
        }

    }

    /**
     * @param dns1
     * @param wifiConf
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    public static void setDNS(InetAddress dns1, InetAddress dns2, WifiConfiguration wifiConf)
            throws SecurityException, IllegalArgumentException,
            NoSuchFieldException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        Object linkProperties = null;
        ArrayList mDnses = null;
        if (Build.VERSION.SDK_INT >= 21) {
            Object staticIpConf = wifiConf.getClass()
                    .getMethod("getStaticIpConfiguration").invoke(wifiConf);
            mDnses = (ArrayList) getDeclaredField(staticIpConf,
                    "dnsServers");
        } else {
            linkProperties = getField(wifiConf, "linkProperties");
            mDnses = (ArrayList) getDeclaredField(linkProperties,
                    "mDnses");
        }
        if (linkProperties == null)
            return;
        mDnses.clear(); // or add a new dns address , here I just want to
        // replace DNS1
        mDnses.add(dns1);
        if(null != dns2){
            mDnses.add(dns2);
        }
    }


}
