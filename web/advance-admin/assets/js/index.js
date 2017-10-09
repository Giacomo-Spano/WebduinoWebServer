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

var $scenarioPanel;
var $timeintervalRow;

var $instructionsPanel;
var $instructionRow;

var $sensorsPanel;
var $sensorRow;

var $shieldsPanel;
var $shieldRow;

var client;

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

    //var myJSONForm =  $('form[id="prova"]');
    //var ret = myJSONForm.toJSON();
    /*client = new Paho.MQTT.Client("location.hostname, Number(location.port), "clientIdjsxxxx");
     client.onConnectionLost = onConnectionLost;
     client.onMessageArrived = onMessageArrived;
     client.connect({onSuccess:onConnect});*/


    /*MqttMessage message = new MqttMessage();
     message.setPayload("A single message".getBytes());
     client.publish("pahodemo/test", message);
     client.disconnect();*/


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


    /*$("#result").load("zones.html", function () {
     //alert( "Load was performed." );
     $zonesPanel = $(this).find('div[id="zonespanel"]');
     $zoneRow = $zonesPanel.find('tr[name="zone"]');
     loadZones();
     });*/
}

function postData(datatype, json, callback) {

    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {

            var json = JSON.parse(this.response);
            if (json.answer = 'success') {

                callback(true);
                //return true
            } else {
                //element.find('td[name="commandstatus"]').text("command failed");
                callback(false);
            }
        }
    };

    xhttp.open("POST", systemServletPath + "?data=" + datatype, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    var str = JSON.stringify(json);
    //xhttp.send(commandJson.toString());
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

function scenarioProgramForm(program) {

    loadForm("scenarioprogramform.html", function () {

        // back button
        $(this).find('button[name="backbutton"]').click(function () {
            loadScenario(program.scenarioid);
        });

        addSection("Dati generali");

        addNumInput("id", program.id, "Id", 0, 99999, 1, true);
        addTextInput("name", program.name, "Nome");
        addNumInput("scenarioid", program.scenarioid, "Id scenario", 0, 99999, 1, true);

    }, function (json, button) {
        if (button == 'save') {
            postData("program", json, function (result) {
                loadScenario(program.scenarioid);
            });
        }
    });
}

function scenarioTimeRangeForm(timerange, scenario, sensors, zones, instructiontypes) {

    loadForm("scenariotimerangeform.html", function () {

        addSection("Dati generali");

        addNumInput("id", timerange.id, "Id", 0, 99999, 1, true);
        addTextInput("name", timerange.name, "Nome");
        addNumInput("programid", timerange.programid, "Id programma", 0, 99999, 1, true);
        addTimeInput("starttime", timerange.starttime, "ora inizio");
        addTimeInput("endtime", timerange.starttime, "ora fine");
        addCheckBoxInput("enabled", timerange.enabled, "attiva");

        addSection("Istruzioni");

        if (timerange.programinstructions != null) {
            timerange.programinstructions.forEach(function (programinstruction) {
                createProgramInstruction(programinstruction, sensors, zones, instructiontypes);
            });
        }

    }, function (json, button) {
        if (button == 'save') {
            postData("timerange", json, function (result) {
                loadScenarios();
            });
        } else if (button == 'add') {
            var instruction = {
                "timerangeid": timerange.id,
                "type": instructiontypes[0],
                "id": 0,
                "actuatorid": 0,
                "targetvalue": 0,
                "zoneid": 0,
                "zonename": "",
                "name": "program instruction",
                "sunday": false,
                "monday": false,
                "tueday": false,
                "wedday": false,
                "thuday": false,
                "friday": false,
                "satday": false,
                "priority": 0,
                "schedule": false,
                "seconds": 0,
            };
            createProgramInstruction(instruction, sensors, zones, instructiontypes);
        }
    });
}

function scenarioForm(scenario, sensors, zones, instructiontypes) {

    loadForm("scenarioform.html", function () {

        addNumInput("id", scenario.id, "Id", 0, 99999, 1, true);
        addTextInput("name", scenario.name, "Nome");
        //addCheckBoxInput("dateenabled", scenario.calendar.dateenabled, "data");

        var calendarid = createItem("calendar", "Calendar");

        addSubitemCheckInput(calendarid, "dateenabled", scenario.calendar.dateenabled, "data");

        var startdate = null;
        if (scenario.calendar.startdate != null)
            startdate = new Date(scenario.calendar.startdate);
        addSubitemDateInput(calendarid, "startdate", startdate, "Data inizio");

        var enddate = null;
        if (scenario.calendar.enddate != null)
            enddate = new Date(scenario.calendar.enddate);
        addSubitemDateInput(calendarid, "enddate", enddate, "Data fine");

        scenario.calendar.timeintervals.forEach(function (p1) {
            id = createSubitem(calendarid, "timeintervals", "fascia oraria");
            addSubitemNumInput(id, "id", p1.id, "Id");
            addSubitemTextInput(id, "name", p1.name, "Name");
            addSubitemTimeInput(id, "starttime", p1.starttime, "start");
            addSubitemTimeInput(id, "endtime", p1.endtime, "end");
            addSubitemCheckInput(id, "sunday", p1.sunday, "S");
            addSubitemCheckInput(id, "monday", p1.monday, "M");
            addSubitemCheckInput(id, "tuesday", p1.tuesday, "T");
            addSubitemCheckInput(id, "wednesday", p1.wednesday, "W");
            addSubitemCheckInput(id, "thursday", p1.sunday, "T");
            addSubitemCheckInput(id, "friday", p1.friday, "F");
            addSubitemCheckInput(id, "saturday", p1.saturday, "S");
        });
        addSeparator(" ");

    }, function (json, button) {

        if (button == 'save') {
            postData("scenario", json, function (result) {
                loadScenario(scenario.id);
            });
        } else if (button == 'add') {

            var program = {
                "scenarioid": scenario.id,
                "id": 0,
                "name": "new program",
                "enabled": true,
            };
            createProgram(program);
        }
    });
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

// SHIELD
function loadShields() {

    $("#result").load("shields.html", function () {
        $shieldsPanel = $(this).find('div[id="shieldspanel"]');
        $shieldRow = $shieldsPanel.find('tr[name="shield"]');

        var tbody = $shieldsPanel.find('tbody[name="shieldlist"]');
        $.getJSON(systemServletPath + "?requestcommand=shields", function (data) {
            tbody[0].innerHTML = "";
            $.each(data, function (idx, elem) {
                var newtr = $shieldRow.clone();
                setShieldElement(newtr, elem);
                tbody.append(newtr);
            });
        })
            .done(function () {
            })
            .fail(function () {
                alert("cannot load shields");
            })
            .always(function () {
            });

        $shieldsPanel.find('button[id="edit"]').click(function () {


        });
    });
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

function loadShield(shieldid) {

    $("#result").load("shield.html", function () {
        $shieldPanel = $(this).find('div[id="shieldpanel"]');
        $sensorRow = $shieldPanel.find('tr[name="sensor"]');


        var tbody = $shieldPanel.find('tbody[name="sensorlist"]');
        $.getJSON(systemServletPath + "?requestcommand=sensors&id=" + shieldid, function (data) {
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

function setShieldElement(element, shield) {

    element.find('td[name="id"]').text(shield.shieldid);
    element.find('td[name="name"]').text(shield.shieldname);
    element.find('td[name="url"]').text(shield.url + ":" + shield.port);
    element.find('td[name="MACAddress"]').text(shield.macaddress);
    element.find('td[name="mqttserver"]').text(shield.mqttserver + ":" + shield.mqttport);
    // last update
    element.find('td[name="date"]').text(shield.lastupdate);
    element.find('td[name="swversion"]').text(shield.swversion);
    element.click(function () {
        loadShield(shield.shieldid)
    });


    var restartButton = element.find('button[name="restartbutton"]');
    restartButton.click(function () {

        var command = 'reboot'
        restartButton.text("sending" + command + " command...");
        var commandJson = {
            'shieldid': shield.shieldid,
            //'actuatorid': shield.id,
            'command': command,
        };
        sendShieldCommand(commandJson)
    });

    element.find('button[id="edit"]').click(function () {
        path = "./webduino_settings.html?id=" + shield.shieldid;
        window.location.href = path;
        //loadProva();
    });
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
    var text = "non attivo";
    if (scenario.active)
        var text = "non attivo";
    element.find('td[name="status"]').text(scenario.ac);

    element.click(function () {
        loadScenario(scenario.id)
    });
}

// SCENARIO
function loadScenario(id) {

    $("#result").load("scenario.html", function () {

        // back button
        $(this).find('button[name="backbutton"]').click(function () {
            loadScenarios();
        });

        $scenarioPanel = $(this).find('div[id="scenariopanel"]');

        $calendarPanel = $(this).find('div[id="calendarpanel"]');
        $calendarRow = $calendarPanel.find('tr[name="timeinterval"]');
        var calendartbody = $calendarPanel.find('tbody[name="timeintervallist"]');

        $programPanel = $(this).find('div[id="programpanel"]');
        $programRow = $programPanel.find('tr[name="program"]');
        var programstbody = $(this).find('div[id="programpanel"]').find('tbody[name="programlist"]');

        $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (data) {

            var scenario = data;
            $scenarioPanel.find('span[id="name"]').innerHTML = data.name;
            $scenarioPanel.find('span[id="name"]').focus(function(){
                //$(this).append("<li>Item Focused</li>");
                $(this).next().click(function () {
                    scenario.name = $(this).text();
                    postData("scenario", scenario/*JSON.stringify(scenario)*/, function (result) {

                        loadScenario(scenario.id);
                    });
                });
            });

            /*postData("scenario", json, function (result) {
                loadScenario(scenario.id);
            });*/



            $scenarioPanel.find('span[id="id"]')[0].innerHTML = data.id;
            if (data.active)
                $scenarioPanel.find('span[id="status"]')[0].innerHTML = "Attivo";
            else
                $scenarioPanel.find('span[id="status"]')[0].innerHTML = "Non attivo";
            if (data.dateenabled) {
                if (data.startdate)
                    $scenarioPanel.find('span[id="startdate"]')[0].innerHTML = data.startdate;
                else
                    $scenarioPanel.find('span[id="startdate"]')[0].innerHTML = "--";
                if (data.startdate)
                    $scenarioPanel.find('span[id="enddate"]')[0].innerHTML = data.enddate;
                else
                    $scenarioPanel.find('span[id="endtdate"]')[0].innerHTML = "--";
            }
            $scenarioPanel.find('button[name="edit"]').click(function () {
                $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
                    $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
                        $.getJSON(systemServletPath + "?requestcommand=instructiontypes", function (instructiontypes) {
                            scenarioForm(scenario, sensors, zones, instructiontypes);
                        })
                    });
                });
            });

            // calendar
            calendartbody[0].innerHTML = "";
            $.each(scenario.calendar.timeintervals, function (idx, elem) {
                var newtr = $timeintervalRow.clone();
                setTimeintervalElement(newtr, id, elem);
                calendartbody.append(newtr);
            });
            $calendarPanel.find('button[name="add"]').click(function () {
                $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
                    $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
                        $.getJSON(systemServletPath + "?requestcommand=instructiontypes", function (instructiontypes) {
                            var timeinterval = {
                                "scenarioid": scenario.id,
                                "id": 0,
                                "name": "program",
                                "enabled": true,
                            };
                            //scenarioTimeRangeForm(program, scenario, sensors, zones, instructiontypes);
                        });
                    });
                });
            });

            // programs
            programstbody[0].innerHTML = "";
            $.each(scenario.programs, function (idx, elem) {
                var newtr = $programRow.clone();
                setProgramElement(newtr, elem, scenario);
                programstbody.append(newtr);
            });
            $programPanel.find('button[name="add"]').click(function () {
                var program = {
                    "scenarioid": scenario.id,
                    "id": 0,
                    "name": "nuovo programma",
                    "enabled": true,
                };
                //scenarioProgramForm(program, sensors, zones, instructiontypes);
                postData("program", program, function (result) {
                    loadScenario(scenario.id);
                });

            });

        });
    });
}

