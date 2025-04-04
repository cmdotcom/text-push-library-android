package com.cm.cmpush.helper

import org.json.JSONObject

internal inline fun <T1 : Any, T2 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    block: (T1, T2) -> R?
): R? {
    return if (p1 != null && p2 != null) block(p1, p2) else null
}

internal inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> safeLet(
    p1: T1?,
    p2: T2?,
    p3: T3?,
    block: (T1, T2, T3) -> R?
): R? {
    return if (p1 != null && p2 != null && p3 != null) block(p1, p2, p3) else null
}

/**
 * Calculate the total hashcode of the JSONObject
 * Only works when variables are [String] or [Integer]
 */
internal fun JSONObject.calculateHashCode(): Int {
    var hash = 0

    this.keys().forEach { key ->
        hash += get(key).hashCode()
    }

    return hash
}