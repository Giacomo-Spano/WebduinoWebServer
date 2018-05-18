/**
 * Created by giaco on 28/05/2017.
 */
var systemServletPath = "../system";
var shieldServletPath = "../shield";

var $timeinterval;
var $zonesPanel;
var $zoneRow;
var $zonePanel;
var $scenariosPanel;
var $scenarioRow;
var $nexttimerangesPanel;
var $nexttimerangeRow;
var $programPanel;
var $instructionsPanel;
var $instructionRow;
var $sensorsPanel;
var $sensorRow;
var panel;

var client;

var pagetitle;
var pagesubtitle;
var notification;
var notificationsuccess;
var backbutton;

function onConnect() {
    // Once a connection has been made, make a subscription and send a message.
    console.log("onConnect");
    client.subscribe("fromServer");
    message = new Paho.MQTT.Message("Hello");
    message.destinationName = "toServer";
    client.send(message);
};
function onConnectionLost(responseObject) {
    if (responseObject.errorCode !== 0)
        console.log("onConnectionLost:" + responseObject.errorMessage);
};
function onMessageArrived(message) {
    console.log("onMessageArrived:" + message.payloadString);
    client.disconnect();
};
function onSignIn(googleUser) {
    var profile = googleUser.getBasicProfile();
    console.log('ID: ' + profile.getId()); // Do not send to your backend! Use an ID token instead.
    console.log('Name: ' + profile.getName());
    console.log('Image URL: ' + profile.getImageUrl());
    console.log('Email: ' + profile.getEmail()); // This is null if the 'email' scope is not present.

    var id_token = googleUser.getAuthResponse().id_token;

    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'http://localhost:8080/webduino/tokensignin');
    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    xhr.onload = function () {
        console.log('Signed in as: ' + xhr.responseText);
    };
    xhr.send(id_token);


}

function deactivatemenuitems() {
    $('a[id="item_home"]').attr("class", "");
    $('a[id="item_dashboard"]').attr("class", "");
    $('a[id="item_systems"]').attr("class", "");
    $('a[id="item_scenarios"]').attr("class", "");
    $('a[id="item_zones"]').attr("class", "");
    $('a[id="item_shields"]').attr("class", "");
}

function myFunction() {
    // Create a client instance
    client = new Paho.MQTT.Client("192.168.1.3", Number(1883), "/wss");
    client.startTrace();
// set callback handlers
    client.onConnectionLost = onConnectionLost;
    client.onMessageArrived = onMessageArrived;
    client.onConnected = onConnect();

// connect the client
    client.connect();
    console.log("attempting to connect...")
}

function load() {
    pagetitle = $(this).find('div[name="pagetitle"]');
    pagesubtitle = $(this).find('div[name="pagesubtitle"]');
    notification = $(this).find('div[name="notification"]');
    notificationsuccess = $(this).find('div[name="notification-success"]');
    backbutton = $(this).find('button[name="backbutton"]');

    $('a[id="item_home"]').click(function () {
        deactivatemenuitems();
        $('a[id="item_home"]').attr("class", "active-menu");
        return false;
    });

    $('a[id="item_dashboard"]').click(function () {
        deactivatemenuitems();
        $('a[id="item_dashboard"]').attr("class", "active-menu");
        loadDashboard();
        return false;
    });

    $('a[id="item_scenarios"]').click(function () {
        deactivatemenuitems();
        $('a[id="item_scenarios"]').attr("class", "active-menu");
        loadScenarios();
        return false;
    });

    $('a[id="item_systems"]').click(function () {
        deactivatemenuitems();
        $('a[id="item_systems"]').attr("class", "active-menu");
        loadWebduinoSystems();
        return false;
    });

    $('a[id="item_zones"]').click(function () {
        deactivatemenuitems();
        $('a[id="item_zones"]').attr("class", "active-menu");
        loadZones();
        return false;
    });

    $('a[id="item_shields"]').click(function () {
        deactivatemenuitems();
        $('a[id="item_shields"]').attr("class", "active-menu");
        loadShields();
        return false;
    });

    $('a[id="item_triggers"]').click(function () {
        deactivatemenuitems();
        $('a[id="item_triggers"]').attr("class", "active-menu");
        loadTriggers();
        return false;
    });
}

