// 读者端公共脚本：登录态、请求头、提示框
var baseURL = "/";

var readerToken = localStorage.getItem("readerToken");
if (!readerToken || readerToken == "null") {
  location.href = baseURL + "reader/login.html";
}

// jQuery 全局配置：统一带上 token，过期自动回登录页
$.ajaxSetup({
  dataType: "json",
  cache: false,
  headers: {
    token: readerToken,
  },
  complete: function (xhr) {
    if (xhr.responseJSON && xhr.responseJSON.code == 401) {
      localStorage.removeItem("readerToken");
      localStorage.removeItem("readerName");
      localStorage.removeItem("readerId");
      location.href = baseURL + "reader/login.html";
    }
  },
});

function readerLogout() {
  localStorage.removeItem("readerToken");
  localStorage.removeItem("readerName");
  localStorage.removeItem("readerId");
  location.href = baseURL + "reader/login.html";
}

//重写 alert
window.alert = function (msg, callback) {
  if ($("#alert").length > 0) {
    $("#alert-content").html(msg);
  } else {
    var html =
      '<div id="alert" class="modal fade">' +
      '<div class="modal-dialog modal-sm">' +
      '<div class="modal-content">' +
      '<div class="modal-header"><h4 class="modal-title">提示</h4></div>' +
      '<div class="modal-body" id="alert-content">' + msg + "</div>" +
      '<div class="modal-footer"><button type="button" class="btn btn-primary" id="alert-ok">关闭</button></div>' +
      "</div></div></div>";
    $("body").append(html);
  }
  $("#alert-ok").unbind();
  $("#alert-ok").click(function () {
    $("#alert").modal("hide");
    if (callback) callback("ok");
  });
  $("#alert").modal("show");
};

//重写 confirm
window.confirm = function (msg, callback) {
  if ($("#confirm").length > 0) {
    $("#confirm-content").html(msg);
  } else {
    var html =
      '<div id="confirm" class="modal fade">' +
      '<div class="modal-dialog modal-sm">' +
      '<div class="modal-content">' +
      '<div class="modal-header"><h4 class="modal-title">提示</h4></div>' +
      '<div class="modal-body" id="confirm-content">' + msg + "</div>" +
      '<div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">取消</button>' +
      '<button type="button" class="btn btn-primary" id="confirm-ok">确定</button></div>' +
      "</div></div></div>";
    $("body").append(html);
  }
  $("#confirm-ok").unbind();
  $("#confirm-ok").click(function () {
    $("#confirm").modal("hide");
    if (callback) callback("ok");
  });
  $("#confirm").modal("show");
};

function formatMoney(value) {
  if (value === null || value === undefined || value === "") return "0.00";
  return Number(value).toFixed(2);
}

function orderStatusText(status) {
  var map = { 0: "已取消", 1: "借阅中", 2: "已归还", 3: "已评价" };
  return map[status] || "未知";
}

function orderStatusCode(status) {
  var map = { 0: "default", 1: "primary", 2: "warning", 3: "success" };
  return map[status] || "default";
}
