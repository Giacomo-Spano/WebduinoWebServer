/**
 * Created by giaco on 27/10/2017.
 */
var $shieldsPanel;
var $sensorRow;
//var $sensorindex = 0;
var $shield;


function getSchield(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=shield&id=" + id, function (shield) {
        callback(shield);
    });
}


function setShieldElement(element, shield) {

    element.find('td[name="id"]').text(shield.shieldid);
    element.find('td[name="name"]').text(shield.shieldname);
    //element.find('td[name="url"]').text(shield.url + ":" + shield.port);
    element.find('td[name="macaddress"]').text(shield.macaddress);
    element.find('td[name="mqttserver"]').text(shield.mqttserver + ":" + shield.mqttport);
    // last update
    element.find('td[name="date"]').text(shield.lastupdate);
    element.find('td[name="swversion"]').text(shield.swversion);

    $.getJSON(systemServletPath + "?requestcommand=swversions", function (swversions) {
        $.each(swversions, function (val, swversion) {
            element.find('select[name="swversion"]').append(new Option(swversion.name, swversion.version));
        });
    });

    var updateButton = element.find('button[name="updatebutton"]');
    updateButton.click(function () {

    });


    var editButton = element.find('button[name="editbutton"]');
    editButton.click(function () {
        loadShield(shield);
    });

    var statusButton = element.find('button[name="statusbutton"]');
    editButton.click(function () {

    });



    var restartButton = element.find('button[name="rebootbutton"]');
    restartButton.click(function () {

        var command = 'reboot'
        restartButton.text("sending" + command + " command...");
        var json = {
            'shieldid': shield.shieldid,
            'command': command,
        };
        postShieldData(json, function (result, response) {

            restartButton.text("command sent");
            if (result) {
                notificationsuccess.show();
                notificationsuccess.find('label[name="description"]').text("comando inviato" + response);
                loadDashboard();
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        });
    });


}

function addSensor(sensors, id, parentid, elem, sensortypes, pins) {
    var sensor = $sensorRow.clone();
    sensor.find('td[name="id"]').text(elem.id);
    sensor.find('td[name="parentid"]').text(elem.parentid);
    sensor.find('td[name="subaddress"]').text(elem.subaddress);

    $.each(sensortypes, function (val, sensortype) {
        sensor.find('select[name="type"]').append(new Option(sensortype.description, sensortype.value));
    });
    sensor.find('select[name="type"]').val(elem.type);
    $.each(pins, function (val, pin) {
        sensor.find('select[name="pin"]').append(new Option(pin.description, pin.value));
    });
    sensor.find('select[name="pin"]').val(elem.pin);
    sensor.find('input[name="name"]').val(elem.name);
    sensor.find('input[name="description"]').val(elem.description);
    sensor.find('input[name="enabled"]').prop('checked', elem.enabled);

    sensor.attr("id", id);
    sensor.attr("parentid", parentid);
    sensor.attr("subaddress", elem.subaddress);

    sensor.find('button[name="delete"]').attr("subaddress", elem.subaddress);
    sensor.find('button[name="delete"]').click(function () {
        subaddress = $(this).attr("subaddress");
        deleteSensorBySubaddress(subaddress, $shield.sensors);
        loadSensors($shield.sensors, sensortypes, pins);
    });
    sensor.find('button[name="addchild"]').click(function () {
        updateSensorsData(); // questo serve per aggiornare eventuali modifiche manuali
        var sensor = {
            "id": 0,
            "shieldid": elem.shieldid,
            "subaddress": "",
            "parentid": elem.id,
            "name": "nuovo sensore figlio",
            "type": "onewiresensor",
        };
        if (elem.childsensors == undefined) {
            var emptyArray = [];
            elem["childsensors"] = emptyArray;
        }
        elem.childsensors.push(sensor);
        sensor.subaddress = String(elem.subaddress + "." + elem.childsensors.length);
        loadSensors($shield.sensors, sensortypes, pins);
        updateSensorsData();
        disableEdit(false);
    });

    $shieldsPanel.find('tbody[name="list"]').append(sensor);

    if (elem.childsensors != null) {
        $.each(elem.childsensors, function (idx, childelem) {
            addSensor(elem.childsensors, idx + 1, id, childelem, sensortypes, pins);
        });
    }
}

function loadSensors(sensors, sensortypes, pins) {

    var tbody = $shieldsPanel.find('tbody[name="list"]');
    tbody[0].innerHTML = "";
    if (sensors != null) {
        $.each(sensors, function (idx, elem) {
            addSensor(sensors, idx + 1, 0, elem, sensortypes, pins);
        });
    }
}

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
function loadShield(shield) {

    $shield = shield;

    $("#result").load("shield.html", function () {
            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                loadShields();
            });
            pagetitle.text('Shield');
            notification.hide();

            $shieldsPanel = $(this).find('div[id="panel"]');
            $shieldsPanel.find('p[name="headingright"]').text(shield.shieldid);
            $shieldsPanel.find('input[name="shieldenabled"]').prop('checked', shield.enabled);
            $shieldsPanel.find('input[name="localport"]').val(shield.port);

            $shieldsPanel.find('input[name="shieldname"]').val(shield.shieldname);
            $shieldsPanel.find('textarea[name="description"]').val(shield.description);
            $shieldsPanel.find('input[name="servername"]').val(shield.server);
            $shieldsPanel.find('input[name="serverport"]').val(shield.serverport);
            $shieldsPanel.find('input[name="mqttname"]').val(shield.mqttserver);
            $shieldsPanel.find('input[name="mqttport"]').val(shield.mqttport);

            /*$.getJSON(systemServletPath + "?requestcommand=swversions", function (swversions) {
                $.each(swversions, function (val, swversion) {
                    $shieldsPanel.find('select[name="swversion"]').append(new Option(swversion.name, swversion.version));
                });
            });*/


            $sensorRow = $shieldsPanel.find('tr[name="row"]');

            // save button
            var savebutton = $shieldsPanel.find('button[name="save"]');
            savebutton.hide();
            disableEdit(true);
            savebutton.click(function () {
                shield.enabled = $shieldsPanel.find('input[name="shieldenabled"]').prop('checked');
                shield.port = $shieldsPanel.find('input[name="localport"]').val();
                shield.name = $shieldsPanel.find('input[name="shieldname"]').val();
                shield.description = $shieldsPanel.find('textarea[name="description"]').val();
                shield.server = $shieldsPanel.find('input[name="servername"]').val();
                shield.serverport = $shieldsPanel.find('input[name="serverport"]').val();
                shield.mqttserver = $shieldsPanel.find('input[name="mqttname"]').val();
                shield.mqttport = $shieldsPanel.find('input[name="mqttport"]').val();


                updateSensorsData();


                postData("shield", shield, function (result, response) {
                    if (result) {
                        notification.find('label[name="description"]').text("sensore salvato");
                        var json = jQuery.parseJSON(response);
                        loadShield(json);
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });

            var cancelbutton = $shieldsPanel.find('button[name="cancel"]');
            cancelbutton.hide();
            cancelbutton.click(function () {
                getSchield(shield.shieldid, function (shield) {
                    loadShield(shield);
                })

            });

            var editbutton = $shieldsPanel.find('button[name="edit"]');
            editbutton.click(function () {
                savebutton.show();
                cancelbutton.show();
                editbutton.hide();
                addbutton.show();
                disableEdit(false);
                //$shieldsPanel.find('input').prop('disabled', false);
                //$shieldsPanel.find('select').prop('disabled', false);
            });

            var addbutton = $shieldsPanel.find('button[name="add"]');
            addbutton.hide();

            // sensors
            $.getJSON(systemServletPath + "?requestcommand=sensortypes", function (sensortypes) {
                $.getJSON(systemServletPath + "?requestcommand=pins", function (pins) {

                    if (shield.sensors != undefined)
                        loadSensors($shield.sensors, sensortypes, pins);

                    addbutton.click(function () {
                        updateSensorsData(); // questo serve per aggiornare eventuali modifiche manuali
                        var sensor = {
                            "id": 0,
                            "parentid": 0,
                            "subaddress": "",
                            "shieldid": shield.shieldid,
                            "name": "nuovo sensore",
                            "description": "description",
                            "type": "onewiresensor",
                        };
                        if (shield.sensors == undefined) {
                            var emptyArray = [];
                            shield["sensors"] = emptyArray;
                        }
                        shield.sensors.push(sensor);
                        sensor.subaddress = String(shield.sensors.length);
                        loadSensors($shield.sensors, sensortypes, pins);
                        updateSensorsData();

                        disableEdit(false);
                    });

                });
            });
        }
    );
}

function updateSensor(sensor) {
    var tr = $shieldsPanel.find('tr[subaddress="' + sensor.subaddress + '"]');
    sensor.name = tr.find('td').find('input[name=name]').val();
    sensor.description = tr.find('td').find('input[name=description]').val();
    sensor.pin = tr.find('td').find('select[name=pin]').val();
    sensor.type = tr.find('td').find('select[name=type]').val();
    sensor.enabled = tr.find('input[name="enabled"]').prop('checked');

    if (sensor.childsensors != undefined) {
        $.each(sensor.childsensors, function (idx, elem) {
            updateSensor(elem);
        });
    }
}

function updateSensorsData() {
    if ($shield.sensors != undefined) {
        $.each($shield.sensors, function (idx, elem) {
            //elem.subaddress = "" + idx + 1;
            updateSensor(elem);
        });
    }
}

function deleteSensorBySubaddress(subaddress, sensors) {
    if (sensors == undefined || sensors.length == 0) return;
    for (var i = 0; i < sensors.length; i++) {
        if (subaddress == sensors[i].subaddress) {
            sensors.splice(i, 1);
            break;
        }
        if (sensors[i].childsensors == undefined || sensors[i].childsensors.length == 0)
            continue;
        deleteSensorBySubaddress(subaddress, sensors[i].childsensors);
    }
}