// SCENARIO PROGRAM
function loadScenarioProgram(program, scenario) {

    $("#result").load("scenarioprogram.html", function () {

        // back button
        $(this).find('button[name="backbutton"]').click(function () {
            loadScenario(program.scenarioid);
        });

        $scenarioPanel = $(this).find('div[id="scenariopanel"]');
        $instructionRow = $scenarioPanel.find('tr[name="instruction"]');

        $timerange = $(this).find('div[id="timerange"]');
        $programinstructions = $(this).find('div[id="programinstructions"]');

        $timerange.remove();

        var timerangelistdiv = $scenarioPanel.find('div[name="timerangelist"]');

        $scenarioPanel.find('span[id="name"]')[0].innerHTML = program.name;
        $scenarioPanel.find('span[id="id"]')[0].innerHTML = program.id;
        $scenarioPanel.find('span[id="scenarioid"]')[0].innerHTML = program.scenarioid;
        $scenarioPanel.find('span[id="enabled"]')[0].innerHTML = program.enabled;

        timerangelistdiv[0].innerHTML = "";

        if (program.timeranges != null) {
            $.each(program.timeranges, function (idx, timerange) {
                var tr = $timerange.clone();
                tr.find('span[name="id"]').text(timerange.id);
                tr.find('span[name="name"]').text(timerange.name);
                if (timerange.startdate != null) tr.find('span[name="startdate"]').text(timerange.startdate);
                if (timerange.enddate != null) tr.find('span[name="enddate"]').text(timerange.enddate);
                timerangelistdiv.append(tr);

                var tbody = tr.find('tbody[name="instructionlist"]');
                tbody[0].innerHTML = "";
                if (timerange.programinstructions != null) {

                    $.each(timerange.programinstructions, function (idx, elem) {
                        var pi = $instructionRow.clone();
                        setInstructionElement(pi, elem);
                        tbody.append(pi);
                    });
                }

                tr.find('button[name="edit"]').click(function () {
                    $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {
                        $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {
                            $.getJSON(systemServletPath + "?requestcommand=instructiontypes", function (instructiontypes) {
                                scenarioTimeRangeForm(timerange, scenario, sensors, zones, instructiontypes);
                            })
                        });
                    });
                });

            });
        }

        $scenarioPanel.find('button[id="edit"]').click(function () {
            scenarioProgramForm(program);
        });

    });
}


