package com.nju.community.controller.interceptor;

import com.nju.community.entity.User;
import com.nju.community.service.MessageService;
import com.nju.community.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

//所有页面的标签页都要显示未读消息数量
@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    //post——调controller之后调模板之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        //判断当前用户是否登录，
        if(user != null && modelAndView != null) {
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", letterUnreadCount + noticeUnreadCount);

        }
    }
}
