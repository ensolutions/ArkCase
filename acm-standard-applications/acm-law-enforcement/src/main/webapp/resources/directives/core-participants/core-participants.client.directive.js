'use strict';

/**
 * @ngdoc directive
 * @name global.directive:coreParticipants
 * @restrict E
 *
 * @description
 *
 * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/directives/core-participants/core-participants.client.directive.js directives/core-participants/core-participants.client.directive.js}
 *
 * The "Core-Participants" directive add participant grid functionality
 *
 * @param {Object} participantsInit object containing data for directive to work
 * @param {string} participantsInit.moduleId string for the id of the module
 * @param {string} participantsInit.componentId string for the id of the component
 * @param {function} participantsInit.retrieveObjectInfo function to retrieve objectInfo
 * @param {function} participantsInit.saveObjectInfo function to save objectInfo
 * @param {string} participantsInit.objectType string for the type of the object
 * @param {string} participantsInit.participantTitle string for the title of participants directive, can be optional
 *
 * @example
 <example>
 <file name="index.html">
 <core-participants participants-init="participantsInit"/>
 </file>
 <file name="app.js">
 angular.module('cases').controller('Cases.ParticipantsController', ['$scope', 'Case.InfoService', 'ObjectService'
 , function ($scope, CaseInfoService, ObjectService) {

 $scope.participantsInit = {
 moduleId: 'cases',
 componentId: 'participants',
 retrieveObjectInfo: CaseInfoService.getCaseInfo,
 validateObjectInfo: CaseInfoService.validateCaseInfo,
 saveObjectInfo: CaseInfoService.saveCaseInfo,
 objectType: ObjectService.ObjectTypes.CASE_FILE
 }
 }
 ]);
 </file>
 </example>
 */
