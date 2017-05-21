var sensorServletPath = "../sensor";
var actuatorServletPath = "../actuator";
var shieldServletPath = "../shield";
var programServletPath = "../program";
var $mSensorPanel;
var $mSensorRow;
var $actuators;
var $shieldss;
var $activeProgram;

var $shieldrow;
//var $shieldTable;


function commanCallback(element, actuator) {
    //element.find('td[name="status"]').text(actuator.status + 'modificato');
    setActuatorElement(element, actuator);
}

function sendActuatorCommand(actuatorId, command, duration, sensorId, remote, target, element) {

    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {

            var json = JSON.parse(this.response);
            if (json.answer = 'success') {

                var actuator = JSON.parse(json.actuator);
                commanCallback(element, actuator);
            } else {
                element.find('td[name="commandstatus"]').text("command failed");
            }
        }
    };

    xhttp.open("POST", actuatorServletPath, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.send("{ \"status\" : 1" +
        ",\"id\" : " + actuatorId +
        ",\"command\" : " + command +
        ",\"duration\" : " + duration +
        ",\"target\" : " + target +
        ",\"sensorid\" : " + sensorId +
        ",\"remote\" : " + remote +
        ",\"program\" : 0" +
        ",\"timerange\" : 0" +
        "}");
}

function addNewSensorLine(newtr, elem) {

    // id
    newtr.find('td[name="id"]').text(elem.id);
    // shieldid
    newtr.find('td[name="shieldid"]').text(elem.shieldid);
    if (elem.online)
        newtr.find('td[name="onlinestatus"]').text("Online");
    else
        newtr.find('td[name="onlinestatus"]').text("Offline");
    // last upodate
    newtr.find('td[name="date"]').text(elem.lastupdate);
    // type
    newtr.find('td[name="type"]').text(elem.type);
    // status
    if (elem.type == "temperaturesensor") {
        text = "temp:" + elem.temperature + "°C" + " av.temp:" + elem.avtemperature + "°C";
        newtr.find('td[name="status"]').text(text);
    } else if (elem.type == "doorsensor") {
        text = "door ";
        if (elem.status == true)
            text += "open";
        else
            text += "closed";
        newtr.find('td[name="status"]').text(text);
    } else if (elem.type == "heatersensor") {
        text = "status: " + elem.status
            + " rele: " + elem.relestatus
            + " target: " + elem.target
            + " temperature : " + elem.temperature
            + " scenario: " + elem.scenario + "." + elem.timeinterval + " "
            + " zone: " + elem.zone;

        newtr.find('td[name="status"]').text(text);
    } else {
        newtr.find('td[name="status"]').text("undefined");
    }
    //name
    newtr.find('td[name="name"]').text(elem.name);
    // subaddress
    newtr.find('td[name="subaddress"]').text(elem.subaddress);

}

function loadSensors() {
    $.getJSON(sensorServletPath, function (data) {
        console.log("success");

        tr = $mSensorPanel.find('tr[name="sensor"]');
        while (tr.size() > 1) {
            tr.last().remove();
        }

        a = data;
        $.each(a, function (idx, elem) {

            tr = $mSensorPanel.find('tr[name="sensor"]');
            var newtr = $mSensorRow.clone();
            addNewSensorLine(newtr, elem);

            if (elem.childsensors != null) {

                $.each(elem.childsensors, function (idx, elem) {
                    var newtr = $mSensorRow.clone();
                    addNewSensorLine(newtr, elem);
                    tr.last().after(newtr);
                });
            }

            tr.last().after(newtr);
        });
    })
        .done(function () {
            console.log("succes");
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
            alert('getJSON request failed! ' + textStatus);
        })
        .always(function () {
            //console.log("get Sensor Error");
        });
}
function setActuatorElement(element, actuator) {

    element.find('td[name="id"]').text(actuator.id);
    element.find('td[name="shieldid"]').text(actuator.shieldid);
    //element.find('td[name="onlinestatus"]').text(actuator.onlinestatus);
    if (actuator.online)
        element.find('td[name="onlinestatus"]').text("Online");
    else
        element.find('td[name="onlinestatus"]').text("Offline");
    element.find('td[name="type"]').text(actuator.type);
    element.find('td[name="date"]').text(actuator.lastupdate);
    element.find('td[name="temperature"]').text(actuator.temperature + "°C");
    element.find('td[name="avtemperature"]').text(actuator.avtemperature + "°C");

    element.find('td[name="remote"]').text(actuator.remotetemperature + "°C");
    element.find('input[name="target"]').val(actuator.target);
    if (actuator.localsensor)
        element.find('td[name="sensors"]').text("Locale");
    else
        element.find('td[name="sensors"]').text("Remoto: " + actuator.sensorID);

    element.find('td[name="url"]').text(actuator.url);
    element.find('td[name="name"]').text(actuator.name);
    if (actuator.relestatus)
        element.find('td[name="relestatus"]').text("Acceso");
    else
        element.find('td[name="relestatus"]').text("Spento");

    var statusElement = element;//.find('td[name="status"]');
    if (actuator.status == "program")
        element.find('td[name="status"]').text(actuator.status + " " + actuator.program + "." + actuator.timerange);
    else
        element.find('td[name="status"]').text(actuator.status);

    element.find('td[name="duration"]').text(actuator.duration);
    element.find('td[name="remaining"]').text(actuator.remaining);

    element.find('td[name="commandstatus"]').text("");
    //element.find('button[name="commandbutton"]').style.visibility='visible';

    if (actuator.status == 'manual') { // Manual
        element.find('button[name="commandbutton"]').text("stop manual");
        element.find('button[name="commandbutton"]').click(function () {
            element.find('td[name="commandstatus"]').text("sending stop manual command");
            //element.find('button[name="commandbutton"]').style.visibility='hidden';
            sendActuatorCommand(actuator.id, 'stop', 30, 0, true, 23.0, element)
        });
    } else if (actuator.status == 'idle' || actuator.status == 'program') { // Manual{
        element.find('button[name="commandbutton"]').text("start manual");
        element.find('button[name="commandbutton"]').click(function () {
            element.find('td[name="commandstatus"]').text("sending start manual command");
            //element.find('button[name="commandbutton"]').style.visibility='hidden';
            sendActuatorCommand(actuator.id, 'start', 30, 0, true, 23.0, element)
        });
    }
}

