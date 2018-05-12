/**
 * Created by giaco on 27/10/2017.
 */
var $scenarioPanel;
var $triggerRow;
var $calendaRow;
var $programRow;
var $scenario;
var $trigger;


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

function addScenarioTrigger(idx, elem) {
    var triggeritem = $triggerRow.clone();

    triggeritem.find('td[name="id"]').text(elem.id).click(function () {
        loadScenarioTrigger(elem);
    });
    triggeritem.find('td[name="triggerid"]').text(elem.triggerid).click(function () {
        loadScenarioTrigger(elem);
    });
    triggeritem.find('td[name="triggername"]').text(elem.name).click(function () {
        loadScenarioTrigger(elem);
    });
    triggeritem.find('td input[name="triggerenabled"]').prop('checked', elem.enabled).prop('disabled', true);
    triggeritem.find('td[name="value"]').text(elem.status);
    getTrigger(elem.triggerid,function (trg) {
        triggeritem.find('td[name="status"]').text(trg.status);
    })

    $scenarioPanel.find('tbody[name="triggerlist"]').append(triggeritem);
}

function addProgram(idx, elem) {
    var program = $programRow.clone();


    program.find('td[name="id"]').text(elem.id);
    program.find('td[name="scenarioid"]').text(elem.scenarioid);
    program.find('td input[name="programname"]').val(elem.name);
    program.find('td input[name="programdescription"]').val(elem.description);
    program.find('td input[name="programenabled"]').prop('checked', elem.enabled);
    if (elem.startdate != undefined)
        program.find('td input[name="programstartdate"]').val(elem.startdate);
    if (elem.enddate != undefined)
        program.find('td input[name="programenddate"]').val(elem.enddate);
    program.find('td input[name="programsunday"]').prop('checked', elem.sunday);
    program.find('td input[name="programmonday"]').prop('checked', elem.monday);
    program.find('td input[name="programtuesday"]').prop('checked', elem.tuesday);
    program.find('td input[name="programwednesday"]').prop('checked', elem.wednesday);
    program.find('td input[name="programthursday"]').prop('checked', elem.thursday);
    program.find('td input[name="programfriday"]').prop('checked', elem.friday);
    program.find('td input[name="programsaturday"]').prop('checked', elem.saturday);
    program.find('td[name="status"]').text(elem.status);
    program.find('td[name="nextjob"]').text(elem.nextjobdate);
    if (elem.activetimerange != undefined)
        program.find('td[name="activetimerange"]').text(elem.activetimerange.actionstatus);

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
        //scenarioDisableEdit(false);
    });

    $scenarioPanel.find('tbody[name="programlist"]').append(program);
}

