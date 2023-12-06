package com.nju.community.controller;

import com.nju.community.entity.Message;
import com.nju.community.entity.Page;
import com.nju.community.entity.User;
import com.nju.community.service.MessageService;
import com.nju.community.service.UserService;
import com.nju.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
