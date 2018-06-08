/**
 * Created by giaco on 27/10/2017.
 */
const ACTION_ACTUATOR = "actuator";
const ACTION_SERVICE = "service";
const ACTION_TRIGGER = "trigger";

var $actionTypes = {
    'actuator': 'Attuatore',
    'service': 'Servizio',
    'trigger': 'Trigger',
    'webduinosystem': 'WebduinoSystem'
};

var $conditionTypes = {
    'zonesensorvalue': 'Valore sensore zona',
    'zonesensorstatus': 'Stato sensore zona',
    'triggerstatus': 'Stato trigger'
};

var $operatorTypes = {
    '=': 'Equals',
    '>': 'Greater',
    '<': 'Lower'
};

var $programtimerangePanel;
var $conditionRow;
var $actionRow;
var $programtimerange;
var $tag;

/*function getTimerange(id, callback) {
 $.getJSON(systemServletPath + "?requestcommand=timerange&id=" + id, function (program) {
 callback(program);
 });
 }*/

function addCondition(idx, elem, triggers, zones, sensors, services) {
    var condition = $conditionRow.clone();

    condition.find('td[name="id"]').text(elem.id);

    // zone
    $.each(zones, function (index, zone) {
        condition.find('td  select[name="zone"]').append(new Option(zone.name, zone.id));
    });
    condition.find('td select[name="zone"]').change(function () {
        var zoneid = elem.zoneid;
        $('option', condition.find('td select[name="zonesensor"]')).remove();
        getZone(zoneid, function (zone) {
            // zonesensor
            $.each(zone.zonesensors, function (index, elem) {
                var option = new Option(elem.name, elem.id);

                condition.find('td select[name="zonesensor"]').append($(option));
            });
            condition.find('td select[name="zonesensor"]').change(function () {
                getSensorFromZoneSensor(zoneid, this.value, function (sensor) {
                    //condition.find('td select[name="zonesensor"]').attr('sensorid', '' + sensor.id);
                    if (sensor.doublevalue != null) {
                        condition.find('td input[name="value"]').prop('min', sensor.mindoublevalue);
                        condition.find('td input[name="value"]').prop('max', sensor.maxdoublevalue);
                        condition.find('td input[name="value"]').prop('step', sensor.stepdoublevalue);
                    }
                    $('option', condition.find('td select[name="sensorstatus"]')).remove();
                    //sensorid = condition.find('td select[name="zonesensor"]').attr('sensorid');
                    $.each(sensor.statuslist, function (index, elem) {
                        var option = new Option(elem, elem);
                        condition.find('td select[name="sensorstatus"]').append($(option));
                    });

                    if (elem.sensorstatus == null || elem.sensorstatus == "") {
                        elem.sensorstatus = sensor.statuslist[0];
                    }
                    condition.find('td select[name="sensorstatus"]').val(elem.sensorstatus);
                })

            });
            condition.find('td input[name="value"]').val(elem.value);
            zonesensorid = 0;
            if (elem.zonesensorid == 0 || elem.zonesensorid == null) {
                zonesensorid = zone.zonesensors[0].id;
            } else {
                zonesensorid = elem.zonesensorid;
            }
            condition.find('td  select[name="zonesensor"]').val(zonesensorid).change();
        });

    });
    zoneid = 0;
    if (elem.zoneid == 0 || elem.zoneid == null) {
        zoneid = zones[0].id;
    } else {
        zoneid = elem.zoneid;
    }
    condition.find('td select[name="zone"]').val(zoneid).change();

    // trigger
    $.each(triggers, function (val, trigger) {
        condition.find('td  select[name="trigger"]').append(new Option(trigger.name, trigger.id));
    });
    condition.find('td select[name="trigger"]').change(function () {
        $('option', condition.find('td select[name="triggerstatus"]')).remove();
        getTrigger(triggerid, function (trigger) {
            $.each(trigger.statuslist, function (index, elem) {
                var option = new Option(elem, elem);
                condition.find('td select[name="triggerstatus"]').append($(option));
            });
        });
    });
    triggerid = 0;
    if (elem.triggerid == 0 || elem.triggerid == null) {
        triggerid = triggers[0].id;
    } else {
        triggerid = elem.triggerid;
    }
    condition.find('td  select[name="trigger"]').val(triggerid).change();

    // valueoperator
    $.each($operatorTypes, function (text, key) {
        var option = new Option(key, text);
        condition.find('td select[name="valueoperator"]').append($(option));

    });
    if (elem.valueoperator == null)
        elem.valueoperator = "=";
    condition.find('td select[name="valueoperator"]').val(elem.valueoperator);

    // condition types
    $.each($conditionTypes, function (text, key) {
        var option = new Option(key, text);
        condition.find('td select[name="type"]').append($(option));

    });
    condition.find('td select[name="type"]').change(function () {
        if (this.value == "zonesensorvalue" && zones.length > 0) {
            condition.find('td select[name="zone"]').prop('disabled', false);
            condition.find('td select[name="zonesensor"]').prop('disabled', false);
            condition.find('td select[name="trigger"]').prop('disabled', true);
            condition.find('td select[name="sensorstatus"]').prop('disabled', true);
            condition.find('td select[name="operatorvalue"]').prop('disabled', false);
            condition.find('td input[name="value"]').prop('disabled', false);
            condition.find('td select[name="triggerstatus"]').prop('disabled', true);
        } else if (this.value == "zonesensorstatus" && zones.length > 0) {
            condition.find('td select[name="zone"]').prop('disabled', false);
            condition.find('td select[name="zonesensor"]').prop('disabled', false);
            condition.find('td select[name="trigger"]').prop('disabled', true);
            condition.find('td select[name="sensorstatus"]').prop('disabled', false);
            condition.find('td select[name="operatorvalue"]').prop('disabled', true);
            condition.find('td input[name="value"]').prop('disabled', true);
            condition.find('td select[name="triggerstatus"]').prop('disabled', true);
        } else if (this.value == "triggerstatus" && triggers.length > 0) {
            condition.find('td select[name="zone"]').prop('disabled', true);
            condition.find('td select[name="zonesensor"]').prop('disabled', true);
            condition.find('td select[name="trigger"]').prop('disabled', false);
            condition.find('td select[name="sensorstatus"]').prop('disabled', true);
            condition.find('td select[name="operatorvalue"]').prop('disabled', true);
            condition.find('td input[name="value"]').prop('disabled', true);
            condition.find('td select[name="triggerstatus"]').prop('disabled', false);
        }
    });
    condition.find('td select[name="type"]').val(elem.type).change();

    condition.find('td[name="status"]').text(elem.status);
    $programtimerangePanel.find('tbody[name="conditionlist"]').append(condition);


    condition.find('button[name="deletecondition"]').attr("idx", idx);
    condition.find('button[name="deletecondition"]').click(function () {
        var index = $(this).attr("idx");
        $programtimerange.conditions.splice(index, 1);
        loadProgramTimeRange($tag, $programtimerange);
    });
}

