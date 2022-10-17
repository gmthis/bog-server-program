package cn.xd.server.bog.entity.forum

import cn.xd.server.bog.annotation.NoArg
import kotlinx.serialization.Serializable

@Serializable
@NoArg
data class ForumDB(
    val id: Int,
    val rank: Int,
    val name: String,
    val name2: String,
    val headimg: String,
    val info: String,
    val hide: Boolean,
    val timeline: Boolean,
    val close: Boolean
)
