package cn.xd.server.bog.service

import org.springframework.transaction.annotation.Transactional

interface CookieService {
    /**
     * 生成一个新的cookie
     * @return 回复客户端的JSON
     */
    @Transactional
    fun newCookie(): String

    /**
     * 导入一个cookie
     * @return 回复客户端的JSON
     */
    @Transactional
    fun importCookie(master: String, cookieAdd: String): String

    /**
     * 删除某个影武者的导入
     *
     * 注意函数内不会检查masterToken和masterCookie的合法性,在调用前需要检查
     * @return 回复客户端的JSON
     */
    @Transactional
    fun removeCookie(masterToken: String, masterCookie: String, cookie: String): String

    /**
     * 为一个cookie添加备注
     * @return 回复客户端的JSON
     */
    @Transactional
    fun remarkCookie(masterCookie: String, masterToken: String, cookie: String, remarks: String): String

    /**
     * 为cookie增加一次签到计数
     */
    @Transactional
    fun singIn(cookie: String, token: String): String

    /**
     * 查找一个cookie的信息
     * @return cookie信息
     */
    @Transactional
    fun findCookieInfo(cookie: String, token: String): String
}