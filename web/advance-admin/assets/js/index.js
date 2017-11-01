/**
 * Created by giaco on 28/05/2017.
 */
var systemServletPath = "../system";
var shieldServletPath = "../shield";

var $zonesPanel;
var $zoneRow;

var $zonePanel;
var $sensorRow;

var $scenariosPanel;
var $scenarioRow;

var $programPanel;
var $timeintervalRow;

var $instructionsPanel;
var $instructionRow;

var $sensorsPanel;
var $sensorRow;



var panel;

var client;

var pagetitle;
var pagesubtitle;
var notification;
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

    //prova(1);

    pagetitle = $(this).find('div[name="pagetitle"]');
    pagesubtitle = $(this).find('div[name="pagesubtitle"]');
    notification = $(this).find('div[name="notification"]');
    backbutton = $(this).find('button[name="backbutton"]');
    //notification.hide();


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

function createProgram(program) {

    id = createSubitem(0, "program", "Program");
    addSubitemNumInput(id, "id", program.id, "Id", 0, 999999, 1, true);
    addSubitemTextInput(id, "name", program.name, "Name");
    addSubitemCheckInput(id, "enabled", program.enabled, "Attivo", function (checked) {
    });

}

function createProgramInstruction(programinstruction, sensors, zones, instructiontypes) {

    id = createSubitem(0, "programinstructions", "Lista istruzioni");
    addSubitemNumInput(id, "id", programinstruction.id, "Id", 0, 999999, 1, true);
    addSubitemNumInput(id, "timerangeidid", programinstruction.timerangeid, "Timerange id", 0, 999999, 1, true);

    var list = [];
    for (i = 0; i < instructiontypes.length; i++) {
        var type = [];

        type.push(instructiontypes[i].instruction);
        type.push(instructiontypes[i].description);
        list.push(type);
    }
    addSubitemSelect(id, "type", list, "Type", programinstruction.type);
    addSubitemTextInput(id, "name", programinstruction.name, "Name");

    var list = [];
    for (i = 0; i < sensors.length; i++) {
        var sensor = [];
        sensor.push(sensors[i].id);
        var name = "" + sensors[i].id + "." + sensors[i].name + "(" + sensors[i].type + ")";
        sensor.push(name);
        list.push(sensor);
    }
    addSubitemSelect(id, "actuatorid", list, "actuatorid", programinstruction.actuatorid);
    addSubitemNumInput(id, "targetvalue", programinstruction.targetvalue, "targetvalue", 0, 30, "0.01");
    var zonelist = [];
    for (i = 0; i < zones.length; i++) {
        var zone = [];
        zone.push(zones[i].id);
        var name = "" + zones[i].id + "." + zones[i].name;
        zone.push(name);
        zonelist.push(zone);
    }
    addSubitemSelect(id, "zoneid", zonelist, "zoneid", programinstruction.zoneid);
    addSubitemTimeInput(id, "time", programinstruction.time, "Time");
    addSubitemCheckInput(id, "schedule", programinstruction.schedule, "Data");
    addSubitemCheckInput(id, "sunday", programinstruction.sunday, "S");
    addSubitemCheckInput(id, "monday", programinstruction.monday, "M");
    addSubitemCheckInput(id, "tuesday", programinstruction.tuesday, "T");
    addSubitemCheckInput(id, "wednesday", programinstruction.wednesday, "W");
    addSubitemCheckInput(id, "thursday", programinstruction.sunday, "T");
    addSubitemCheckInput(id, "friday", programinstruction.friday, "Venerdì", function (checked) {
    });
    addSubitemCheckInput(id, "saturday", programinstruction.saturday, "Sabato", function (checked) {
    });
    addSubitemNumInput(id, "priority", programinstruction.priority, "priorità", 0, 99);
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
function loadZones() {

    $("#result").load("zones.html", function () {
        $zonesPanel = $(this).find('div[id="zonespanel"]');
        $zoneRow = $zonesPanel.find('tr[name="zone"]');

        var tbody = $zonesPanel.find('tbody[name="zonelist"]');
        $.getJSON(systemServletPath + "?requestcommand=zones", function (data) {
            tbody[0].innerHTML = "";
            $.each(data, function (idx, elem) {
                var newtr = $zoneRow.clone();
                setZoneElement(newtr, elem);
                tbody.append(newtr);
            });
        })
            .done(function () {
            })
            .fail(function () {
                alert("cannot load zones");
            })
            .always(function () {
            });
    });
}

function setZoneElement(element, zone) {
    element.find('td[name="id"]').text(zone.id);
    element.find('td[name="name"]').text(zone.name);
    element.click(function () {
        loadZone(zone.id)
    });
}

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

    $("#result").load("shields2.html", function () {
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
        });
    });
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

    // status
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
            sendSensorCommand(commandJson, sensor)
        });

        // test open/close button
        label = "close";
        if (!sensor.openstatus)
            label = "open";
        var statusButton = element.find('button[name="statusbutton"]');
        statusButton.text(label);
        statusButton.click(function () {

            var command = 'testopen'
            if (!sensor.openstatus) {
                command = 'testclose';
            }
            statusButton.text("sending" + command + " command...");
            var commandJson = {
                'shieldid': sensor.shieldid,
                'actuatorid': sensor.id,
                'command': command,
            };
            sendSensorCommand(commandJson, sensor)
        });


    } else if (sensor.type == "heatersensor") {
        text = "status: " + sensor.status
            + " rele: " + sensor.relestatus
            + " target: " + sensor.target
            + " temperature : " + sensor.temperature
            + " scenario: " + sensor.scenario + "." + sensor.timeinterval + " "
            + " zone: " + sensor.zone;

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
        loadScenario(scenario)
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
function enableScenarioEdit(savebutton, cancelbutton, editbutton) {
    savebutton.show();
    cancelbutton.show();
    editbutton.hide();
    $scenarioPanel.find('input[name="name"]').prop('disabled', false);
    $scenarioPanel.find('input[name="enabled"]').prop('disabled', false);
    $scenarioPanel.find('textarea[name="description"]').prop('disabled', false);
    $scenarioPanel.find('input[name="priority"]').prop('disabled', false);
    $scenarioPanel.find('input[name="startdate"]').prop('disabled', false);
    $scenarioPanel.find('input[name="enddate"]').prop('disabled', false);

    $scenarioPanel.find('p[class="help-block"]').show();
}

function loadScenario(scenario, editmode) {

    if (editmode == undefined) {
        editmode = false;
    }

    $("#result").load("scenario.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            loadScenarios();
        });

        pagetitle.text('Scenario');
        notification.hide();

        $scenarioPanel = $(this).find('div[id="scenariopanel"]');
        $scenarioPanel.find('p[name="headingright"]').text(scenario.id);

        $calendarPanel = $(this).find('div[id="calendarpanel"]');
        $calendarRow = $calendarPanel.find('tr[name="timeinterval"]');
        var calendartbody = $calendarPanel.find('tbody[name="timeintervallist"]');

        $triggerPanel = $(this).find('div[id="triggerpanel"]');
        $triggerRow = $triggerPanel.find('tr[name="trigger"]');
        var triggertbody = $triggerPanel.find('tbody[name="triggerlist"]');

        $programPanel = $(this).find('div[id="programpanel"]');
        $programRow = $programPanel.find('tr[name="program"]');
        var programstbody = $(this).find('div[id="programpanel"]').find('tbody[name="programlist"]');

        // dati generali
        $scenarioPanel.find('input[name="name"]').val(scenario.name);
        $scenarioPanel.find('textarea[name="description"]').val(scenario.description);
        $scenarioPanel.find('input[name="enabled"]').prop('checked', scenario.enabled);
        $scenarioPanel.find('input[name="priority"]').val(scenario.priority);


        $scenarioPanel.find('input[name="startdate"]').val(scenario.startdate);
        if (scenario.startdate != undefined)
        $scenarioPanel.find('input[name="startdate"]').datepicker({
            dateFormat: "dd/mm/yy"
        });
        if (scenario.enddate != undefined)
            $scenarioPanel.find('input[name="enddate"]').val(scenario.enddate);
        $scenarioPanel.find('input[name="enddate"]').datepicker({
            dateFormat: "dd/mm/yy"
        });
        $scenarioPanel.find('p[name="status"]').text(scenario.status);

        // save button
        var savebutton = $scenarioPanel.find('button[name="save"]');
        savebutton.hide();
        $scenarioPanel.find('input').prop('disabled', true);
        $scenarioPanel.find('textarea').prop('disabled', true);
        $scenarioPanel.find('select').prop('disabled', true);
        $scenarioPanel.find('p[class="help-block"]').hide();
        savebutton.click(function () {
            scenario.name = $scenarioPanel.find('input[name="name"]').val();
            scenario.description = $scenarioPanel.find('textarea[name="description"]').val();
            scenario.enabled = $scenarioPanel.find('input[name="enabled"]').prop('checked');
            scenario.startdate = $scenarioPanel.find('input[name="startdate"]').val();
            scenario.enddate = $scenarioPanel.find('input[name="enddate"]').val();
            scenario.priority = $scenarioPanel.find('input[name="priority"]').val();

            postData("scenario", scenario, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("program salvata");
                    var json = jQuery.parseJSON(response);
                    loadScenario(json);
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });

        var cancelbutton = $scenarioPanel.find('button[name="cancel"]');
        cancelbutton.hide();
        cancelbutton.click(function () {
            loadScenario(scenario);
        });

        var editbutton = $scenarioPanel.find('button[name="edit"]');
        editbutton.click(function () {
            enableScenarioEdit(savebutton, cancelbutton, editbutton);
        });

        if (editmode) {
            enableScenarioEdit(savebutton, cancelbutton, editbutton);
            cancelbutton.hide();
        }

        // calendar
        calendartbody[0].innerHTML = "";
        if (scenario.calendar != undefined && scenario.calendar.timeintervals != undefined) {

            $.each(scenario.calendar.timeintervals, function (idx, timeinterval) {
                var newtr = $calendarRow.clone();
                setTimeintervalElement(newtr, timeinterval);
                calendartbody.append(newtr);
            });
            $calendarPanel.find('button[name="add"]').click(function () {
                var timeinterval = {
                    "scenarioid": scenario.id,
                    "id": 0,
                    "name": "nuovo timeinterval",
                    "enabled": false,
                    "priority": 0,
                };
                postData("timeinterval", timeinterval, function (result, response) {
                    if (result) {
                        var json = jQuery.parseJSON(response);
                        getScenario(json.scenarioid, function (scenario) {
                            loadScenario(scenario);
                        })
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });
        }

        // scenario trigger
        triggertbody[0].innerHTML = "";
        $.getJSON(systemServletPath + "?requestcommand=triggertypes", function (triggertypes) {

            if (scenario.triggers != undefined) {
                $.each(scenario.triggers, function (idx, elem) {
                    var newtr = $triggerRow.clone();
                    setTriggerElement(newtr, elem, triggertypes);
                    triggertbody.append(newtr);
                });
                $triggerPanel.find('button[name="add"]').click(function () {
                    var trigger = {
                        "scenarioid": scenario.id,
                        "id": 0,
                        "name": "nuovo trigger",
                        "enabled": false,
                        "priority": 0,
                    };
                    postData("trigger", trigger, function (result, response) {
                        if (result) {
                            var json = jQuery.parseJSON(response);
                            getScenario(json.scenarioid, function (scenario) {
                                loadScenario(scenario);
                            })
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    });
                });
            }
        });

        // programs
        programstbody[0].innerHTML = "";
        $.each(scenario.programs, function (idx, elem) {
            var newtr = $programRow.clone();
            setProgramElement(newtr, elem/*, scenario*/);
            programstbody.append(newtr);
        });
        $programPanel.find('button[name="add"]').click(function () {
            var program = {
                "scenarioid": scenario.id,
                "id": 0,
                "name": "nuovo programma",
                "enabled": false,
                "priority": 0,
            };
            postData("program", program, function (result, response) {
                if (result) {
                    //notification.find('label[name="description"]').text("program salvata");
                    var json = jQuery.parseJSON(response);
                    getScenario(scenario.id, function (scenario) {
                        loadScenario(scenario);
                    });
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });
    });
}

// SCENARIO PROGRAM
function enableProgramEdit(savebutton, cancelbutton, editbutton) {
    savebutton.show();
    cancelbutton.show();
    editbutton.hide();
    $programPanel.find('input[name="name"]').prop('disabled', false);
    $programPanel.find('input[name="enabled"]').prop('disabled', false);
    $programPanel.find('textarea[name="description"]').prop('disabled', false);
    $programPanel.find('input[name="priority"]').prop('disabled', false);
    $programPanel.find('p[class="help-block"]').show();
    $programPanel.find('input[name="sunday"]').prop('disabled', false);
    $programPanel.find('input[name="monday"]').prop('disabled', false);
    $programPanel.find('input[name="tuesday"]').prop('disabled', false);
    $programPanel.find('input[name="wednesday"]').prop('disabled', false);
    $programPanel.find('input[name="thursday"]').prop('disabled', false);
    $programPanel.find('input[name="friday"]').prop('disabled', false);
    $programPanel.find('input[name="saturday"]').prop('disabled', false);
}

function addTimeRange(timerange, timerangelistdiv, sensors, zones, instructiontypes, editmode) {

    if (editmode == undefined) {
        editmode = false;
    }

    var trpanel = $timerangePanel.clone();
    trpanel.find('p[name="headingright"]').text(timerange.programid + "." + timerange.id);

    var tr = $timerange.clone();
    tr.find('input[name="name"]').val(timerange.name);
    tr.find('textarea[name="description"]').val(timerange.description);
    tr.find('input[name="enabled"]').prop('checked', timerange.enabled);

    tr.find('input[name="starttime"]').timepicker({
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

    tr.find('input[name="endtime"]').timepicker({
        timeFormat: 'HH:mm',
        interval: 15,
        minTime: '00:00',
        maxTime: '23:59',
        defaultTime: '23:59',
        startTime: '00:00',
        dynamic: true,
        dropdown: true,
        scrollbar: true
    });

    if (timerange.starttime != null) tr.find('input[name="starttime"]').val(timerange.starttime);
    if (timerange.endtime != null) tr.find('input[name="endtime"]').val(timerange.endtime);

    // save button
    var trsavebutton = tr.find('button[name="save"]');
    trsavebutton.hide();
    tr.find('input').prop('disabled', true);
    tr.find('textarea').prop('disabled', true);
    tr.find('select').prop('disabled', true);
    tr.find('p[class="help-block"]').hide();
    trsavebutton.click(function () {
        timerange.name = tr.find('input[name="name"]').val();
        timerange.description = tr.find('textarea[name="description"]').val();
        timerange.enabled = tr.find('input[name="enabled"]').prop('checked');
        timerange.starttime = tr.find('input[name="starttime"]').val();
        timerange.endtime = tr.find('input[name="endtime"]').val();
        postData("timerange", timerange, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("timerange salvato");
                var json = jQuery.parseJSON(response);
                getProgram(json.programid, function (program) {
                    loadScenarioProgram(program);
                })
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        });
    });

    var trcancelbutton = tr.find('button[name="cancel"]');
    trcancelbutton.hide();
    trcancelbutton.click(function () {
        loadScenarioProgram(program);
    });

    var treditbutton = tr.find('button[name="edit"]');
    treditbutton.click(function () {
        trsavebutton.show();
        trcancelbutton.show();
        treditbutton.hide();
        tr.find('input[name="name"]').prop('disabled', false);
        tr.find('input[name="enabled"]').prop('disabled', false);
        tr.find('textarea[name="description"]').prop('disabled', false);
        tr.find('input[name="starttime"]').prop('disabled', false);
        tr.find('input[name="endtime"]').prop('disabled', false);

        tr.find('p[class="help-block"]').show();
    });

    var trdeletetbutton = tr.find('button[name="deletetimerange"]');
    trdeletetbutton.click(function () {
        postData("timerange", timerange, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("timerange cancellato");
                var json = jQuery.parseJSON(response);
                loadScenarioProgram(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        }, "delete");

    });

    ////
    trpanel.find('div[name="body"]').append(tr);
    timerangelistdiv.append(trpanel);
    var tbody = tr.find('tbody[name="instructionlist"]');
    tbody[0].innerHTML = "";


    if (timerange.programinstructions != null) {
        $.each(timerange.programinstructions, function (idx, elem) {
            var instruction = $instructionRow.clone();
            instruction.find('td[name="id"]').text(elem.id);
            instruction.find('td[name="type"]').text(elem.type);
            instruction.find('td[name="name"]').text(elem.name);
            instruction.find('td[name="actuatorid"]').text(elem.actuatorid + "(" + elem.actuatorname + ")");
            instruction.find('td[name="targetvalue"]').text(elem.targetvalue);
            instruction.find('td[name="zoneid"]').text(elem.zoneid + "(" + elem.zonename + ")");
            instruction.find('td[name="priority"]').text(elem.priority);
            /*instruction.click(function () {
             loadScenarioProgramAction(elem, sensors, zones, instructiontypes);
             });*/
            instruction.find('button[name="detail"]').click(function () {
                loadScenarioProgramAction(elem, sensors, zones, instructiontypes);
            });
            instruction.find('button[name="deleteinstruction"]').click(function () {

                postData("instruction", elem, function (result, response) {
                    if (result) {
                        notification.find('label[name="description"]').text("istruzione salvata");
                        var json = jQuery.parseJSON(response);
                        getProgram(timerange.programid, function (program) {
                            loadScenarioProgram(program);
                        });
                    } else {
                        notification.show();
                        //notification.find('label[name="description"]').text(response);
                    }
                }, "delete");


            });
            tbody.append(instruction);
        });
    }

    tr.find('button[name="addinstruction"]').click(function () {
        var instruction = {
            "timerangeid": timerange.id,
            "id": 0,
            "name": "nuova instruzione",
            "type": "delayalarm",
        };
        postData("instruction", instruction, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("istruzione salvata");
                var json = jQuery.parseJSON(response);
                getTimeRange(json.timerangeid, function (timerange) {
                    getProgram(timerange.programid, function (program) {
                        loadScenarioProgram(program);
                    });
                });
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        });
        //setInstructionElement(instruction, elem, sensors, zones, instructiontypes);
        //tbody.append(instruction);
        //loadScenarioProgramAction(instruction, sensors, zones, instructiontypes, true);
    });
}


function getTimeRange(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=timerange&id=" + id, function (timerange) {
        callback(timerange);
    });
}

function getProgram(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=program&id=" + id, function (program) {
        callback(program);
    });
}

function getScenario(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (scenario) {
        callback(scenario);
    });
}


function loadScenarioProgram(program, editmode) {

    $("#result").load("scenarioprogram.html", function () {
        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getScenario(program.scenarioid, function (scenario) {
                loadScenario(scenario);
            });
        });

        pagetitle.text('Programma');
        notification.hide();

        $programPanel = $(this).find('div[id="programpanel"]');
        $programPanel.find('p[name="headingright"]').text(program.scenarioid + "." + program.id);

        $programPanel.find('input[name="name"]').val(program.name);
        $programPanel.find('textarea[name="description"]').val(program.description);
        $programPanel.find('input[name="enabled"]').prop('checked', program.enabled);
        $programPanel.find('input[name="priority"]').val(program.priority);

        $programPanel.find('input[name="sunday"]').prop('checked', program.sunday);
        $programPanel.find('input[name="monday"]').prop('checked', program.monday);
        $programPanel.find('input[name="tuesday"]').prop('checked', program.tuesday);
        $programPanel.find('input[name="wednesday"]').prop('checked', program.wednesday);
        $programPanel.find('input[name="thursday"]').prop('checked', program.thursday);
        $programPanel.find('input[name="friday"]').prop('checked', program.friday);
        $programPanel.find('input[name="saturday"]').prop('checked', program.saturday);

        // save button
        var savebutton = $programPanel.find('button[name="save"]');
        savebutton.hide();
        $programPanel.find('input').prop('disabled', true);
        $programPanel.find('textarea').prop('disabled', true);
        $programPanel.find('select').prop('disabled', true);
        $programPanel.find('p[class="help-block"]').hide();
        savebutton.click(function () {
            program.name = $programPanel.find('input[name="name"]').val();
            program.description = $programPanel.find('textarea[name="description"]').val();
            program.enabled = $programPanel.find('input[name="enabled"]').prop('checked');
            program.priority = $programPanel.find('input[name="priority"]').val();
            program.sunday = $programPanel.find('input[name="enabled"]').prop('sunday');
            program.monday = $programPanel.find('input[name="enabled"]').prop('monday');
            program.tuesday = $programPanel.find('input[name="enabled"]').prop('tuesday');
            program.wednesday = $programPanel.find('input[name="enabled"]').prop('wednesday');
            program.thursday = $programPanel.find('input[name="enabled"]').prop('thursday');
            program.friday = $programPanel.find('input[name="enabled"]').prop('friday');
            program.saturday = $programPanel.find('input[name="enabled"]').prop('saturday');
            postData("program", program, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("program salvata");
                    var json = jQuery.parseJSON(response);
                    loadScenarioProgram(json);
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(error);
                }
            });
        });

        var cancelbutton = $programPanel.find('button[name="cancel"]');
        cancelbutton.hide();
        cancelbutton.click(function () {
            loadScenarioProgram(program);
        });

        var editbutton = $programPanel.find('button[name="edit"]');
        editbutton.click(function () {
            enableProgramEdit(savebutton, cancelbutton, editbutton);

        });


        //$triggerPanel = $(this).find('div[id="timerangepanel"]');
        $timerangeRow = $programPanel.find('tr[name="timerange"]');
        var timerangebody = $programPanel.find('tbody[name="timeranges"]');
        timerangebody[0].innerHTML = "";


        if (program.timeranges != undefined) {
            $.each(program.timeranges, function (idx, elem) {
                var newtr = $timerangeRow.clone();
                setTimeRangeElement(newtr, elem);
                timerangebody.append(newtr);
            });

        }

        $programPanel.find('button[name="add"]').click(function () {
            var timerange = {
                "programid": program.id,
                "id": 0,
                "name": "nuovo trigger",
                "enabled": false,
                "priority": 0,
            };
            postData("timerange", timerange, function (result, response) {
                if (result) {
                    var json = jQuery.parseJSON(response);
                    getProgram(json.programid, function (program) {
                        loadScenarioProgram(program);
                    })
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });
        // timerange
        /*$timerangePanel = $(this).find('div[id="timerangepanel"]');
         $timerange = $(this).find('div[id="timerange"]');
         $instructionRow = $timerange.find('tr[name="instruction"]');
         $programinstructions = $(this).find('div[id="programinstructions"]');
         $timerangePanel.remove();
         $timerange.remove();

         var timerangelistdiv = $(this).find('div[name="timerangelist"]');
         timerangelistdiv[0].innerHTML = "xxx";

         var addtimerangebutton = $(this).find('button[name="addtimerange"]');
         $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
         $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
         $.getJSON(systemServletPath + "?requestcommand=instructiontypes", function (instructiontypes) {
         if (program.timeranges != null) {
         $.each(program.timeranges, function (idx, timerange) {


         addTimeRange(timerange, timerangelistdiv, sensors, zones, instructiontypes);
         });
         }

         addtimerangebutton.click(function () {
         var timerange = {
         "programid": program.id,
         "id": 0,
         "name": "nuovo time range",
         "priority": 0,
         "enabled": false,
         };
         postData("timerange", timerange, function (result, response) {
         if (result) {
         notification.find('label[name="description"]').text("program salvata");
         var json = jQuery.parseJSON(response);
         getProgram(json.programid, function (program) {
         loadScenarioProgram(program);
         })
         } else {
         notification.show();
         notification.find('label[name="description"]').text(error);
         }
         });
         });
         })
         });
         });*/
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

function loadScenarioProgramAction(action, sensors, zones, instructiontypes) {

    $("#result").load("scenarioprograminstruction.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getTimeRange(action.timerangeid, function (timerange) {
                loadScenarioProgramTimeRange(timerange);
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
        $instructionPanel.find('input[name="seconds"]').val(action.seconds);
        $instructionPanel.find('input[name="enabled"]').val(action.enabled);

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
            action.seconds = $instructionPanel.find('input[name="seconds"]').val();
            action.enabled = $instructionPanel.find('input[name="enabled"]').prop('checked');
            action.priority = $instructionPanel.find('input[name="priority"]').val();
            postData("instruction", action, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("istruction salvata");
                    var json = jQuery.parseJSON(response);
                    loadScenarioProgramAction(json, sensors, zones, instructiontypes);

                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }

            });
        });

        var cancelbutton = $instructionPanel.find('button[name="cancel"]');
        cancelbutton.hide();
        cancelbutton.click(function () {
            loadScenarioProgramAction(action, sensors, zones, instructiontypes);
        });

        var editbutton = $instructionPanel.find('button[name="edit"]');
        editbutton.click(function () {
            enableInstructionEdit(savebutton, cancelbutton, editbutton);
        });
    });
}

