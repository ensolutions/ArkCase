'use strict';

angular.module('complaints').controller('ComplaintsListController', ['$scope', '$state', '$stateParams', '$translate'
    , 'UtilService', 'ObjectService', 'Complaint.ListService', 'Complaint.InfoService', 'Helper.ObjectBrowserService'
    , 'ServCommService'
    , function ($scope, $state, $stateParams, $translate
        , Util, ObjectService, ComplaintListService, ComplaintInfoService, HelperObjectBrowserService
        , ServCommService) {

        //
        // Check to see if complaint page is shown as a result returned by Frevvo
        // Reset the tree cache so that new entry created by Frevvo can be shown.
        // This is a temporary solution until UI and backend communication is implemented
        //
        var topics = ["new-complaint", "close-complaint"];
        _.each(topics, function (topic) {
            var data = ServCommService.popRequest("frevvo", topic);
            if (data) {
                ComplaintListService.resetComplaintsTreeData();
            }
        });


        //"treeConfig", "treeData", "onLoad", and "onSelect" will be set by Tree Helper
        new HelperObjectBrowserService.Tree({
            scope: $scope
            , state: $state
            , stateParams: $stateParams
            , moduleId: "complaints"
            , resetTreeData: function () {
                return ComplaintListService.resetComplaintsTreeData();
            }
            , getTreeData: function (start, n, sort, filters, query) {
                return ComplaintListService.queryComplaintsTreeData(start, n, sort, filters, query);
            }
            , getNodeData: function (complaintId) {
                return ComplaintInfoService.getComplaintInfo(complaintId);
            }
            , makeTreeNode: function (complaintInfo) {
                return {
                    nodeId: Util.goodValue(complaintInfo.complaintId, 0)
                    , nodeType: ObjectService.ObjectTypes.COMPLAINT
                    , nodeTitle: Util.goodValue(complaintInfo.complaintTitle)
                    , nodeToolTip: Util.goodValue(complaintInfo.complaintTitle)
                };
            }
        });

    }
]);