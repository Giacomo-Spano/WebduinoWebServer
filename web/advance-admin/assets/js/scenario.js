/**
 * Created by giaco on 27/10/2017.
 */
var $scenarioPanel;
var $triggerRow;
var $calendaRow;
var $programRow;
var $scenario;

var $addcalendarbutton;
var $addtriggerbutton;
var $addprogrambutton;

function getScenario(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (scenario) {
        callback(scenario);
    });
}

function getTrigger(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=trigger&id=" + id, function (trigger) {
        callback(trigger);
    });
}

function addTrigger(idx, elem) {
    var trigger = $triggerRow.clone();

    trigger.find('td[name="id"]').text(elem.id);
    trigger.find('td[name="name"]').text(elem.name);
    trigger.find('td[name="status"]').text(elem.status);

    trigger.find('button[name="edittrigger"]').attr("idx", idx);
    trigger.find('button[name="edittrigger"]').click(function () {
        var index = $(this).attr("idx");
        //loadAction($scenario.actions[index], sensors, zones, actiontypes);
    });
    trigger.find('button[name="deletetrigger"]').attr("idx", idx);
    trigger.find('button[name="deletetrigger"]').click(function () {
        var index = $(this).attr("idx");
        $scenario.triggers.splice(index, 1);
        loadTriggers($scenario.triggers);
        triggersDisableEdit(false);
    });

    $scenarioPanel.find('tbody[name="triggerlist"]').append(trigger);
}

function addProgram(idx, elem) {
    var program = $programRow.clone();


    program.find('td[name="id"]').text(elem.id);
    program.find('td[name="scenarioid"]').text(elem.scenarioid);
    program.find('td[name="name"]').text(elem.name);
    program.find('td input[name="enabled"]').prop('checked', elem.enabled);
    program.find('td input[name="startdate"]').val(elem.startdate);
    program.find('td input[name="enddate"]').val(elem.enddate);
    program.find('td[name="status"]').text(elem.status);
    program.find('td[name="nextjob"]').text(elem.nextjobdate);

    program.find('button[name="editprogram"]').attr("idx", idx);
    program.find('button[name="editprogram"]').click(function () {
        var index = $(this).attr("idx");
        loadProgram(elem);
    });
    program.find('button[name="deleteprogram"]').attr("idx", idx);
    program.find('button[name="deleteprogram"]').click(function () {
        var index = $(this).attr("idx");
        $scenario.programs.splice(index, 1);
        loadPrograms($scenario.programs);
        scenarioDisableEdit(false);
    });

    $scenarioPanel.find('tbody[name="programlist"]').append(program);
}

function addCalendar(idx, elem) {
    var calendar = $calendarRow.clone();

    calendar.find('td[name="id"]').text(elem.id);
    calendar.find('td[name="name"]').text(elem.name);
    calendar.find('td[name="status"]').text(elem.status);

    calendar.find('button[name="editcalendar"]').attr("idx", idx);
    calendar.find('button[name="editcalendar"]').click(function () {
        var index = $(this).attr("idx");
        //loadAction($scenario.actions[index], sensors, zones, actiontypes);
    });
    calendar.find('button[name="deletecalendar"]').attr("idx", idx);
    calendar.find('button[name="deletecalendar"]').click(function () {
        var index = $(this).attr("idx");
        $scenario.calendar.timeintervals.splice(index, 1);
        loadCalendars($scenario.calendar.timeintervals);
        triggersDisableEdit(false);
    });

    $scenarioPanel.find('tbody[name="calendarlist"]').append(calendar);
}

function loadTriggers(triggers) {
    var tbody = $scenarioPanel.find('tbody[name="triggerlist"]');
    tbody[0].innerHTML = "";
    if (triggers != null) {
        $.each(triggers, function (idx, elem) {
            addTrigger(idx, elem);
        });
    }
}

function loadCalendars(calendars) {
    var tbody = $scenarioPanel.find('tbody[name="calendarlist"]');
    tbody[0].innerHTML = "";
    if (calendars != null) {
        $.each(calendars, function (idx, elem) {
            addCalendar(idx, elem);
        });
    }
}

