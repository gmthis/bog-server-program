package cn.xd.server.bog.service.impl

import cn.xd.server.bog.dao.CookieDAO
import cn.xd.server.bog.dao.StringDAO
import cn.xd.server.bog.entity.string.Image
import cn.xd.server.bog.service.StringService
import cn.xd.server.bog.util.IllegalParameterJson
import cn.xd.server.bog.util.dealWith
import cn.xd.server.bog.util.errorJson
import cn.xd.server.bog.util.successJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.Resource

@Service
class StringServiceImpl: StringService {

    @Resource
    private lateinit var cookieDAO: CookieDAO
    @Resource
    private lateinit var stringDAO: StringDAO
    @Resource
    private lateinit var json: Json

    companion object {
        val forumList = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 16, 17, 42, 444, 999, 2233, 114514)
        val notTimeLineForum = listOf(5, 6, 8, 42, 999)

        private val log = LoggerFactory.getLogger(StringServiceImpl::class.java)

        private val forumNotExist = errorJson(2201, "板块不存在")
        private val pageNotExist = errorJson(2202, "页码不存在")
        private val contentNotExist = errorJson(2203, "内容不存在")
        private val contentIsNotMainContent = errorJson(2204, "串非主内容")
        private val cookieNotExist = errorJson(2101, "请求中至少有一个cookie不存在或被ban")
        private val cookieIsNotOwnerOfContent = errorJson(2205, "cookie及cookie的影武者不是这个内容的发布者")
        private val cookieNotImport = errorJson(2102, "请求中至少有一个cookie未导入")
        private val cookieSignCountNotEnough = errorJson(2108, "cookie签到次数不足,不可发图")
        private val resNotExist = errorJson(2206, "回复串不存在")
        private val resNotMain = errorJson(2207, "回复串不是主串")
        private val resIsDel = errorJson(2208, "回复目标被删除")
        private val resIsLock = errorJson(2209, "回复目标被锁定")
    }

    override fun getSingleContent(id: Int): String {
        val singleContent = stringDAO.findSingleContent(id) ?: return contentNotExist

        return successJson(6001, json.encodeToJsonElement(singleContent))
    }

    override fun getContentAndReply(id: Int, page: Int, pageDef: Int, order: Int): String {
        if (page <= 0) return pageNotExist

        val content = stringDAO.findContent(id) ?: return contentNotExist
        if (content.res != 0) return contentIsNotMainContent

        val jumpOver = if (page == 1) 0 else (page * pageDef) - pageDef

        content.reply += if (order == 0){
            stringDAO.findStringRepliesPaginationASC(id, jumpOver, pageDef)
        } else {
            stringDAO.findStringRepliesPaginationDESC(id, jumpOver, pageDef)
        }

        return successJson(6001, json.encodeToJsonElement(content))
    }


    override fun fuzzySearch(keyword: String, page: Int, pageDef: Int): String {
        val jumpOver = if (page == 1) 0 else (page * pageDef) - pageDef

        val search = stringDAO.fuzzySearch(keyword, jumpOver, pageDef)

        return successJson(6001, json.encodeToJsonElement(search))
    }

    @Transactional
    override fun delString(id: Int, _cookie: String): String {
        val content = stringDAO.findContent(id) ?: return contentNotExist

        val (cookie, token) = checkCookieAndTokenLegality(_cookie) ?: return IllegalParameterJson
        val cookieDB = cookieDAO.safeFindCookie(cookie, token) ?: return cookieNotExist

        if (cookieDB.admin == true){
            delString(id, content.res != 0)
            return successJson(7000).also {
                log.info("删除内容: $id, 操作cookie: $cookie")
            }
        }

        if (cookieDB.cookie == content.cookie){
            delString(id, content.res != 0)
            return successJson(7000).also {
                log.info("删除内容: $id, 操作cookie: $cookie")
            }
        }

        val dbList = cookieDAO.findCookieOwnedByCookie(cookieDB.cookie)
        for (c in dbList) {
            if (c.cookie == content.cookie){
                delString(id, content.res != 0)
                return successJson(7000).also {
                    log.info("删除内容: $id, 操作cookie: $cookie")
                }
            }
        }

        return cookieIsNotOwnerOfContent
    }

    @Transactional
    override fun sendString(
        res: Int,
        forum: Int,
        title: String,
        name: String,
        content: String,
        _cookie: String,
        webapp: Int,
        img: List<Image>?
    ): String {
        val (cookie, token) = checkCookieAndTokenLegality(_cookie) ?: return cookieNotExist
        val cookieDB = cookieDAO.safeFindCookie(cookie, token) ?: return cookieNotExist

        if (cookieDB.isBan) return cookieNotExist
        if (!cookieDB.register) return cookieNotImport
        if (cookieDB.sign < 7 && img != null) return cookieSignCountNotEnough

        if (res < 0) return resNotExist
        if (res != 0){
            val findContent = stringDAO.findContent(res) ?: return resNotExist
            if (findContent.res != 0) return resNotMain
            if (findContent.isDel) return resIsDel
            if (findContent.lock == true) return resIsLock
            stringDAO.addString(
                res = res,
                time = System.currentTimeMillis(),
                forum = 0,
                name = name,
                emoji = cookieDB.emoji,
                cookie = cookie,
                title = "",
                content = dealWith(content),
                images = img
            )
            val copy = findContent.copy(
                replyCount = findContent.replyCount + 1,
                hideCount = if (findContent.hideCount != 0)
                    findContent.hideCount + 1
                else
                    if ((findContent.replyCount + 1) > 5)
                            (findContent.replyCount + 1) - 5
                    else
                        0,
                root = if (findContent.isNotUpdateRoot) findContent.root else System.currentTimeMillis()
            )

            stringDAO.updateString(copy)
            val stringId = stringDAO.findCookieLastSendString(cookie)
            stringDAO.addReplyString(stringId.id, res)
            return successJson(1, JsonPrimitive(stringId.id)).also {
                log.info("添加内容: ${stringId.id}, 操作cookie: $cookie, 回复内容,非主内容")
            }
        }

        if (forum !in forumList) return forumNotExist
        if (forum == 0) return forumNotExist

        stringDAO.addString(
            res = res,
            time = System.currentTimeMillis(),
            forum = forum,
            name = name,
            emoji = cookieDB.emoji,
            cookie = cookie,
            title = title,
            content = dealWith(content),
            images = img
        )
        val lastSendString = stringDAO.findCookieLastSendString(cookie)
        stringDAO.addMainString(lastSendString.id, lastSendString.forum, lastSendString.root)
        if (lastSendString.forum !in notTimeLineForum){
            stringDAO.addShowString(lastSendString.id, lastSendString.forum, lastSendString.root)
        }

        return successJson(1, JsonPrimitive(lastSendString.id)).also {
            log.info("添加内容: ${lastSendString.id}, 操作cookie: $cookie, 所在版块: $forum 主内容")
        }
    }


    @Transactional
    fun delString(id: Int, isReply: Boolean): Int{
        if (isReply){
            stringDAO.delReply(id)
        }else{
            stringDAO.delMain(id)
        }
        return stringDAO.del(id)
    }

    /**
     * 检查完整cookie的合法性,并切割返回
     * @return 分割后的cookie和token
     */
    private fun checkCookieAndTokenLegality(cookieAndToken: String): Pair<String, String>?{
        val list = cookieAndToken.split("#")
        if (list.size != 2) return null
        if (list[0].length != 8) return null
        if (list[1].length != 32) return null
        return Pair(list[0], list[1])
    }
}