function postData(datatype, json, callback, param) {

    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                callback(true, this.response);
            } else if (this.status == 400) {
                callback(false, this.response);
            }
        }
    };

    path = systemServletPath + "?data=" + datatype;
    if (param != undefined)
        path += "&param=" + param;
    xhttp.open("POST", path, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    var str = JSON.stringify(json);
    xhttp.send(str);
}

function zoneForm(zone, sensors) {

    loadForm("zoneform.html", function (/*json*/) {

            addNumInput("id", zone.id, "Id");
            addTextInput("name", zone.name, "Nome");

            var sensorlist = [];
            for (i = 0; i < sensors.length; i++) {
                var sensor = [];
                sensor.push(sensors[i].id);
                var name = "" + sensors[i].id + "." + sensors[i].name + "(" + sensors[i].type + ")";
                sensor.push(name);
                sensorlist.push(sensor);
            }
            for (i = 0; i < zone.zonesensors.length; i++) {
                id = createSubitem(0, "zonesensors", "ZoneSensor");
                addSubitemNumInput(id, "id", zone.zonesensors[i].id, "Id");
                addSubitemTextInput(id, "name", zone.zonesensors[i].name, "Name");
                addSubitemSelect(id, "sensorid", sensorlist, "sensorid", zone.zonesensors[i].sensorid);
                addSeparator(" ");
            }
        }
        ,
        function (json) { // callback function
            postData("zone", json, function (result) {
                loadZones();
            });
        }
    );
}

// ZONES
/*function setZoneElement(element, zone) {
    element.find('td[name="id"]').text(zone.id);
    element.find('td[name="name"]').text(zone.name);
    element.click(function () {
        loadZone(zone.id)
    });
}*/

// ZONE
function loadZone(id) {

    $("#result").load("zone.html", function () {
        $zonePanel = $(this).find('div[id="zonepanel"]');
        $sensorRow = $zonePanel.find('tr[name="sensor"]');

        var tbody = $zonePanel.find('tbody[name="sensorlist"]');
        $.getJSON(systemServletPath + "?requestcommand=zone&id=" + id, function (zone) {

            $zonePanel.find('span#id').text(zone.id);
            $zonePanel.find('span#name').text(zone.name);
            if (zone.type != null)
                $zonePanel.find('span#type').text(zone.type);
            else
                $zonePanel.find('span#type').text('');

            tbody[0].innerHTML = "";

            $.each(zone.zonesensors, function (idx, elem) {
                var newtr = $sensorRow.clone();
                setZoneSensorElement(newtr, elem);
                tbody.append(newtr);
            });

            $zonePanel.find('button[id="edit"]').click(function () {
                $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
                    zoneForm(zone, sensors);
                })
                    .done(function () {
                    })
                    .fail(function () {
                        alert("cannot load sensors");
                    })
                    .always(function () {
                    });
            });


        })
            .done(function () {
            })
            .fail(function () {
                alert("cannot load zone");
            })
            .always(function () {
            });
    });
}

function setZoneSensorElement(element, sensor) {

    element.find('td[name="id"]').text(sensor.id);
    element.find('td[name="name"]').text(sensor.sensorid + sensor.name);
    element.find('td[name="type"]').text(sensor.type);


}

function loadDashboard() {

    $("#result").load("dashboard.html", function () {
        $sensorsPanel = $(this).find('div[id="sensorpanel"]');
        $sensorRow = $sensorsPanel.find('tr[name="sensor"]');

        var tbody = $sensorsPanel.find('tbody[name="sensorlist"]');
        $.getJSON(systemServletPath + "?requestcommand=sensors", function (data) {
            tbody[0].innerHTML = "";
            $.each(data, function (idx, elem) {
                var newtr = $sensorRow.clone();
                setSensorElement(newtr, elem);
                tbody.append(newtr);
            });
        })
            .done(function () {
            })
            .fail(function () {
                alert("cannot load sensorss");
            })
            .always(function () {
            });
    });
}

