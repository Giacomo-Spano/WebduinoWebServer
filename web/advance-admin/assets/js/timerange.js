/**
 * Created by giaco on 27/10/2017.
 */
var $timerangePanel;
var $actionRow;
var $timerange;

function getTimerange(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=program&id=" + id, function (program) {
        callback(program);
    });
}

function addAction(idx, elem, actiontypes, zones, sensors) {
    var action = $actionRow.clone();

    action.find('td[name="id"]').text(elem.id);
    action.find('td input[name="enabled"]').prop('checked', elem.enabled);

    $.each(actiontypes, function (val, actiontype) {
        action.find('td select[name="type"]').append(new Option(actiontype.description, actiontype.instruction));
    });
    action.find('td select[name="type"]').val(elem.type).change();
    action.find('td input[name="name"]').val(elem.name);
    action.find('td input[name="description"]').val(elem.description);

    $.each(sensors, function (val, sensor) {
        action.find('td  select[name="actuatorid"]').append(new Option(sensor.name, sensor.id));
    });
    action.find('td  select[name="actuatorid"]').val(elem.actuatorid);
    action.find('td  input[name="targetvalue"]').val(elem.targetvalue);

    $.each(zones, function (val, zone) {
        action.find('td  select[name="zoneid"]').append(new Option(zone.name, zone.id));
    });
    action.find('td  select[name="zoneid"]').val(elem.zoneid);
    action.find('td  input[name="thresholdvalue"]').val(elem.thresholdvalue);
    var end = action.find('td  input[name="seconds"]').timepicker({
        timeFormat: 'HH:mm',
        interval: 15,
        minTime: '00:00',
        maxTime: '23:59',
        defaultTime: '00:00',
        startTime: '00:01',
        dynamic: true,
        dropdown: true,
        scrollbar: true
    });
    action.find('td  input[name="seconds"]').val(elem.seconds);
    action.find('td  input[name="priority"]').val(elem.priority);
    action.find('td[name="status"]').val(elem.status);
    action.find('button[name="editaction"]').attr("idx", idx);
    action.find('button[name="editaction"]').click(function () {
        var index = $(this).attr("idx");
        loadAction($timerange.actions[index], sensors, zones, actiontypes);
    });
    action.find('button[name="deleteaction"]').attr("idx", idx);
    action.find('button[name="deleteaction"]').click(function () {
        var index = $(this).attr("idx");
        $timerange.actions.splice(index, 1);
        loadActions($timerange.actions, actiontypes, zones, sensors);
        timerangeDisableEdit(false);
    });
    action.find('button[name="addaction"]').attr("idx", idx);
    action.find('button[name="addaction"]').click(function () {

        var index = $(this).attr("idx");
        updateActionData(); // questo serve per aggiornare eventuali modifiche manuali
        var action = {
            "timerangeid": timerange.id,
            "id": 0,
            "name": "nuovo action",
            "type": "delayalarm",
            "enabled": false,
            "priority": 0,
            "thresholdvalue": 0,
            "targetvalue": 0,
            "zoneid": 0,
        };
        if ($timerange.actions == undefined) {
            var emptyArray = [];
            program["timeranges"] = emptyArray;
        }
        $timerange.actions.splice(index, 0, action);
        loadTimeranges($timerange.actions);
        timerangeDisableEdit(false);
    });

    $timerangePanel.find('tbody[name="list"]').append(action);
}

function loadActions(actions, actiontypes, zones, sensors) {
    var tbody = $timerangePanel.find('tbody[name="list"]');
    tbody[0].innerHTML = "";
    if (actions != null) {
        $.each(actions, function (idx, elem) {
            addAction(idx, elem, actiontypes, zones, sensors);
        });
    }
}

