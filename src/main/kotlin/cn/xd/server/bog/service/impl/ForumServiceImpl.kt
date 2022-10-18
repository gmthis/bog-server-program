package cn.xd.server.bog.service.impl

import cn.xd.server.bog.dao.ForumDAO
import cn.xd.server.bog.dao.StringDAO
import cn.xd.server.bog.entity.string.Reply
import cn.xd.server.bog.service.ForumService
import cn.xd.server.bog.util.errorJson
import cn.xd.server.bog.util.successJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.springframework.stereotype.Service
import javax.annotation.Resource

@Service
class ForumServiceImpl: ForumService {
    @Resource
    private lateinit var forumDAO: ForumDAO
    @Resource
    private lateinit var stringDAO: StringDAO
    @Resource
    private lateinit var json: Json

    companion object {
        val forumList = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 16, 17, 42, 444, 999, 2233, 114514)

        val forumNotExist = errorJson(2201, "板块不存在")
        val pageNotExist = errorJson(2202, "页码不存在")

    }

    override fun getAllForum(): String {
        return successJson(6001, json.encodeToJsonElement(forumDAO.getAllForum()))
    }

    override fun getForumContent(id: Int, page: Int, pageNum: Int): String {
        if (id !in forumList) return forumNotExist
        if (page <= 0) return pageNotExist

        val jumpOver = if (page == 1) 0 else (page * pageNum) - pageNum

        val forumContent = if (id == 0) stringDAO.findTimeLineContent(jumpOver, pageNum)
            else stringDAO.findForumContent(id, jumpOver, pageNum)
        if (forumContent.isEmpty()) return pageNotExist

        for (stringSend in forumContent) {
            val lastFive = getStringRepliesLastFive(stringSend.id, stringSend.replyCount)
            stringSend.reply += lastFive
        }

        return successJson(6001, json.encodeToJsonElement(forumContent))
    }

    private fun getStringRepliesLastFive(id: Int, replyCount: Int): List<Reply>{
        return stringDAO.findStringRepliesPaginationDESC(id, if (replyCount - 5 <= 0) 0 else replyCount - 5, 5)
    }

}