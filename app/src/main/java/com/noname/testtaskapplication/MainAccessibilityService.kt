package com.noname.testtaskapplication

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MainAccessibilityService : AccessibilityService() {
    companion object{
        const val TAG = "AccessibilityService"
    }
    override fun onInterrupt() {}

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        event.source?.apply {
            Log.d(TAG,"new event received")
            when(event.eventType){
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED->{
                    if(event.className=="com.whatsapp.Conversation"){
                        var nodesList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.whatsapp:id/message_text")
                        nodesList.forEach {
                            val messageText = it.text
                            val time = it.parent.getChild(1).getChild(0).text
                            Log.d(TAG,"$time: $messageText")
                        }
                    }
                }
            }
            // Use the event and node information to determine
            // what action to take

            // take action on behalf of the user
            performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)

            // recycle the nodeInfo object
            recycle()
        }
    }
}