/**
 * ComplaintList.Page
 *
 * manages all dynamic created page element
 *
 * @author jwu
 */
ComplaintList.Page = {
    initialize : function() {
    }

    ,buildComplaintList: function(arr) {
        var html = "";
        if (Acm.isNotEmpty(arr)) {
            var len = arr.length;
            for (var i = 0; i < len; i++) {
                var c = arr[i];
                if (0 == i) {
                    Complaint.setComplaintId(c.complaintId);
                }

                html += "<li class='list-group-item'> <a href='#' class='thumb-sm pull-left m-r-sm'> <img src='"
                    + Acm.getContextPath() + "/resources/images/a0.png'" + "class='img-circle'> </a> "
                    + "<a href='#' class='clear text-ellipsis'> <small class='pull-right'>"
                    + Acm.getDateFromDatetime(c.created) + "</small><strong class='block'>"
                    + c.complaintTitle + "</strong><small>"
                    + c.creator + "</small></a><input type='hidden' value='" + c.complaintId + "' /> </li>";
            }
        }

        ComplaintList.Object.setHtmlUlComplaints(html);
        ComplaintList.Object.registerClickListItemEvents();

        ComplaintList.Event.doClickLnkListItem();
    }

    ,buildTableDocDocuments: function(c) {
        ComplaintList.Object.resetTableDocDocuments();
        var urlBase = Acm.getContextPath() + "/api/v1/plugin/ecm/download/byId/";

        var childObjects = c.childObjects;
        if (Acm.isNotEmpty(childObjects)) {
            var len = childObjects.length;
            for (var i = 0; i < len; i++) {
                var obj = childObjects[i];
                if (obj.targetType == "FILE") {
                    var row = "<tr class='odd gradeA'>"
                        + "<td><a href='" + urlBase + obj.targetId + "'>" + obj.targetId + "</a></td>"
//                        + "<td>" + obj.targetId + "</td>"
                        + "<td>" + obj.targetName + "</td>"
                        + "<td>" + Acm.getDateFromDatetime(obj.created) + "</td>"
                        + "<td>" + obj.creator + "</td>"
                        + "<td>" + obj.status + "</td>"
                        + "</tr>";
                    ComplaintList.Object.addRowTableDocDocuments(row);
                }
            } //for
        }
    }

};