function loadShields() {

    $("#result").load("shields.html", function () {
        var panel = $(this).find('div[id="panel"]');
        var row = panel.find('tr[name="row"]');

        var tbody = panel.find('tbody[name="list"]');
        $.getJSON(systemServletPath + "?requestcommand=shields", function (data) {
            tbody[0].innerHTML = "";
            $.each(data, function (idx, elem) {
                var newtr = row.clone();
                setShieldElement(newtr, elem);
                tbody.append(newtr);
            });

            panel.find('button[name="add"]').click(function () {

                var scenario = {
                    "id": 0,
                    "name": "scenario nuovo",
                    "enabled": false,
                    "priority": 0,
                };
                postData("scenario", scenario, function (result, response) {
                    if (result) {
                        var json = jQuery.parseJSON(response);
                        loadScenarios();
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });


            });
        });
    });
}

// SCENARIOS
function loadScenarios() {

    $("#result").load("scenarios.html", function () {
        $scenariosPanel = $(this).find('div[id="scenariospanel"]');
        $scenarioRow = $scenariosPanel.find('tr[name="scenario"]');

        $nexttimerangesPanel = $(this).find('div[id="nexttimeranges"]');
        $nexttimerangeRow = $nexttimerangesPanel.find('tr[name="nexttimerangerow"]');

        var tbody = $scenariosPanel.find('tbody[name="scenariolist"]');
        $.getJSON(systemServletPath + "?requestcommand=scenarios", function (data) {
            tbody[0].innerHTML = "";
            $.each(data, function (idx, elem) {
                var newtr = $scenarioRow.clone();
                setScenarioElement(newtr, elem);
                tbody.append(newtr);
            });

            $scenariosPanel.find('button[name="add"]').click(function () {

                var scenario = {
                    "id": 0,
                    "name": "scenario nuovo",
                    "enabled": false,
                    "priority": 0,
                };
                postData("scenario", scenario, function (result, response) {
                    if (result) {
                        var json = jQuery.parseJSON(response);
                        loadScenarios();
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });


            });

            $.getJSON(systemServletPath + "?requestcommand=nextprograms&id="+0, function (nexttimeranges) {
                var tbody = $nexttimerangesPanel.find('tbody[name="nexttimerangelist"]');
                tbody[0].innerHTML = "";
                if (nexttimeranges != null) {
                    $.each(nexttimeranges, function (idx, elem) {
                        addNextTimeRange(idx, elem);
                    });
                }
            });

        });
    });
}

function addNextTimeRange(idx, elem) {
    var timerange = $nexttimerangeRow.clone();

    timerange.find('td[name="date"]').text(elem.date);
    timerange.find('td[name="time"]').text(elem.starttime + "-" + elem.endtime);
    timerange.find('td[name="scenario"]').text(elem.scenarioid+"."+elem.scenarioname);
    timerange.find('td[name="timeinterval"]').text(elem.timeintervalid);
    timerange.find('td[name="program"]').text(elem.programid+"."+elem.programname);
    timerange.find('td[name="timerange"]').text(elem.timerangeid+"."+elem.timerangename);
    timerange.find('td[name="action"]').text(elem.action.id+"."+elem.action.name);
    timerange.find('td[name="type"]').text(elem.action.type+"."+elem.action.targetvalue);
    timerange.find('td[name="actuatorid"]').text(elem.actuatorid);
    timerange.find('td[name="conflicts"]').text(elem.conflicts);

    $nexttimerangesPanel.find('tbody[name="nexttimerangelist"]').append(timerange);
}

function setSensorElement(element, sensor) {

    // id
    element.find('td[name="id"]').text(sensor.id);
    // shieldid
    element.find('td[name="shieldid"]').text(sensor.shieldid);
    if (sensor.online)
        element.find('td[name="onlinestatus"]').text("Online");
    else
        element.find('td[name="onlinestatus"]').text("Offline");
    // last update
    element.find('td[name="date"]').text(sensor.lastupdate);
    // type
    element.find('td[name="type"]').text(sensor.type);
    //name
    element.find('td[name="name"]').text(sensor.name);

    /*element.find('td[name="name"]').addEventListener('input', function() {
     console.log('Hey, somebody changed something in my text!');
     });*/
    editBtn = element.find('button[id="edit"]');
    //editables = document.querySelectorAll('#name, #type')

    editBtn.click(function () {

        editables = element.find('td[name="name"]');
        id = element.find('td[name="id"]').text();


        if (!editables[0].isContentEditable) {
            for (var i = 0; i < editables.length; i++) {
                editables[i].contentEditable = 'true';
            }
            this.innerHTML = 'Save Changes';
        } else {
            for (var i = 0; i < editables.length; i++) {
                editables[0].contentEditable = 'false';
                editBtn.innerHTML = 'Enable Editing';
                //localStorage.setItem(editables[i].getAttribute('id'), editables[i].innerHTML);

            }

            var json = {'id': id, 'name': editables[0].innerHTML};

            postData("sensor", json, function (result) {
                //loadZones();
            });
            this.innerHTML = 'Edit';
        }
    });

    //pin
    element.find('td[name="pin"]').text(sensor.pin);
    // subaddress
    element.find('td[name="subaddress"]').text(sensor.subaddress);

    var detailButton = element.find('button[name="detailbutton"]');
    detailButton.text(label);
    detailButton.click(function () {
        loadHeater(sensor);

    });

    var statusButton = element.find('button[name="statusbutton"]');
    statusButton.text(label);
    statusButton.click(function () {

        var command = 'updatesensor'
        statusButton.text("sending" + command + " command...");
        var json = {
            'id': sensor.id,
            'command': command,
        };
        postShieldData(json, function (result, response) {

            statusButton.text("command sent");
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


    //
    if (sensor.type == "temperaturesensor") {
        text = "temp:" + sensor.temperature + "°C" + " av.temp:" + sensor.avtemperature + "°C";
        element.find('td[name="status"]').text(text);
    } else if (sensor.type == "doorsensor") {

        text = "door ";
        if (sensor.openstatus == true)
            text += "open";
        else
            text += "closed";
        element.find('td[name="status"]').text(text);

        // testmode button
        var label = "test mode";
        if (sensor.testmode)
            var label = "end test mode";
        var testButton = element.find('button[name="testbutton"]');
        testButton.text(label);
        testButton.click(function () {
            var command = 'teststart'
            if (sensor.testmode) {
                command = 'teststop';
            }
            statusButton.text("sending" + command + " command...");
            var commandJson = {
                'shieldid': sensor.shieldid,
                'actuatorid': sensor.id,
                'command': command
            };
            //sendSensorCommand(commandJson, sensor)
            $.post(shieldServletPath, JSON.stringify(commandJson), function (data) {
                console.log(data.name); // John
                console.log(data.time); // 2pm
            }, "json");
        });

        // test open/close button
        label = "close";
        
        if (!sensor.openstatus)
            label = "open";


    } else if (sensor.type == "heatersensor") {
        text = "status: " + sensor.status
            + " rele: " + sensor.relestatus
            + " target: " + sensor.target
            + " temperature : " + sensor.temperature
            + " " + sensor.lasttemperatureupdate
            + " lastcommandate" + sensor.lastcommanddate
            + " enddate" + sensor.enddate
            + " actionid" + sensor.action
            + " zone: " + sensor.zone
            + " duration: " + sensor.duration
            + " >remaining: " + sensor.remaining
            + " ";

        element.find('td[name="status"]').text(text);
    } else {
        element.find('td[name="status"]').text("undefined");
    }

}

function sendShieldCommand(commandJson) {

    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {

            var json = JSON.parse(this.response);
            /*if (json.answer = 'success') {

             var actuator = JSON.parse(json.actuator);
             commanCallback(element, actuator);
             } else {
             element.find('td[name="commandstatus"]').text("command failed");
             }*/
        }
    };

    xhttp.open("POST", shieldServletPath, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    var str = JSON.stringify(commandJson);
    //xhttp.send(commandJson.toString());
    xhttp.send(str);
}

function setScenarioElement(element, scenario) {

    element.find('td[name="id"]').text(scenario.id);
    element.find('td[name="name"]').text(scenario.name);
    element.find('td[name="enabled"]').text(scenario.enabled);
    element.find('td[name="status"]').text(scenario.status);

    element.click(function () {
        loadWebduinoSystemScenario(scenario);
    });
    element.find('button[name="delete"]').click(function () {
        postData("scenario", scenario, function (result, response) {
            var json = jQuery.parseJSON(response);
            loadScenarios();
            return;
        }, "delete");
    });
    element.find('td[name="status"]').text(scenario.status);
    element.find('td[name="nextjobdate"]').text(scenario.nextjobdate);
    element.find('td[name="startdate"]').text(scenario.startdate);
    element.find('td[name="enddate"]').text(scenario.enddate);
}

// SCENARIO

function getProgram(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=program&id=" + id, function (program) {
        callback(program);
    });
}

function getWebduinoSystemScenario(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=webduinosystemscenario&id=" + id, function (scenario) {
        callback(scenario);
    });
}

function getWebduinoSystem(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=webduinosystem&id=" + id, function (system) {
        callback(system);
    });
}

