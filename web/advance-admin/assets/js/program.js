/**
 * Created by giaco on 27/10/2017.
 */
var $programPanel;
var $timerangeRow;
var $program;

function getScenario(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (scenario) {
        callback(scenario);
    });
}

function addTimeRange(idx, elem) {
    var timerange = $timerangeRow.clone();

    timerange.find('td[name="id"]').text(elem.id);
    timerange.find('td input[name="enabled"]').prop('checked', elem.enabled).prop('disabled', true);
    timerange.find('td[name="starttime"]').text(elem.starttime);
    timerange.find('td[name="endtime"]').text(elem.endtime);
    timerange.find('td[name="name"]').text(elem.name);
    timerange.find('td[name="description"]').text(elem.description);
    timerange.find('td[name="status"]').text(elem.status);
    timerange.attr("idx", idx);

    var status = "";
    if (elem.actions != undefined)
        for (var i = 0; i < elem.actions.length; i++) {
            status += elem.actions[i].status
            status += ";<br>";
        }
    timerange.find('td[name="action"]').text(status);

    /*timerange.find('button[name="edittimerange"]').attr("idx", idx);
    timerange.find('button[name="edittimerange"]').click(function () {
        var index = $(this).attr("idx");
        loadProgramTimeRange($("#result"), $program.timeranges[index])
    });*/

    $programPanel.find('tbody[name="list"]').append(timerange);

    timerange.click(function () {
        var index = $(this).attr("idx");
        loadProgramTimeRange($("#result"), $program.timeranges[index])
    });
}

function loadProgramTimeranges(timeranges) {

    var tbody = $programPanel.find('tbody[name="list"]');
    tbody[0].innerHTML = "";
    if (timeranges != null) {
        $.each(timeranges, function (idx, elem) {
            addTimeRange(idx, elem);
        });
    }
}

function loadProgram(program) {

    $program = program;

    $("#result").load("program.html", function () {
            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                getScenario(program.scenarioid, function (scenario) {
                    loadWebduinoSystemScenario(scenario);
                })

            });
            pagetitle.text('Programma');
            notification.hide();
            notificationsuccess.hide();

            $programPanel = $(this).find('div[id="panel"]');
            $programPanel.find('p[name="headingright"]').text(program.programid);
            $programPanel.find('input[name="programenabled"]').prop('checked', program.enabled);

            $programPanel.find('input[name="name"]').val(program.name);
            $programPanel.find('textarea[name="description"]').val(program.description);
            $programPanel.find('input[name="priority"]').val(program.priority);
            $programPanel.find('input[name="sunday"]').prop('checked', program.sunday);
            $programPanel.find('input[name="monday"]').prop('checked', program.monday);
            $programPanel.find('input[name="tuesday"]').prop('checked', program.tuesday);
            $programPanel.find('input[name="wednesday"]').prop('checked', program.wednesday);
            $programPanel.find('input[name="thursday"]').prop('checked', program.thursday);
            $programPanel.find('input[name="friday"]').prop('checked', program.friday);
            $programPanel.find('input[name="saturday"]').prop('checked', program.saturday);

            $timerangeRow = $programPanel.find('tr[name="row"]');

            $.getJSON(systemServletPath + "?requestcommand=nextprograms&id=" + program.id, function (nextprograms) {

            });

            // save button
            var savebutton = $programPanel.find('button[name="save"]');
            savebutton.click(function () {

                program.name = $programPanel.find('input[name="name"]').val();
                program.description = $programPanel.find('textarea[name="description"]').val();
                program.enabled = $programPanel.find('input[name="programenabled"]').prop('checked');
                program.priority = $programPanel.find('input[name="priority"]').val();
                program.sunday = $programPanel.find('input[name="sunday"]').prop('checked');
                program.monday = $programPanel.find('input[name="monday"]').prop('checked');
                program.tuesday = $programPanel.find('input[name="tuesday"]').prop('checked');
                program.wednesday = $programPanel.find('input[name="wednesday"]').prop('checked');
                program.thursday = $programPanel.find('input[name="thursday"]').prop('checked');
                program.friday = $programPanel.find('input[name="friday"]').prop('checked');
                program.saturday = $programPanel.find('input[name="saturday"]').prop('checked');

                postData("program", program, function (result, response) {
                    if (result) {
                        notification.show();
                        notificationsuccess.find('label[name="description"]').text("programma salvato");
                        var json = jQuery.parseJSON(response);
                        getScenario(json.scenarioid, function (scenario) {
                            loadWebduinoSystemScenario(scenario);
                        });
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });

            var cancelbutton = $programPanel.find('button[name="cancel"]').click(function () {
                getScenario(program.scenarioid, function (scenario) {
                    loadWebduinoSystemScenario(scenario);
                });
            });

            var deletebutton = $programPanel.find('button[name="delete"]').click(function () {
                if (program.id != 0) {
                    postData("program", program, function (result, response) {
                        if (result) {
                            getScenario(program.scenarioid, function (scenario) {
                                loadWebduinoSystemScenario(scenario);
                            });
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    },"delete");
                } else {
                    getScenario(program.scenarioid, function (scenario) {
                        loadWebduinoSystemScenario(scenario);
                    });
                }
            });

            var addbutton = $programPanel.find('button[name="addtimerange"]').click(function () {

                var timerange = {
                    "programid": $program.id,
                    "id": 0,
                    "starttime": "00:00",
                    "endtime": "23:59",
                    "name": "nuovo timerange",
                    "enabled": false,
                    "priority": 0,
                    "index": 0,
                };
                if ($program.timeranges == undefined) {
                    var emptyArray = [];
                    program["timeranges"] = emptyArray;
                }
                postData("timerange", timerange, function (result, response) {
                    if (result) {
                        var json = jQuery.parseJSON(response);
                        loadProgramTimeRange($("#result"), json);
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });

            // timeranges
            var tbody = $programPanel.find('tbody[name="list"]');
            tbody[0].innerHTML = "";
            loadProgramTimeranges($program.timeranges);

        }
    );
}

