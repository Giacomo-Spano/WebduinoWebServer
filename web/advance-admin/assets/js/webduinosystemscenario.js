/**
 * Created by giaco on 27/10/2017.
 */
var $scenarioPanel;
var $triggerRow;
var $calendaRow;
var $programRow;
var $scenario;
var $trigger;


function getWebduinoSystemScenario(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=webduinosystemscenario&id=" + id, function (scenario) {
        callback(scenario);
    });
}

function getTrigger(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=trigger&id=" + id, function (trigger) {
        callback(trigger);
    });
}

function addScenarioTrigger(idx, elem) {
    var triggeritem = $triggerRow.clone();
    triggeritem.find('td[name="id"]').text(elem.id);
    triggeritem.find('td input[name="triggerenabled"]').prop('checked', elem.enabled).prop('disabled', true);
    triggeritem.find('td[name="triggerid"]').text(elem.triggerid);
    triggeritem.find('td[name="triggername"]').text(elem.name);
    triggeritem.find('td[name="activestatus"]').text(elem.activestatus);
    triggeritem.find('td[name="triggerstatus"]').text(elem.triggerstatus);
    triggeritem.find('td[name="status"]').text(elem.status);
    triggeritem.attr("idx", idx);

    triggeritem.click(function () {
        var index = $(this).attr("idx");
        loadScenarioTrigger($scenario.triggers[index]);
    });
    $scenarioPanel.find('tbody[name="triggerlist"]').append(triggeritem);
}

function addProgram(idx, elem) {
    var program = $programRow.clone();
    program.find('td[name="id"]').text(elem.id);
    program.find('td[name="scenarioid"]').text(elem.scenarioid);
    program.find('td[name="programname"]').text(elem.name);
    program.find('td[name="programdescription"]').text(elem.description);
    program.find('td input[name="programenabled"]').prop('checked', elem.enabled).prop('disabled', true);
    program.find('td input[name="programsunday"]').prop('checked', elem.sunday).prop('disabled', true);
    program.find('td input[name="programmonday"]').prop('checked', elem.monday).prop('disabled', true);
    program.find('td input[name="programtuesday"]').prop('checked', elem.tuesday).prop('disabled', true);
    program.find('td input[name="programwednesday"]').prop('checked', elem.wednesday).prop('disabled', true);
    program.find('td input[name="programthursday"]').prop('checked', elem.thursday).prop('disabled', true);
    program.find('td input[name="programfriday"]').prop('checked', elem.friday).prop('disabled', true);
    program.find('td input[name="programsaturday"]').prop('checked', elem.saturday).prop('disabled', true);
    program.find('td[name="programpriority"]').text(elem.priority);
    program.find('td[name="programstatus"]').text(elem.status);
    program.find('td[name="nextjob"]').text(elem.nextjobdate);
    program.attr("idx", idx);

    if (elem.activetimerange != undefined)
        program.find('td[name="activetimerange"]').text(elem.activetimerange.actionstatus);

    program.click(function () {
        var index = $(this).attr("idx");
        loadProgram($scenario.programs[index]);
    });
    $scenarioPanel.find('tbody[name="programlist"]').append(program);
}

function addCalendar(idx, elem) {
    var timeinterval = $calendarRow.clone();
    timeinterval.find('td[name="calendarid"]').text(elem.id);
    timeinterval.find('td[name="calendarname"]').text(elem.name);
    timeinterval.find('td input[name="calendarenabled"]').prop('checked', elem.enabled).prop('disabled', true);
    timeinterval.find('td[name="calendarstartdate"]').text(elem.startdatetime);
    timeinterval.find('td[name="calendarenddate"]').text(elem.enddatetime);
    timeinterval.find('td input[name="calendarsunday"]').prop('checked', elem.sunday).prop('disabled', true);
    timeinterval.find('td input[name="calendarmonday"]').prop('checked', elem.monday).prop('disabled', true);
    timeinterval.find('td input[name="calendartuesday"]').prop('checked', elem.tuesday).prop('disabled', true);
    timeinterval.find('td input[name="calendarwednesday"]').prop('checked', elem.wednesday).prop('disabled', true);
    timeinterval.find('td input[name="calendarthursday"]').prop('checked', elem.thursday).prop('disabled', true);
    timeinterval.find('td input[name="calendarfriday"]').prop('checked', elem.friday).prop('disabled', true);
    timeinterval.find('td input[name="calendarsaturday"]').prop('checked', elem.saturday).prop('disabled', true);
    timeinterval.find('td[name="calendarstatus"]').text(elem.status);
    timeinterval.attr("idx", idx);
    timeinterval.click(function () {
        var index = $(this).attr("idx");
        loadScenarioTimeinterval($scenario.timeintervals[index]);
    });
    $scenarioPanel.find('tbody[name="calendarlist"]').append(timeinterval);
}

function loadScenarioTriggers(triggers) {
    var tbody = $scenarioPanel.find('tbody[name="triggerlist"]');
    tbody[0].innerHTML = "";
    if (triggers != null) {
        $.each(triggers, function (idx, elem) {
            addScenarioTrigger(idx, elem);
        });
    }
}

