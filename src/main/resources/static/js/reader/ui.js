// 读者端公共组件：顶栏、侧边栏、页脚（样式与后台保持一致，参考 admin 端实现）
Vue.component("reader-header", {
  data: function () {
    return {
      nickname: localStorage.getItem("readerName") || "读者",
    };
  },
  template:
    "<div>" +
    '<header class="main-header">' +
    '<a href="index.html" class="logo">' +
    '<span class="logo-mini"><b>借阅</b></span>' +
    '<span class="logo-lg"><b>图书借阅</b> · 读者端</span>' +
    "</a>" +
    '<nav class="navbar navbar-static-top" role="navigation">' +
    '<div class="navbar-custom-menu">' +
    '<ul class="nav navbar-nav">' +
    '<li><a href="profile.html"><i class="fa fa-user"></i> &nbsp;{{nickname}}</a></li>' +
    '<li><a href="javascript:;" @click="logout"><i class="fa fa-sign-out"></i> &nbsp;退出</a></li>' +
    "</ul>" +
    "</div>" +
    "</nav>" +
    "</header>" +
    "</div>",
  methods: {
    logout: function () {
      readerLogout();
    },
  },
});

Vue.component("reader-sidebar", {
  data: function () {
    return {
      menus: [
        { name: "图书借阅", url: "/reader/index.html", icon: "fa fa-book" },
        { name: "我的借阅", url: "/reader/order.html", icon: "fa fa-file-text-o" },
        { name: "我的押金", url: "/reader/deposit.html", icon: "fa fa-money" },
        { name: "个人中心", url: "/reader/profile.html", icon: "fa fa-user-o" },
      ],
    };
  },
  template:
    '<aside class="main-sidebar">' +
    '<section class="sidebar">' +
    '<ul class="sidebar-menu">' +
    '<li class="header">读者服务</li>' +
    '<li v-for="(item, index) in menus" :class="{active: isActive(item)}">' +
    '<a :href="item.url"><i :class="item.icon"></i> <span>{{item.name}}</span></a>' +
    "</li>" +
    "</ul>" +
    "</section>" +
    "</aside>",
  methods: {
    isActive: function (item) {
      return location.pathname.indexOf(item.url.replace("/reader/", "")) !== -1;
    },
  },
});

Vue.component("reader-footer", {
  template:
    '<footer class="main-footer">' +
    '<div class="pull-right hidden-xs">Version 1.0</div>' +
    "图书借阅管理系统 · 读者端" +
    "</footer>",
});
