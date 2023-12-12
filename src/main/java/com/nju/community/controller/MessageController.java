package com.nju.community.controller;

import com.nju.community.entity.Message;
import com.nju.community.entity.Page;
import com.nju.community.entity.User;
import com.nju.community.service.MessageService;
import com.nju.community.service.UserService;
import com.nju.community.util.CommunityUtil;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MessageController {

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
        if(NoReadList!=null)
        messageService.updateStatus(NoReadList,1);

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


}