function loadCalendars(timeintervals) {
    var tbody = $scenarioPanel.find('tbody[name="calendarlist"]');
    tbody[0].innerHTML = "";
    if (timeintervals != null && timeintervals.length > 0) {
        $.each(timeintervals, function (idx, elem) {
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

function addTriggerSection(scenario) {
    if (scenario.triggers != undefined)
        loadScenarioTriggers($scenario.triggers);

    var addbutton = $scenarioPanel.find('button[name="addtrigger"]').click(function () {

        getTriggers(function (trglist) {
            var trigger = {
                "scenarioid": scenario.id,
                "triggerid": trglist[0].id,
                "status": trglist[0].statuslist[0],
                "id": 0,
                "name": "nuovo trigger",
                "priority": 0,
                "enabled": "true",
            };
            loadScenarioTrigger(trigger);
        })


    });
    if (scenario.id == 0)
        addbutton.prop('disabled', true);
}

function addCalendarSection(scenario) {
    if (scenario.timeintervals != undefined)
        loadCalendars($scenario.timeintervals);
    var addbutton = $scenarioPanel.find('button[name="addcalendar"]').click(function () {
        var timeinterval = {
            "scenarioid": scenario.id,
            "id": 0,
            "priority": 0,
            "name": "nuovo calendario",
            "enabled": true,
        };
        if (scenario.timeintervals == undefined) {
            var emptyArray = [];
            scenario["timeintervals"] = emptyArray;
        }
        loadScenarioTimeinterval(timeinterval);
    });
    if (scenario.id == 0)
        addbutton.prop('disabled', true);
}

function addProgramsSection(scenario) {
    if (scenario.programs != undefined)
        loadPrograms($scenario.programs);
    var addbutton = $scenarioPanel.find('button[name="addprogram"]').click(function () {
        var program = {
            "scenarioid": scenario.id,
            "id": 0,
            "priority": 0,
            "name": "nuovo program",
            "description": "nuovo description",
            "enabled": true,
        };
        /*if (scenario.programs == undefined) {
         var emptyArray = [];
         scenario["programs"] = emptyArray;
         }*/
        loadProgram(program);
    });
    if (scenario.id == 0)
        addbutton.prop('disabled', true);
}

function saveWebduinoSystemScenario(scenario) {
    $scenario.name = $scenarioPanel.find('input[name="name"]').val();
    $scenario.description = $scenarioPanel.find('textarea[name="description"]').val();
    $scenario.enabled = $scenarioPanel.find('input[name="scenarioenabled"]').prop('checked');
    $scenario.starttime = $scenarioPanel.find('input[name="startdate"]').val();
    $scenario.endtime = $scenarioPanel.find('input[name="enddate"]').val();
    var newitem = (scenario.id == 0);
    postData("webduinosystemscenario", scenario, function (result, response) {
        if (result) {
            var json = jQuery.parseJSON(response);
            if (newitem) {
                loadWebduinoSystemScenario(json);
            } else {
                loadWebduinoSystem();
                var json = jQuery.parseJSON(response);
                getWebduinoSystem(json.webduinosystemid, function (system) {
                    loadWebduinoSystem(system);
                });
            }
        } else {
            notification.show();
            notification.find('label[name="description"]').text(response);
        }
    });
}
function loadWebduinoSystemScenario(scenario) {
    $scenario = scenario;
    $("#result").load("webduinosystemscenario.html", function () {
            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                getWebduinoSystem($scenario.webduinosystemid, function (system) {
                    loadWebduinoSystem(system);
                })
            });
            pagetitle.text('Scenario');
            notification.hide();
            notificationsuccess.hide();
            $scenarioPanel = $(this).find('div[id="panel"]');
            $triggerRow = $scenarioPanel.find('tr[name="triggerrow"]');
            $calendarRow = $scenarioPanel.find('tr[name="calendarrow"]');
            $programRow = $scenarioPanel.find('tr[name="programrow"]');

            //$triggers = triggers;
            $scenarioPanel.find('p[name="headingright"]').text(scenario.programid + "." + scenario.id);
            $scenarioPanel.find('input[name="scenarioenabled"]').prop('checked', scenario.enabled);
            $scenarioPanel.find('input[name="index"]').val(scenario.index);
            $scenarioPanel.find('input[name="name"]').val(scenario.name);
            $scenarioPanel.find('textarea[name="description"]').val(scenario.description);
            $scenarioPanel.find('input[name="priority"]').val(scenario.priority);
            // save button
            var savebutton = $scenarioPanel.find('button[name="save"]');
            savebutton.click(function () {
                saveWebduinoSystemScenario(scenario);
            });

            var cancelbutton = $scenarioPanel.find('button[name="cancel"]');
            cancelbutton.click(function () {
                getWebduinoSystem($scenario.webduinosystemid, function (system) {
                    loadWebduinoSystem(system);
                })
            });

            var deletebutton = $scenarioPanel.find('button[name="delete"]').click(function () {
                if (scenario.id != 0) {
                    postData("webduinosystemscenario", scenario, function (result, response) {
                        if (result) {
                            getWebduinoSystem($scenario.webduinosystemid, function (system) {
                                loadWebduinoSystem(system);
                            });
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    }, "delete");
                } else {
                    getWebduinoSystem($scenario.webduinosystemid, function (system) {
                        loadWebduinoSystem(system);
                    });
                }
            });
            if (scenario.id == 0)
                deletebutton.prop('disabled', true);

            // calendar
            addCalendarSection(scenario);
            // triggers
            addTriggerSection(scenario);
            // programs
            addProgramsSection(scenario);
        }
    );
}
