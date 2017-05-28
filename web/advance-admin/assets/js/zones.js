var systemServletPath = "../system";
var $zonesPanel;
var $zoneRow;

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

function loadZones() {

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

function setZoneElement(element, zone) {

    var link = element.find('a[name="link"]')[0];
    link.href = "../advance-admin/webduino_zone.html?id=" + zone.id;
    link.text = zone.id;
    element.find('td[name="name"]').text(zone.name);
}

function deactivatemenuitems() {
    $('a[id="item_home"]').attr("class", "");
    $('a[id="item_dashboard"]').attr("class", "");
    $('a[id="item_scenarios"]').attr("class", "");
    $('a[id="item_zones"]').attr("class", "");
}

function load() {

    $('a[id="item_home"]').click(function(){
        deactivatemenuitems();
        $('a[id="item_home"]').attr("class", "active-menu");
        return false;
    });

    $('a[id="item_dashboard"]').click(function(){
        deactivatemenuitems();
        $('a[id="item_dashboard"]').attr("class", "active-menu");
        return false;
    });

    $('a[id="item_scenarios"]').click(function(){
        deactivatemenuitems();
        $('a[id="item_scenarios"]').attr("class", "active-menu");
        return false;
    });

    $('a[id="item_zones"]').click(function(){
        deactivatemenuitems();
        $('a[id="item_zones"]').attr("class", "active-menu");
        return false;
    });


    $( "#result" ).load( "zones.html" , function() {
        //alert( "Load was performed." );
        $zonesPanel = $(this).find('div[id="zonespanel"]');
        $zoneRow = $zonesPanel.find('tr[name="zone"]');

        loadZones();
    });

    //$zonesPanel = $(this).find('div[id="zonespanel"]');
    //$zoneRow = $zonesPanel.find('tr[name="zone"]');

    //loadZones();
}