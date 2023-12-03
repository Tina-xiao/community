package com.nju.community.util;


import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";
    //根节点
    private TrieNode root = new TrieNode();

    //初始化trie树，只要在构造函数完成后初始化一次即可
    @PostConstruct
    public void init(){

        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
               //把字节流转化成字符流,再转成缓冲流，缓冲流读取数据效率更高
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                ){
                String keyword;
                //按行读取敏感词
                while( (keyword = reader.readLine()) != null) {
                    //添加到前缀树
                    this.addKeyword(keyword);
                }


        } catch (IOException e) {
            logger.error("加载敏感词失败" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    //把敏感词添加到前缀树
    private void addKeyword(String keyword) {
        int length = keyword.length();
        TrieNode tmp = root;
        for (int i = 0; i< length; i++){
            char c=keyword.charAt(i);
            TrieNode subNode = tmp.getSubNode(c);
            if( subNode==null){
                subNode = new TrieNode();
                tmp.addSubNode(c,subNode);
            }
            //指向子节点
            tmp = subNode;
        }
        //设置结束标识，表明该节点是叶节点
        tmp.setKeywordEnd(true);
    }

    //输入可能含有敏感词的字符串，返回替换***后的字符串
    public String filter(String text){
        if(StringUtils.isBlank(text))
            return null;
        //指针1指向trie树
        TrieNode tmp = root;
        //指针2和3在字符串中遍历，记录下标，截取可能存在的敏感词
        int begin = 0;
        int end = 0;
        //结果,用变长字符串更灵活
        StringBuilder stringBuilder = new StringBuilder();
        while( end < text.length()){
            Character c = text.charAt(end);
            //跳过符号
            //例: 嫖%#娼@#
            if(isSymbol(c)){
                //若指针1处于根节点
                if(tmp == root){
                    stringBuilder.append(c);
                    begin++;
                }
                //无论符号在开头或是中间，指针3一定会走一步
                end++;
                //跳过while后续
                continue;
            }
            //检查下级节点
            tmp = tmp.getSubNode(c);

            if(tmp == null) {
                //tmp==null -> end没走到叶节点说明以begin为开头的字符串不是敏感词
                stringBuilder.append(c);
                end=++begin;
                tmp = root;
                continue;
            }
            else if(tmp.isKeywordEnd()){
                //如果是叶节点，则发现敏感词，将begin到end替换掉
                stringBuilder.append(REPLACEMENT);
                begin = ++end;
                tmp=root;
            }
            else{

                end++;
            }

        }
        //对于3到终点2没到终点的最后一批字符计入结果
        stringBuilder.append(text.substring(begin));
        return stringBuilder.toString();
    }

    //判断是否为ASCII之外的特殊符号，是的话返回true
    private boolean isSymbol(Character c){
        // 东亚的文字范围不认为是特殊文字，包括中文日文韩文等
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0X9FFF);
    }

    //内部类前缀树
    private class TrieNode{

        //关键词结束标识,在trie树中是否为敏感词汇并且是叶节点，只有走到叶节点才算是敏感词
        private boolean isKeywordEnd = false;

        //子节点,根据字符找到对应节点
        private Map<Character,TrieNode>  subNodes = new HashMap<>();

        //添加子节点
        public void addSubNode(Character c,TrieNode node){
            subNodes.put(c,node);
        }

        //根据字符获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }
    }


}
