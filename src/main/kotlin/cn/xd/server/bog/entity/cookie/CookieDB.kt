package cn.xd.server.bog.entity.cookie

import cn.xd.server.bog.annotation.NoArg
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@NoArg
data class CookieDB(
    val cookie: String,
    val token: String,
    val admin: Boolean = false,
    val emoji: String? = null,
    val vip: Long = 0,
    val sign: Int = 0,
    val signtime: Long = 0,
    val pint: Int = 0,
    val exp: Int = 0,
    val remark: String? = null,
    val register: Boolean = false,
    val mainCookie: String? = null,
    val isMainCookie: Boolean = false
){

    fun getRemarkStyle() = CookieRemark(cookie, remark ?: "")

    @Serializable
    data class CookieRemark(
        @SerialName("cookie")
        val cookie: String,
        @SerialName("remark")
        val remark: String?
    )
}