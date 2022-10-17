package cn.xd.server.bog.service

import cn.xd.server.bog.entity.string.Image

interface StringService {

    fun getSingleContent(id: Int): String

    fun getContentAndReply(id: Int, page: Int, pageDef: Int, order: Int): String

    fun fuzzySearch(keyword: String, page: Int, pageDef: Int): String

    fun delString(id: Int, _cookie: String): String

    fun sendString(
        res: Int,
        forum: Int,
        title: String,
        name: String,
        content: String,
        _cookie: String,
        webapp: Int,
        img: List<Image>?
    ): String
}