var systemServletPath = "../system";
var $scenariosPanel;
var $scenarioRow;

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

function loadScenarios() {

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
            //console.log("succes");
        })
        .fail(function () {
            alert("cannot load scenarios");
        })
        .always(function () {
            //console.log("error2");
        });
}

function setScenarioElement(element, scenario) {

    var link = element.find('a[name="link"]')[0];
    link.href = "../advance-admin/webduino_scenario.html?id=" + scenario.id;
    link.text = scenario.id;
    //element.find('td[name="id"]').text(scenario.id);
    element.find('td[name="name"]').text(scenario.name);
    var text = "non attivo";
    if (scenario.active)
        var text = "non attivo";
    element.find('td[name="status"]').text(scenario.ac);
}

function load() {

    $scenariosPanel = $(this).find('div[id="scenariospanel"]');
    $scenarioRow = $scenariosPanel.find('tr[name="scenario"]');

    loadScenarios();
}