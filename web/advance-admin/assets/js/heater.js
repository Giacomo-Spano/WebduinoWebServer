/**
 * Created by giaco on 27/10/2017.
 */
var $heater;
var $panel;


function disableEdit(enabled) {
    $shieldsPanel.find('input').prop('disabled', enabled);
    $shieldsPanel.find('textarea').prop('disabled', enabled);
    $shieldsPanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $shieldsPanel.find('p[class="help-block"]').hide();
    else
        $shieldsPanel.find('p[class="help-block"]').show();

    $shieldsPanel.find('button[name="addchild"]').prop('disabled', enabled);
    $shieldsPanel.find('button[name="delete"]').prop('disabled', enabled);

}

function loadData(heater) {

}

function setDurationandTarget() {
    if ($panel.find('select[name="program"]').val() == "auto") {
        $panel.find('input[name="target"]').prop('disabled', true);
        $panel.find('input[name="duration"]').prop('disabled', true);

        if ($heater.duration != undefined && $heater.duration != "")
            $panel.find('input[name="duration"]').val($heater.duration);
        $panel.find('input[name="target"]').val($heater.target);

    } else {
        $panel.find('input[name="target"]').prop('disabled', false);
        $panel.find('input[name="duration"]').prop('disabled', false);
        $panel.find('input[name="duration"]').val("00:30");
        $panel.find('input[name="target"]').val("22");

    }
}

function loadHeater(heater) {

    $heater = heater;

    $("#result").load("heater.html", function () {

        // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                loadDashboard();
            });
            pagetitle.text('Sensore Temperatura');
            notificationsuccess.hide();
            notification.hide();

            //loadData.call(this, heater);
            $panel = $(this).find('div[id="panel"]');
            $panel.find('p[name="name"]').text(heater.name);
            $panel.find('p[name="temperature"]').text(heater.temperature);
            $panel.find('p[name="status"]').text(heater.status);
            $panel.find('p[name="date"]').text(heater.lastupdate);
            if (heater.action != undefined)
                $panel.find('p[name="action"]').text(heater.action);
            $panel.find('p[name="rele"]').text(heater.relestatus);
            $panel.find('p[name="pin"]').text(heater.pin);
            $panel.find('p[name="shieldid"]').text(heater.shieldid);
            $panel.find('p[name="id"]').text(heater.id);


            $('select[name="program"]').on('change', function () {
                setDurationandTarget.call();
            });

            if (heater.program != undefined)
                $panel.find('p[name="program"]').text(heater.program);
            $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
                $.each(zones, function (val, zone) {
                    $panel.find('select[name="zoneid"]').append(new Option(zone.name, zone.id));
                });
            });

            var duration = $panel.find('input[name="duration"]').timepicker({
                timeFormat: 'HH:mm',
                interval: 15,
                minTime: '00:00',
                maxTime: '02:00',
                defaultTime: '00:30',
                startTime: '00:01',
                dynamic: true,
                dropdown: true,
                scrollbar: true
            });

            if (heater.status == "manual")
                $panel.find('select[name="program"]').val("manual");
            else
                $panel.find('select[name="program"]').val("auto");
            setDurationandTarget();

            // save button
            var savebutton = $panel.find('button[name="save"]');
            savebutton.click(function () {
                var command = 'off';
                if ($panel.find('select[name="program"]').val() == 'manual')
                    command = 'manual';

                //time="12:12:12";
                tt = $panel.find('input[name="duration"]').val().split(":");
                duration = (parseInt(tt[0]) * 60 + parseInt(tt[1])) * 60; //urata in secondi


                var json = {
                    'shieldid': heater.shieldid,
                    'actuatorid': heater.id,
                    'command': command,
                    'zone': $panel.find('select[name="zoneid"]').val(),
                    'duration': duration,
                    'target': $panel.find('input[name="target"]').val(),
                };


                postShieldData(json, function (result, response) {
                    if (result) {
                        notificationsuccess.show();
                        notificationsuccess.find('label[name="description"]').text("comando inviato" + response);
                        var json = jQuery.parseJSON(response);
                        //loadData(json);
                        getSensor(heater.id, function (sensor) {
                            loadHeater(sensor);
                        })
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });

            });
        }
    );
}

function getSensor(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=sensor&id=" + id, function (sensor) {
        callback(sensor);
    });
}







