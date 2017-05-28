var sensorServletPath = "../sensor";
var actuatorServletPath = "../actuator";
var shieldServletPath = "../shield";
var programServletPath = "../program";
var $mSensorPanel;
var $mSensorRow;
var $actuators;
var $shieldss;
var $activeProgram;


var systemServletPath = "../system";
var $shieldrow;
//var $shieldTable;


var $zonesPanel;
var $zonesRow;


function commanCallback(element, actuator) {
    //element.find('td[name="status"]').text(actuator.status + 'modificato');
    setActuatorElement(element, actuator);
}

function sendSensorCommand(commandJson, element) {

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
        if (elem.openstatus == true)
            text += "open";
        else
            text += "closed";
        newtr.find('td[name="status"]').text(text);

        // testmode button
        var label = "test mode";
        if (elem.testmode)
            var label = "end test mode";
        var testButton = newtr.find('button[name="testbutton"]');
        testButton.text(label);
        testButton.click(function () {
            var command = 'teststart'
            if (elem.testmode) {
                command = 'teststop';
            }
            statusButton.text("sending" + command + " command...");
            var commandJson = {
                'shieldid': elem.shieldid,
                'actuatorid': elem.id,
                'command': command
            };
            sendSensorCommand(commandJson, elem)
        });

        // test open/close button
        label = "close";
        if (!elem.openstatus)
            label = "open";
        var statusButton = newtr.find('button[name="statusbutton"]');
        statusButton.text(label);
        statusButton.click(function () {

            var command = 'testopen'
            if (!elem.openstatus) {
                command = 'testclose';
            }
            statusButton.text("sending" + command + " command...");
            var commandJson = {
                'shieldid': elem.shieldid,
                'actuatorid': elem.id,
                'command': command,
            };
            sendSensorCommand(commandJson, elem)
        });


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

function setShieldElement(element, shield) {

    var idelem = element.find('a[name="link"]')[0];
    idelem.href = "../advance-admin/webduino_settings.html?id=" + shield.shieldid;
    idelem.text = shield.shieldid;

    element.find('td[name="boardname"]').text(shield.shieldname);
    //element.find('td[name="url"]').text(shield.url);
    element.find('td[name="MACAddress"]').text(shield.macaddres);

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
        });

    })
        .done(function () {
        })
        .fail(function () {
            alert("cannot load shield");
        })
        .always(function () {
        });
}


function loadZone() {

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
            //console.log("succes");
        })
        .fail(function () {
            alert("cannot load zones");
        })
        .always(function () {
            //console.log("error2");
        });
}

function setZoneElement(element, scenario) {

    element.find('td[name="id"]').text(scenario.id);
    element.find('td[name="name"]').text(scenario.name);
    var text = "non attivo";
    if (scenario.active)
        var text = "non attivo";
    element.find('td[name="status"]').text(scenario.ac);
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

    $zonesPanel = $(this).find('div[id="zonespanel"]');
    $zoneRow = $zonesPanel.find('tr[name="zone"]');
    loadZone();


}