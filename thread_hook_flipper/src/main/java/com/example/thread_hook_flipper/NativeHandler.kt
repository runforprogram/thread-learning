package com.example.thread_hook_flipper

import android.util.Log
import androidx.annotation.Keep
import com.facebook.flipper.android.AndroidFlipperClient
import org.json.JSONObject

@Keep
object NativeHandler {

    private val TAG = "NativeHandler"

    @JvmStatic
    fun nativeReport(resultJson: String) {
        Log.d(TAG, "nativeReport,  resultJson:$resultJson")
        val json = JSONObject(resultJson)
        val callStack = json.opt("createCallStack")
        if (callStack != null) {
            val callStackStr = callStack.toString()
            var indexJava =callStackStr.indexOf("java.lang.Thread.nativeCreate")
            var packageInfo =""
            if (indexJava!=-1){
                val javaStack = callStackStr.substring(indexJava,callStackStr.length)
                indexJava = javaStack.indexOf("com")
                if (indexJava!=-1){
                    packageInfo = javaStack.substring(indexJava,javaStack.length)
                    Log.d(TAG, "nativeReport,  call package:$packageInfo")
                }else{
                    Log.d(TAG, "nativeReport,  call stack:$javaStack")
                }
            }else{
                Log.d(TAG,"not get call package stack=$callStack")
            }
        }
        if (ThreadWatcherInstaller.sContext != null) {
            val client = AndroidFlipperClient.getInstance(ThreadWatcherInstaller.sContext)
            if (client != null) {
                val plugin = client.getPluginByClass(ThreadWatcherPlugin::class.java)
                plugin?.addThreadInfo(resultJson)
            }
        }
    }
}