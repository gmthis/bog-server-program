package cn.xd.server.bog.entity.string

import cn.xd.server.bog.util.DateSerializable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Serializable
data class StringSend(
    val id: Int,
    val res: Int,
    @Serializable(with = DateSerializable::class) val root: Long,
    val time: Long,
    val forum: Int,
    val name: String,
    val emoji: String,
    val cookie: String,
    val admin: Boolean?,
    val title: String,
    val content: String,
    val lock: Boolean?,
    val images: List<Image>?,
    val replyCount: Int,
    val hideCount: Int,
    @Transient val isDel: Boolean = false,
    @Transient val isNotUpdateRoot: Boolean = false,
    val reply: MutableList<Reply> = ArrayList(),
){
    constructor(
        id: Int,
        res: Int,
        root: Long,
        time: Long,
        forum: Int,
        name: String,
        emoji: String,
        cookie: String,
        admin: Boolean?,
        title: String,
        content: String,
        lock: Boolean?,
        images: List<Image>?,
        replyCount: Int,
        hideCount: Int,
        isDel: Boolean,
        isNotUpdateRoot: Boolean,
    ): this(
        id = id,
        res = res,
        root = root,
        time = time,
        forum = forum,
        name = name,
        emoji = emoji,
        cookie = cookie,
        admin = admin,
        title = title,
        content = content,
        lock = lock,
        images = images,
        replyCount = replyCount,
        hideCount = hideCount,
        isDel = isDel,
        isNotUpdateRoot = isNotUpdateRoot,
        reply = ArrayList()
    )
}

@Serializable
data class Reply(
    val id: Int,
    val res: Int,
    val time: Long,
    val name: String,
    val emoji: String,
    val cookie: String,
    val admin: Boolean?,
    val content: String,
    val images: List<Image>?,
)

@Serializable
data class SingleContent(
    val id: Int,
    val res: Int,
    val time: Long,
    val forum: Int,
    val name: String,
    val emoji: String,
    val cookie: String,
    val admin: Boolean?,
    val title: String,
    val content: String,
    val lock: Boolean?,
    val images: List<Image>?
)

@Serializable
data class Image(
    val url: String,
    val ext: String
): java.io.Serializable