function addAction(idx, elem, triggers, zones, sensors, services, webduinosystems) {
    var action = $actionRow.clone();

    action.find('td[name="id"]').text(elem.id);

    // actuator
    $.each(sensors, function (val, sensor) {
        action.find('td  select[name="actuator"]').append(new Option(sensor.name, sensor.id));
    });
    action.find('td select[name="actuator"]').change(function () {
        actuatorid = this.value;
        actioncommand = 0;
        //$('option', action.find('td select[name="actioncommand"]')).remove();
        getSensor(actuatorid, function (sensor) {
                handleActionCommandList.call(this, sensor, action, elem);
            }
        )
        ;
    });
    actuatorid = 0;
    if (elem.actuatorid == 0 || elem.actuatorid == null) {
        actuatorid = sensors[0].id;
    } else {
        actuatorid = elem.actuatorid;
    }
    action.find('td select[name="actuator"]').val(actuatorid);
    if (elem.type == "actuator")
        action.find('td select[name="actuator"]').change();

    // trigger
    $.each(triggers, function (val, trigger) {
        action.find('td  select[name="trigger"]').append(new Option(trigger.name, trigger.id));
    });
    action.find('td select[name="trigger"]').change(function () {
        triggerid = this.value;
        $('option', action.find('td select[name="actioncommand"]')).remove();
        getTrigger(triggerid, function (trigger) {
            handleActionCommandList.call(this, trigger, action, elem);
        });
    });
    triggerid = 0;
    if (elem.triggerid == 0 || elem.triggerid == null) {
        triggerid = triggers[0].id;
    } else {
        triggerid = elem.triggerid;
    }
    action.find('td select[name="trigger"]').val(triggerid);
    if (elem.type == "trigger")
        action.find('td select[name="trigger"]').change();

    // webduinosystem
    $.each(webduinosystems, function (val, webduinosystem) {
        action.find('td  select[name="webduinosystem"]').append(new Option(webduinosystem.name, webduinosystem.id));
    });
    action.find('td select[name="webduinosystem"]').change(function () {
        webduinosystemid = this.value;
        $('option', action.find('td select[name="actioncommand"]')).remove();
        getWebduinoSystem(webduinosystemid, function (webduinosystem) {
            handleActionCommandList.call(this, webduinosystem, action, elem);
        });
    });
    webduinosystemid = 0;
    if (elem.webduinosystemid == 0 || elem.webduinosystemid == null) {
        webduinosystemid = webduinosystems[0].id;
    } else {
        webduinosystemid = elem.webduinosystemid;
    }
    action.find('td select[name="webduinosystem"]').val(triggerid);
    if (elem.type == "webduinosystem")
        action.find('td select[name="webduinosystem"]').change();

    // service
    $.each(services, function (val, service) {
        action.find('td  select[name="service"]').append(new Option(service.name, service.id));
    });
    action.find('td select[name="service"]').change(function () {
        serviceid = this.value;
        $('option', action.find('td select[name="actioncommand"]')).remove();
        getService(serviceid, function (service) {
                handleActionCommandList.call(this, service, action, elem);
            }
        );
    });
    serviceid = 0;
    if (elem.serviceid == 0 || elem.serviceid == null) {
        serviceid = services[0].id;
    } else {
        serviceid = elem.serviceid;
    }
    action.find('td select[name="service"]').val(serviceid);
    if (elem.type == "service")
        action.find('td select[name="service"]').change();


    // zone
    $.each(zones, function (index, zone) {
        action.find('td  select[name="zone"]').append(new Option(zone.name, zone.id));
    });
    action.find('td select[name="zone"]').change(function () {
        zoneid = this.value;
        $('option', action.find('td select[name="zonesensor"]')).remove();
        getZone(zoneid, function (zone) {
            $.each(zone.zonesensors, function (index, elem) {
                var option = new Option(elem.name, elem.id);
                action.find('td select[name="zonesensor"]').append($(option));
            });
            action.find('td select[name="zone"]').change(function () {
                zonesensorid = this.value;
            });
            //zonesensorid = 0;
            if (elem.zonesensorid == null || elem.zonesensorid == 0) {
                zonesensorid = zone.zonesensors[0].id;
            } else {
                zonesensorid = elem.zonesensorid;
            }
            action.find('td  select[name="zonesensor"]').val(zonesensorid).change();
        })

    });
    zoneid = 0;
    if (elem.zoneid == 0 || elem.zoneid == null) {
        zoneid = zones[0].id;
    } else {
        zoneid = elem.zoneid;
    }
    action.find('td select[name="zone"]').val(zoneid).change();

    $.each($actionTypes, function (text, key) {
        var option = new Option(key, text);
        action.find('td select[name="type"]').append($(option));
    });
    action.find('td select[name="type"]').change(function () {
        if (this.value == 'actuator') {
            action.find('td select[name="actuator"]').prop('disabled', false);
            action.find('td select[name="trigger"]').prop('disabled', true);
            action.find('td select[name="webduinosystem"]').prop('disabled', true);
            action.find('td select[name="service"]').prop('disabled', true);
            action.find('td select[name="actuator"]').val(actuatorid).change();
        } else if (this.value == 'trigger') {
            action.find('td select[name="actuator"]').prop('disabled', true);
            action.find('td select[name="trigger"]').prop('disabled', false);
            action.find('td select[name="webduinosystem"]').prop('disabled', true);
            action.find('td select[name="service"]').prop('disabled', true);
            action.find('td select[name="trigger"]').val(triggerid).change();
        } else if (this.value == 'service') {
            action.find('td select[name="actuator"]').prop('disabled', true);
            action.find('td select[name="trigger"]').prop('disabled', true);
            action.find('td select[name="webduinosystem"]').prop('disabled', true);
            action.find('td select[name="service"]').prop('disabled', false);
            action.find('td select[name="service"]').val(serviceid).change();
        } else if (this.value == 'webduinosystem') {
            action.find('td select[name="actuator"]').prop('disabled', true);
            action.find('td select[name="trigger"]').prop('disabled', true);
            action.find('td select[name="webduinosystem"]').prop('disabled', false);
            action.find('td select[name="service"]').prop('disabled', true);
            action.find('td select[name="webduinosystem"]').val(webduinosystemid).change();
        }
    });
    action.find('td select[name="type"]').val(elem.type).change();

    action.find('td[name="status"]').text(elem.status);
    $programtimerangePanel.find('tbody[name="actionlist"]').append(action);

    action.find('button[name="deleteaction"]').attr("idx", idx);
    action.find('button[name="deleteaction"]').click(function () {
        var index = $(this).attr("idx");
        $programtimerange.actions.splice(index, 1);
        loadProgramTimeRange($tag, $programtimerange);
    });
}

