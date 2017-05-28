var systemServletPath = "../system";
var $instructionsPanel;
var $instructionRow;

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

    xhttp.open("POST", systemServletPath, true);
    xhttp.setRequestHeader("Content-type", "application/json");
    var str = JSON.stringify(commandJson);
    //xhttp.send(commandJson.toString());
    xhttp.send(str);
}

function loadInstructions(scenarioid,timeintervalid) {

    var tbody = $instructionsPanel.find('tbody[name="instructionlist"]');


    $.getJSON(systemServletPath + "?requestcommand=instructions&id="+scenarioid + "&timeintervalid=" + timeintervalid, function (data) {
        tbody[0].innerHTML = "";

        $.each(data, function (idx, elem) {
            var newtr = $instructionRow.clone();
            setInstructionElement(newtr, elem);
            tbody.append(newtr);
        });
    })
        .done(function () {
            //console.log("succes");
        })
        .fail(function () {
            alert("cannot load scenarios");
        })
        .always(function () {
            //console.log("error2");
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
}

function load() {
    var scenarioid = getUrlVars()["scenarioid"];
    var timeintervalid = getUrlVars()["timeintervalid"];

    $instructionsPanel = $(this).find('div[id="instructionspanel"]');
    $instructionRow = $instructionsPanel.find('tr[name="instruction"]');

    loadInstructions(scenarioid,timeintervalid);
}