function loadPrograms(programs) {
    var tbody = $scenarioPanel.find('tbody[name="programlist"]');
    tbody[0].innerHTML = "";
    if (programs != null) {
        $.each(programs, function (idx, elem) {
            addProgram(idx, elem);
        });
    }
}

function scenarioDisableEdit(enabled) {
    $scenarioPanel.find('input').prop('disabled', enabled);
    $scenarioPanel.find('input').prop('disabled', enabled);
    $scenarioPanel.find('textarea').prop('disabled', enabled);
    $scenarioPanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $scenarioPanel.find('p[class="help-block"]').hide();
    else
        $scenarioPanel.find('p[class="help-block"]').show();


    /*$addcalendarbutton.prop('disabled', !enabled);
    $addtriggerbutton.prop('disabled', !enabled);
    $addprogrambutton.prop('disabled', !enabled);*/


    $scenarioPanel.find('button[name="deleteprogram"]').prop('disabled', enabled);
    $scenarioPanel.find('button[name="editprogram"]').prop('disabled', !enabled);




}
function addTriggerSection(scenario) {
    if (scenario.triggers != undefined)
        loadTriggers($scenario.triggers);

    var addtriggerbutton = $scenarioPanel.find('button[name="addtrigger"]');
    addtriggerbutton.hide();
    addtriggerbutton.click(function () {
        updateTriggerData(); // questo serve per aggiornare eventuali modifiche manuali
        var trigger = {
            "timerangeid": scenario.id,
            "id": 0,
            "name": "nuovo action",
            "type": "delayalarm",
            "enabled": false,
            "priority": 0,
            "thresholdvalue": 0,
            "targetvalue": 0,
            "zoneid": 0,

        };
        if (scenario.triggers == undefined) {
            var emptyArray = [];
            scenario["triggers"] = emptyArray;
        }
        scenario.triggers.push(trigger);
        loadTriggers($scenario.triggers);
        scenarioDisableEdit(false);
    });
    return addtriggerbutton;
}

function addCalendarSection(scenario) {
    if (scenario.calendar.timeintervals != undefined)
        loadCalendars($scenario.calendar.timeintervals);

    var addcalendarbutton = $scenarioPanel.find('button[name="addcalendar"]');
    addcalendarbutton.hide();
    addcalendarbutton.click(function () {
        updateTriggerData(); // questo serve per aggiornare eventuali modifiche manuali
        var trigger = {
            "timerangeid": scenario.id,
            "id": 0,
            "name": "nuovo action",
            "type": "delayalarm",
            "enabled": false,
            "priority": 0,
            "thresholdvalue": 0,
            "targetvalue": 0,
            "zoneid": 0,

        };
        if (scenario.calendar.timeintervals == undefined) {
            var emptyArray = [];
            scenario["triggers"] = emptyArray;
        }
        scenario.calendar.timeintervals.push(trigger);
        loadCalendars($scenario.calendar.timeintervals);
        scenarioDisableEdit(false);
    });
    return addcalendarbutton;
}

