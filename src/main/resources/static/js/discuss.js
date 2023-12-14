
function like(btn, entityType, entityId, entityUserId) {
    //发送异步请求
    $.post(
        //web路径
        CONTEXT_PATH + "/like",
        //携带参数
        {"entityType": entityType, "entityId": entityId, "entityUserId":entityUserId},
        function (data) {
            data = $.parseJSON(data);
            //成功就刷新页面
            if (data.code == 0) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1 ? "已赞" : "赞");

                //window.location.reload();
            } else {
                alert(data.msg);
            }

        }
    );
}
