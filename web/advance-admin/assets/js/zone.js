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
        setSensorList(this.value, zoneSensor);
    });
    zoneSensor.find('td select[name="type"]').val(elem.type);
    setSensorList(elem.type, zoneSensor);

    zoneSensor.find('td[name="status"]').val(elem.status);
    zoneSensor.find('td[name="name"]').val(elem.name);

    zoneSensor.find('button[name="deletesensor"]').attr("idx", idx);
    zoneSensor.find('button[name="deletesensor"]').click(function () {
        var index = $(this).attr("idx");
        $zone.zonesensors.splice(index, 1);
        loadZoneSensors($zone.zonesensors, sensortypes);
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
                savebutton.click(function () {

                    zone.name = $zonePanel.find('input[name="name"]').val();
                    zone.description = $zonePanel.find('textarea[name="description"]').val();
                    zone.enabled = $zonePanel.find('input[name="timerangeabled"]').prop('checked');
                    updateZoneSensorData();
                    var newitem = (zone.id == 0);
                    postData("zone", zone, function (result, response) {
                        if (result) {

                            var json = jQuery.parseJSON(response);
                            if (newitem) {
                                notification.show();
                                notificationsuccess.find('label[name="description"]').text("zona salvata");
                                loadZone(json);
                            } else {
                                loadZones();
                            }
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    });
                });

                var cancelbutton = $zonePanel.find('button[name="cancel"]');
                cancelbutton.click(function () {
                    loadZones();
                });

                var deletebutton = $zonePanel.find('button[name="delete"]');
                deletebutton.click(function () {
                    postData("zone", zone, function (result, response) {
                        if (result) {
                            var json = jQuery.parseJSON(response);
                            loadZones();
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    }, "delete");
                });

                // sensors
                loadZoneSensors($zone.zonesensors, sensortypes);
                var addsensorbutton = $zonePanel.find('button[name="add"]');
                addsensorbutton.click(function () {
                    updateZoneSensorData(); // questo serve per aggiornare eventuali modifiche manuali
                    var zonesensor = {
                        "zoneid": zone.id,
                        "id": 0,
                        "name": "nuovo sensore",
                        "type": sensortypes[0],
                        "enabled": false,
                        "zoneid": 0,

                    };
                    if (zone.zonesensors == undefined) {
                        var emptyArray = [];
                        zone["zonesensors"] = emptyArray;
                    }
                    zone.zonesensors.push(zonesensor);
                    loadZoneSensors($zone.zonesensors, sensortypes);
                });
                if (zone.id == 0)
                    addsensorbutton.prop('disabled', true);
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
