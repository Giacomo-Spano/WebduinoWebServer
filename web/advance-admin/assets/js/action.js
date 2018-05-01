/**
 * Created by giaco on 05/11/2017.
 */



var $actionPanel;

function loadAction(action, sensors, zones, triggers, services) {

    $("#result").load("action.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getTimeRange(action.programinstructionid, function (programinstruction) {
                loadProgramInstructions(programinstruction, triggers, zones, sensors, services);
            })

        });
        pagetitle.text('Azione');
        notification.hide();

        $actionPanel = $(this).find('div[id="actionpanel"]');
        $actionPanel.find('p[name="headingright"]').text(action.timerangeid + "." + action.id);
        $actionPanel.find('input[name="name"]').val(action.name);
        $actionPanel.find('textarea[name="description"]').val(action.description);
        $.each(zones, function (val, zone) {
            $actionPanel.find('select[name="zone"]').append(new Option(zone.name, zone.id));
        });
        $actionPanel.find('select[name="zone"]').val(action.zoneid);
        $actionPanel.find('select[name="priority"]').val(action.priority);
        $actionPanel.find('select[name="actuator"]').val(action.actuatorid);
        $.each(sensors, function (val, sensor) {
            $actionPanel.find('select[name="actuator"]').append(new Option(sensor.name, sensor.id));
        });
        $actionPanel.find('select[name="actuator"]').val(action.actuatorid);
        $actionPanel.find('input[name="targetvalue"]').val(action.targetvalue);
        $.each($actionTypes, function (text, key) {
            var option = new Option(key, text);
            action.find('td select[name="type"]').append($(option));
        });
        $actionPanel.find('select[name="type"]').val(action.type);
        $actionPanel.find('input[name="thresholdvalue"]').val(action.thresholdvalue);
        //$actionPanel.find('input[name="seconds"]').val(action.seconds);
        $actionPanel.find('input[name="seconds"]').timepicker({
            timeFormat: 'HH:mm:ss',
            interval: 1,
            minTime: '00:00:00',
            //maxTime: '59:59',
            defaultTime: '00:00:05',
            startTime: '00:00:00',
            dynamic: true,
            dropdown: true,
            scrollbar: true
        });


        $actionPanel.find('input[name="enabled"]').prop('checked',action.enabled);

        // save button
        var savebutton = $actionPanel.find('button[name="save"]');
        savebutton.hide();
        $actionPanel.find('input').prop('disabled', true);
        $actionPanel.find('textarea').prop('disabled', true);
        $actionPanel.find('select').prop('disabled', true);
        $actionPanel.find('p[class="help-block"]').hide();
        savebutton.click(function () {
            action.name = $actionPanel.find('input[name="name"]').val();
            action.description = $actionPanel.find('textarea[name="description"]').val();
            action.zoneid = $actionPanel.find('select[name="zone"]').val();
            action.type = $actionPanel.find('select[name="type"]').val();
            action.actuatorid = $actionPanel.find('select[name="actuator"]').val();
            action.targetvalue = $actionPanel.find('input[name="targetvalue"]').val();
            action.thresholdvalue = $actionPanel.find('input[name="thresholdvalue"]').val();
            //action.seconds = $actionPanel.find('input[name="seconds"]').val();
            var hms = $actionPanel.find('input[name="seconds"]').val();
            //var hms = '02:04:33';   // your input string
            var a = hms.split(':'); // split it at the colons
            // minutes are worth 60 seconds. Hours are worth 60 minutes.
            action.seconds = (+a[0]) * 60 * 60 + (+a[1]) * 60 + (+a[2]);



            action.enabled = $actionPanel.find('input[name="enabled"]').prop('checked');
            action.priority = $actionPanel.find('input[name="priority"]').val();
            postData("action", action, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("azione salvata");
                    var json = jQuery.parseJSON(response);
                    loadProgramInstruction(json, triggers, zones, sensors, services);

                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }

            });
        });

        var cancelbutton = $actionPanel.find('button[name="cancel"]');
        cancelbutton.hide();
        cancelbutton.click(function () {
            loadProgramInstruction(action, programinstruction, triggers, zones, sensors, services);
        });

        var editbutton = $actionPanel.find('button[name="edit"]');
        editbutton.click(function () {
            enableActionEdit(savebutton, cancelbutton, editbutton);
        });
    });
}

function enableActionEdit(savebutton, cancelbutton, editbutton) {
    savebutton.show();
    cancelbutton.show();
    editbutton.hide();
    $actionPanel.find('input[name="name"]').prop('disabled', false);
    $actionPanel.find('textarea[name="description"]').prop('disabled', false);
    $actionPanel.find('select[name="zone"]').prop('disabled', false);
    $actionPanel.find('select[name="type"]').prop('disabled', false);
    $actionPanel.find('select[name="actuator"]').prop('disabled', false);
    $actionPanel.find('input[name="targetvalue"]').prop('disabled', false);
    $actionPanel.find('input[name="thresholdvalue"]').prop('disabled', false);
    $actionPanel.find('input[name="seconds"]').prop('disabled', false);
    $actionPanel.find('input[name="enabled"]').prop('disabled', false);
    $actionPanel.find('input[name="priority"]').prop('disabled', false);
    $actionPanel.find('p[class="help-block"]').show();
}
