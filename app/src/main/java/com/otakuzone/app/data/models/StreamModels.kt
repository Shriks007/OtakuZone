package com.otakuzone.app.data.models

import com.google.gson.annotations.SerializedName

data class AniKageStreamResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("servers") val servers: List<StreamServer>?
)

data class StreamServer(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String, // "sub" or "dub"
    @SerializedName("format") val format: String, // "v-sub", "h-sub", "s-sub", "e-sub", "dub", "e-dub", "v-dub"
    @SerializedName("url") val url: String,
    @SerializedName("default") val isDefault: Boolean = false,
    var isFailed: Boolean = false
) {
    val isZoko: Boolean
        get() = name.equals("Zoko", ignoreCase = true) || format.contains("v-")

    val isSub: Boolean
        get() = type.equals("sub", ignoreCase = true) || format.contains("sub", ignoreCase = true)

    val isDub: Boolean
        get() = type.equals("dub", ignoreCase = true) || format.contains("dub", ignoreCase = true)
}

enum class AudioMode {
    SUB,
    DUB
}
