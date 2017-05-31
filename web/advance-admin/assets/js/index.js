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
        $.getJSON(systemServletPath + "?requestcommand=zone&id=" + id, function (data) {

            tbody[0].innerHTML = "";

            $.each(data.zonesensors, function (idx, elem) {
                var newtr = $sensorRow.clone();
                setZoneSensorElement(newtr, elem);
                tbody.append(newtr);
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

        var tbody = $scenarioPanel.find('tbody[name="timeintervallist"]');
        $.getJSON(systemServletPath + "?requestcommand=scenario&id=" + id, function (data) {

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
            $.each(data.calendar.timeintervals, function (idx, elem) {
                var newtr = $timeintervalRow.clone();
                setTimeintervalElement(newtr, id, elem);
                tbody.append(newtr);
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
        loadInstructions(scenarioid,timeinterval.id)
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
function loadInstructions(scenarioid, timeintervalid) {

    $("#result").load("instructions.html", function () {
        $instructionsPanel = $(this).find('div[id="instructionspanel"]');
        $instructionRow = $instructionsPanel.find('tr[name="instruction"]');

        var tbody = $instructionsPanel.find('tbody[name="instructionlist"]');
        $.getJSON(systemServletPath + "?requestcommand=instructions&id=" + scenarioid + "&timeintervalid=" + timeintervalid, function (data) {
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
}

$.fn.toJSO = function () {
    var obj = {},
        $kids = $(this).children('[name]');
    if (!$kids.length) {
        return $(this).val();
    }
    $kids.each(function () {
        var $el = $(this),
            name = $el.attr('name');
        if ($el.siblings("[name=" + name + "]").length) {
            if (!/radio|checkbox/i.test($el.attr('type')) || $el.prop('checked')) {
                obj[name] = obj[name] || [];
                obj[name].push($el.toJSO());
            }
        } else {
            obj[name] = $el.toJSO();
        }
    });
    return obj;
};

var func = function(obj){
    console.log( JSON.stringify( obj ) );
};
$("input[type='submit']").click(function () {
    func($("form").toJSO());
    func($("form").serialize());
    return false;
});
