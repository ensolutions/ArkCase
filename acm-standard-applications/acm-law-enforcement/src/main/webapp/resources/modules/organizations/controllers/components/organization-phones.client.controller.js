'use strict';

angular.module('organizations').controller('Organizations.PhonesController', ['$scope', '$q', '$stateParams', '$translate', '$modal'
    , 'UtilService', 'ObjectService', 'Organization.InfoService', 'Authentication'
    , 'Helper.UiGridService', 'Helper.ObjectBrowserService', 'PermissionsService', 'Object.LookupService'
    , function ($scope, $q, $stateParams, $translate, $modal
        , Util, ObjectService, OrganizationInfoService, Authentication
        , HelperUiGridService, HelperObjectBrowserService, PermissionsService, ObjectLookupService) {


        Authentication.queryUserInfo().then(
            function (userInfo) {
                $scope.userId = userInfo.userId;
                return userInfo;
            }
        );

        var componentHelper = new HelperObjectBrowserService.Component({
            scope: $scope
            , stateParams: $stateParams
            , moduleId: "organizations"
            , componentId: "phones"
            , retrieveObjectInfo: OrganizationInfoService.getOrganizationInfo
            , validateObjectInfo: OrganizationInfoService.validateOrganizationInfo
            , onConfigRetrieved: function (componentConfig) {
                return onConfigRetrieved(componentConfig);
            }
            , onObjectInfoRetrieved: function (objectInfo) {
                onObjectInfoRetrieved(objectInfo);
            }
        });

        var gridHelper = new HelperUiGridService.Grid({scope: $scope});

        var promiseUsers = gridHelper.getUsers();

        var onConfigRetrieved = function (config) {
            $scope.config = config;
            PermissionsService.getActionPermission('editOrganization', $scope.objectInfo, {objectType: ObjectService.ObjectTypes.ORGANIZATION}).then(function (result) {
                if (result) {
                    gridHelper.addButton(config, "edit");
                    gridHelper.addButton(config, "delete", null, null, "isDefault");
                }
            });
            gridHelper.setColumnDefs(config);
            gridHelper.setBasicOptions(config);
            gridHelper.disableGridScrolling(config);
            gridHelper.setUserNameFilterToConfig(promiseUsers, config);
        };

        var onObjectInfoRetrieved = function (objectInfo) {
            $scope.objectInfo = objectInfo;
            var phones = _.filter($scope.objectInfo.contactMethods, {type: 'phone'});
            $scope.gridOptions.data = phones;
        };

        ObjectLookupService.getSubContactMethodType('email').then(
            function (contactMethodTypes) {
                $scope.phoneTypes = contactMethodTypes;
                return contactMethodTypes;
            });

        $scope.addNew = function () {
            var phone = {};
            phone.created = Util.dateToIsoString(new Date());
            phone.creator = $scope.userId;
            phone.className = "com.armedia.acm.plugins.addressable.model.ContactMethod";

            //put contactMethod to scope, we will need it when we return from popup
            $scope.phone = phone;
            var item = {
                id: '',
                parentId: $scope.objectInfo.id,
                type: 'phone',
                subType: '',
                value: '',
                description: ''
            };
            showModal(item, false);
        };

        $scope.editRow = function (rowEntity) {
            $scope.phone = rowEntity;
            var item = {
                id: rowEntity.id,
                type: rowEntity.type,
                subType: rowEntity.subType,
                value: rowEntity.value,
                description: rowEntity.description
            };
            showModal(item, true);

        };

        $scope.deleteRow = function (rowEntity) {
            var id = Util.goodMapValue(rowEntity, "id", 0);
            if (0 < id) {    //do not need to call service when deleting a new row with id==0
                $scope.objectInfo.contactMethods = _.remove($scope.objectInfo.contactMethods, function (item) {
                    return item.id != id;
                });
                saveObjectInfoAndRefresh()
            }
        };

        function showModal(phone, isEdit) {
            var params = {};
            params.phone = phone || {};
            params.isEdit = isEdit || false;
            params.isDefault = $scope.isDefault(phone);

            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'modules/organizations/views/components/organization-phones-modal.client.view.html',
                controller: 'Organizations.PhonesModalController',
                size: 'md',
                backdrop: 'static',
                resolve: {
                    params: function () {
                        return params;
                    }
                }
            });
            modalInstance.result.then(function (data) {
                var phone;
                if (!data.isEdit)
                    phone = $scope.phone;
                else {
                    phone = _.find($scope.objectInfo.contactMethods, {id: data.phone.id});
                }
                phone.type = 'phone';
                phone.subType = data.phone.subLookup;
                phone.value = data.phone.value;
                phone.description = data.phone.description;

                if (!data.isEdit) {
                    $scope.objectInfo.contactMethods.push(phone);
                }

                var phones = _.filter($scope.objectInfo.contactMethods, {type: 'phone'});
                if (data.isDefault || phones.length == 1) {
                    $scope.objectInfo.defaultPhone = phone;
                }

                saveObjectInfoAndRefresh();
            });
        }

        function saveObjectInfoAndRefresh() {
            var promiseSaveInfo = Util.errorPromise($translate.instant("common.service.error.invalidData"));
            if (OrganizationInfoService.validateOrganizationInfo($scope.objectInfo)) {
                var objectInfo = Util.omitNg($scope.objectInfo);
                promiseSaveInfo = OrganizationInfoService.saveOrganizationInfo(objectInfo);
                promiseSaveInfo.then(
                    function (objectInfo) {
                        $scope.$emit("report-object-updated", objectInfo);
                        return objectInfo;
                    }
                    , function (error) {
                        $scope.$emit("report-object-update-failed", error);
                        return error;
                    }
                );
            }
            return promiseSaveInfo;
        }

        $scope.isDefault = function (data) {
            var id = 0;
            if ($scope.objectInfo.defaultPhone) {
                id = $scope.objectInfo.defaultPhone.id
            }
            var phones = _.filter($scope.objectInfo.contactMethods, {type: 'phone'});
            if (phones && phones.length == 0) {
                return true;
            }
            return data.id == id;
        };
    }
]);