function loadScenarioTimeinterval(timeinterval) {

    $("#result").load("scenariotimeinterval.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getScenario(timeinterval.scenarioid, function (scenario) {
                loadScenario(scenario);
            })
        });
        pagetitle.text('Time interva');
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
        savebutton.hide();
        panel.find('input').prop('disabled', true);
        panel.find('textarea').prop('disabled', true);
        panel.find('select').prop('disabled', true);
        panel.find('p[class="help-block"]').hide();
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
                    loadScenarioTimeinterval(json);
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });

        var cancelbutton = panel.find('button[name="cancel"]');
        cancelbutton.hide();
        cancelbutton.click(function () {
            loadScenarioTimeinterval(timeinterval);
        });

        var editbutton = panel.find('button[name="edit"]');
        editbutton.click(function () {
            savebutton.show();
            cancelbutton.show();
            editbutton.hide();
            panel.find('input[name="name"]').prop('disabled', false);
            panel.find('textarea[name="description"]').prop('disabled', false);
            panel.find('select[name="type"]').prop('disabled', false);
            panel.find('input[name="value"]').prop('disabled', false);
            panel.find('input[name="status"]').prop('disabled', false);
            panel.find('input[name="startdatetime"]').prop('disabled', false);
            panel.find('input[name="enddatetime"]').prop('disabled', false);
            panel.find('input[name="enabled"]').prop('disabled', false);
            panel.find('input[name="priority"]').prop('disabled', false);
            panel.find('input[name="sunday"]').prop('disabled', false);
            panel.find('input[name="monday"]').prop('disabled', false);
            panel.find('input[name="tuesday"]').prop('disabled', false);
            panel.find('input[name="wednesday"]').prop('disabled', false);
            panel.find('input[name="thursday"]').prop('disabled', false);
            panel.find('input[name="friday"]').prop('disabled', false);
            panel.find('input[name="saturday"]').prop('disabled', false);
            panel.find('p[class="help-block"]').show();
        });
    });
}