function handleActionCommandList(sensor, action, elem) {
    $('option', action.find('td select[name="actioncommand"]')).remove();
    if (sensor.actioncommandlist.length != null && sensor.actioncommandlist.length > 0) {
        action.find('td select[name="actioncommand"]').prop('disabled', false);
        var index = 0;
        $.each(sensor.actioncommandlist, function (index, elem) {
            var option = new Option(elem.name, elem.command);
            action.find('td select[name="actioncommand"]').append($(option));
        });
        action.find('td select[name="actioncommand"]').change(function () {
            for (i = 0; i < sensor.actioncommandlist.length; i++) {
                if (sensor.actioncommandlist[i].command == this.value) {
                    actioncommand = sensor.actioncommandlist[i];
                    if (actioncommand.zone) {
                        action.find('td select[name="zone"]').prop('disabled', false);
                        action.find('td select[name="zonesensor"]').prop('disabled', false);
                        action.find('td select[name="zone"]').val(zoneid).change();
                    } else {
                        action.find('td select[name="zone"]').prop('disabled', true);
                        action.find('td select[name="zonesensor"]').prop('disabled', true);
                    }
                    if (actioncommand.targetvalue) {
                        action.find('td input[name="value"]').prop('disabled', false);
                    } else {
                        action.find('td input[name="value"]').prop('disabled', true);
                    }
                    if (actioncommand.duration) {
                        action.find('td input[name="seconds"]').prop('disabled', false);
                    } else {
                        action.find('td input[name="seconds"]').prop('disabled', true);
                    }
                    if (actioncommand.param) {
                        action.find('td input[name="param "]').prop('disabled', false);
                    } else {
                        action.find('td input[name="param"]').prop('disabled', true);
                    }
                    break;
                }
            }
        });
        actioncommand = elem.actioncommand;//sensor.actioncommandlist[this.value];
        if (elem.actioncommand == null || elem.actioncommand == "")
            actioncommand = sensor.actioncommandlist[0];
        action.find('td select[name="actioncommand"]').val(actioncommand).change();
    } else {
        action.find('td select[name="actioncommand"]').prop('disabled', true);
        action.find('td select[name="zone"]').prop('disabled', true);
        action.find('td select[name="zonesensor"]').prop('disabled', true);
        action.find('td input[name="value"]').prop('disabled', true);
        action.find('td input[name="seconds"]').prop('disabled', true);
        action.find('td input[name="param"]').prop('disabled', true);
    }
}