function getWebduinoSystemActuator(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=webduinosystemactuator&id=" + id, function (system) {
        callback(system);
    });
}

function getWebduinoSystemService(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=webduinosystemservicer&id=" + id, function (system) {
        callback(system);
    });
}

function getWebduinoSystemZone(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=webduinosystemzone&id=" + id, function (system) {
        callback(system);
    });
}

function getSensor(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=sensor&id=" + id, function (sensor) {
        callback(sensor);
    });
}

function getSensorFromZoneSensor(zoneid, zonesensorid, callback) {
    $.getJSON(systemServletPath + "?requestcommand=zonesensor&zoneid=" + zoneid + "&zonesensorid=" +zonesensorid , function (sensor) {
        callback(sensor);
    });
}

function getSensors(callback) {
    $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
        callback(sensors);
    });
}

function getZones(callback) {
    $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
        callback(zones);
    });
}

function getProgramInstruction(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=instruction&id=" + id, function (instruction) {
        callback(instruction);
    });
}

function getService(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=service&id=" + id, function (instruction) {
        callback(instruction);
    });
}

function getTriggers(callback) {
    $.getJSON(systemServletPath + "?requestcommand=triggers", function (triggers) {
        callback(triggers);
    });
}

function getServices(callback) {
    $.getJSON(systemServletPath + "?requestcommand=services", function (services) {
        callback(services);
    });
}