function addProgramsSection(scenario) {
    if (scenario.programs != undefined)
        loadPrograms($scenario.programs);

    var addprogrambutton = $scenarioPanel.find('button[name="addprogram"]');
    addprogrambutton.hide();
    addprogrambutton.click(function () {
        updateTriggerData(); // questo serve per aggiornare eventuali modifiche manuali
        var program = {
            "scenarioidid": scenario.id,
            "id": 0,
            "name": "nuovo program",
            "name": "nuovo description",
            "enabled": false,
        };
        if (scenario.programs == undefined) {
            var emptyArray = [];
            scenario["programs"] = emptyArray;
        }
        scenario.programs.push(program);
        loadPrograms($scenario.programs);
        scenarioDisableEdit(false);
    });
    return addprogrambutton;
}
function loadScenario(scenario) {

    $scenario = scenario;

    $("#result").load("scenario.html", function () {
            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                getProgram(scenario.programid, function (program) {
                    loadProgram(program);
                })
            });
            pagetitle.text('Scenario');
            notification.hide();
            notificationsuccess.hide();

            $scenarioPanel = $(this).find('div[id="panel"]');
            //$zoneSensorRow = $zonePanel.find('tr[name="row"]').clone();
            $triggerRow = $scenarioPanel.find('tr[name="triggerrow"]');
            $calendarRow = $scenarioPanel.find('tr[name="calendarrow"]');
            $programRow = $scenarioPanel.find('tr[name="programrow"]');

            $scenarioPanel.find('p[name="headingright"]').text(scenario.programid + "." + scenario.id);
            $scenarioPanel.find('input[name="timerangeabled"]').prop('checked', scenario.enabled);
            $scenarioPanel.find('input[name="index"]').val(scenario.index);
            $scenarioPanel.find('input[name="name"]').val(scenario.name);
            $scenarioPanel.find('textarea[name="description"]').val(scenario.description);
            $scenarioPanel.find('input[name="startdate"]').val(scenario.startdate);
            if (scenario.startdate != undefined)
                $scenarioPanel.find('input[name="startdate"]').datetimepicker({
                    dateFormat: "d/m/Y H:i"
                });
            if (scenario.enddate != undefined)
                $scenarioPanel.find('input[name="enddate"]').val(scenario.enddate);
            $scenarioPanel.find('input[name="enddate"]').datetimepicker({
                dateFormat: "d/m/Y H:i"
            });

            $scenarioPanel.find('input[name="priority"]').val(scenario.priority);

            // save button
            var savebutton = $scenarioPanel.find('button[name="save"]');
            savebutton.hide();
            savebutton.click(function () {

                scenario.name = $scenarioPanel.find('input[name="name"]').val();
                scenario.description = $scenarioPanel.find('textarea[name="description"]').val();
                scenario.enabled = $scenarioPanel.find('input[name="timerangeabled"]').prop('checked');
                scenario.starttime = $scenarioPanel.find('input[name="starttime"]').val();
                scenario.endtime = $scenarioPanel.find('input[name="endtime"]').val();
                updateTriggerData();

                postData("timerange", scenario, function (result, response) {
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

            var cancelbutton = $scenarioPanel.find('button[name="cancel"]');
            cancelbutton.hide();
            cancelbutton.click(function () {
                getScenario($scenario.id, function (scenario) {
                    loadScenario(scenario);
                })
            });

            var editbutton = $scenarioPanel.find('button[name="edit"]');
            editbutton.click(function () {
                savebutton.show();
                cancelbutton.show();
                editbutton.hide();
                $addcalendarbutton.show()
                $addtriggerbutton.show();
                $addprogrambutton.show();
                scenarioDisableEdit(false);
            });


            // calendar
            $addcalendarbutton = addCalendarSection(scenario);
            // triggers
            $addtriggerbutton = addTriggerSection(scenario);
            // programs
            $addprogrambutton = addProgramsSection(scenario);

            scenarioDisableEdit(true);
        }
    );
}

function updateTriggerData() {
    if ($scenario.actions != undefined) {
        var i = 0;
        $scenarioPanel.find('tr[name="row"]').each(function (idx, elem) {
            var elem = $scenario.actions[i];
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

function scenarioDisableEdit(enabled) {
    $scenarioPanel.find('input').prop('disabled', enabled);
    $scenarioPanel.find('textarea').prop('disabled', enabled);
    $scenarioPanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $scenarioPanel.find('p[class="help-block"]').hide();
    else
        $scenarioPanel.find('p[class="help-block"]').show();

    $scenarioPanel.find('button[name="deletecalendar"]').prop('disabled', enabled);
    $scenarioPanel.find('button[name="editcalendar"]').prop('disabled', !enabled);
    $scenarioPanel.find('button[name="deletetrigger"]').prop('disabled', enabled);
    $scenarioPanel.find('button[name="edittrigger"]').prop('disabled', !enabled);
    $scenarioPanel.find('button[name="deleteprogram"]').prop('disabled', enabled);
    $scenarioPanel.find('button[name="editprogram"]').prop('disabled', !enabled);

}
