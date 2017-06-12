/**
 * Created by giaco on 28/05/2017.
 */
var systemServletPath = "../system";

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
var $ssensorRow;

function deactivatemenuitems() {
    $('a[id="item_home"]').attr("class", "");
    $('a[id="item_dashboard"]').attr("class", "");
    $('a[id="item_scenarios"]').attr("class", "");
    $('a[id="item_zones"]').attr("class", "");
}

function load() {

    //var myJSONForm =  $('form[id="prova"]');
    //var ret = myJSONForm.toJSON();

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

    xhttp.open("POST", systemServletPath + "?data="+datatype, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    var str = JSON.stringify(json);
    //xhttp.send(commandJson.toString());
    xhttp.send(str);
}

function scenarioForm(scenario, sensors, zones) {

    loadForm("scenarioform.html", function (/*json*/) {

        addNumInput("id", scenario.id, "Id");
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

        scenario.programinstructions.forEach(function (p1) {
            id = createSubitem(0, "programinstructions", "Programma");
            addSubitemNumInput(id, "id", p1.id, "Id");
            addSubitemNumInput(id, "scenarioid", p1.scenarioid, "Id Scenario");
            addSubitemTextInput(id, "type", p1.type, "Type");
            addSubitemTextInput(id, "name", p1.name, "Name");

            var list = [];
            for (i = 0; i < sensors.length; i++) {
                var sensor = [];
                sensor.push(sensors[i].id);
                var name = "" + sensors[i].id + "." + sensors[i].name + "(" + sensors[i].type + ")";
                sensor.push(name);
                list.push(sensor);
            }
            ;
            addSubitemSelect(id, "actuatorid", list, "actuatorid", p1.actuatorid);

            addSubitemNumInput(id, "targetvalue", p1.targetvalue, "targetvalue", 0, 30, "0.01");
            //addSubitemNumInput(id, "zoneid", p1.zoneid, "zoneid");
            var zonelist = [];
            for (i = 0; i < zones.length; i++) {
                var zone = [];
                zone.push(zones[i].id);
                var name = "" + zones[i].id + "." + zones[i].name;
                zone.push(name);
                zonelist.push(zone);
            }
            ;
            addSubitemSelect(id, "zoneid", zonelist, "zoneid", p1.zoneid);

            addSubitemTimeInput(id, "time", p1.time, "Time");

            addSubitemCheckInput(id, "schedule", p1.schedule, "Data");
            addSubitemTimeInput(id, "starttime", p1.starttime, "start");
            addSubitemTimeInput(id, "endtime", p1.endtime, "end");
            addSubitemCheckInput(id, "sunday", p1.sunday, "S");
            addSubitemCheckInput(id, "monday", p1.monday, "M");
            addSubitemCheckInput(id, "tuesday", p1.tuesday, "T");
            addSubitemCheckInput(id, "wednesday", p1.wednesday, "W");
            addSubitemCheckInput(id, "thursday", p1.sunday, "T");
            addSubitemCheckInput(id, "friday", p1.friday, "F");
            addSubitemCheckInput(id, "saturday", p1.saturday, "S");
            addSubitemNumInput(id, "priority", p1.priority, "priorità", 0, 99);
        });


        addSeparator(" ");

    }, function (json) {
        postData("scenario",json, function (result) {
            loadScenarios();
        });
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
            postData("zone",json, function (result) {
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
    element.find('td[name="name"]').text(sensor.name);


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
    // last upodate
    element.find('td[name="date"]').text(sensor.lastupdate);
    // type
    element.find('td[name="type"]').text(sensor.type);
    //name
    element.find('td[name="name"]').text(sensor.name);
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
        $scenarioPanel = $(this).find('div[id="scenariopanel"]');
        $timeintervalRow = $scenarioPanel.find('tr[name="timeinterval"]');
        $instructionRow = $scenarioPanel.find('tr[name="instruction"]');

        var tbody = $scenarioPanel.find('tbody[name="timeintervallist"]');
        var tbody2 = $scenarioPanel.find('tbody[name="instructionlist"]');
        $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (data) {

            var scenario = data;

            $scenarioPanel.find('h4[id="name"]')[0].innerHTML = data.name;
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
            tbody[0].innerHTML = "";
            $.each(scenario.calendar.timeintervals, function (idx, elem) {
                var newtr = $timeintervalRow.clone();
                setTimeintervalElement(newtr, id, elem);
                tbody.append(newtr);
            });

            tbody2[0].innerHTML = "";
            $.each(scenario.programinstructions, function (idx, elem) {
                var newtr = $instructionRow.clone();
                setInstructionElement(newtr, elem);
                tbody2.append(newtr);
            });


            $scenarioPanel.find('button[id="edit"]').click(function () {

                $.getJSON(systemServletPath + "?requestcommand=sensors", function (sensors) {

                    //scenarioForm(scenario,sensors);
                    $.getJSON(systemServletPath + "?requestcommand=zones", function (zones) {

                        scenarioForm(scenario, sensors, zones);
                    })
                        .done(function () {
                        })
                        .fail(function () {
                            alert("cannot load sensorss");
                        })
                        .always(function () {
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

        })
            .done(function () {
            })
            .fail(function () {
                alert("cannot load timeintervals");
            })
            .always(function () {
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
    element.find('td[name="starttime"]').text(instruction.starttime);
    element.find('td[name="endtime"]').text(instruction.endtime);
    element.find('td[name="days"]').text(getDays(instruction));
    element.find('td[name="priority"]').text(instruction.priority);


}

var func = function (obj) {
    console.log(JSON.stringify(obj));
};
$("input[type='submit']").click(function () {
    func($("form").toJSO());
    func($("form").serialize());
    return false;
});
