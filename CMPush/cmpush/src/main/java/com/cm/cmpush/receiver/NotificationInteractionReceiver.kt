package com.cm.cmpush.receiver

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.cm.cmpush.CMPush
import com.cm.cmpush.objects.CMData
import com.cm.cmpush.objects.CMPushEvent
import com.cm.cmpush.objects.CMPushEventType
import com.cm.cmpush.objects.CMPushStatus
import org.json.JSONObject

class NotificationInteractionReceiver : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "NotificationInteractionReceiver onCreate()")

        super.onCreate(savedInstanceState)
        handleNotificationIntent(this, intent)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            handleNotificationIntent(this, intent)
        }
        finish()
    }

    private fun handleNotificationIntent(context: Context, intent: Intent) {
        val notificationId = intent.getIntExtra(CMPush.KEY_NOTIFICATION_ID, -1)


        if (notificationId == -1) {
            Log.e(CMPush.TAG, "Not a valid intent for statistics")
            return
        }

        val messageId = intent.getStringExtra(CMPush.KEY_MESSAGE_ID) ?: kotlin.run {
            Log.e(CMPush.TAG, "Missing messageId in intent!")
            return
        }

        Log.d(CMPush.TAG, "Handling intent with notificationId: $notificationId")

        // Remove notification
        NotificationManagerCompat.from(context).cancel(notificationId)

        val appIntent = CMPush.appIntent.clone() as Intent
        // Handle suggestion action
        intent.getStringExtra(CMPush.KEY_SUGGESTION)?.let { suggestionJSON ->
            val suggestion = CMData.Suggestion.fromJSONObject(JSONObject(suggestionJSON))

            val eventType: CMPushEventType = CMPushEventType.MessageOpened
            val reference: String = messageId
            when (suggestion.action) {
                CMData.Suggestion.Action.OpenUrl -> {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(suggestion.url)).apply {
                            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }

                CMData.Suggestion.Action.Reply -> {
                    context.startActivity(
                        appIntent.apply {
                            putExtra(CMPush.REPLY, suggestion.postbackdata)
                        }
                    )
                }

                CMData.Suggestion.Action.OpenAppPage -> {
                    context.startActivity(
                        appIntent.apply {
                            putExtra(CMPush.OPEN_APP_PAGE, suggestion.page)
                        }
                    )
                }

                CMData.Suggestion.Action.Unknown -> {
                    Log.e(CMPush.TAG, "Unknown action for suggestion..")
                }
            }

            CMPush.reportStatus(
                context,
                CMPushStatus(
                    CMPushEvent(
                        eventType,
                        reference,
                        hashMapOf<String, String>().apply {
                            suggestion.label.takeIf { it.isNotEmpty() }?.let { put("label", it) }
                            suggestion.postbackdata.takeIf { it.isNotEmpty() }?.let { put("postbackdata", it) }
                            suggestion.url.takeIf { it.isNotEmpty() }?.let { put("url", it) }
                            suggestion.page.takeIf { it.isNotEmpty() }?.let { put("page", it) }
                        }
                    )
                ),
                null
            )
            // Handle default action if defined
        } ?: intent.getStringExtra(CMPush.KEY_DEFAULT_ACTION)?.let { defaultActionJSON ->
            val defaultAction = CMData.DefaultAction.fromJSONObject(JSONObject(defaultActionJSON))

            val eventType: CMPushEventType = CMPushEventType.MessageOpened
            val reference: String = messageId
            when (defaultAction.action) {
                CMData.DefaultAction.Action.OpenUrl -> {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(defaultAction.url)).apply {
                            this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }

                CMData.DefaultAction.Action.Reply -> {
                    context.startActivity(
                        appIntent.apply {
                            putExtra(CMPush.REPLY, defaultAction.postbackdata)
                        }
                    )
                }

                CMData.DefaultAction.Action.OpenAppPage -> {
                    context.startActivity(
                        appIntent.apply {
                            putExtra(CMPush.OPEN_APP_PAGE, defaultAction.page)
                        }
                    )
                }

                CMData.DefaultAction.Action.Unknown -> {
                    Log.e(CMPush.TAG, "Unknown action for default action..")
                }
            }

            CMPush.reportStatus(
                context,
                CMPushStatus(
                    CMPushEvent(
                        eventType,
                        reference,
                        hashMapOf<String, String>().apply {
                            put("label", "defaultAction")
                            defaultAction.postbackdata.takeIf { it.isNotEmpty() }?.let { put("postbackdata", it) }
                            defaultAction.url.takeIf { it.isNotEmpty() }?.let { put("url", it) }
                            defaultAction.page.takeIf { it.isNotEmpty() }?.let { put("page", it) }
                        }
                    )
                ),
                null
            )
            // If the default action was not set
        } ?: run {
            // Just an Open intent
            context.startActivity(
                appIntent
            )
            CMPush.reportStatus(
                context,
                CMPushStatus(CMPushEvent(CMPushEventType.MessageOpened, messageId)),
                null
            )
        }
    }

    companion object {
        const val TAG = "CMPushLibrary-NotificationInteractionReceiver"
    }
}