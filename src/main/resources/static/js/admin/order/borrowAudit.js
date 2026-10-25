// 借阅审核页面
var vm = new Vue({
  el: "#app",
  data: {
    orders: [],
    page: 1,
    limit: 10,
    detailOrder: {},
    detailBooks: [],
    auditForm: { id: null, orderNumber: "", approve: true, remark: "" },
  },
  created: function () {
    this.load();
  },
  methods: {
    formatDate: function (value) {
      return value ? String(value).substring(0, 10) : "-";
    },
    load: function () {
      var self = this;
      $.get(baseURL + "borrowAudit/list", { page: self.page, limit: self.limit }, function (r) {
        if (r.code == 0) self.orders = r.rows || [];
      });
    },
    changePage: function (delta) {
      var next = this.page + delta;
      if (next < 1) return;
      this.page = next;
      this.load();
    },
    detail: function (o) {
      var self = this;
      $.get(baseURL + "borrowAudit/info/" + o.id, function (r) {
        if (r.code != 0) {
          alert(r.msg || "查询失败");
          return;
        }
        self.detailOrder = r.order || {};
        self.detailBooks = (r.order && r.order.orderBookList) ? r.order.orderBookList : [];
        $("#detailModal").modal("show");
      });
    },
    openAudit: function (o, approve) {
      this.auditForm = {
        id: o.id,
        orderNumber: o.orderNumber,
        approve: approve,
        remark: "",
      };
      $("#auditModal").modal("show");
    },
    submitAudit: function () {
      var self = this;
      $.ajax({
        type: "POST",
        url: baseURL + "borrowAudit/audit",
        contentType: "application/json",
        data: JSON.stringify(self.auditForm),
        success: function (r) {
          $("#auditModal").modal("hide");
          if (r.code == 0) {
            alert(self.auditForm.approve ? "已通过借阅申请" : "已拒绝借阅申请", function () {
              self.load();
            });
          } else {
            alert(r.msg || "审核失败");
          }
        },
      });
    },
  },
});