function loadScenarioTimeinterval(timeinterval) {

    $timeinterval = timeinterval;
    $("#result").load("scenariotimeinterval.html", function () {
        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getWebduinoSystemScenario($timeinterval.scenarioid, function (scenario) {
                loadWebduinoSystemScenario(scenario);
            })
        });
        pagetitle.text('Time interval');
        notification.hide();

        var panel = $(this).find('div[id="timeintervalpanel"]');
        panel.find('p[name="headingright"]').text(timeinterval.scenarioid + "." + timeinterval.id);
        panel.find('input[name="name"]').val(timeinterval.name);
        panel.find('textarea[name="description"]').val(timeinterval.description);
        panel.find('select[name="type"]').val(timeinterval.type);
        panel.find('input[name="priority"]').val(timeinterval.priority);
        panel.find('input[name="value"]').val(timeinterval.value);
        panel.find('input[name="enabled"]').prop('checked', timeinterval.enabled);
        panel.find('input[name="startdatetime"]').datetimepicker({
            format: 'd/m/Y H:i',
        });
        panel.find('input[name="startdatetime"]').val(timeinterval.startdatetime);
        panel.find('input[name="enddatetime"]').datetimepicker({
            format: 'd/m/Y H:i',
        });
        panel.find('input[name="enddatetime"]').val(timeinterval.enddatetime);
        panel.find('input[name="sunday"]').prop('checked', timeinterval.sunday);
        panel.find('input[name="monday"]').prop('checked', timeinterval.monday);
        panel.find('input[name="tuesday"]').prop('checked', timeinterval.tuesday);
        panel.find('input[name="wednesday"]').prop('checked', timeinterval.wednesday);
        panel.find('input[name="thursday"]').prop('checked', timeinterval.thursday);
        panel.find('input[name="friday"]').prop('checked', timeinterval.friday);
        panel.find('input[name="saturday"]').prop('checked', timeinterval.saturday);

        // save button
        var savebutton = panel.find('button[name="save"]');
        savebutton.click(function () {
            timeinterval.name = panel.find('input[name="name"]').val();
            timeinterval.description = panel.find('textarea[name="description"]').val();
            timeinterval.type = panel.find('select[name="type"]').val();
            timeinterval.value = panel.find('input[name="value"]').val();
            timeinterval.status = panel.find('input[name="status"]').val();
            timeinterval.enabled = panel.find('input[name="enabled"]').prop('checked');
            timeinterval.priority = panel.find('input[name="priority"]').val();
            timeinterval.startdatetime = panel.find('input[name="startdatetime"]').val();
            timeinterval.enddatetime = panel.find('input[name="enddatetime"]').val();
            timeinterval.sunday = panel.find('input[name="sunday"]').prop('checked');
            timeinterval.monday = panel.find('input[name="monday"]').prop('checked');
            timeinterval.tuesday = panel.find('input[name="tuesday"]').prop('checked');
            timeinterval.wednesday = panel.find('input[name="wednesday"]').prop('checked');
            timeinterval.thursday = panel.find('input[name="thursday"]').prop('checked');
            timeinterval.friday = panel.find('input[name="friday"]').prop('checked');
            timeinterval.saturday = panel.find('input[name="saturday"]').prop('checked');
            postData("timeinterval", timeinterval, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("timeiunterval salvato");
                    var json = jQuery.parseJSON(response);
                    getWebduinoSystemScenario($timeinterval.scenarioid, function (scenario) {
                        loadWebduinoSystemScenario(scenario);
                    })
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });

        var cancelbutton = panel.find('button[name="cancel"]');
        cancelbutton.click(function () {
            getWebduinoSystemScenario($timeinterval.scenarioid, function (scenario) {
                loadWebduinoSystemScenario(scenario);
            })
        });

        var deletebutton = panel.find('button[name="delete"]');
        deletebutton.click(function () {
            postData("timeinterval", timeinterval, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("timeiunterval salvato");
                    var json = jQuery.parseJSON(response);
                    getWebduinoSystemScenario($timeinterval.scenarioid, function (scenario) {
                        loadWebduinoSystemScenario(scenario);
                    })
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            },"delete");
        });
    });
}

