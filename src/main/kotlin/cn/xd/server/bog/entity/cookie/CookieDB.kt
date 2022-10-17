package cn.xd.server.bog.entity.cookie

import cn.xd.server.bog.annotation.NoArg
import cn.xd.server.bog.util.DateSerializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import java.text.SimpleDateFormat
import java.util.*

@NoArg
data class CookieDB(
    val cookie: String,
    val token: String,
    val admin: Boolean? = null,
    val emoji: String = "",
    val vip: Long? = null,
    val sign: Int = 0,
    val signtime: Long = 0,
    val point: Int = 0,
    val exp: Int = 0,
    val remark: String? = null,
    val register: Boolean = false,
    val mainCookie: String? = null,
    val isMainCookie: Boolean = false,
    val isBan: Boolean = false
){

    fun getRemarkStyle() = CookieRemark(cookie, remark ?: "")

    fun getSignInfo() = SignInfo(
        sign = sign,
        point = point,
        exp = exp,
        signtime = signtime
    )

    fun getCookieInfo(
        list: MutableList<CookieRemark> = mutableListOf()
    ) = CookieInfo(
        cookie, vip, sign, signtime, point, exp, list
    ).also {
        it.list.add(0, getRemarkStyle())
    }

    @Serializable
    data class CookieInfo(
        val cookie: String,
        val vip: Long?,
        val sign: Int,
        @Serializable(with = DateSerializable::class) val signtime: Long,
        val point: Int,
        val exp: Int,
        val list: MutableList<CookieRemark> = mutableListOf()
    )

    @Serializable
    data class CookieRemark(
        @SerialName("cookie")
        val cookie: String,
        @SerialName("remark")
        val remark: String?
    )

    @Serializable
    data class SignInfo(
        val sign: Int,
        @Serializable(with = DateSerializable::class) val signtime: Long,
        val point: Int,
        val exp: Int
    )
}