package cn.xd.server.bog.dao

import cn.xd.server.bog.entity.string.Image
import cn.xd.server.bog.entity.string.Reply
import cn.xd.server.bog.entity.string.SingleContent
import cn.xd.server.bog.entity.string.StringSend
import cn.xd.server.bog.util.typehandler.ImageJsonHandler
import org.apache.ibatis.annotations.*

@Mapper
interface StringDAO {

    @Select("select c1.* from all_string c1 join (select c2.id from main_string c2 where c2.forum = #{forumID} and c2.is_del != true order by c2.root desc limit #{pageNum}, #{num}) c3 on c1.id = c3.id")
    fun findForumContent(forumID: Int, pageNum: Int, num: Int): List<StringSend>

    @Select("select c1.* from all_string c1 join (select c2.id from main_string c2 join (select c3.id from show_string c3) c4 on c2.id = c4.id and c2.is_del != true order by c2.root desc limit #{pageNum}, #{num}) c5 on c1.id = c5.id")
    fun findTimeLineContent(pageNum: Int, num: Int): List<StringSend>

    @Select("select c1.id, res, time, name, emoji, cookie, admin, content, images from all_string c1 join (select c2.id from reply_string c2 where c2.res = #{id} and c2.is_del != true) c3 on c1.id = c3.id")
    fun findStringReplies(id: Int):List<Reply>

    @Select("select c1.id, res, time, name, emoji, cookie, admin, content, images from all_string c1 join (select c2.id from reply_string c2 where c2.res = #{id} and c2.is_del != true) c3 on c1.id = c3.id order by c1.time desc limit #{pageNum}, #{num}")
    fun findStringRepliesPaginationDESC(id: Int, pageNum: Int, num: Int): List<Reply>

    @Select("select c1.id, res, time, name, emoji, cookie, admin, content, images from all_string c1 join (select c2.id from reply_string c2 where c2.res = #{id} and c2.is_del != true) c3 on c1.id = c3.id order by c1.time limit #{pageNum}, #{num}")
    fun findStringRepliesPaginationASC(id: Int, pageNum: Int, num: Int): List<Reply>

    @Select("select id, res, time, forum, name, emoji, cookie, admin, title, content, `lock`, images from all_string where id = #{id} and is_del != true")
    fun findSingleContent(id: Int): SingleContent?

    @Select("select all_string.* from all_string where id = #{id} and is_del != true")
    fun findContent(id: Int): StringSend?

    @Select("select all_string.* from all_string where match(content) against(#{key}) and is_del != true limit #{pageNum}, #{num}")
    fun fuzzySearch(key: String, pageNum: Int, num: Int): List<StringSend>

    @Update("update reply_string set is_del = true where id = #{id}")
    fun delReply(id: Int)

    @Update("update main_string set is_del = true where id = #{id}")
    fun delMain(id: Int)

    @Delete("update all_string set is_del = true where id = #{id}")
    fun del(id: Int): Int

//    @Insert("insert into all_string(res, root, time, forum, name, emoji, cookie, admin, title, content, `lock`, images, reply_count, hide_count) VALUE (0, #{root}, #{root}, #{forumID}, '', '', #{cookie}, false, '', '假数据', false, null, 0, 0)")
//    fun generateFakeForumContent(cookie: String, forumID: Int, root: Long): Int
//
//    @Insert("insert into all_string(res, root, time, forum, name, emoji, cookie, admin, title, content, `lock`, images, reply_count, hide_count) VALUE (#{stringId}, 0, #{time}, 0, '', '', #{cookie}, false, '', '假数据', false, null, 0, 0)")
//    fun generateFakeReply(cookie: String, stringId: Int, time: Long): Int

    @Insert("insert into all_string (res, root, time, forum, name, emoji, cookie, title, content, images) values (#{res}, #{time}, #{time}, #{forum}, #{name}, #{emoji}, #{cookie}, #{title}, #{content}, #{images, typeHandler=cn.xd.server.bog.util.typehandler.ImageJsonHandler})")
    fun addString(res: Int, time: Long, forum: Int, name: String, emoji: String, cookie: String, title: String, content: String, images: List<Image>?): Int

    @Insert("insert into show_string(id, forum, root) VALUE (#{id}, #{forum}, #{root})")
    fun addShowString(id: Int, forum: Int, root: Long): Int

    @Insert("insert into main_string(id, forum, root) VALUE (#{id}, #{forum}, #{root})")
    fun addMainString(id: Int, forum: Int, root: Long): Int

    @Insert("insert into reply_string(id, res) VALUE (#{id}, #{res})")
    fun addReplyString(id: Int, res: Int): Int

    @Update("update all_string set root = #{root}, reply_count = #{replyCount}, hide_count = #{hideCount}, is_del = #{isDel}, is_not_update_root = #{isNotUpdateRoot} where id = #{id}")
    fun updateString(string: StringSend): Int

    @Update("update main_string set root = #{root}, is_del = #{isDel} where id = #{id}")
    fun updateMainString(id: String, root: Long, isDel: Boolean): Int

    @Update("update reply_string set is_del = #{isDel} where id = #{id}")
    fun updateReply(id: String, isDel: Boolean): Int

    @Update("update show_string set root = #{root} where id = #{id}")
    fun updateShow(id: String, root: Long): Int

    @Select("select * from all_string where cookie = cookie order by time desc limit 0, 1")
    fun findCookieLastSendString(cookie: String): StringSend
}