function loadScenarioTrigger(scenariotrigger) {

    $("#result").load("scenariotrigger.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getWebduinoSystemScenario(scenariotrigger.scenarioid, function (scenario) {
                loadWebduinoSystemScenario(scenario);
            })
        });
        pagetitle.text('Trigger');
        notification.hide();

        var panel = $(this).find('div[id="triggerpanel"]');
        panel.find('p[name="headingright"]').text(scenariotrigger.scenarioid + "." + scenariotrigger.id);

        getTriggers(function (trigger) {
            $.each(trigger, function (val, trigger) {
                panel.find('select[name="triggerid"]').append(new Option(trigger.name, trigger.id));
            });
            panel.find('select[name="triggerid"]').change(function () {
                getTrigger(this.value,function (trigger) {
                    $('option', panel.find('select[name="scenariotriggerstatus"]')).remove();
                    $.each(trigger.statuslist, function (val, status) {
                        panel.find('select[name="scenariotriggerstatus"]').append(new Option(status, status));
                    });
                    panel.find('select[name="scenariotriggerstatus"]').val(scenariotrigger.status);
                });
            });
            panel.find('select[name="triggerid"]').val(scenariotrigger.triggerid).change();
            panel.find('select[name="scenariotriggerstatus"]').val(scenariotrigger.status);
        });
        panel.find('input[name="triggerstatus"]').val(scenariotrigger.status).prop('disabled', true);
        panel.find('input[name="enabled"]').prop('checked',scenariotrigger.enabled);

        // save button
        var savebutton = panel.find('button[name="save"]');
        savebutton.click(function () {
            scenariotrigger.status = panel.find('select[name="scenariotriggerstatus"]').val();
            scenariotrigger.triggerid = panel.find('select[name="triggerid"]').val();
            scenariotrigger.enabled = panel.find('input[name="enabled"]').prop('checked');
            postData("scenariotrigger", scenariotrigger, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("trigger salvata");
                    var json = jQuery.parseJSON(response);
                    getWebduinoSystemScenario(json.scenarioid,function (scenario) {
                        loadWebduinoSystemScenario(scenario);
                    });
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });

        var cancelbutton = panel.find('button[name="cancel"]');
        cancelbutton.click(function () {
            getWebduinoSystemScenario(scenariotrigger.scenarioid,function (scenario) {
                loadWebduinoSystemScenario(scenario);
            });
        });

        var deletebutton = panel.find('button[name="delete"]');
        deletebutton.click(function () {

            postData("scenariotrigger", scenariotrigger, function (result, response) {
                if (result) {
                    getWebduinoSystemScenario(scenariotrigger.scenarioid, function (scenario) {
                        loadWebduinoSystemScenario(scenario);
                    })
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            },"delete");
        });
    });
}


