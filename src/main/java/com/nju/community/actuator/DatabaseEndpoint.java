package com.nju.community.actuator;

import com.nju.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
//http://localhost:8080/community/actuator/database
@Endpoint(id = "database")
public class DatabaseEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    //通过连接池访问连接
    @Autowired
    private DataSource dataSource;

    //@ReadOperation表示这个请求方法是通过get来访问的,如果是其他的请求要访问其他的option
    @ReadOperation
    public String checkConnection() {
        try (//在该小括号初始化的资源，编译的时候自动加finally关闭掉
                Connection conn = dataSource.getConnection();
        )
        {
           return CommunityUtil.getJSONString(0,"获取连接成功");
        } catch (SQLException e) {
            logger.error("获取连接失败"+e.getMessage());
            return CommunityUtil.getJSONString(1,"获取连接失败");
        }
    }


}
