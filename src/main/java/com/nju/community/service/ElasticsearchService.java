package com.nju.community.service;

import com.nju.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticsearchService {

    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public void saveDiscussPost(DiscussPost post){
        elasticsearchRepository.save(post);
    }

    public void deleteDiscussPost(DiscussPost post) {
        elasticsearchRepository.delete(post);
    }

    //keyword查询用的关键词，current现在多少页，limit一页显示多少数据
    public Page searchDiscussPost(String keyword, int current, int limit){
        Criteria criteria = new Criteria("title").matches(keyword)
                .or(new Criteria("content").matches(keyword));

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
                .withPageable(PageRequest.of(current,limit));
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

        //构建Page对象，数据，分页信息（当前页数,一页多少信息），一共多少条数据
        Page<DiscussPost> page = new PageImpl<>(discussPostList,PageRequest.of(current,limit), result.getTotalHits());
        return  page;

    }

}