function setShieldElement(element, shield) {

    var idelem = element.find('a[name="shieldlink"]')[0];
    idelem.href = "../advance-admin/webduino_settings.html?id=" + shield.shieldid;
    idelem.text = shield.shieldid;

    element.find('td[name="boardname"]').text(shield.shieldname);
    //element.find('td[name="url"]').text(shield.url);
    element.find('td[name="MACAddress"]').text(shield.macaddres);

}

function loadActuators() {
    $.getJSON(actuatorServletPath, function (data) {
        console.log("success");

        a = data;
        $.each(a, function (idx, elem) {

            var newtr;
            tr = $actuators.find('tr[name="actuator"]');
            if (idx > 0) {
                newtr = tr.clone();
            } else {
                newtr = tr;
            }
            setActuatorElement(newtr, elem);

            tr.last().after(newtr);
        });

    })
        .done(function () {
            //console.log("succes");
        })
        .fail(function () {
            //console.log("error1");
            alert("cannot load actuator");
        })
        .always(function () {
            //console.log("error2");
        });
}
function loadShields() {
    $.getJSON(shieldServletPath + "?command=shields", function (data) {
        console.log("success");


        a = data;
        $.each(a, function (idx, elem) {

            var newtr = $shieldrow.clone();
            var tbody = $shields.find('tbody[name="shieldlist"]');
            setShieldElement(newtr, elem);
            tbody.append(newtr);


            //var table = $shields.find('tbody[name="shieldtable"]')

            //tbo.find('tbody').append(newtr);
            //tr.last().after(newtr);
            //table.insertRow();
            /*tr = $shields.find('tr[name="shield"]');
             if (idx > 0) {
             newtr = tr.clone();
             } else {
             newtr = tr;
             }
             setShieldElement(newtr, elem);*/

            //tr.last().after(newtr);
            //tr.appendChild(newtr);
        });

    })
        .done(function () {
            //console.log("succes");
        })
        .fail(function () {
            //console.log("error1");
            alert("cannot load shield");
        })
        .always(function () {
            //console.log("error2");
        });
}
function loadActiveProgramList() {

    loadActiveProgram();

    $.getJSON(programServletPath + '?next=true', function (data) {
        console.log("success");

        tr = $activeProgram.find('tr[name="activeprogram"]');
        a = data;
        $.each(a, function (idx, elem) {


            //newtr = tr.clone();
            if (idx > 0) {
                last = newtr;
                newtr = tr.clone();
                last.last().after(newtr);
            } else {
                newtr = tr;
                //tr.last().before(newtr);
            }

            newtr.find('td[name="lastupdate"]').text(idx);

            newtr.find('td[name="id"]').text(elem.id);
            newtr.find('td[name="name"]').text(elem.name);
            newtr.find('td[name="timerange"]').text(elem.timerangeid + " " + elem.timerangename);
            newtr.find('td[name="start"]').text(elem.startdate);
            newtr.find('td[name="end"]').text(elem.enddate);
            newtr.find('td[name="temperature"]').text(elem.temperature);
            newtr.find('td[name="sensors"]').text("#" + elem.sensor);
            //newtr.find('td[name="sensors"]').text("#" + elem.sensors + " " + elem.sensorname + " (" + elem.sensortemperature + "°C)");
            //newtr.find('td[name="endtime"]').text(elem.startdate);
            //newtr.find('td[name="temperature"]').text(elem.temperature);
            //newtr.find('td[name="sensors"]').text("#" + elem.sensors + " " + elem.sensorname + " (" + elem.sensortemperature + "°C)");

            //tr.last().before(newtr);
        });
    })
        .done(function () {
            console.log("succes");
        })
        .fail(function () {
            console.log("Nessun programma attivo");
            alert("error1");
        })
        .always(function () {
            console.log("error2");
        });
}

function loadActiveProgram() {
    $.getJSON(programServletPath + '?active=true', function (data) {
        console.log("success");

        tr = $("#lastprogramupdate");
        tr.text(data.lastupdate);

    })
        .done(function () {
            console.log("succes");
        })
        .fail(function () {
            console.log("load active program error");
        })
        .always(function () {
            console.log("cannot load active program");
        });
}


function load() {

    $mSensorPanel = $(this).find('div[id="sensorpanel"]');
    $mSensorRow = $mSensorPanel.find('tr[name="sensor"]');

    $actuators = $(this).find('div[id="actuatorpanel"]');
    $shields = $(this).find('div[id="shieldpanel"]');
    $activeProgram = $(this).find('div[id="activeprogrampanel"]');

    loadSensors();

    $shieldrow = $shields.find('tr[name="shield"]').clone();
    $shields.find('tbody[name="shieldlist"]')[0].innerHTML = "";
    loadShields();
}