package com.nju.community.controller;

import com.nju.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    //统计页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        return "/site/admin/data";
    }

    //统计网站
    @RequestMapping(path = "/data/uv", method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end,
                        Model model) {
    //@DateTimeFormat注解的功能是将一个日期字符串转化为对应的Date类型，
    // 主要处理前端时间类型与后端pojo对象中的成员变量进行数据绑定，在注解属性patttern中所约束的时间格式并不会影响后端返回前端的时间类型数据格式。
        long uv = dataService.calculateUV(start, end);
        model.addAttribute("uvResult", uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
       // return "/site/admin/date";也可以
        //还需要另外一个方法
        return "forward:/data";
    }

    //统计活跃用户
    @RequestMapping(path = "/data/dau", method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end,
                        Model model) {
        //@DateTimeFormat注解的功能是将一个日期字符串转化为对应的Date类型，
        // 主要处理前端时间类型与后端pojo对象中的成员变量进行数据绑定，在注解属性patttern中所约束的时间格式并不会影响后端返回前端的时间类型数据格式。
        long dau = dataService.calculateDAU(start, end);
        model.addAttribute("dauResult", dau);
        //为了页面能显示时间，再传回页面
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        // return "/site/admin/date";也可以
        //还需要另外一个方法
        return "forward:/data";
    }

}
