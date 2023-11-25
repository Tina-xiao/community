package com.nju.community.entity;

//封装分页相关信息

public class Page {
    //当前页码
    private int current = 1;
    //显示上线
    private int limit = 10;
    //数据总数，用于计算总页数
    private int rows;
    //查询路径，用来复用分页链接
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1)
        this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    //获取当前页的起始行
    public int getOffset(){
        return (current-1 )* limit;
    }
    //获取总页数
    public int getTotal(){
        return (int)Math.ceil(rows/limit);
    }

    //获取起始显示页码
    public int getFrom(){
        int from = current-2;
        return from<1? 1 : from;

    }
    //获取终止显示页码
    public int getTo(){
        int end =current+2;
        int total = getTotal();
        return  end > total? total : end;
    }


    @Override
    public String toString() {
        return "Page{" +
                "current=" + current +
                ", limit=" + limit +
                ", rows=" + rows +
                ", path='" + path + '\'' +
                '}';
    }
}
