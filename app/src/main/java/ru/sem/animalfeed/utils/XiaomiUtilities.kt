package ru.sem.animalfeed.utils

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.text.TextUtils
import android.util.Log


object XiaomiUtilities {

    const val OP_ACCESS_XIAOMI_ACCOUNT = 10015
    const val OP_AUTO_START = 10008
    const val OP_BACKGROUND_START_ACTIVITY = 10021
    const val OP_BLUETOOTH_CHANGE = 10002
    const val OP_BOOT_COMPLETED = 10007
    const val OP_DATA_CONNECT_CHANGE = 10003
    const val OP_DELETE_CALL_LOG = 10013
    const val OP_DELETE_CONTACTS = 10012
    const val OP_DELETE_MMS = 10011
    const val OP_DELETE_SMS = 10010
    const val OP_EXACT_ALARM = 10014
    const val OP_GET_INSTALLED_APPS = 10022
    const val OP_GET_TASKS = 10019
    const  val OP_INSTALL_SHORTCUT = 10017
    const val OP_NFC = 10016
    const val OP_NFC_CHANGE = 10009
    const val OP_READ_MMS = 10005
    const val OP_READ_NOTIFICATION_SMS = 10018
    const val OP_SEND_MMS = 10004
    const val OP_SERVICE_FOREGROUND = 10023
    const val OP_SHOW_WHEN_LOCKED = 10020
    const val OP_WIFI_CHANGE = 10001
    const val OP_WRITE_MMS = 10006
    const val TAG = "XiaomiUtilities"

    fun isMIUI(): Boolean {
        return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))
    }

    @TargetApi(19)
    fun isCustomPermissionGranted(permission: Int, applicationContext: Context): Boolean {
        try {
            val mgr = applicationContext.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val m = AppOpsManager::class.java.getMethod(
                "checkOpNoThrow",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                String::class.java
            )
            val result = m.invoke(mgr, permission, Process.myUid(), applicationContext.packageName) as Int
            return result == AppOpsManager.MODE_ALLOWED
        } catch (x: Exception) {
            Log.e(TAG, "isCustomPermissionGranted: ", x)
        }
        return true
    }

    fun getMIUIMajorVersion(): Int {
        val prop = getSystemProperty("ro.miui.ui.version.name")
        if (prop != null) {
            try {
                return prop.replace("V", "").toInt()
            } catch (ignore: NumberFormatException) {
            }
        }
        return -1
    }

    fun getPermissionManagerIntent(applicationContext: Context): Intent {
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.putExtra("extra_package_uid", Process.myUid())
        intent.putExtra("extra_pkgname", applicationContext.packageName)
        return intent
    }

    private fun getSystemProperty(key: String): String? {
        try {
            val props = Class.forName("android.os.SystemProperties")
            return props.getMethod("get", String::class.java).invoke(null, key) as String
        } catch (ignore: java.lang.Exception) {
        }
        return null
    }
}