function setTimeintervalElement(element, scenarioid, timeinterval) {

    element.find('td[name="id"]').text(timeinterval.id);
    element.find('td[name="name"]').text(timeinterval.name);
    element.find('td[name="days"]').text(getDays(timeinterval));
    element.find('td[name="starttime"]').text(timeinterval.starttime);
    element.find('td[name="endtime"]').text(timeinterval.endtime);

    element.click(function () {
        loadInstructions(scenarioid)
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

function setInstructionElement(element, instruction) {

    element.find('td[name="id"]').text(instruction.id);
    element.find('td[name="type"]').text(instruction.type);
    element.find('td[name="name"]').text(instruction.name);
    element.find('td[name="actuatorid"]').text(instruction.actuatorid + "(" + instruction.actuatorname + ")");
    element.find('td[name="targetvalue"]').text(instruction.targetvalue);
    element.find('td[name="zoneid"]').text(instruction.zoneid + "(" + instruction.zonename + ")");
    element.find('td[name="seconds"]').text(instruction.seconds);

    element.find('td[name="schedule"]').text(instruction.schedule);
    element.find('td[name="days"]').text(getDays(instruction));
    element.find('td[name="priority"]').text(instruction.priority);


}

function setProgramElement(element, program, scenario) {

    element.find('td[name="id"]').text(program.id);
    element.find('td[name="scenarioid"]').text(program.scenarioid);
    element.find('td[name="name"]').text(program.name);
    element.find('td[name="enabled"]').text(program.enabled);
    element.click(function () {
        loadScenarioProgram(program, scenario)
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

