package com.nju.community.controller;

import com.alibaba.fastjson2.JSONObject;
import com.nju.community.entity.Message;
import com.nju.community.entity.Page;
import com.nju.community.entity.User;
import com.nju.community.service.MessageService;
import com.nju.community.service.UserService;
import com.nju.community.util.CommunityConstant;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path="/letter/list",method = RequestMethod.GET)
    public String getLetterList(Model model, Page page){
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        //会话列表
        List<Message> conversationList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
       // List<Message> letterList = messageService.findLetters(, page.getOffset(), page.getLimit());
        List<Map<String,Object>> conversations = new ArrayList<>();
        if(conversationList != null){
            for(Message message:conversationList){
                Map<String,Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount",messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount",messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                //显示对话人的信息
                int targetId = user.getId() == message.getToId() ? message.getFromId() : message.getToId();
                map.put("target",userService.findUserById(targetId));
               conversations.add(map);
            }
        }
        model.addAttribute("conversations",conversations);
        //查询该用户所有未读消息数量
        model.addAttribute("letterUnreadCount",
                messageService.findLetterUnreadCount(user.getId(), null));
        return "/site/letter";

    }

    @RequestMapping(path="/letter/detail/{conversationId}",method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId,Model model,Page page){

        page.setLimit(5);
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        List<Map<String,Object>> letters = new ArrayList<>();
        //私信列表
        List<Message> letterList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        if(letterList!=null){
            for(Message letter:letterList){
                Map<String,Object> map = new HashMap<>();
                map.put("letter",letter);
                //发信人
                map.put("fromUser",userService.findUserById(letter.getFromId()));
                letters.add(map);
            }
        }
        //与之对话的用户
        model.addAttribute("target",getLetterTarget(conversationId));
        model.addAttribute("letters",letters);

        //将未读消息设为已读
        List<Integer> NoReadList = getLetterIds(letterList);
        if(!NoReadList.isEmpty()){
            messageService.readMessage(NoReadList);
        }

        return "/site/letter-detail";
    }

    // 111_112拆分出对话目标
    private User getLetterTarget(String conversationId){
        String[] ids = conversationId.split("_");
        int id0=Integer.parseInt(ids[0]);
        int id1=Integer.parseInt(ids[1]);
        if(hostHolder.getUser().getId()==id0)
            return userService.findUserById(id1);
        else
            return  userService.findUserById(id0);
    }

    //发私信
    @RequestMapping(path = "/letter/send",method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName , String content){
         //验证统一异常处理
        //Integer.valueOf("abc");

        User from = hostHolder.getUser();
        User to = userService.findUserByName(toName);
        if(to == null){
            return CommunityUtil.getJSONString(1,"目标用户不存在！");
        }
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setFromId(from.getId());
        message.setToId(to.getId());
        if(message.getFromId()< message.getToId()){
            message.setConversationId(message.getFromId()+"_"+ message.getToId());
        }else{
            message.setConversationId(message.getToId()+"_"+message.getFromId());
        }
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0);
    }


    //提取未读消息的id，用于别的方法中将未读变已读
    private List<Integer> getLetterIds(List<Message> LetterList){
        List<Integer> ids = new ArrayList<>();
        int toId = hostHolder.getUser().getId();
        if(LetterList != null){
            for(Message m: LetterList){
                //首先需要判断接收者是不是当前用户，否则没法已读
                if(m.getToId()==toId&&m.getStatus() == 0){
                    ids.add(m.getId());
                }
            }
        }
        return ids;
    }

    //通知列表
    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        //三类通知
        //查询评论类通知
        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String,Object> messageVO = new HashMap<>();
        messageVO.put("message",message);

        if(message!=null) {

            //反转义,把json内容(原本存的是event的data)转回map
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            //事件触发人
            messageVO.put("user",userService.findUserById((int)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("discussPostId",data.get("discussPostId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_COMMENT);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread",unread);
        }
        model.addAttribute("commentNotice",messageVO);


        //查询点赞类通知
         message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
         messageVO = new HashMap<>();
         messageVO.put("message",message);

        if(message!=null) {

            //反转义,把json内容(原本存的是event的data)转回map
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user",userService.findUserById((int)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("discussPostId",data.get("discussPostId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_LIKE);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread",unread);
        }
        model.addAttribute("likeNotice",messageVO);


        //查询关注类通知
        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        messageVO.put("message",message);

        if(message!=null) {

            //反转义,把json内容(原本存的是event的data)转回map
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String,Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user",userService.findUserById((int)data.get("userId")));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("entityId",data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(),TOPIC_FOLLOW);
            messageVO.put("count",count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread",unread);
        }
        model.addAttribute("followNotice",messageVO);

        //该用户的未读系统通知数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(),null);
        model.addAttribute("noticeUnreadCount",noticeUnreadCount);
        //该用户的未读私信数量,页面显示需要
        int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
        model.addAttribute("letterUnreadCount",letterUnreadCount);

        return "/site/notice";
    }

    //某一类通知的list
    @RequestMapping(path = "/notice/detail/{topic}",method = RequestMethod.GET)
    public String getNoticedetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
       //每条通知都有一个map信息，聚合起来成为一个list
        List<Map<String,Object>> noticeVoList = new ArrayList<>();
        if(noticeList != null) {
            for(Message notice: noticeList){
                Map<String,Object> map = new HashMap<>();
                //通知
                map.put("notice",notice);
                //内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((int)data.get("userId")));
                map.put("entityId", data.get("entityId"));
                map.put("entityType", data.get("entityType"));
                map.put("discussPostId",data.get("discussPostId"));

                //消息的发出者，比如A关注了你，那么fromUser就是A
                map.put("fromUser",userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);

            }
        }
        model.addAttribute("notices",noticeVoList);

        //设置已读
        List<Integer> ids = getLetterIds(noticeList);
        if(!ids.isEmpty()){
            messageService.readMessage(ids);
        }
        return "/site/notice-detail";
    }

}
