/**
 * Created by giaco on 27/10/2017.
 */
var $zonePanel;
var $zoneSensorRow;
var $zone;

function getZone(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=zone&id=" + id, function (zone) {
        callback(zone);
    });
}

function setSensorList(type, zoneSensor) {
    zoneSensor.find('td  select[name="sensor"]').empty();

    $.getJSON(systemServletPath + "?requestcommand=sensors&type=" + type, function (sensors) {
        $.each(sensors, function (val, sensor) {
            zoneSensor.find('td  select[name="sensor"]').append(new Option(sensor.name, sensor.id));
        });
    });
}

function addZoneSensor(idx, elem, sensortypes) {
    var zoneSensor = $zoneSensorRow.clone();

    zoneSensor.find('td[name="id"]').text(elem.id);
    zoneSensor.find('td input[name="enabled"]').prop('checked', elem.enabled);


    $.each(sensortypes, function (val, type) {
        zoneSensor.find('td  select[name="type"]').append(new Option(type.description, type.value));
    });

    zoneSensor.find('td  select[name="type"]').change(function () {
        setSensorList(this.value,zoneSensor);
    });
    zoneSensor.find('td select[name="type"]').val(elem.type);
    setSensorList(elem.type,zoneSensor);

    zoneSensor.find('td[name="status"]').val(elem.status);
    zoneSensor.find('td[name="name"]').val(elem.name);

    zoneSensor.find('button[name="deleteaction"]').attr("idx", idx);
    zoneSensor.find('button[name="deleteaction"]').click(function () {
        var index = $(this).attr("idx");
        $zone.actions.splice(index, 1);
        loadZoneSensors($zone.zonesensors, sensortypes);
        zoneDisableEdit(false);
    });

    $zonePanel.find('tbody[name="list"]').append(zoneSensor);
}

function loadZoneSensors(zonesensors, sensortypes) {
    var tbody = $zonePanel.find('tbody[name="list"]');
    tbody[0].innerHTML = "";
    if (zonesensors != null) {
        $.each(zonesensors, function (idx, elem) {
            addZoneSensor(idx, elem, sensortypes);
        });
    }
}

function zoneDisableEdit(enabled) {
    $zonePanel.find('input').prop('disabled', enabled);
    $zonePanel.find('textarea').prop('disabled', enabled);
    $zonePanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $zonePanel.find('p[class="help-block"]').hide();
    else
        $zonePanel.find('p[class="help-block"]').show();

    $zonePanel.find('button[name="addactio"]').prop('disabled', enabled);
    $zonePanel.find('button[name="deleteaction"]').prop('disabled', enabled);
    $zonePanel.find('button[name="editaction"]').prop('disabled', !enabled);

}

function loadZone(zone) {

    $zone = zone;


    $.getJSON(systemServletPath + "?requestcommand=sensortypes", function (sensortypes) {

        $("#result").load("zone.html", function () {
                // back button
                backbutton.unbind("click");
                backbutton.click(function () {
                    loadZones();
                });
                pagetitle.text('Zone');
                notification.hide();
                notificationsuccess.hide();

                $zonePanel = $(this).find('div[id="panel"]');
                $zoneSensorRow = $zonePanel.find('tr[name="row"]');

                $zonePanel.find('p[name="headingright"]').text(zone.id);
                $zonePanel.find('input[name="zoneeabled"]').prop('checked', zone.enabled);
                $zonePanel.find('input[name="name"]').val(zone.name);
                $zonePanel.find('textarea[name="description"]').val(zone.description);

                // save button
                var savebutton = $zonePanel.find('button[name="save"]');
                savebutton.hide();
                zoneDisableEdit(true);
                savebutton.click(function () {

                    zone.name = $zonePanel.find('input[name="name"]').val();
                    zone.description = $zonePanel.find('textarea[name="description"]').val();
                    zone.enabled = $zonePanel.find('input[name="timerangeabled"]').prop('checked');
                    updateZoneSensorData();

                    postData("zone", zone, function (result, response) {
                        if (result) {
                            notification.show();
                            notificationsuccess.find('label[name="description"]').text("zona salvata");
                            var json = jQuery.parseJSON(response);
                            loadZone(json);
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    });
                });

                var cancelbutton = $zonePanel.find('button[name="cancel"]');
                cancelbutton.hide();
                cancelbutton.click(function () {
                    getZone($zone.id, function (zone) {
                        //loadTimeRange(zone);
                    })
                });

                var editbutton = $zonePanel.find('button[name="edit"]');
                editbutton.click(function () {
                    savebutton.show();
                    cancelbutton.show();
                    editbutton.hide();
                    addbutton.show();
                    zoneDisableEdit(false);
                });

                var addbutton = $zonePanel.find('button[name="add"]');
                addbutton.hide();

                // sensors
                if (zone.zonesensors != undefined)
                    loadZoneSensors($zone.zonesensors, sensortypes);
                /*else
                 $zonePanel.find('tbody[name="list"]').innerHTML = "";*/

                addbutton.click(function () {
                    updateZoneSensorData(); // questo serve per aggiornare eventuali modifiche manuali
                    var zonesensor = {
                        "zoneid": zone.id,
                        "id": 0,
                        "name": "nuovo sensore",
                        "type": "delayalarm",
                        "enabled": false,
                        "zoneid": 0,

                    };
                    if (zone.zonesensors == undefined) {
                        var emptyArray = [];
                        zone["zonesensors"] = emptyArray;
                    }
                    zone.zonesensors.push(zonesensor);
                    loadZoneSensors($zone.zonesensors, sensortypes);
                    zoneDisableEdit(false);
                });
            }
        );

    });
}

function updateZoneSensorData() {
    if ($zone.zonesensors != undefined) {
        var i = 0;
        $zonePanel.find('tr[name="row"]').each(function (idx, elem) {
            var elem = $zone.zonesensors[i];
            elem.enabled = $(this).find('td input[name="enabled"]').prop('checked');
            elem.type = $(this).find('td select[name="type"]').val();
            elem.name = $(this).find('td input[name="name"]').val();
            elem.description = $(this).find('td input[name="description"]').val();
            elem.zoneid = $(this).find('td select[name="zoneid"]').val();
            elem.sensorid = $(this).find('td select[name="sensor"]').val();
            i++;
        });
    }
}