angular.module('directives').directive(
        'coreParticipants',
        [ '$stateParams', '$q', '$translate', '$modal', 'Acm.StoreService', 'UtilService', 'ConfigService', 'Case.InfoService', 'LookupService', 'Object.LookupService', 'Helper.UiGridService', 'Helper.ObjectBrowserService', 'Object.ParticipantService', 'Object.ModelService', 'MessageService',
                'SearchService', 'Search.QueryBuilderService',
                function($stateParams, $q, $translate, $modal, Store, Util, ConfigService, CaseInfoService, LookupService, ObjectLookupService, HelperUiGridService, HelperObjectBrowserService, ObjectParticipantService, ObjectModelService, MessageService, SearchService, SearchQueryBuilder) {
                    return {
                        restrict: 'E',
                        scope: {
                            participantsInit: '='
                        },
                        link: function(scope, element, attrs) {

                            var typeOwningGroup = "owning group";
                            var typeAssignee = "assignee";
                            var typeOwner = "owner";

                            new HelperObjectBrowserService.Component({
                                scope: scope,
                                stateParams: $stateParams,
                                moduleId: scope.participantsInit.moduleId,
                                componentId: scope.participantsInit.componentId,
                                objectId: scope.participantsInit.objectId,
                                showReplaceChildrenParticipants: scope.participantsInit.showReplaceChildrenParticipants,
                                retrieveObjectInfo: scope.participantsInit.retrieveObjectInfo,
                                validateObjectInfo: scope.participantsInit.validateObjectInfo,
                                resetComponentData: scope.participantsInit.resetComponentData,
                                onConfigRetrieved: function(componentConfig) {
                                    return onConfigRetrieved(componentConfig);
                                },
                                onObjectInfoRetrieved: function(objectInfo) {
                                    onObjectInfoRetrieved(objectInfo);
                                }
                            });

                            var gridHelper = new HelperUiGridService.Grid({
                                scope: scope
                            });
                            var promiseUsers = gridHelper.getUsers();

                            var promiseTypes = ObjectLookupService.getParticipantTypes(scope.participantsInit.objectType).then(function(participantTypes) {
                                scope.participantTypes = participantTypes;
                                return participantTypes;
                            });

                            var onConfigRetrieved = function(config) {
                                if (!scope.participantsInit.participantsTitle)
                                    scope.participantsInit.participantsTitle = $translate.instant("common.directive.coreParticipants.title");
                                scope.config = config;
                                gridHelper.addButton(config, "edit", null, null, "isEditDisabled");
                                gridHelper.addButton(config, "delete", null, null, "isDeleteDisabled");
                                gridHelper.setColumnDefs(config);
                                gridHelper.setBasicOptions(config);
                                gridHelper.disableGridScrolling(config);
                                gridHelper.setUserNameFilterToConfig(promiseUsers);
                            };

                            var showModal = function(participant, isEdit, showReplaceChildrenParticipants) {
                                var modalScope = scope.$new();
                                participant.replaceChildrenParticipant = true;
                                modalScope.participant = participant || {};
                                modalScope.isEdit = isEdit || false;
                                modalScope.showReplaceChildrenParticipants = showReplaceChildrenParticipants || false;
                                modalScope.selectedType = participant.selectedType ? participant.selectedType : "";

                                var params = {};

                                params.owningGroup = ObjectModelService.getParticipantByType(scope.objectInfo, typeOwningGroup);

                                var modalInstance = $modal.open({
                                    scope: modalScope,
                                    animation: true,
                                    templateUrl: "directives/core-participants/core-participants-modal.client.view.html",
                                    controller: "Directives.CoreParticipantsModalController",
                                    size: 'lg',
                                    backdrop: 'static',
                                    resolve: {
                                        params: function() {
                                            return params;
                                        }
                                    }
                                });

                                modalInstance.result.then(function(data) {
                                    if (ObjectParticipantService.validateType(data.participant.participantLdapId, data.participant.participantType)) {
                                        scope.participant.id = data.participant.id;
                                        scope.participant.participantLdapId = data.participant.participantLdapId;
                                        scope.participant.participantType = data.participant.participantType;

                                        var assignee = ObjectModelService.getParticipantByType(scope.objectInfo, typeAssignee);
                                        var owner = ObjectModelService.getParticipantByType(scope.objectInfo, typeOwner);
                                        var owningGroup = "";

                                        if (data.participant.participantType == typeOwningGroup) {
                                            owningGroup = data.participant.participantLdapId;
                                        }

                                        var typeNoAccess = 'No Access';
                                        if (scope.config.typeNoAccess) {
                                            typeNoAccess = scope.config.typeNoAccess;
                                        }

                                        if (data.isEdit) {
                                            var participant = _.find(scope.objectInfo.participants, function(pa) {
                                                return Util.compare(pa.id, data.participant.id);
                                            });
                                            participant.participantLdapId = data.participant.participantLdapId;
                                            participant.id = data.participant.id;

                                            if (data.participant.participantType == typeNoAccess && assignee == data.participant.participantLdapId) {
                                                MessageService.error($translate.instant("common.directive.coreParticipants.message.error.noAccessCombo"));
                                            } else {
                                                participant.participantType = data.participant.participantType;
                                                participant.replaceChildrenParticipant = data.participant.replaceChildrenParticipant;
                                                var participantPerson = owner ? owner : assignee;
                                                if (!Util.isEmpty(participantPerson) && !Util.isEmpty(owningGroup)) {
                                                    if (!ObjectParticipantService.isParticipantMemberOfGroup(participantPerson, owningGroup)) {
                                                        _.remove(scope.objectInfo.participants, function(p) {
                                                            return p.participantLdapId == participantPerson && (p.participantType == "assignee" || p.participantType == "owner")
                                                        });
                                                    }
                                                }
                                            }
                                        } else {
                                            var participant = {};
                                            participant.participantLdapId = data.participant.participantLdapId;

                                            if (data.participant.participantType == typeNoAccess && assignee == data.participant.participantLdapId) {
                                                MessageService.error($translate.instant("common.directive.coreParticipants.message.error.noAccessCombo"));
                                            } else {
                                                participant.participantType = data.participant.participantType;
                                                participant.className = scope.config.className;
                                                participant.replaceChildrenParticipant = data.participant.replaceChildrenParticipant;
                                                scope.objectInfo.participants.push(participant);
                                            }
                                        }
                                        if (ObjectParticipantService.validateParticipants(scope.objectInfo.participants, scope.participantsInit.objectType != "FOLDER" && scope.participantsInit.objectType != "FILE")) {
                                            saveObjectInfoAndRefresh();
                                        } else {
                                            refresh();
                                        }
                                    }
                                });
                            };

                            var onObjectInfoRetrieved = function(objectInfo) {
                                scope.objectInfo = objectInfo;
                                scope.gridOptions = scope.gridOptions || {};
                                scope.gridOptions.data = objectInfo.participants;
                            };

                            scope.addNew = function() {
                                var participant = {};

                                //put participant to scope, we will need it when we return from popup
                                scope.participant = participant;
                                var item = {
                                    id: '',
                                    participantType: '',
                                    participantLdapId: '',
                                    participantTypes: scope.participantTypes,
                                    config: scope.config
                                };
                                showModal(item, false, scope.participantsInit.showReplaceChildrenParticipants);
                            };

                            scope.editRow = function(rowEntity) {
                                scope.participant = rowEntity;
                                var item = rowEntity;
                                item.participantTypes = scope.participantTypes;
                                item.config = scope.config;

                                if (rowEntity.participantLdapId === '*' || Util.isEmpty(rowEntity.participantLdapId)) {
                                    item.selectedType = rowEntity.participantType;

                                    showModal(item, true, scope.participantsInit.showReplaceChildrenParticipants);
                                } else {
                                    var participantDataPromise = ObjectParticipantService.findParticipantById(rowEntity.participantLdapId);
                                    participantDataPromise.then(function(participantData) {
                                        if (!Util.isArrayEmpty(participantData)) {
                                            item.selectedType = participantData[0].object_type_s ? participantData[0].object_type_s : "";

                                            showModal(item, true, scope.participantsInit.showReplaceChildrenParticipants);
                                        }
                                    })
                                }
                            };

                            scope.deleteRow = function(rowEntity) {
                                gridHelper.deleteRow(rowEntity);
                                var id = Util.goodMapValue(rowEntity, "id", 0);
                                if (0 < id) { //do not need to call service when deleting a new row
                                    saveObjectInfoAndRefresh();
                                }
                            }

                            scope.onClickReplaceChildrenParticipants = function() {
                                var len = scope.objectInfo.participants.length;
                                for (var i = 0; i < len; i++) {
                                    scope.objectInfo.participants[i].replaceChildrenParticipant = true;
                                }
                                saveObjectInfoAndRefresh();
                            }

                            scope.isDeleteDisabled = function(rowEntity) {
                                return !rowEntity.deletable;
                            };
                            scope.isEditDisabled = function(rowEntity) {
                                return !(rowEntity.editableUser || rowEntity.editableType);
                            };

                            var saveObjectInfoAndRefresh = function() {
                                var saveObject = Util.omitNg(scope.objectInfo);
                                saveObject.objectId = scope.participantsInit.objectId;
                                scope.participantsInit.saveObjectInfo(saveObject).then(function(objectSaved) {
                                    refresh();
                                    return objectSaved;
                                }, function(error) {
                                    MessageService.error(error.data);
                                    refresh();
                                    return error;
                                });
                            };

                            var refresh = function() {
                                scope.$emit('report-object-refreshed', scope.participantsInit.objectId ? scope.participantsInit.objectId : $stateParams.id);
                            };
                        },
                        templateUrl: 'directives/core-participants/core-participants.client.view.html'
                    }
                } ]);
