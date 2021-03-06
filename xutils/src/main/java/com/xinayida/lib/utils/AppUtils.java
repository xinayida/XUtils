package com.xinayida.lib.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 应用工具类
 * Created by stephan on 16/7/9.
 */
public class AppUtils {
    private static String imei = null;
    private static String imsi = null;
    private static String wifiMac = null;
    private static String deviceId = null;

    public static String getIMSI(Context context) {

        if (imsi == null) {
            try {
                TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                imsi = mTelephonyMgr.getSubscriberId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imsi;
    }

    public static String getIMEI(Context context) {

        if (imei == null) {
            try {
                TelephonyManager mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                imei = mTelephonyMgr.getDeviceId();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imei;
    }

    public static String getWIFIMac(Context context) {
        if (wifiMac != null) {
            return wifiMac;
        }
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        String wifiMac = null;
        if (wm != null) {
            WifiInfo info = wm.getConnectionInfo();
            if (info != null) {
                wifiMac = info.getMacAddress();
            }
            if (wifiMac != null) {
                wifiMac = wifiMac.replace(":", "");
            }
        }
        return wifiMac;
    }

    public static String getAndroidId(Context context) {
        String aId = null;
        try {
            aId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
        }
        return aId;
    }

    public static String getDeviceID(Context context) {
        if (isEmualtor(context)/*|| checkIsSimulator()*/) {
            return "EMU";
        }
        if (deviceId != null) {
            return deviceId;
        }
        StringBuilder sb = new StringBuilder();
        /* SERIAL */
        String serial = null;
        try {
            Class buildClass = Class.forName("android.os.Build");
            Field serialField = buildClass.getDeclaredField("SERIAL");
            if (serialField != null) {
                serial = (String) serialField.get(null);
            }
        } catch (Exception e) {
            serial = null;
        }

        /* IMEI/MEID */
        String imei = getIMEI(context);

        /* ANDROID_ID */
        String aId = getAndroidId(context);

        /* WIFI MAC */
        String wifiMac = getWIFIMac(context);

        // if ("000000000000000".equals(imei))
        // {// 过滤模拟器
        // return imei;
        // }

        if (serial != null) {
            sb.append(serial);
        }
        if (imei != null) {
            sb.append(imei);
        }
        if (aId != null) {
            sb.append(aId);
        }
        if (wifiMac != null) {
            sb.append(wifiMac);
        }

        // String mostSigBits = aId + wifiMac;
        // String leastSigBits = imei + serial;
        // UUID deviceUUid = new UUID(mostSigBits.hashCode(), leastSigBits.hashCode());
        deviceId = AuthenUtil.encodeByMD5(sb.toString());
        return deviceId;
    }

    /**
     * 判断是否是模拟器
     *
     * @return
     * @see [类、类#方法、类#成员]
     */
    private static boolean isEmualtor(Context context) {
        String BOOTLOADER = Build.BOOTLOADER;
        String BOARD = Build.BOARD;
        String BRAND = Build.BRAND;
        if (BOARD == "unknown" || BOOTLOADER == "unknown" || BRAND == "generic") {
            String imei = getIMEI(context);
            if ("000000000000000".equals(imei)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取App安装包信息
     *
     * @return
     */
    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return info;
    }

    public static String getCacheDir(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            File cacheFile = context.getExternalCacheDir();
            if (null != cacheFile) {
                return cacheFile.getPath();
            }
        }
        return context.getCacheDir().getPath();
    }

    /**
     * 判断是否是主进程
     *
     * @return
     */
    public static boolean isMainProcess(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本号]
     *
     * @param context
     * @return 当前应用的版本号
     */
    public static int getVersionCode(Context context) {
        if (context != null) {
            try {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionCode;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * 获得当前进程名称
     *
     * @param paramContext
     * @return
     */
    public static String getCurProcessName(Context paramContext) {
        int pid = Process.myPid();
        Iterator localIterator = ((ActivityManager) paramContext.getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses().iterator();
        while (localIterator.hasNext()) {
            ActivityManager.RunningAppProcessInfo localRunningAppProcessInfo = (ActivityManager.RunningAppProcessInfo) localIterator.next();
            if (localRunningAppProcessInfo.pid == pid) {
                return localRunningAppProcessInfo.processName;
            }
        }
        return null;
    }


    /**
     * 手机号验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][0-9]{10}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /**
     * 系统默认分享
     *
     * @param context
     * @param activityTitle
     * @param msgTitle
     * @param msgText
     * @param imgPath
     */
    public static void shareMsg(Context context, String activityTitle, String msgTitle, String msgText,
                                String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (imgPath == null || imgPath.equals("")) {
            intent.setType("text/plain"); // 纯文本
        } else {
            File f = new File(imgPath);
            if (f != null && f.exists() && f.isFile()) {
                intent.setType("image/png");
                Uri u = Uri.fromFile(f);
                intent.putExtra(Intent.EXTRA_STREAM, u);
            }
        }
        intent.putExtra(Intent.EXTRA_SUBJECT, msgTitle);
        intent.putExtra(Intent.EXTRA_TEXT, msgText);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(Intent.createChooser(intent, activityTitle));
    }

    public static void detailSetting(Activity context) {
        Uri packageURI = Uri.parse("package:" + context.getPackageName());
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
