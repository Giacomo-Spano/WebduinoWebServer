/**
 * Created by giaco on 27/10/2017.
 */
const ACTION_ACTUATOR = "actuator";
const ACTION_SERVICE = "service";
const ACTION_TRIGGER = "trigger";

var $actionTypes = {
    'actuator': 'Attuatore',
    'service': 'Servizio',
    'trigger': 'Trigger'
};

var $conditionTypes = {
    'zonesensorvalue': 'Valore sensore zona',
    'zonesensorstatus': 'Stato sensore zona',
    'triggerstatus': 'Stato trigger'
};


var $programinstructionPanel;

var $conditionRow;
var $actionRow;
var $programinstruction;

function getTimerange(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=program&id=" + id, function (program) {
        callback(program);
    });
}

function addCondition(idx, elem, triggers, zones, sensors, services) {
    var condition = $conditionRow.clone();

    condition.find('td[name="id"]').text(elem.id);

    // zone
    $.each(zones, function (index, zone) {
        condition.find('td  select[name="zone"]').append(new Option(zone.name, zone.id));
    });
    condition.find('td select[name="zone"]').change(function () {
        $('option', condition.find('td select[name="zonesensor"]')).remove();
        getZone(zoneid, function (zone) {
            // zonesensor
            $.each(zone.zonesensors, function (index, elem) {
                var option = new Option(elem.name, elem.id);
                condition.find('td select[name="zonesensor"]').append($(option));
            });
            condition.find('td select[name="zonesensor"]').change(function () {
                getSensor(zonesensorid, function (sensor) {
                    if (sensor.doublevalue != null) {
                        condition.find('td input[name="value"]').prop('min', sensor.mindoublevalue);
                        condition.find('td input[name="value"]').prop('max', sensor.maxdoublevalue);
                        condition.find('td input[name="value"]').prop('step', sensor.stepdoublevalue);
                    }
                });
            });
            condition.find('td input[name="value"]').val(elem.value);
            zonesensorid = 0;
            if (elem.zonesensorid == 0 || elem.zonesensorid == null) {
                zonesensorid = zone.zonesensors[0].id;
            } else {
                zonesensorid = elem.zonesensorid;
            }
            condition.find('td  select[name="zonesensor"]').val(elem.zonesensorid).change();
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
    condition.find('td  select[name="trigger"]').val(elem.triggerid).change();

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
    condition.find('td select[name="type"]').on(function () {

    });
    condition.find('td select[name="type"]').off(function () {

    });

    $programinstructionPanel.find('tbody[name="conditionlist"]').append(condition);

    condition.find('button[name="editcondition"]').attr("idx", idx);
    condition.find('button[name="editcondition"]').click(function () {
        var index = $(this).attr("idx");
        loadProgramInstruction($programinstruction.programinstructions[index], triggers, zones, sensors, services);
    });
    condition.find('button[name="deletecondition"]').attr("idx", idx);
    condition.find('button[name="deletecondition"]').click(function () {
        var index = $(this).attr("idx");
        $programinstruction.actions.splice(index, 1);
        loadProgramInstructions($programinstruction.actions, actiontypes, zones, sensors);
        programinstructionDisableEdit(false);
    });


}


/*function getActuatorCommand(id, callback) {
 $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (scenario) {
 callback(scenario);
 });
 }*/


function addAction(idx, elem, triggers, zones, sensors, services) {
    var action = $actionRow.clone();

    action.find('td[name="id"]').text(elem.id);

    // actuator
    $.each(sensors, function (val, sensor) {
        action.find('td  select[name="actuator"]').append(new Option(sensor.name, sensor.id));
    });
    action.find('td  select[name="actuator"]').val(elem.actuatorid);

    // trigger
    $.each(triggers, function (val, trigger) {
        action.find('td  select[name="trigger"]').append(new Option(trigger.name, trigger.id));
    });
    action.find('td  select[name="trigger"]').val(elem.triggerid);

    // service
    $.each(services, function (val, service) {
        action.find('td  select[name="service"]').append(new Option(service.name, service.id));
    });
    action.find('td  select[name="service"]').val(elem.serviceid);

    // zone
    $.each(zones, function (index, zone) {
        action.find('td  select[name="zone"]').append(new Option(zone.name, zone.id));
    });
    action.find('td select[name="zone"]').change(function () {
        $('option', action.find('td select[name="zonesensor"]')).remove();
        getZone(elem.zoneid, function (zone) {
            $.each(zone.zonesensors, function (index, elem) {
                var option = new Option(elem.id, elem.name);
                action.find('td select[name="zonesensor"]').append($(option));
            });
        })

    });
    action.find('td select[name="zone"]').val(elem.zoneid);
    action.find('td select[name="zone"]').val(elem.zoneid).change();

    $.each($actionTypes, function (text, key) {
        var option = new Option(key, text);
        action.find('td select[name="type"]').append($(option));
    });
    action.find('td select[name="type"]').change(function () {
        if (this.value == 'actuator') {

            action.find('td select[name="actuator"]').prop('disabled', false);
            action.find('td select[name="trigger"]').prop('disabled', true);
            action.find('td select[name="service"]').prop('disabled', true);

            $('option', action.find('td select[name="actioncommand"]')).remove();
            getSensor(elem.actuatorid, function (sensor) {
                $.each(sensor.actioncommandlist, function (index, elem) {
                    var option = new Option(elem.command, elem.name);
                    action.find('td select[name="actioncommand"]').append($(option));
                });
            });

        } else if (this.value == 'trigger') {
            action.find('td select[name="actuator"]').prop('disabled', true);
            action.find('td select[name="trigger"]').prop('disabled', false);
            action.find('td select[name="service"]').prop('disabled', true);

        } else if (this.value == 'service') {
            action.find('td select[name="actuator"]').prop('disabled', true);
            action.find('td select[name="trigger"]').prop('disabled', true);
            action.find('td select[name="service"]').prop('disabled', false);

        }
    });
    action.find('td select[name="type"]').val(elem.type).change();

    action.find('td input[name="name"]').val(elem.name);
    action.find('td input[name="description"]').val(elem.description);


    action.find('td  input[name="targetvalue"]').val(elem.targetvalue);


    action.find('button[name="editaction"]').attr("idx", idx);
    action.find('button[name="editaction"]').click(function () {
        var index = $(this).attr("idx");
        loadAction($programinstruction.actions[index], sensors, zones, triggers, services);
    });
    action.find('button[name="deleteaction"]').attr("idx", idx);
    action.find('button[name="deleteaction"]').click(function () {
        var index = $(this).attr("idx");
        $programinstruction.actions.splice(index, 1);
        loadProgramInstructions($programinstruction.actions, triggers, zones, sensors, services);
        programinstructionDisableEdit(false);
    });

    $programinstructionPanel.find('tbody[name="actionlist"]').append(action);
}

function loadProgramActionsAndConditions(conditions, actions, triggers, zones, sensors, services) {
    var tbodycondition = $programinstructionPanel.find('tbody[name="conditionlist"]');
    tbodycondition[0].innerHTML = "";
    if (conditions != null) {
        $.each(conditions, function (idx, elem) {
            addCondition(idx, elem, triggers, zones, sensors, services);
        });
    }

    var tbodyaction = $programinstructionPanel.find('tbody[name="actionlist"]');
    tbodyaction[0].innerHTML = "";
    if (actions != null) {
        $.each(actions, function (idx, elem) {
            addAction(idx, elem, triggers, zones, sensors, services);
        });
    }
}

function programinstructionDisableEdit(enabled) {
    $programinstructionPanel.find('input').prop('disabled', enabled);
    $programinstructionPanel.find('textarea').prop('disabled', enabled);
    $programinstructionPanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $programinstructionPanel.find('p[class="help-block"]').hide();
    else
        $programinstructionPanel.find('p[class="help-block"]').show();

    //$programinstructionPanel.find('button[name="addaction"]').prop('disabled', enabled);
    //$programinstructionPanel.find('button[name="addcondition"]').prop('disabled', enabled);
    $programinstructionPanel.find('button[name="deletecondition"]').prop('disabled', enabled);
    $programinstructionPanel.find('button[name="editcondition"]').prop('disabled', !enabled);
    $programinstructionPanel.find('button[name="deleteaction"]').prop('disabled', enabled);
    $programinstructionPanel.find('button[name="editaction"]').prop('disabled', !enabled);

}
function loadProgramInstruction(programinstruction, triggers, zones, sensors, services) {

    $programinstruction = programinstruction;

    /*$.getJSON(systemServletPath + "?requestcommand=triggers", function (triggers) {
     $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
     $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
     $.getJSON(systemServletPath + "?requestcommand=services", function (services) {*/

    $("#result").load("programinstruction.html", function () {
            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                getTimerange(programinstruction.timerangeid, function (timerange) {
                    loadTimeRange(timerange);
                })
            });
            pagetitle.text('Istruzione programma');
            notification.hide();
            notificationsuccess.hide();


            $programinstructionPanel = $(this).find('div[id="panel"]');
            $conditionRow = $programinstructionPanel.find('tr[name="conditionrow"]');
            $actionRow = $programinstructionPanel.find('tr[name="actionrow"]');

            $programinstructionPanel.find('p[name="headingright"]').text(programinstruction.timerangeid + "." + programinstruction.id);
            $programinstructionPanel.find('input[name="programinstructionenabled"]').prop('checked', programinstruction.enabled);
            //$programinstructionPanel.find('input[name="index"]').val(programinstruction.index);
            $programinstructionPanel.find('input[name="name"]').val(programinstruction.name);
            $programinstructionPanel.find('textarea[name="description"]').val(programinstruction.description);
            $programinstructionPanel.find('input[name="priority"]').val(programinstruction.priority);

            // save button
            var savebutton = $programinstructionPanel.find('button[name="save"]');
            savebutton.hide();
            programinstructionDisableEdit(true);
            savebutton.click(function () {

                programinstruction.name = $programinstructionPanel.find('input[name="name"]').val();
                programinstruction.description = $programinstructionPanel.find('textarea[name="description"]').val();
                programinstruction.enabled = $programinstructionPanel.find('input[name="timerangeabled"]').prop('checked');
                programinstruction.priority = $programinstructionPanel.find('input[name="priority"]').val();
                updateConditionAndActionData();

                postData("timerange", timerange, function (result, response) {
                    if (result) {
                        notification.show();
                        notificationsuccess.find('label[name="description"]').text("timerange salvato");
                        var json = jQuery.parseJSON(response);
                        loadTimeRange(json);
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });

            var cancelbutton = $programinstructionPanel.find('button[name="cancel"]');
            cancelbutton.hide();
            cancelbutton.click(function () {
                getProgramInstruction($programinstruction.id, function (programinstruction) {
                    loadProgramInstruction(programinstruction, triggers, zones, sensors, services);
                })
            });

            var editbutton = $programinstructionPanel.find('button[name="edit"]');
            editbutton.click(function () {
                savebutton.show();
                cancelbutton.show();
                editbutton.hide();
                addconditionbutton.show();
                addactionbutton.show();
                programinstructionDisableEdit(false);
            });

            var addconditionbutton = $programinstructionPanel.find('button[name="addcondition"]');
            //addconditionbutton.hide();

            var addactionbutton = $programinstructionPanel.find('button[name="addaction"]');
            //addactionbutton.hide();

            // timeranges
            if (programinstruction.actions != undefined || programinstruction.conditions != null)
                loadProgramActionsAndConditions($programinstruction.conditions, $programinstruction.actions, triggers, zones, sensors, services);

            addconditionbutton.click(function () {
                updateConditionAndActionData(); // questo serve per aggiornare eventuali modifiche manuali
                var condition = {
                    "programinstructionid": programinstruction.id,
                    "id": 0,
                    "type": "zonesensorvalue",
                    "zoneid": null,
                    "zonesensorid": null,
                    "sensorstatus": "",
                    "triggerstatus": "",
                    "value": 0,
                    "valueoperatoir": "=",
                };
                if (programinstruction.conditions == undefined) {
                    var emptyArray = [];
                    programinstruction["conditions"] = emptyArray;
                }
                programinstruction.conditions.push(condition);

                loadProgramActionsAndConditions($programinstruction.conditions, $programinstruction.actions, triggers, zones, sensors, services);
                programinstructionDisableEdit(false);
            });

            addactionbutton.click(function () {
                updateConditionAndActionData(); // questo serve per aggiornare eventuali modifiche manuali
                var action = {
                    "programinstructionid": programinstruction.id,
                    "id": 0,
                    "type": "zonesensorvalue",
                    "zonesensorvalue": 0,
                    "zoneid": 0,
                };
                if (programinstruction.actions == undefined) {
                    var emptyArray = [];
                    programinstruction["actions"] = emptyArray;
                }
                programinstruction.actions.push(action);

                loadProgramActionsAndConditions($programinstruction.conditions, $programinstruction.actions, triggers, zones, sensors, services);
                programinstructionDisableEdit(false);
            });
        }
    );
    /*});
     });
     });
     });*/
}

function updateConditionAndActionData() {
    if ($programinstruction.conditions != undefined) {
        var i = 0;
        $programinstructionPanel.find('tr[name="conditionrow"]').each(function (idx, elem) {
            var elem = $programinstruction.actions[i];
            elem.type = $(this).find('td select[name="type"]').val();
            elem.zone = $(this).find('td select[name="zone"]').val();
            elem.zonesensor = $(this).find('td select[name="zonesensor"]').val();
            elem.trigger = $(this).find('td select[name="trigger"]').val();
            elem.sensorstatus = $(this).find('td select[name="sensorstatus"]').val();
            elem.triggerstatus = $(this).find('td select[name="triggerstatus"]').val();
            elem.value = $(this).find('td input[name="value"]').val();
            elem.valueoperator = $(this).find('td select[name="valueoperator"]').val();
            i++;
        });
    }

    if ($programinstruction.actions != undefined) {
        var i = 0;
        $programinstructionPanel.find('tr[name="actionrow"]').each(function (idx, elem) {
            var elem = $programinstruction.actions[i];
            elem.type = $(this).find('td select[name="type"]').val();
            elem.actioncommand = $(this).find('td select[name="actioncommand"]').val();
            elem.targetvalue = $(this).find('td input[name="targetvalue"]').val();
            elem.seconds = $(this).find('td input[name="seconds"]').val();
            elem.actuator = $(this).find('td select[name="actuator"]').val();
            elem.service = $(this).find('td select[name="service"]').val();
            elem.zone = $(this).find('td select[name="zone"]').val();
            elem.zonesensor = $(this).find('td select[name="zonesensor"]').val();
            elem.trigger = $(this).find('td select[name="trigger"]').val();
            elem.param = $(this).find('td input[name="param"]').val();
            i++;
        });
    }
}
