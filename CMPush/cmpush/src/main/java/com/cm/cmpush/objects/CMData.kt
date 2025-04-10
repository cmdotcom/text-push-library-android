package com.cm.cmpush.objects

import com.cm.cmpush.helper.JSONHelper.getStringOrNull
import org.json.JSONObject


fun JSONObject.optStringCaseInsensitive(key: String): String {
    // Find the actual key matching the input key case-insensitively
    val actualKey = keys().asSequence().firstOrNull { it.equals(key, ignoreCase = true) }
    return actualKey?.let { optString(it) } ?: ""
}

internal class CMData(
    val title: String?,
    val body: String,
    val messageId: String,
    val media: Media?,
    val suggestions: Array<Suggestion>,
    val defaultAction: DefaultAction?,
) {
    class Media(
        val mediaName: String,
        val mediaUri: String,
        val mimeType: String
    )

    class Suggestion(
        val action: Action,
        val label: String,
        val url: String,
        val page: String,
        val postbackdata: String
    ) {
        fun toJSONObject(): JSONObject = JSONObject().apply {
            put("action", action.name)
            put("label", label)
            put("url", url)
            put("page", page)
            put("postbackdata", postbackdata)
        }

        companion object {
            fun fromJSONObject(jsonObject: JSONObject) = Suggestion(
                action = Action.translateValue(jsonObject.optString("action")),
                label = jsonObject.optString("label"),
                url = jsonObject.optString("url"),
                page = jsonObject.optString("page"),
                postbackdata = jsonObject.optStringCaseInsensitive("postbackdata")
            )
        }

        enum class Action {
            OpenUrl, Reply, OpenAppPage, Unknown;

            companion object {
                fun translateValue(value: String): Action {
                    return try {
                        valueOf(value)
                    } catch (e: IllegalArgumentException) {
                        Unknown
                    }
                }
            }
        }
    }

    class DefaultAction(
        val action: Action,
        val url: String,
        val page: String,
        val postbackdata: String
    ) {
        fun toJSONObject(): JSONObject = JSONObject().apply {
            put("action", action.name)
            put("url", url)
            put("page", page)
            put("postbackdata", postbackdata)
        }

        companion object {
            fun fromJSONObject(jsonObject: JSONObject) = DefaultAction(
                action = Action.translateValue(jsonObject.optString("action")),
                url = jsonObject.optString("url"),
                page = jsonObject.optString("page"),
                postbackdata = jsonObject.optStringCaseInsensitive("postbackdata")
            )
        }

        enum class Action {
            OpenUrl, Reply, OpenAppPage, Unknown;

            companion object {
                fun translateValue(value: String): Action {
                    return try {
                        valueOf(value)
                    } catch (e: IllegalArgumentException) {
                        Unknown
                    }
                }
            }
        }
    }

    companion object {
        fun fromJSONObject(jsonObject: JSONObject?): CMData? {
            if (jsonObject == null) return null

            return CMData(
                title = jsonObject.getStringOrNull("title"),
                body = jsonObject.optString("body"),
                messageId = jsonObject.getStringOrNull("messageId") ?: return null,
                media = jsonObject.optJSONObject("media")?.let {
                    Media(
                        mediaName = it.optString("mediaName"),
                        mediaUri = it.optString("mediaUri"),
                        mimeType = it.optString("mimeType"),
                    )
                },
                suggestions = jsonObject.optJSONArray("suggestions")?.let { suggestions ->
                    Array(suggestions.length()) { i ->
                        suggestions.getJSONObject(i).let { suggestion ->
                            Suggestion.fromJSONObject(suggestion)
                        }
                    }
                } ?: arrayOf(),
                defaultAction = jsonObject.optJSONObject("defaultAction")?.let {
                    DefaultAction.fromJSONObject(it)
                }
            )
        }
    }
}