function addCalendar(idx, elem) {
    var calendar = $calendarRow.clone();

    calendar.find('td[name="calendarid"]').text(elem.id).click(function () {
        loadScenarioTimeinterval(elem);
    });

    calendar.find('td[name="calendarname"]').text(elem.name).click(function () {
        loadScenarioTimeinterval(elem);
    });
    calendar.find('td input[name="calendarenabled"]').prop('checked', elem.enabled).prop('disabled', true);
    calendar.find('td[name="calendarstartdate"]').text(elem.startdatetime);
    calendar.find('td[name="calendarenddate"]').text(elem.enddatetime);
    calendar.find('td input[name="calendarsunday"]').prop('checked', elem.sunday).prop('disabled', true);
    calendar.find('td input[name="calendarmonday"]').prop('checked', elem.monday).prop('disabled', true);
    calendar.find('td input[name="calendartuesday"]').prop('checked', elem.tuesday).prop('disabled', true);
    calendar.find('td input[name="calendarwednesday"]').prop('checked', elem.wednesday).prop('disabled', true);
    calendar.find('td input[name="calendarthursday"]').prop('checked', elem.thursday).prop('disabled', true);
    calendar.find('td input[name="calendarfriday"]').prop('checked', elem.friday).prop('disabled', true);
    calendar.find('td input[name="calendarsaturday"]').prop('checked', elem.saturday).prop('disabled', true);

    calendar.find('td[name="calendarstatus"]').text(elem.status);

    /*calendar.find('button[name="deletecalendar"]').attr("idx", idx);
    calendar.find('button[name="deletecalendar"]').click(function () {
        var index = $(this).attr("idx");
        $scenario.calendar.timeintervals.splice(index, 1);
        loadCalendars($scenario.calendar.timeintervals);
        triggersDisableEdit(false);
    });*/

    $scenarioPanel.find('tbody[name="calendarlist"]').append(calendar);
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

    getTriggers(function (triggers) {

        if (scenario.triggers != undefined)
            loadScenarioTriggers($scenario.triggers);

        var addtriggerbutton = $scenarioPanel.find('button[name="addtrigger"]');
        addtriggerbutton.click(function () {
            updateScenarioData(); // questo serve per aggiornare eventuali modifiche manuali
            var trigger = {
                "scenarioid": scenario.id,
                "triggerid" : triggers[0].id,
                "status" : triggers[0].statuslist[0],
                "id": 0,
                "name": "nuovo action",
                "enabled": "",
            };
            postData("scenariotrigger", trigger, function (result, response) {
                if (result) {
                    var json = jQuery.parseJSON(response);
                    loadScenarioTrigger(json);
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });
    });
}

function addCalendarSection(scenario) {
    if (scenario.calendar.timeintervals != undefined)
        loadCalendars($scenario.calendar.timeintervals);

    var addcalendarbutton = $scenarioPanel.find('button[name="addcalendar"]');
    addcalendarbutton.click(function () {
        //updateScenarioData(); // questo serve per aggiornare eventuali modifiche manuali
        var timeinterval = {
            "scenarioid": scenario.id,
            "id": 0,
            "name": "nuovo action",
            "enabled": false,

        };
        if (scenario.calendar.timeintervals == undefined) {
            var emptyArray = [];
            scenario["triggers"] = emptyArray;
        }
        postData("timeinterval", timeinterval, function (result, response) {
            if (result) {
                var json = jQuery.parseJSON(response);
                loadScenarioTimeinterval(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        });
    });
    return addcalendarbutton;
}

function addProgramsSection(scenario) {
    if (scenario.programs != undefined)
        loadPrograms($scenario.programs);

    var addprogrambutton = $scenarioPanel.find('button[name="addprogram"]');
    //addprogrambutton.hide();
    addprogrambutton.click(function () {
        updateScenarioData(); // questo serve per aggiornare eventuali modifiche manuali
        var program = {
            "scenarioid": scenario.id,
            "id": 0,
            "name": "nuovo program",
            "description": "nuovo description",
            "enabled": false,
        };
        if (scenario.programs == undefined) {
            var emptyArray = [];
            scenario["programs"] = emptyArray;
        }
        /*scenario.programs.push(program);
         loadPrograms($scenario.programs);*/
        postData("program", program, function (result, response) {
            if (result) {
                var json = jQuery.parseJSON(response);
                loadProgram(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        });

    });
    return addprogrambutton;
}
function loadScenario(scenario) {

    $scenario = scenario;

    $("#result").load("scenario.html", function () {
            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                loadScenarios();
            });
            pagetitle.text('Scenario');
            notification.hide();
            notificationsuccess.hide();

            $scenarioPanel = $(this).find('div[id="panel"]');
            $triggerRow = $scenarioPanel.find('tr[name="triggerrow"]');
            $calendarRow = $scenarioPanel.find('tr[name="calendarrow"]');
            $programRow = $scenarioPanel.find('tr[name="programrow"]');

            $.getJSON(systemServletPath + "?requestcommand=triggers", function (triggers) {

                $triggers = triggers;

                $scenarioPanel.find('p[name="headingright"]').text(scenario.programid + "." + scenario.id);
                $scenarioPanel.find('input[name="scenarioenabled"]').prop('checked', scenario.enabled);
                $scenarioPanel.find('input[name="index"]').val(scenario.index);
                $scenarioPanel.find('input[name="name"]').val(scenario.name);
                $scenarioPanel.find('textarea[name="description"]').val(scenario.description);
                /*if (scenario.startdate != undefined)
                 $scenarioPanel.find('input[name="startdate"]').val(scenario.startdate);
                 $scenarioPanel.find('input[name="startdate"]').datetimepicker({
                 dateFormat: "d/m/Y H:i"
                 });
                 if (scenario.enddate != undefined)
                 $scenarioPanel.find('input[name="enddate"]').val(scenario.enddate);
                 $scenarioPanel.find('input[name="enddate"]').datetimepicker({
                 dateFormat: "d/m/Y H:i"
                 });*/

                $scenarioPanel.find('input[name="priority"]').val(scenario.priority);

                // save button
                var savebutton = $scenarioPanel.find('button[name="save"]');
                //savebutton.hide();
                savebutton.click(function () {

                    $scenario.name = $scenarioPanel.find('input[name="name"]').val();
                    $scenario.description = $scenarioPanel.find('textarea[name="description"]').val();
                    $scenario.enabled = $scenarioPanel.find('input[name="scenarioenabled"]').prop('checked');
                    $scenario.starttime = $scenarioPanel.find('input[name="startdate"]').val();
                    $scenario.endtime = $scenarioPanel.find('input[name="enddate"]').val();
                    updateScenarioData();

                    postData("scenario", scenario, function (result, response) {
                        if (result) {
                            notification.show();
                            notificationsuccess.find('label[name="description"]').text("timerange salvato");
                            var json = jQuery.parseJSON(response);
                            loadScenario(json);
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    });
                });

                var cancelbutton = $scenarioPanel.find('button[name="cancel"]');
                //cancelbutton.hide();
                cancelbutton.click(function () {
                    getScenario($scenario.id, function (scenario) {
                        loadScenario(scenario);
                    })
                });



                // calendar
                addCalendarSection(scenario);
                // triggers
                addTriggerSection(scenario);
                // programs
                addProgramsSection(scenario);


            });

        }
    );
}

function formatDate(dateObject) {
    var d = new Date(dateObject);
    var day = d.getDate();
    var month = d.getMonth() + 1;
    var year = d.getFullYear();
    if (day < 10) {
        day = "0" + day;
    }
    if (month < 10) {
        month = "0" + month;
    }
    var date = day + "/" + month + "/" + year;

    return date;
};

function updateScenarioData() {

    if ($scenario.calendar.timeintervals != undefined) {
        var i = 0;
        $scenarioPanel.find('tr[name="calendarrow"]').each(function (idx, elem) {
            var elem = $scenario.calendar.timeintervals[i];
            elem.enabled = $(this).find('td input[name="calendarenabled"]').prop('checked');
            elem.name = $(this).find('td input[name="calendarname"]').val();
            elem.description = $(this).find('td input[name="calendardescription"]').val();
            elem.monday = $(this).find('td input[name="calendarmonday"]').prop('checked');
            elem.tuesday = $(this).find('td input[name="calendartuesday"]').prop('checked');
            elem.wednesday = $(this).find('td input[name="calendarwednesday"]').prop('checked');
            elem.thursday = $(this).find('td input[name="calendarmonday"]').prop('checked');
            elem.friday = $(this).find('td input[name="calendarfriday"]').prop('checked');
            elem.saturday = $(this).find('td input[name="calendarsaturday"]').prop('checked');
            elem.sunday = $(this).find('td input[name="calendarsunday"]').prop('checked');
            //var date = $(this).find('td input[name="calendarstartdate"]').datepicker("getDate");
            //formatDate(date);
            elem.startdatetime = $(this).find('td input[name="calendarstartdate"]').datepicker().val();
            elem.enddatetime = $(this).find('td input[name="calendarenddate"]').datepicker().val();
            //calendar.find('td input[name="calendarstartdate"]').datetimepicker("setDate", elem.startdatetime);

            i++;
        });
    }

    if ($scenario.triggers != undefined) {
        var i = 0;
        $scenarioPanel.find('tr[name="triggerrow"]').each(function (idx, row) {
            var elem = $scenario.triggers[idx];
            elem.enabled = $(this).find('td input[name="triggerenabled"]').prop('checked');
            elem.triggerid = $(this).find('td select[name="triggerid"]').val();
            elem.priority = $(this).find('td input[name="triggerpriority"]').val();
            i++;
        });
    }

    if ($scenario.programs != undefined) {
        var i = 0;
        $scenarioPanel.find('tr[name="programrow"]').each(function (idx, elem) {
            var elem = $scenario.programs[i];
            elem.enabled = $(this).find('td input[name="programenabled"]').prop('checked');
            elem.name = $(this).find('td input[name="programname"]').val();
            elem.description = $(this).find('td input[name="programdescription"]').val();
            elem.monday = $(this).find('td input[name="programmonday"]').prop('checked');
            elem.tuesday = $(this).find('td input[name="programtuesday"]').prop('checked');
            elem.wednesday = $(this).find('td input[name="programwednesday"]').prop('checked');
            elem.thursday = $(this).find('td input[name="programmonday"]').prop('checked');
            elem.friday = $(this).find('td input[name="programfriday"]').prop('checked');
            elem.saturday = $(this).find('td input[name="programsaturday"]').prop('checked');
            elem.sunday = $(this).find('td input[name="programsunday"]').prop('checked');
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
