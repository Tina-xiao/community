package com.nju.community;
import com.nju.community.dao.DiscussPostMapper;
import com.nju.community.dao.elasticsearch.DiscussPostRepository;
import com.nju.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.CriteriaQueryBuilder;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    //是从mysql取数据存入elasticsearch
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    //对于discussPostRepository处理不了的需要template来处理
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void testInsert(){
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));

    }

    @Test
    //插入多条数据
    public void testInsertList() {

        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(113,0,100));
    }

    //修改elasticsearch数据
    @Test
    public void update( ){
        DiscussPost post = discussPostMapper.selectDiscussPostById(211);
        System.out.println(post.getContent());
        post.setContent(" 修改****************");
        discussPostRepository.save(post);//覆盖原有
        System.out.println(post.getContent());
    }

    @Test
    public void testDelete() {
        //删掉一条数据
        discussPostRepository.delete(discussPostMapper.selectDiscussPostById(211));
        //删掉该索引的所有数据
        //discussPostRepository.deleteAll();
    }


    //匹配查询的主要作用就是提供一个关键字和字段，将关键字分词后将ES中对应字段包含相应关键字的结果返回，主要运用在搜索功能中。
    @Test
    public void testSearchByTemplate() {
        //注入elasticsearchTemplate，用它进行匹配查询，Repository功能不支持

    /* 旧版本es
        SearchQuery searchQuery = new NativeQueryBuilder()
               //一个值对应多个字段
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(SortOptionsBuilders.fieldSort("type").order(Setting.SortOrder.desc))
                .withSort(SortOptionsBuilders.fieldSort("score").order(Setting.SortOrder.desc))
                .withSort(SortOptionsBuilders.fieldSort("createTime").order(Setting.SortOrder.desc))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        //如果匹配到了前后加什么,例: <em>互联网</em>
                        new HighlightBulider.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBulider.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        //开始查询
        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class,
        new SearchResultMapper() {
        @Override
        public <T> ...
        }
        )

        // Page<DiscussPost> page = discussPostRepository.search(searchQuery); 底层原理是 elasticsearchTemplate.queryForPage(searchQuery, class,SearchResultMapper);
        //底层得到了高亮显示的值，但是没有整合到一起返回，所以输出的信息没有高亮
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());
        for(DiscussPost post : page) {
            System.out.println(post);
        }

     */


        //Springboot3.1+Elasticsearch8.x 新版本匹配查询
        //构造查询语句
        Criteria criteria = new Criteria("title").matches("互联网寒冬")
                        .or(new Criteria("content").matches("互联网寒冬"));

        //添加排序条件，并在最后构建匹配查询的时候依次添加条件。
//        Sort sort = Sort.by(Sort.Direction.DESC,"type");
//        Sort sort2 = Sort.by(Sort.Direction.DESC,"createTime");
//        Sort sort3 = Sort.by(Sort.Direction.DESC,"createTime");

        //高亮查询
        List<HighlightField> highlightFieldList = new ArrayList<>();
        HighlightField highlightField = new HighlightField("title", HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").build());
        highlightFieldList.add(highlightField);
        highlightField = new HighlightField("content", HighlightFieldParameters.builder().withPreTags("<em>").withPostTags("</em>").build());
        highlightFieldList.add(highlightField);

        Highlight highlight = new Highlight(highlightFieldList);
        HighlightQuery highlightQuery = new HighlightQuery(highlight,DiscussPost.class);

        //构建查询
        CriteriaQueryBuilder builder = new CriteriaQueryBuilder(criteria)
                .withSort(Sort.by(Sort.Direction.DESC,"type"))
                .withSort(Sort.by(Sort.Direction.DESC,"score"))
                .withSort(Sort.by(Sort.Direction.DESC,"createTime"))
                .withHighlightQuery(highlightQuery)
                .withPageable(PageRequest.of(0,10));
        CriteriaQuery query = new CriteriaQuery(builder);

        //通过elasticsearchTemplate查询
        SearchHits<DiscussPost> result = elasticsearchTemplate.search(query, DiscussPost.class);
        //处理结果,将高亮和结果合并
        List<SearchHit<DiscussPost>> searchHitList = result.getSearchHits();
        List<DiscussPost> discussPostList = new ArrayList<>();
        //遍历命中数据
        for(SearchHit<DiscussPost> hit:searchHitList){
            //先将匹配到的原始数据存入新的post，要注意，这里的内容并不是所有，只包含和匹配到的字段上下文有关的部分语句
            DiscussPost post = hit.getContent();
            //将高亮结果添加到返回的结果类中显示（覆盖）
            //获取与title有关的高亮显示内容，有可能匹配多个字段，比如有多个互联网与之匹配，这里我们只取第一个匹配的，即index为0
            //var自动匹配类型，其实为List<String>
            var titleHighlight = hit.getHighlightField("title");
            if(titleHighlight.size()!=0){
                //用高亮处理后的内容覆盖原内容
                post.setTitle(titleHighlight.get(0));
            }
            var contentHighlight = hit.getHighlightField("content");
            if(contentHighlight.size()!=0){
                post.setContent(contentHighlight.get(0));
            }
            discussPostList.add(post);
        }

        //构建Page对象，数据，分页信息，一共多少条数据
        Page<DiscussPost> page = new PageImpl<>(discussPostList,PageRequest.of(9,10), result.getTotalHits());
        for (DiscussPost post:page){
            System.out.println(post);
        }


    }
}