function getDays(elem) {
    var day = "";
    if (elem.sunday)
        day += "D";
    else
        day += "-";
    if (elem.monday)
        day += "L";
    else
        day += "-";
    if (elem.tuesday)
        day += "M";
    else
        day += "-";
    if (elem.wednesday)
        day += "M";
    else
        day += "-";
    if (elem.thursday)
        day += "G";
    else
        day += " ";
    if (elem.friday)
        day += "V";
    else
        day += "-";
    if (elem.saturday)
        day += "S";
    else
        day += "-";
    return day;
}

function setScenarioProgramElement(element, program/*, scenario*/) {

    element.find('td[name="id"]').text(program.id);
    element.find('td[name="scenarioid"]').text(program.scenarioid);
    element.find('td[name="name"]').text(program.name);
    element.find('td[name="enabled"]').text(program.enabled);
    element.find('td[name="start"]').text(program.startdate);
    element.find('td[name="end"]').text(program.enddate);
    element.find('td[name="status"]').text(program.status);
    element.find('td[name="nextjob"]').text(program.nextjobdate);

    if (program.activetimerange != undefined) {
        element.find('td[name="timerange"]').text(program.activetimerange.name);
        element.find('td[name="action"]').text(program.activetimerange.actionstatus);
    }

    element.click(function () {
        loadProgram(program);
    });


    element.find('button[name="delete"]').click(function () {
        postData("program", program, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("trigger eliminato");
                var json = jQuery.parseJSON(response);
                loadWebduinoSystemScenario(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(error);
            }


        }, "delete");
    });
}


var func = function (obj) {
    console.log(JSON.stringify(obj));
};
$("input[type='submit']").click(function () {
    func($("form").toJSO());
    func($("form").serialize());
    return false;
});


function postShieldData(json, callback, param) {

    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                callback(true, this.response);
            } else if (this.status == 400) {
                callback(false, this.response);
            }
        }
    };

    path = shieldServletPath/* + "?data=" + datatype*/;
    if (param != undefined)
        path += "&param=" + param;
    xhttp.open("POST", path, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    var str = JSON.stringify(json);
    xhttp.send(str);
}





