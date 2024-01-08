package com.nju.community.dao;

import com.nju.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
//声明一下这个组件不建议使用
@Deprecated
public interface LoginTicketMapper {

    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    //xml的自动生成主键在properties里面配置，标注sql在下面的标注里配置
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired from login_ticket ",
            "where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    //双引号转义\",动态sql和xml一样，不过需要<script>引起来
    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\">",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket,int status);


}
