package cn.xd.server.bog.util

private val quoteRegular = "&gt;&gt;Po.[0-9]+".toRegex()
private val randomRegular = "\\[[0-9]+-[0-9]+]".toRegex()
private val httpRegular = "[a-zA-Z]+://\\S*".toRegex()

fun dealWith(content: String): String{

    var result = unescape(content)

    for (matchResult in quoteRegular.findAll(result)) {
        result = result.replace(
            matchResult.value,
            "<br /><span class=\"quote\">${matchResult.value}</span><br />"
        )
    }

    for (matchResult in randomRegular.findAll(content)) {
        val value = matchResult.value
        val left = value.substring(1, value.indexOf("-"))
        val right = value.substring(value.indexOf("-") + 1, value.length - 1)

        val random = (left.toInt() .. right.toInt()).random()

        result = result.replace(
            matchResult.value,
            "<b>$random ($left ~ $right)</b>"
        )
    }

    for (matchResult in httpRegular.findAll(content)) {
        result = result.replace(
            matchResult.value,
            "<a href=\"${matchResult.value}\" title=\"${matchResult.value}\" target=\"_blank\" rel=\"nofollow noreferrer\"><i class=\"if if-link\"><\\i>网页链接</a>"
        )
    }

    return result
}

fun unescape(content: String): String{
    if (content.trim().isEmpty()) return content

    val stringBuilder = StringBuilder()

    for (c in content) {
        when(c){
            '>' -> {
                stringBuilder.append("&gt;")
            }
            '<' -> {
                stringBuilder.append("&lt;")
            }
            '&' -> {
                stringBuilder.append("&amp;")
            }
            '\n' -> {
                stringBuilder.append("<br />")
            }
            else -> {
                stringBuilder.append(c)
            }
        }
    }
    return stringBuilder.toString()
}