function loadScenarioTrigger(trigger, triggertypes) {

    $("#result").load("scenariotrigger.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getScenario(trigger.scenarioid, function (scenario) {
                loadScenario(scenario);
            })
        });
        pagetitle.text('Trigger');
        notification.hide();

        var panel = $(this).find('div[id="triggerpanel"]');
        panel.find('p[name="headingright"]').text(trigger.scenarioid + "." + trigger.id);
        panel.find('input[name="name"]').val(trigger.name);
        panel.find('textarea[name="description"]').val(trigger.description);
        panel.find('select[name="type"]').val(trigger.type);
        $.each(triggertypes, function (val, triggertype) {
            panel.find('select[name="type"]').append(new Option(triggertype.name, triggertype.id));
        });
        panel.find('input[name="priority"]').val(trigger.priority);
        panel.find('input[name="value"]').val(trigger.value);
        panel.find('input[name="enabled"]').val(trigger.enabled);

        // save button
        var savebutton = panel.find('button[name="save"]');
        savebutton.hide();
        panel.find('input').prop('disabled', true);
        panel.find('textarea').prop('disabled', true);
        panel.find('select').prop('disabled', true);
        panel.find('p[class="help-block"]').hide();
        savebutton.click(function () {
            trigger.name = panel.find('input[name="name"]').val();
            trigger.description = panel.find('textarea[name="description"]').val();
            trigger.type = panel.find('select[name="type"]').val();
            trigger.value = panel.find('input[name="value"]').val();
            trigger.status = panel.find('input[name="status"]').val();
            trigger.enabled = panel.find('input[name="enabled"]').prop('checked');
            trigger.priority = panel.find('input[name="priority"]').val();
            postData("trigger", trigger, function (result, response) {
                if (result) {
                    notification.find('label[name="description"]').text("istruction salvata");
                    var json = jQuery.parseJSON(response);
                    loadScenario(json);
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });

        var cancelbutton = panel.find('button[name="cancel"]');
        cancelbutton.hide();
        cancelbutton.click(function () {
            loadScenarioTrigger(trigger, triggertypes);
        });

        var editbutton = panel.find('button[name="edit"]');
        editbutton.click(function () {
            savebutton.show();
            cancelbutton.show();
            editbutton.hide();
            panel.find('input[name="name"]').prop('disabled', false);
            panel.find('textarea[name="description"]').prop('disabled', false);
            panel.find('select[name="type"]').prop('disabled', false);
            panel.find('input[name="value"]').prop('disabled', false);
            panel.find('input[name="status"]').prop('disabled', false);
            panel.find('input[name="enabled"]').prop('disabled', false);
            panel.find('input[name="priority"]').prop('disabled', false);
            panel.find('p[class="help-block"]').show();
        });
    });
}


