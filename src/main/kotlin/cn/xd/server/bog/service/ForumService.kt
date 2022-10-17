package cn.xd.server.bog.service

interface ForumService {

    /**
     * 获取所有的forum
     */
    fun getAllForum(): String

    /**
     * 获取forum的内容
     */
    fun getForumContent(id: Int, page: Int, pageNum: Int): String

}