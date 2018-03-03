/**
 * Created by giaco on 05/11/2017.
 */

function loadAction(action, sensors, zones, instructiontypes) {

    $("#result").load("action.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getTimeRange(action.timerangeid, function (timerange) {
                loadTimeRange(timerange);
            })

        });
        pagetitle.text('Istruzioni programma');
        notification.hide();

        $instructionPanel = $(this).find('div[id="instructionpanel"]');
        $instructionPanel.find('p[name="headingright"]').text(action.timerangeid + "." + action.id);
        $instructionPanel.find('input[name="name"]').val(action.name);
        $instructionPanel.find('textarea[name="description"]').val(action.description);
        $.each(zones, function (val, zone) {
            $instructionPanel.find('select[name="zone"]').append(new Option(zone.name, zone.id));
        });
        $instructionPanel.find('select[name="zone"]').val(action.zoneid);
        $instructionPanel.find('select[name="priority"]').val(action.priority);
        $instructionPanel.find('select[name="actuator"]').val(action.actuatorid);
        $.each(sensors, function (val, sensor) {
            $instructionPanel.find('select[name="actuator"]').append(new Option(sensor.name, sensor.id));
        });
        $instructionPanel.find('select[name="actuator"]').val(action.actuatorid);
        $instructionPanel.find('input[name="targetvalue"]').val(action.targetvalue);
        $.each(instructiontypes, function (val, instructiontype) {
            $instructionPanel.find('select[name="type"]').append(new Option(instructiontype.description, instructiontype.instruction));
        });
        $instructionPanel.find('select[name="type"]').val(action.type);
        $instructionPanel.find('input[name="thresholdvalue"]').val(action.thresholdvalue);
        //$instructionPanel.find('input[name="seconds"]').val(action.seconds);
        $instructionPanel.find('input[name="seconds"]').timepicker({
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


        $instructionPanel.find('input[name="enabled"]').prop('checked',action.enabled);

        // save button
        var savebutton = $instructionPanel.find('button[name="save"]');
        savebutton.hide();
        $instructionPanel.find('input').prop('disabled', true);
        $instructionPanel.find('textarea').prop('disabled', true);
        $instructionPanel.find('select').prop('disabled', true);
        $instructionPanel.find('p[class="help-block"]').hide();
        savebutton.click(function () {
            action.name = $instructionPanel.find('input[name="name"]').val();
            action.description = $instructionPanel.find('textarea[name="description"]').val();
            action.zoneid = $instructionPanel.find('select[name="zone"]').val();
            action.type = $instructionPanel.find('select[name="type"]').val();
            action.actuatorid = $instructionPanel.find('select[name="actuator"]').val();
            action.targetvalue = $instructionPanel.find('input[name="targetvalue"]').val();
            action.thresholdvalue = $instructionPanel.find('input[name="thresholdvalue"]').val();
            //action.seconds = $instructionPanel.find('input[name="seconds"]').val();
            var hms = $instructionPanel.find('input[name="seconds"]').val();
            //var hms = '02:04:33';   // your input string
            var a = hms.split(':'); // split it at the colons
            // minutes are worth 60 seconds. Hours are worth 60 minutes.
            action.seconds = (+a[0]) * 60 * 60 + (+a[1]) * 60 + (+a[2]);



            action.enabled = $instructionPanel.find('input[name="enabled"]').prop('checked');
            action.priority = $instructionPanel.find('input[name="priority"]').val();
            postData("instruction", action, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("istruction salvata");
                    var json = jQuery.parseJSON(response);
                    loadAction(json, sensors, zones, instructiontypes);

                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }

            });
        });

        var cancelbutton = $instructionPanel.find('button[name="cancel"]');
        cancelbutton.hide();
        cancelbutton.click(function () {
            loadAction(action, sensors, zones, instructiontypes);
        });

        var editbutton = $instructionPanel.find('button[name="edit"]');
        editbutton.click(function () {
            enableInstructionEdit(savebutton, cancelbutton, editbutton);
        });
    });
}

function enableInstructionEdit(savebutton, cancelbutton, editbutton) {
    savebutton.show();
    cancelbutton.show();
    editbutton.hide();
    $instructionPanel.find('input[name="name"]').prop('disabled', false);
    $instructionPanel.find('textarea[name="description"]').prop('disabled', false);
    $instructionPanel.find('select[name="zone"]').prop('disabled', false);
    $instructionPanel.find('select[name="type"]').prop('disabled', false);
    $instructionPanel.find('select[name="actuator"]').prop('disabled', false);
    $instructionPanel.find('input[name="targetvalue"]').prop('disabled', false);
    $instructionPanel.find('input[name="thresholdvalue"]').prop('disabled', false);
    $instructionPanel.find('input[name="seconds"]').prop('disabled', false);
    $instructionPanel.find('input[name="enabled"]').prop('disabled', false);
    $instructionPanel.find('input[name="priority"]').prop('disabled', false);
    $instructionPanel.find('p[class="help-block"]').show();
}