function timerangeDisableEdit(enabled) {
    $timerangePanel.find('input').prop('disabled', enabled);
    $timerangePanel.find('textarea').prop('disabled', enabled);
    $timerangePanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $timerangePanel.find('p[class="help-block"]').hide();
    else
        $timerangePanel.find('p[class="help-block"]').show();

    $timerangePanel.find('button[name="addactio"]').prop('disabled', enabled);
    $timerangePanel.find('button[name="deleteaction"]').prop('disabled', enabled);
    $timerangePanel.find('button[name="editaction"]').prop('disabled', !enabled);

}
function loadTimeRange(timerange) {

    $timerange = timerange;

    $.getJSON(systemServletPath + "?requestcommand=instructiontypes", function (actiontypes) {
        $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
            $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {

                $("#result").load("timerange.html", function () {
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


                        $timerangePanel = $(this).find('div[id="panel"]');
                        //$actionRow = $timerangePanel.find('tr[name="row"]').clone();
                        $actionRow = $timerangePanel.find('tr[name="row"]');

                        $timerangePanel.find('p[name="headingright"]').text(timerange.programid + "." + timerange.id);
                        $timerangePanel.find('input[name="timerangeabled"]').prop('checked', timerange.enabled);
                        $timerangePanel.find('input[name="index"]').val(timerange.index);
                        $timerangePanel.find('input[name="name"]').val(timerange.name);
                        $timerangePanel.find('textarea[name="description"]').val(timerange.description);
                        $timerangePanel.find('input[name="starttime"]').timepicker({
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
                        if (timerange.starttime != null) $timerangePanel.find('input[name="starttime"]').val(timerange.starttime);
                        $timerangePanel.find('input[name="endtime"]').timepicker({
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

                        if (timerange.endtime != null) $timerangePanel.find('input[name="endtime"]').val(timerange.endtime);
                        $timerangePanel.find('input[name="priority"]').val(timerange.priority);

                        // save button
                        var savebutton = $timerangePanel.find('button[name="save"]');
                        savebutton.hide();
                        timerangeDisableEdit(true);
                        savebutton.click(function () {

                            timerange.name = $timerangePanel.find('input[name="name"]').val();
                            timerange.description = $timerangePanel.find('textarea[name="description"]').val();
                            timerange.enabled = $timerangePanel.find('input[name="timerangeabled"]').prop('checked');
                            timerange.starttime = $timerangePanel.find('input[name="starttime"]').val();
                            timerange.endtime = $timerangePanel.find('input[name="endtime"]').val();
                            updateActionData();

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

                        var cancelbutton = $timerangePanel.find('button[name="cancel"]');
                        cancelbutton.hide();
                        cancelbutton.click(function () {
                            getTimerange($timerange.id, function (timerange) {
                                loadTimeRange(timerange);
                            })
                        });

                        var editbutton = $timerangePanel.find('button[name="edit"]');
                        editbutton.click(function () {
                            savebutton.show();
                            cancelbutton.show();
                            editbutton.hide();
                            addbutton.show();
                            timerangeDisableEdit(false);
                        });

                        var addbutton = $timerangePanel.find('button[name="add"]');
                        addbutton.hide();

                        // timeranges
                        if (timerange.actions != undefined)
                            loadActions($timerange.actions, actiontypes, zones, sensors);

                        addbutton.click(function () {
                            updateActionData(); // questo serve per aggiornare eventuali modifiche manuali
                            var action = {
                                "timerangeid": timerange.id,
                                "id": 0,
                                "name": "nuovo action",
                                "type": "delayalarm",
                                "enabled": false,
                                "priority": 0,
                                "thresholdvalue": 0,
                                "targetvalue": 0,
                                "zoneid": 0,

                            };
                            if (timerange.actions == undefined) {
                                var emptyArray = [];
                                timerange["actions"] = emptyArray;
                            }
                            timerange.actions.push(action);
                            loadActions($timerange.actions, actiontypes, zones, sensors);
                            timerangeDisableEdit(false);
                        });
                    }
                );

            });////
        });
    });
}

function updateActionData() {
    if ($timerange.actions != undefined) {
        var i = 0;
        $timerangePanel.find('tr[name="row"]').each(function (idx, elem) {
            var elem = $timerange.actions[i];
            elem.enabled = $(this).find('td input[name="enabled"]').prop('checked');
            elem.type = $(this).find('td select[name="type"]').val();
            elem.name = $(this).find('td input[name="name"]').val();
            elem.description = $(this).find('td input[name="description"]').val();
            elem.actuatorid = $(this).find('td select[name="actuatorid"]').val();
            elem.targetvalue = $(this).find('td input[name="targetvalue"]').val();
            elem.zoneid = $(this).find('td select[name="zoneid"]').val();
            elem.thresholdvalue = $(this).find('td input[name="thresholdvalue"]').val();
            elem.priority = $(this).find('td input[name="priority"]').val();
            i++;
        });
    }
}
