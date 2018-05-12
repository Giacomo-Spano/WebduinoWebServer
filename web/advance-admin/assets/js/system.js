/**
 * Created by giaco on 27/10/2017.
 */
var $systemPanel;
var $systemActuatorRow;
var $systemZoneRow;
var $systemScenarioRow;
var $systemServiceRow;
var $system;

function addActuator(idx, elem) {
    var actuator = $systemActuatorRow.clone();
    actuator.find('td[name="id"]').text(elem.id);
    actuator.find('td[name="name"]').text(elem.name).click(function () {
    });;
    actuator.click(function () {
    });
    $systemPanel.find('tbody[name="actuatorlist"]').append(actuator);
}

function loadActuators(actuators) {
    var tbody = $systemPanel.find('tbody[name="actuatorlist"]');
    tbody[0].innerHTML = "";
    if (actuators != null) {
        $.each(actuators, function (idx, elem) {
            addActuator(idx, elem);
        });
    }
}

function loadSystem(system) {

    $system = system;

        $("#result").load("system.html", function () {
                // back button
                backbutton.unbind("click");
                backbutton.click(function () {
                    loadWebduinoSystems();
                });
                pagetitle.text('Sistema');
                notification.hide();
                notificationsuccess.hide();

                $systemPanel = $(this).find('div[id="panel"]');
                $systemActuatorRow = $systemPanel.find('tr[name="actuatorrow"]');
                $systemZoneRow = $systemPanel.find('tr[name="zonerow"]');
                $systemServiceRow = $systemPanel.find('tr[name="servicerow"]');
                $systemScenarioRow = $systemPanel.find('tr[name="scenariorow"]');

                $systemPanel.find('p[name="headingright"]').val(system.id);
                $systemPanel.find('div[name="name"]').text(system.name);

                // actuators
                if (system.actuators != undefined)
                    loadActuators($system.actuators);
            }
        );
}
