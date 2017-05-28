var systemServletPath = "../system";
var $scenariosPanel;
var $timeintervalRow;
var $scenarioId = 0;

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

function loadScenario(id) {

    var tbody = $scenariosPanel.find('tbody[name="timeintervallist"]');



    $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (data) {

        $scenariosPanel.find('h4[id="name"]')[0].innerHTML = data.name;
        $scenariosPanel.find('span[id="id"]')[0].innerHTML = data.id;
        if (data.active)
            $scenariosPanel.find('span[id="status"]')[0].innerHTML = "Attivo";
        else
            $scenariosPanel.find('span[id="status"]')[0].innerHTML = "Non attivo";
        if (data.dateenabled) {
            if (data.startdate)
                $scenariosPanel.find('span[id="startdate"]')[0].innerHTML = data.startdate;
            else
                $scenariosPanel.find('span[id="startdate"]')[0].innerHTML = "--";
            if (data.startdate)
                $scenariosPanel.find('span[id="enddate"]')[0].innerHTML = data.enddate;
            else
                $scenariosPanel.find('span[id="endtdate"]')[0].innerHTML = "--";

        }

        tbody[0].innerHTML = "";

        $.each(data.calendar.timeintervals, function (idx, elem) {
            var newtr = $timeintervalRow.clone();
            setTimeintervalElement(newtr, id, elem);
            tbody.append(newtr);
        });
    })
        .done(function () {
            //console.log("succes");
        })
        .fail(function () {
            alert("cannot load timeintervals");
        })
        .always(function () {
            //console.log("error2");
        });
}

function setTimeintervalElement(element, scenarioid, timeinterval) {

    var link = element.find('a[name="link"]')[0];
    link.href = "../advance-admin/webduino_instructions.html?scenarioid=" + scenarioid + "&timeintervalid=" + timeinterval.id;
    link.text = timeinterval.id;
    //element.find('td[name="id"]').text(timeinterval.id);
    element.find('td[name="name"]').text(timeinterval.name);
    element.find('td[name="days"]').text(getDays(timeinterval));
    element.find('td[name="starttime"]').text(timeinterval.starttime);
    element.find('td[name="endtime"]').text(timeinterval.endtime);


}

function load() {

    var id = getUrlVars()["id"];

    $scenariosPanel = $(this).find('div[id="scenariopanel"]');
    $timeintervalRow = $scenariosPanel.find('tr[name="timeinterval"]');

    loadScenario(id);
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