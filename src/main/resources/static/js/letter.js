$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

//发送私信后把发送框关掉，提示框显示，然后2s后关掉
// function send_letter() {
// 	$("#sendModal").modal("hide");
//
// 	var toName = $("#recipient-name").val();
// 	var content = $("#message-text").val();
// 	$.post(
// 		CONTEXT_PATH+ "/letter/send",
// 		{"toName":toName,"content":content},
// 		function (data){
// 			//显示错误信息，data是controller返回的值
// 			data = $.parseJSON(data);
// 			$("#hintBody").text(data.msg);
// 			//刷新页面
// 			$("#hintModal").modal("show");
// 			setTimeout(function(){
// 				$("#hintModal").modal("hide");
// 				location.reload();
// 			}, 2000);
// 		}
//
// 	);
// }

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName,"content":content},
		function(data) {
			data = $.parseJSON(data);
			if(data.code == 0) {
				$("#hintBody").text("发送成功!");
			} else {
				$("#hintBody").text(data.msg);
			}

			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}