function loadActionsAndConditions(conditions, actions, triggers, zones, sensors, services, webduinosystems) {
    var tbodycondition = $programtimerangePanel.find('tbody[name="conditionlist"]');
    tbodycondition[0].innerHTML = "";
    if (conditions != undefined) {
        $.each(conditions, function (idx, elem) {
            addCondition(idx, elem, triggers, zones, sensors, services);
        });
    }

    var tbodyaction = $programtimerangePanel.find('tbody[name="actionlist"]');
    tbodyaction[0].innerHTML = "";
    if (actions != undefined) {
        $.each(actions, function (idx, elem) {
            addAction(idx, elem, triggers, zones, sensors, services, webduinosystems);
        });
    }
}

function saveProgramTimerange(timerange) {
    timerange.name = $programtimerangePanel.find('input[name="name"]').val();
    timerange.description = $programtimerangePanel.find('textarea[name="description"]').val();
    timerange.enabled = $programtimerangePanel.find('input[name="programtimerangeenabled"]').prop('checked');
    timerange.starttime = $programtimerangePanel.find('input[name="starttime"]').val();
    timerange.endtime = $programtimerangePanel.find('input[name="endtime"]').val();
    timerange.priority = $programtimerangePanel.find('input[name="priority"]').val();

    updateConditionAndActionData();
    var newitem = (timerange.id == 0);
    postData("timerange", timerange, function (result, response) {
        if (result) {
            var json = jQuery.parseJSON(response);
            if (newitem) {
                notification.show();
                notificationsuccess.find('label[name="description"]').text("programma salvato");
                loadProgramTimeRange($tag, json);
            } else {
                getProgram(json.programid, function (program) {
                    loadProgram(program);
                });
            }
        } else {
            notification.show();
            notification.find('label[name="description"]').text(response);
        }
    });
}
function loadProgramTimeRange(tag, timerange) {

    $tag = tag;
    $programtimerange = timerange;

    $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
        $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
            $.getJSON(systemServletPath + "?requestcommand=triggers", function (triggers) {
                $.getJSON(systemServletPath + "?requestcommand=services", function (services) {
                    $.getJSON(systemServletPath + "?requestcommand=webduinosystems", function (webduinosystems) {
                        /*$("#result")*/
                        tag.load("programtimerange.html", function () {
                                // back button
                                backbutton.unbind("click");
                                backbutton.click(function () {
                                    getProgram(timerange.programid, function (program) {
                                        loadProgram(program);
                                    })
                                });
                                pagetitle.text('Timerange');
                                notification.hide();
                                notificationsuccess.hide();


                                $programtimerangePanel = $(this).find('div[id="panel"]');
                                $conditionRow = $programtimerangePanel.find('tr[name="conditionrow"]');
                                $actionRow = $programtimerangePanel.find('tr[name="actionrow"]');

                                $programtimerangePanel.find('p[name="headingright"]').text(timerange.programid + "." + timerange.id);
                                $programtimerangePanel.find('input[name="programtimerangeenabled"]').prop('checked', timerange.enabled);
                                $programtimerangePanel.find('input[name="index"]').val(timerange.index);
                                $programtimerangePanel.find('input[name="name"]').val(timerange.name);
                                $programtimerangePanel.find('textarea[name="description"]').val(timerange.description);
                                $programtimerangePanel.find('input[name="starttime"]').timepicker({
                                    timeFormat: 'HH:mm',
                                    interval: 15,
                                    minTime: '00:00',
                                    maxTime: '23:59',
                                    defaultTime: '00:00',
                                    startTime: '00:00',
                                    dynamic: true,
                                    dropdown: true,
                                    scrollbar: true
                                });
                                if (timerange.starttime != null) $programtimerangePanel.find('input[name="starttime"]').val(timerange.starttime);
                                $programtimerangePanel.find('input[name="endtime"]').timepicker({
                                    timeFormat: 'HH:mm',
                                    interval: 15,
                                    minTime: '00:00',
                                    maxTime: '23:59',
                                    defaultTime: '23:59',
                                    startTime: '00:00',
                                    dynamic: true,
                                    dropdown: true,
                                    scrollbar: true
                                });

                                if (timerange.endtime != null) $programtimerangePanel.find('input[name="endtime"]').val(timerange.endtime);
                                $programtimerangePanel.find('input[name="priority"]').val(timerange.priority);

                                // save button
                                var savebutton = $programtimerangePanel.find('button[name="save"]');
                                savebutton.click(function () {
                                    saveProgramTimerange(timerange);
                                });

                                var cancelbutton = $programtimerangePanel.find('button[name="cancel"]');
                                cancelbutton.click(function () {
                                    getProgram(timerange.programid, function (program) {
                                        loadProgram(program);
                                    })
                                });

                                var deletebutton = $programtimerangePanel.find('button[name="delete"]').click(function () {
                                    postData("timerange", timerange, function (result, response) {
                                        if (result) {
                                            getProgram(timerange.programid, function (program) {
                                                loadProgram(program);
                                            });
                                        } else {
                                            notification.show();
                                            notification.find('label[name="description"]').text(response);
                                        }
                                    }, "delete");

                                });
                                if (timerange.id == 0)
                                    deletebutton.prop('disabled', true);

                                // sctions and conditions
                                //if (timerange.actions != undefined || timerange.conditions != null)
                                loadActionsAndConditions($programtimerange.conditions, $programtimerange.actions, triggers, zones, sensors, services, webduinosystems);
                                var addconditionbutton = $programtimerangePanel.find('button[name="addcondition"]');
                                addconditionbutton.click(function () {
                                    updateConditionAndActionData(); // questo serve per aggiornare eventuali modifiche manuali
                                    var condition = {
                                        "programinstructionid": timerange.id,
                                        "id": 0,
                                        "type": "zonesensorvalue",
                                        "zoneid": 0,
                                        "zonesensorid": 0,
                                        "sensorstatus": "",
                                        "triggerstatus": "",
                                        "value": 0,
                                        "valueoperatoir": "=",
                                    };
                                    if (timerange.conditions == undefined) {
                                        var emptyArray = [];
                                        timerange["conditions"] = emptyArray;
                                    }
                                    timerange.conditions.push(condition);
                                    loadActionsAndConditions($programtimerange.conditions, $programtimerange.actions, triggers, zones, sensors, services, webduinosystems);
                                });

                                var addactionbutton = $programtimerangePanel.find('button[name="addaction"]');
                                addactionbutton.click(function () {
                                    updateConditionAndActionData(); // questo serve per aggiornare eventuali modifiche manuali
                                    var action = {
                                        "programinstructionid": timerange.id,
                                        "id": 0,
                                        "type": "actuator",
                                        "zonesensorvalue": 0,
                                        "zoneid": 0,
                                    };
                                    if (timerange.actions == undefined) {
                                        var emptyArray = [];
                                        timerange["actions"] = emptyArray;
                                    }
                                    timerange.actions.push(action);
                                    loadActionsAndConditions($programtimerange.conditions, $programtimerange.actions, triggers, zones, sensors, services, webduinosystems);
                                });
                            }
                        );
                    });
                });
            });
        });
    });

}

