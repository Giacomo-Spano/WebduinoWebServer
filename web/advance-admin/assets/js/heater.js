/**
 * Created by giaco on 27/10/2017.
 */
//var $shieldsPanel;


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
function loadHeater(heater) {

    //$shield = heater;

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
            var panel = $(this).find('div[id="panel"]');
            panel.find('p[name="name"]').text(heater.name);
            panel.find('p[name="temperature"]').text(heater.temperature);
            panel.find('p[name="target"]').text(heater.target);
            panel.find('p[name="status"]').text(heater.status);
            panel.find('p[name="date"]').text(heater.lastupdate);
            if (heater.action != undefined)
                panel.find('p[name="action"]').text(heater.action);
            panel.find('p[name="rele"]').text(heater.relestatus);
            panel.find('p[name="pin"]').text(heater.pin);
            panel.find('p[name="shieldid"]').text(heater.shieldid);
            panel.find('p[name="id"]').text(heater.id);


            if (heater.program != undefined)
                panel.find('p[name="program"]').text(heater.program);
            $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
                $.each(zones, function (val, zone) {
                    panel.find('select[name="zoneid"]').append(new Option(zone.name, zone.id));
                });
            });
            //panel.find('select[name="pin"]').val(heater.pin);
            var duration = panel.find('input[name="duration"]').timepicker({
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
            if (heater.duration != undefined && heater.duration != 0)
                panel.find('input[name="duration"]').val(toHHMM(heater.duration));


            // save button
            var savebutton = panel.find('button[name="save"]');
            savebutton.click(function () {
                var command = 'off';
                if (panel.find('select[name="program"]').val() == 'manual')
                    command = 'manual';

                //time="12:12:12";
                tt = panel.find('input[name="duration"]').val().split(":");
                duration = tt[0] * 60 + tt[1] * 1/*+tt[2]*1*/;


                var json = {
                    'shieldid': heater.shieldid,
                    'actuatorid': heater.id,
                    'command': command,
                    'zone': panel.find('select[name="zoneid"]').val(),
                    'duration': duration,
                    'target': panel.find('input[name="target"]').val(),
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


                /*$.post(shieldServletPath, JSON.stringify(json), function (data) {

                 notificationsuccess.show();
                 notificationsuccess.find('label[name="description"]').text("comando inviato" + data);
                 getSensor(heater.id, function (sensor) {
                 loadData(sensor);
                 });

                 }, "json").fail(function (response) {
                 notification.show();
                 notification.find('label[name="description"]').text(response.responseText);
                 });*/


            });
        }
    );
}

function getSensor(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=sensor&id=" + id, function (sensor) {
        callback(sensor);
    });
}

function toHHMM(seconds) {
    var h, m, s, result = '';
    // HOURs
    h = Math.floor(seconds / 3600);
    seconds -= h * 3600;
    if (h) {
        result = h < 10 ? '0' + h + ':' : h + ':';
    }
    // MINUTEs
    m = Math.floor(seconds / 60);
    seconds -= m * 60;
    result += m < 10 ? '0' + m + ':' : m/*+':'*/;
    // SECONDs
    //s=seconds%60;
    //result += s<10 ? '0'+s : s;
    return result;
}






