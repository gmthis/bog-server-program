package cn.xd.server.bog

import cn.xd.server.bog.dao.StringDAO
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import javax.annotation.Resource

@SpringBootTest
class BogApplicationTests {

    @Resource
    lateinit var stringDAO: StringDAO

    @Test
    fun contextLoads(){

//        val timeLineContent = stringDAO.getTimeLineContent(1000, 20)
//        println(timeLineContent.size)
//
//        for (forumDB in timeLineContent) {
//            println("------------------------------------------")
//            println(forumDB)
//            for (stringReply in getStringRepliesLastFive(forumDB.id, forumDB.replyCount, 20)) {
//                println(stringReply)
//            }
//        }

//        val strings = listOf("Gf5Q4B3i", "OQbKs5Ee", "ThMmwdxR", "uGyLvczg", "jzxdbnaw")
//        val ints = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 16, 17, 42, 444, 999, 2233, 114514)
//
//        for (i in (1..2000)){
//
//            val s = strings.random()
//            val i1 = ints.random()
//            val currentTimeMillis = System.currentTimeMillis()
//            stringDAO.generateFakeForumContent(
//                s,
//                i1,
//                currentTimeMillis
//            )
//        }
    }

//    fun getStringRepliesLastFive(id: Int, replyCount: Int, pageNum: Int): List<ReplyDB>{
//        val ceil = ceil(replyCount.toFloat() / pageNum.toFloat()).toInt()
//
//        return stringDAO.findStringRepliesPagination(id, if (ceil <= 1) 0 else (ceil * pageNum) - pageNum, 5)
//    }

    @Test
    fun test2(){
//        val string = stringDAO.getAllString()
////        val arrayOf = arrayOf(5, 6, 8, 42, 999)
////
//        for (stringDB in string) {
//            if (stringDB.res == 0) {
//                val replies = stringDAO.getStringReplies(stringDB.id)
//                stringDAO.setReplyCount(stringDB.id, replies.size, if(replies.size - 5 < 0) 0 else replies.size - 5)
//            }
//        }

    }

    @Test()
    fun test3(){
//        val strings = listOf("Gf5Q4B3i", "OQbKs5Ee", "ThMmwdxR", "uGyLvczg", "jzxdbnaw")
//
//        for (i in (2000..10000)){
//            val random = (1..2000).random()
//            stringDAO.generateFakeReply(strings.random(), random, System.currentTimeMillis())
//            stringDAO.addReplyString(i, random)
//        }

    }

}
