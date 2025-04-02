package com.cm.cmpush.objects

import com.cm.cmpush.helper.JSONHelper
import com.cm.cmpush.helper.JSONHelper.getStringOrNull

sealed class CMPushError(val message: String) {
    data object NotInitialized :
        CMPushError("CMPush SDK is not initialized yet! Please initialize the SDK using CMPush.initialize()")

    data object NotRegistered : CMPushError("Device is not registered yet!")
    data object NoMSISDN : CMPushError("No MSISDN found!")
    data object Offline : CMPushError("Device is offline!")
    class ServerError(val statusCode: Int?, message: String) : CMPushError(
        message = JSONHelper.tryStringToJSONArray(message)?.optJSONObject(0)
            ?.getStringOrNull("messageDetails")
            ?: JSONHelper.tryStringToJSONObject(message)?.getStringOrNull("messageDetails")
            ?: message
    )
}