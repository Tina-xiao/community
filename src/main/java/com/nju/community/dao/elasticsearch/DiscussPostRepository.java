package com.nju.community.dao.elasticsearch;

import com.nju.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

//@Mapper是mysql专有的注解，@Repository是spring提供给数据访问层的注解
@Repository
//<要存什么类型，主键是什么类型>
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer>{
//    声明Repository接口，程序中注入这个接口，可以完成一些简单的操作，比如插入更新，全亮查询等

}