function updateConditionAndActionData() {
    if ($programtimerange.conditions != undefined) {
        var i = 0;
        $programtimerangePanel.find('tr[name="conditionrow"]').each(function (idx, elem) {
            var elem = $programtimerange.conditions[i];
            elem.type = $(this).find('td select[name="type"]').val();
            elem.zoneid = parseInt($(this).find('td select[name="zone"]').val());
            elem.zonesensorid = parseInt($(this).find('td select[name="zonesensor"]').val());
            elem.triggerid = parseInt($(this).find('td select[name="trigger"]').val());
            elem.sensorstatus = $(this).find('td select[name="sensorstatus"]').val();
            elem.triggerstatus = $(this).find('td select[name="triggerstatus"]').val();
            elem.value = parseFloat($(this).find('td input[name="value"]').val());
            elem.valueoperator = $(this).find('td select[name="valueoperator"]').val();
            i++;
        });
    }

    if ($programtimerange.actions != undefined) {
        var i = 0;
        $programtimerangePanel.find('tr[name="actionrow"]').each(function (idx, elem) {
            var elem = $programtimerange.actions[i];
            elem.type = $(this).find('td select[name="type"]').val();
            elem.actioncommand = $(this).find('td select[name="actioncommand"]').val();
            elem.targetvalue = $(this).find('td input[name="targetvalue"]').val();
            elem.seconds = parseInt($(this).find('td input[name="seconds"]').val());
            elem.actuatorid = parseInt($(this).find('td select[name="actuator"]').val());
            elem.serviceid = parseInt($(this).find('td select[name="service"]').val());
            elem.zoneid = parseInt($(this).find('td select[name="zone"]').val());
            elem.zonesensorid = $(this).find('td select[name="zonesensor"]').val();
            elem.triggerid = parseInt($(this).find('td select[name="trigger"]').val());
            elem.param = $(this).find('td input[name="param"]').val();
            i++;
        });
    }
}