function loadScenarioProgramTimeRange(timerange) {

    $("#result").load("scenarioprogramtimerange.html", function () {

            // back button
            backbutton.unbind("click");
            backbutton.click(function () {
                getProgram(timerange.programid, function (program) {
                    loadScenarioProgram(program);
                })
            });
            pagetitle.text('Fascia oraria programma');
            notification.hide();

            panel = $(this).find('div[id="timerangepanel"]');
            panel.find('p[name="headingright"]').text(timerange.scenarioid + "." + timerange.id);
            panel.find('input[name="name"]').val(timerange.name);
            panel.find('textarea[name="description"]').val(timerange.description);
            panel.find('input[name="enabled"]').prop('checked', timerange.enabled);
            panel.find('input[name="starttime"]').timepicker({
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
            panel.find('input[name="endtime"]').timepicker({
                timeFormat: 'HH:mm',
                interval: 15,
                minTime: '00:00',
                maxTime: '23:59',
                defaultTime: '23:59',
                startTime: '00:00',
                dynamic: true,
                dropdown: true,
                scrollbar: true
            });
            if (timerange.starttime != null) panel.find('input[name="starttime"]').val(timerange.starttime);
            if (timerange.endtime != null) panel.find('input[name="endtime"]').val(timerange.endtime);

            // save button
            var savebutton = panel.find('button[name="save"]');
            savebutton.hide();
            panel.find('input').prop('disabled', true);
            panel.find('textarea').prop('disabled', true);
            panel.find('select').prop('disabled', true);
            panel.find('p[class="help-block"]').hide();
            savebutton.click(function () {
                timerange.name = panel.find('input[name="name"]').val();
                timerange.description = panel.find('textarea[name="description"]').val();
                timerange.enabled = panel.find('input[name="enabled"]').prop('checked');
                timerange.starttime = panel.find('input[name="starttime"]').val();
                timerange.endtime = panel.find('input[name="endtime"]').val();

                var tr = panel.find('tr[name="instruction"]');
                $('#tbl2 > tbody  > tr').each(function (n, elem) {
               // $.each(tr, function (n, elem) {
                    timerange.programinstructions[n].name = $(this).find('td').find('input[name="name"]').val();





                });
                postData("timerange", timerange, function (result, response) {
                    if (result) {
                        notification.find('label[name="description"]').text("istruction salvata");
                        var json = jQuery.parseJSON(response);
                        loadScenarioProgramTimeRange(json);
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });

            var cancelbutton = panel.find('button[name="cancel"]');
            cancelbutton.hide();
            cancelbutton.click(function () {
                loadScenarioProgramTimeRange(timerange);
            });

            var editbutton = panel.find('button[name="edit"]');
            editbutton.click(function () {
                savebutton.show();
                cancelbutton.show();
                editbutton.hide();
                panel.find('input').prop('disabled', false);
                panel.find('select').prop('disabled', false);
            });

            // program instructions
            $instructionRow = panel.find('tr[name="instruction"]');
            var tbody = panel.find('tbody[name="instructionlist"]');
            tbody[0].innerHTML = "";
            if (timerange.programinstructions != null) {
                $.each(timerange.programinstructions, function (idx, elem) {
                    var instruction = $instructionRow.clone();
                    instruction.find('td[name="id"]').text(elem.id);
                    //instruction.find('td[name="status"]').text(elem.status);
                    instruction.find('input[name="type"]').text(elem.type);
                    $.getJSON(systemServletPath + "?requestcommand=instructiontypes", function (instructiontypes) {
                        $.each(instructiontypes, function (val, instructiontype) {
                            instruction.find('select[name="type"]').append(new Option(instructiontype.description, instructiontype.instruction));
                        });
                    });
                    instruction.find('input[name="name"]').text(elem.name);
                    instruction.find('select[name="actuatorid"]').text(elem.actuatorid + "(" + elem.actuatorname + ")");
                    $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
                        $.each(sensors, function (val, sensor) {
                            instruction.find('select[name="actuatorid"]').append(new Option(sensor.name, sensor.id));
                        });
                    });

                    instruction.find('input[name="targetvalue"]').text(elem.targetvalue);
                    instruction.find('input[name="zoneid"]').text(elem.zoneid + "(" + elem.zonename + ")");
                    $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
                        $.each(zones, function (val, zone) {
                            instruction.find('select[name="zoneid"]').append(new Option(zone.name, zone.id));
                        });
                    });
                    instruction.find('input[name="thresholdvalue"]').text(elem.thresholdvalue);
                    instruction.find('input[name="priority"]').text(elem.priority);
                    instruction.find('button[name="detail"]').click(function () {
                        $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
                            $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
                                $.getJSON(systemServletPath + "?requestcommand=instructiontypes", function (instructiontypes) {
                                    loadScenarioProgramAction(elem, sensors, zones, instructiontypes);
                                });
                            });
                        });
                    });
                    instruction.find('button[name="deleteinstruction"]').click(function () {

                        postData("instruction", elem, function (result, response) {
                            if (result) {
                                notification.find('label[name="description"]').text("istruzione salvata");
                                var json = jQuery.parseJSON(response);
                                //getProgram(timerange.programid, function (program) {
                                loadScenarioProgramTimeRange(json);
                                //});
                            } else {
                                notification.show();
                                //notification.find('label[name="description"]').text(response);
                            }
                        }, "delete");


                    });
                    tbody.append(instruction);
                });
            }

            panel.find('button[name="addinstruction"]').click(function () {
                var instruction = {
                    "timerangeid": timerange.id,
                    "id": 0,
                    "name": "nuova instruzione",
                    "type": "delayalarm",
                };
                postData("instruction", instruction, function (result, response) {
                    if (result) {
                        notification.find('label[name="description"]').text("istruzione salvata");
                        var json = jQuery.parseJSON(response);
                        getTimeRange(json.timerangeid, function (timerange) {
                            loadScenarioProgramTimeRange(timerange);
                        })
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
                //setInstructionElement(instruction, elem, sensors, zones, instructiontypes);
                //tbody.append(instruction);
                //loadScenarioProgramAction(instruction, sensors, zones, instructiontypes, true);
            });


        }
    );
}

