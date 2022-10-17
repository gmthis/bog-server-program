package cn.xd.server.bog.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 根据指定信息生成错误JSON
 */
fun errorJson(
    code: Int,
    info: String,
    type: String = "error"
) = """{"type":"$type","code":$code,"info":"$info"}"""

/** 内部错误JSON **/
const val InternalErrorJson = """{"type":"error","code":2001,"info":"内部错误"}"""
/** 非法参数错误JSON **/
const val IllegalParameterJson = """{"type":"error","code":2002,"info":"非法参数"}"""

/**
 * 根据指定信息生成成功JSON字符串
 */
fun successJson(
    code: Int,
    info: JsonElement? = null,
    type: String = "OK"
): String {
    return successJsonObject(code, info, type).toString()
}

/**
 * 根据指定信息生成成功JSON对象
 */
fun successJsonObject(
    code: Int,
    info: JsonElement?,
    type: String = "OK"
): JsonObject {
    return JsonObject(
        mutableMapOf<String, JsonElement>().also {
            it["type"] = JsonPrimitive(type)
            it["code"] = JsonPrimitive(code)
            if (info != null) it["info"] = info
        }
    )
}

class DateSerializable: KSerializer<Long> {

    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    override fun deserialize(decoder: Decoder): Long {
        return format.parse(decoder.decodeString()).time
    }

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("java.util.Date")

    override fun serialize(encoder: Encoder, value: Long) {
        encoder.encodeString(format.format(Date(value)))
    }

}