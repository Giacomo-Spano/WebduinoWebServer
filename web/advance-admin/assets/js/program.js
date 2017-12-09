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
    timerange.find('td input[name="enabled"]').prop('checked', elem.enabled);

    timerange.find('input[name="starttime"]').timepicker({
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
    if (elem.starttime != null) timerange.find('input[name="starttime"]').val(elem.starttime);

    timerange.find('input[name="endtime"]').timepicker({
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
    if (elem.endtime != null) timerange.find('input[name="endtime"]').val(elem.endtime);



    timerange.find('td input[name="name"]').val(elem.name);
    timerange.find('td input[name="description"]').val(elem.description);

    timerange.find('td[name="status"]').val(elem.status);
    timerange.find('td[name="action"]').val(elem.actionstatus);

    timerange.find('button[name="edittimerange"]').attr("idx", idx);
    timerange.find('button[name="edittimerange"]').click(function () {
        var index = $(this).attr("idx");
        loadTimeRange($program.timeranges[index]);
    });
    timerange.find('button[name="deletetimerange"]').attr("idx", idx);
    timerange.find('button[name="deletetimerange"]').click(function () {
        var index = $(this).attr("idx");
        $program.timeranges.splice(index, 1);
        loadProgramTimeranges($program.timeranges);
        programDisableEdit(false);
    });
    timerange.find('button[name="addtimerange"]').attr("idx", idx);
    timerange.find('button[name="addtimerange"]').click(function () {

        var index = $(this).attr("idx");
        updateTimerangeData(); // questo serve per aggiornare eventuali modifiche manuali
        var timerange = {
            "programid": $program.id,
            "id": 0,
            "starttime" : "00:00",
            "endtime" : "00:00",
            "name": "nuovo timerange",
            "enabled": false,
            "priority": 0,
            "index": 0,
        };
        if ($program.timeranges == undefined) {
            var emptyArray = [];
            program["timeranges"] = emptyArray;
        }
        $program.timeranges.splice(index, 0, timerange);
        loadProgramTimeranges($program.timeranges);
        programDisableEdit(false);
    });

    $programPanel.find('tbody[name="list"]').append(timerange);
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

function programDisableEdit(enabled) {
    $programPanel.find('input').prop('disabled', enabled);
    $programPanel.find('textarea').prop('disabled', enabled);
    $programPanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $programPanel.find('p[class="help-block"]').hide();
    else
        $programPanel.find('p[class="help-block"]').show();

    $programPanel.find('button[name="addtimerange"]').prop('disabled', enabled);
    $programPanel.find('button[name="deletetimerange"]').prop('disabled', enabled);
    $programPanel.find('button[name="edittimerange"]').prop('disabled', !enabled);

}
function loadProgram(program) {

    $program = program;

    $("#result").load("program.html", function () {
            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                getScenario(program.scenarioid,function (scenario) {
                    loadScenario(scenario);
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

            // save button
            var savebutton = $programPanel.find('button[name="save"]');
            savebutton.hide();
            programDisableEdit(true);
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

                updateTimerangeData();

                postData("program", program, function (result, response) {
                    if (result) {
                        notification.show();
                        notificationsuccess.find('label[name="description"]').text("programma salvato");
                        var json = jQuery.parseJSON(response);
                        loadProgram(json);
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });

            var cancelbutton = $programPanel.find('button[name="cancel"]');
            cancelbutton.hide();
            cancelbutton.click(function () {
                getProgram(program.id, function (program) {
                    loadProgram(program);
                })
            });

            var editbutton = $programPanel.find('button[name="edit"]');
            editbutton.click(function () {
                savebutton.show();
                cancelbutton.show();
                editbutton.hide();
                addbutton.show();
                programDisableEdit(false);
            });

            var addbutton = $programPanel.find('button[name="add"]');
            addbutton.hide();

            // timeranges
            var tbody = $programPanel.find('tbody[name="list"]');
            tbody[0].innerHTML = "";
            if (program.timeranges != undefined)
                loadProgramTimeranges($program.timeranges);

            addbutton.click(function () {
                updateTimerangeData(); // questo serve per aggiornare eventuali modifiche manuali
                var timerange = {
                    "programid": program.id,
                    "id": 0,
                    "starttime" : "00:00",
                    "endtime" : "00:00",
                    "name": "nuovo timerange",
                    "enabled": false,
                    "priority": 0,
                    "index": 0,
                };
                if (program.timeranges == undefined) {
                    var emptyArray = [];
                    program["timeranges"] = emptyArray;
                }
                program.timeranges.push(timerange);
                loadProgramTimeranges($program.timeranges);
                //updateSensorsData();

                programDisableEdit(false);
            });
        }
    );
}

function updateTimerangeData() {
    if ($program.timeranges != undefined) {
        var i = 0;
        $programPanel.find('tr[name="row"]').each(function (idx, elem) {
            var elem = $program.timeranges[i];
            elem.enabled = $(this).find('td input[name="enabled"]').prop('checked');
            elem.starttime = $(this).find('td input[name="starttime"]').val();
            elem.endtime = $(this).find('td input[name="endtime"]').val();
            elem.name = $(this).find('td input[name="name"]').val();
            elem.description = $(this).find('td input[name="description"]').val();
            elem.index = i;
            i++;
        });
    }
}