function setTimeRangeElement(element, timerange) {

    element.find('td[name="id"]').text(timerange.id);
    element.find('td[name="enabled"]').text(timerange.enabled);
    element.find('td[name="starttime"]').text(timerange.starttime);
    element.find('td[name="endtime"]').text(timerange.endtime);
    element.find('td[name="name"]').text(timerange.name);
    element.find('td[name="description"]').text(timerange.description);
    element.find('td[name="status"]').text(timerange.status);
    element.find('td[name="action"]').text(timerange.actionstatus);

    element.find('button[name="edittimerange"]').click(function () {
        loadScenarioProgramTimeRange(timerange);
    });
    element.find('button[name="deletetimerange"]').click(function () {
        postData("timerange", timerange, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("timeinterval eliminato");
                var json = jQuery.parseJSON(response);
                loadScenarioProgram(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(error);
            }
        }, "delete");
    });
}

function setTimeintervalElement(element, timeinterval) {

    element.find('td[name="id"]').text(timeinterval.id);
    element.find('td[name="name"]').text(timeinterval.name);
    element.find('td[name="days"]').text(getDays(timeinterval));
    element.find('td[name="starttime"]').text(timeinterval.startdatetime);
    element.find('td[name="endtime"]').text(timeinterval.enddatetime);
    element.find('td[name="priority"]').text(timeinterval.priority);
    element.find('td[name="status"]').text(timeinterval.status);

    element.find('button[name="edittimeinterval"]').click(function () {
        loadScenarioTimeinterval(timeinterval)
    });
    element.find('button[name="deletetimeinterval"]').click(function () {
        postData("timeinterval", timeinterval, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("timeinterval eliminato");
                var json = jQuery.parseJSON(response);
                loadScenario(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(error);
            }
        }, "delete");
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


// INSTRUCTIONS
function loadInstructions(scenarioid) {

    $("#result").load("instructions.html", function () {
        $instructionsPanel = $(this).find('div[id="instructionspanel"]');
        $instructionRow = $instructionsPanel.find('tr[name="instruction"]');

        var tbody = $instructionsPanel.find('tbody[name="instructionlist"]');
        $.getJSON(systemServletPath + "?requestcommand=instructions&id=" + scenarioid + "&scenarioid=" + scenarioid, function (data) {
            tbody[0].innerHTML = "";

            $.each(data, function (idx, elem) {
                var newtr = $instructionRow.clone();
                setInstructionElement(newtr, elem);
                tbody.append(newtr);
            });
        })
            .done(function () {
            })
            .fail(function () {
                alert("cannot load scenarios");
            })
            .always(function () {
            });
    });
}

function setInstructionElement(element, instruction, sensors, zones, instructiontypes) {

    element.find('td[name="id"]').text(instruction.id);
    element.find('td[name="type"]').text(instruction.type);
    element.find('td[name="name"]').text(instruction.name);
    element.find('td[name="actuatorid"]').text(instruction.actuatorid + "(" + instruction.actuatorname + ")");
    element.find('td[name="targetvalue"]').text(instruction.targetvalue);
    element.find('td[name="zoneid"]').text(instruction.zoneid + "(" + instruction.zonename + ")");
    element.find('td[name="priority"]').text(instruction.priority);
    /*element.click(function () {
     loadScenarioProgramAction(instruction, sensors, zones, instructiontypes);
     });*/

    element.find('button[name="detail"]').click(function () {
        loadScenarioProgramAction(instruction, sensors, zones, instructiontypes);
    });
    element.find('button[name="deleteinstruction"]').click(function () {
    });

}

function setProgramElement(element, program/*, scenario*/) {

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
        loadScenarioProgram(program);
    });

    element.find('button[name="details"]').click(function () {
        loadScenarioProgram(program);
    });
    element.find('button[name="delete"]').click(function () {
        postData("program", program, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("trigger eliminato");
                var json = jQuery.parseJSON(response);
                loadScenario(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(error);
            }


        }, "delete");
    });
}

