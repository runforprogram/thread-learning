package com.example.thread_hook_flipper

import android.util.Log
import com.example.thread_hook.ThreadCreateMonitor
import com.facebook.flipper.core.FlipperConnection
import com.facebook.flipper.core.FlipperObject
import com.facebook.flipper.core.FlipperPlugin
import org.json.JSONObject
import java.lang.reflect.Field


//继承BufferingFlipperPlugin，貌似有些问题，暂时不管，先用FlipperPlugin
//class ThreadWatcherPlugin : BufferingFlipperPlugin() {
class ThreadWatcherPlugin : FlipperPlugin {

    private val TAG = "ThreadWatcherPlugin"
    private var mConnection: FlipperConnection? = null

    override fun getId(): String {
//        return "threadwatcherplugin"
        return "my_thread_plugin"
    }

    override fun onConnect(connection: FlipperConnection?) {
        Log.d(TAG, "onConnect: $connection")
        mConnection = connection
        getAllThread()
    }

    override fun onDisconnect() {
        Log.d(TAG, "onDisconnect: ")
        mConnection = null
    }

    override fun runInBackground(): Boolean {
        Log.d(TAG, "runInBackground: ")
        return true
    }

    //onConnect之前的线程信息，也要上报一下
    fun getAllThread() {
        Log.d(TAG, "getAllThread: ")
        Thread.getAllStackTraces().keys.forEach {
            ThreadCreateMonitor.getThreadInfo(
                ThreadCreateMonitor.getNativeTid(it)
            ).apply {
                if (this.isNotEmpty()) {
                    addThreadInfo(this)
                }
            }
        }
    }
    private fun getNextThreadId(): Long {
        return try {
            val threadSeqNumber: Field = Thread::class.java.getDeclaredField("threadSeqNumber")
            threadSeqNumber.isAccessible = true
            threadSeqNumber.get(null) as Long
        } catch (e: NoSuchFieldException) {
            throw e
        } catch (e: IllegalAccessException) {
            throw e
        }
    }
    //java层最新创建的线程的id最大
    private fun getLatestThread(): Thread?{
        val map = Thread.getAllStackTraces()
        val set: Set<Thread> = map.keys
        val copySet: Set<Thread> = HashSet(set)
        val nextId = getNextThreadId()
        val lastThread = copySet.maxBy { it.id }
        Log.d(TAG,"nextId=$nextId,lastThread.id=${lastThread.id}")
        if (nextId==lastThread.id){
            return lastThread
        }
        return null
    }
    //通过java的Thread对象的nativePeer获取到native的tid和native传上来的tid相等找到java层的thread对象
    private fun getThreadByPeer(tid:Int):Thread?{
        return Thread.getAllStackTraces().keys.firstOrNull {
            tid == ThreadCreateMonitor.getNativeTid(it)
        }
    }
    //获取java层的线程名,没有native 线程名最大16个字符的限制
    //如果线程创建后,就销毁了,Thread.getAllStackTraces()获取不到此线程了.
    private fun getJavaThreadName(tid:Int){
        val threadPeer = getThreadByPeer(tid)
        Log.d(TAG, "from javaTid==NativeTid thread name = ${threadPeer?.name},thread id =${threadPeer?.id} ")
        val thread = getLatestThread()
        Log.d(TAG,"from by max id java thread name= ${thread?.name}")
    }
    //上报线程创建信息
    fun addThreadInfo(resultJson: String) {
        val json = JSONObject(resultJson)
        //先用Java线程名，拿不到的话再用native层的线程名兜底,根据tid进行获取
        var threadName = ""
        val tid = json.optInt("tid")
        val thread = getThreadByPeer(tid)
        Log.d(TAG, "java thread name =${getJavaThreadName(tid)}")
        threadName = json.optString("name")
        Log.d(TAG, "native thread name =$threadName")

        val priority = thread?.priority?.toString() ?: "None"
        val threadGroup = thread?.threadGroup?.name ?: "None"

        FlipperObject.Builder()
            .put("id","newThread")
            .put("tid", tid)
            .put("name", threadName)
            .put("threadGroup", threadGroup)
            .put("priority", priority)
            .put("createCallStack", json.opt("createCallStack"))
            .build()
            .apply {
                Log.d(TAG,"send message to .....")
                mConnection?.send("newThread", this)
            }
    }

    //上报线程数量
    fun reportThreadCount() {

    }

}