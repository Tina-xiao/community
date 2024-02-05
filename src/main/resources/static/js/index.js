$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// <meta name="_csrf" th:content="${_csrf.token}">
	// 	<meta name="_csrf_header" th:content="${_csrf.headerName}">
	//发送AJAX请求之前，将CSRF令牌设置到请求的消息头中
	//获取name=‘_csrf’（名字固定叫这个）的meta属性值
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	//对请求参数做设置，xhr是发送异步请求的核心对象
	$(document).ajaxSend(function (e, xhr, options) {
		//key是header，值是token
		xhr.setRequestHeader(header,token);
	});



	//用id选择器获取标题和内容
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	//发送异步请求
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title,"content":content},
		function(data){
			data = $.parseJSON(data);
			//在提示框中显示返回消息
			$("#hintBody").text(data.msg);
			//显示提示框，2s后自动隐藏
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//成功就刷新页面
				if(data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	);


}