function setTriggerElement(element, trigger, triggertype) {

    element.find('td[name="id"]').text(trigger.id);
    element.find('td[name="scenarioid"]').text(trigger.scenarioid);
    element.find('td[name="name"]').text(trigger.name);
    element.find('td[name="description"]').text(trigger.description);
    element.find('td[name="type"]').text(trigger.type);
    element.find('td[name="status"]').text(trigger.status);
    element.find('td[name="value"]').text(trigger.value);
    element.find('td[name="priority"]').text(trigger.priority);
    element.find('td[name="enabled"]').text(trigger.enabled);
    element.find('td[name="status"]').text(trigger.status);

    /*element.click(function () {
     loadScenarioTrigger(trigger, triggertype);
     });*/

    element.find('button[name="details"]').click(function () {
        loadScenarioTrigger(trigger, triggertype);
    });

    element.find('button[name="delete"]').click(function () {
        postData("trigger", trigger, function (result, response) {
            if (result) {
                notification.find('label[name="description"]').text("trigger eliminato");
                var json = jQuery.parseJSON(response);
                loadScenario(json);
            } else {
                notification.show();
                notification.find('label[name="description"]').text(error);
            }
        }, "delete");
    });
}

function getFormattedDate() {
    var date = new Date();

    var month = date.getMonth() + 1;
    var day = date.getDate();
    var hour = date.getHours();
    var min = date.getMinutes();
    var sec = date.getSeconds();

    month = (month < 10 ? "0" : "") + month;
    day = (day < 10 ? "0" : "") + day;
    hour = (hour < 10 ? "0" : "") + hour;
    min = (min < 10 ? "0" : "") + min;
    sec = (sec < 10 ? "0" : "") + sec;

    var str = date.getFullYear() + "-" + month + "-" + day + "_" + hour + ":" + min + ":" + sec;

    /*alert(str);*/

    return str;
}

function getFormattedTime(date) {
    //var date = new Date();

    var hour = date.getHours();
    var min = date.getMinutes();
    //var sec = date.getSeconds();

    hour = (hour < 10 ? "0" : "") + hour;
    min = (min < 10 ? "0" : "") + min;
    //sec = (sec < 10 ? "0" : "") + sec;

    var str = hour + ":" + min;// + ":" + sec;

    /*alert(str);*/

    return str;
}


var func = function (obj) {
    console.log(JSON.stringify(obj));
};
$("input[type='submit']").click(function () {
    func($("form").toJSO());
    func($("form").serialize());
    return false;
});




