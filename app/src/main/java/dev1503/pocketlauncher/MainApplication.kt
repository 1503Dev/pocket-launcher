package dev1503.pocketlauncher

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class MainApplication: Application() {
    val TAG = "MainApplication"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        val globalDebugWindow = GlobalDebugWindow(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(p0: Activity, p1: Bundle?) {
                Log.d(TAG, "onActivityCreated($p0, $p1)")
                globalDebugWindow.updateActivity(p0)
            }

            override fun onActivityDestroyed(p0: Activity) {
                Log.d(TAG, "onActivityDestroyed($p0)")
                globalDebugWindow.onActivityDestroyed(p0)
            }

            override fun onActivityPaused(p0: Activity) {
                Log.d(TAG, "onActivityPaused($p0)")
            }

            override fun onActivityResumed(p0: Activity) {
                Log.d(TAG, "onActivityResumed($p0)")
            }

            override fun onActivitySaveInstanceState(
                p0: Activity,
                p1: Bundle
            ) {
                Log.d(TAG, "onActivitySaveInstanceState($p0, $p1)")
            }

            override fun onActivityStarted(p0: Activity) {
                Log.d(TAG, "onActivityStarted($p0)")
            }

            override fun onActivityStopped(p0: Activity) {
                Log.d(TAG, "onActivityStopped($p0)")
            }
        })
        registerComponentCallbacks(object: ComponentCallbacks {
            override fun onConfigurationChanged(p0: Configuration) {
                Log.d(TAG, "onConfigurationChanged($p0)")
            }

            override fun onLowMemory() {
                Log.w(TAG, "onLowMemory()")
            }
        })
//        Log.d("LogTest", "Debug")
//        Log.i("LogTest", "Info")
//        Log.e("LogTest", "Error", Exception("Test Error"))
//        Log.w("LogTest", "Warning", Exception("Test Warning"))
//        Log.v("LogTest", "Verbose")
//        Log.wtf("LogTest", "WTF", Exception("Test WTF"))
//        Log.d("LogTest", "Debug multiple lines\nLine 2\nLine 3")
    }
}