var systemServletPath = "../system";
var $zonePanel;
var $sensorRow;

function commanCallback(element, actuator) {
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

    xhttp.open("POST", systemServletPath, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    var str = JSON.stringify(commandJson);
    //xhttp.send(commandJson.toString());
    xhttp.send(str);
}

function loadZone(id) {

    var tbody = $zonePanel.find('tbody[name="sensorlist"]');

    $.getJSON(systemServletPath + "?requestcommand=zone&id=" + id, function (data) {

        //$zonePanel.find('h4[id="name"]')[0].innerHTML = data.name;
        //$zonePanel.find('span[id="id"]')[0].innerHTML = data.id;

        tbody[0].innerHTML = "";

        $.each(data.zonesensors, function (idx, elem) {
            var newtr = $sensorRow.clone();
            setZoneSensorElement(newtr, elem);
            tbody.append(newtr);
        });
    })
        .done(function () {
            //console.log("succes");
        })
        .fail(function () {
            alert("cannot load zone");
        })
        .always(function () {
            //console.log("error2");
        });
}

function setZoneSensorElement(element, sensor) {

    /*var link = element.find('a[name="link"]')[0];
    link.href = "../advance-admin/webduino_instructions.html?scenarioid=" + sensor + "&timeintervalid=" + timeinterval.id;
    link.text = timeinterval.id;*/
    element.find('td[name="id"]').text(sensor.id);
    element.find('td[name="name"]').text(sensor.name);
}

function load() {

    var id = getUrlVars()["id"];

    $zonePanel = $(this).find('div[id="zonepanel"]');
    $sensorRow = $zonePanel.find('tr[name="sensor"]');

    loadZone(id);
}
