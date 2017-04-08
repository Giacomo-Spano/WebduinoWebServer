var actuatorServletPath = "../actuator";
var sensorServletPath = "../sensor";

var sensorItemElem;

var sensorList = [];
var fullDate;
var time;

var actuatorID = 1;


var modalKeyboard;

var manualDuration = 30;
var targetTemperature = 22.0;
var activeSensorId = 0;
var activeSensorName = "";
var localSensor = true;
var localTemperature;

var $activeProgram;


function onTargetButtonClicked(event) {
    targetTemperature = targetTemperature + event.data.param1;
    targetTemperature = +targetTemperature.toFixed(1);

    updateManualProgram();
}
function onDurationButtonClicked(event) {
    manualDuration = manualDuration + event.data.param1 * 60;
    //manualDuration = +targetTemperature.toFixed(1);

    updateManualProgram();
}

function pad(str, max, padder) {
    padder = typeof padder === "undefined" ? "0" : padder;
    return str.toString().length < max ? pad(padder.toString() + str, max, padder) : str;
}


function updateManualProgram(actuator) {

    $('t[name="targettemperature"]').text(actuator.target);

    var duration;
    if (actuator.status == "manual") {
        duration = actuator.remaining;
    } else {
        duration = actuator.duration;
    }

    hour = duration / 3600;
    hour = (hour).toFixed(0);
    minute = (duration / 60 % 60).toFixed(0);
    seconds = (duration % 60).toFixed(0);


    $('t[name="duration"]').text(pad(hour, 2) + ":" + pad(minute, 2) + ":" + pad(seconds, 2));

    $('label[id="activesensor"]').text(activeSensorName + "(id: " + activeSensorId + ")");


}

function getSensors() {

    $.getJSON(sensorServletPath, function (sensors) {

        list = $('div[id="sensorList"]');
        item = sensorItemElem.clone();
        $('div[name="sensoritem"]').empty();

        //item = sensorItemElem.clone();
        item.find('i').text("ytemp locale: " + localTemperature + "°C");
        list.after(item);
        /*sensorItemElem.last().after(item);

         item.find('i').text("ztemp locale: " + localTemperature + "°C");
         sensorItemElem.last().after(item);

         last = item;*/

        while (sensorList.length > 0) {
            sensorList.pop();
        }

        $.each(sensors, function (id, sensor) {

         sensorList.push({id: id, description: sensor.name, temperature: sensor.avtemperature});

         /*last = sensorItem;
         newItem = sensorItem.clone();
         newItem.find('i').html(sensor.name + ": " + sensor.avtemperature + "°C");
         last.last().after(newItem);
            */
         });
    });
}

function getActiveSensorFromID(id) {

    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState == 4 && xhttp.status == 200) {

            sensor = jQuery.parseJSON(xhttp.responseText);
            activeSensorName = sensor.name;
            //updateActuator(xhttp.responseText);
            refreshActuatorControls();
        }
    };
    xhttp.open("GET", sensorServletPath + '?id=' + id, true);
    xhttp.send();
}


function getActuator() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function () {
        if (xhttp.readyState == 4 && xhttp.status == 200) {

            updateActuator(xhttp.responseText);
        }
    };
    xhttp.open("GET", actuatorServletPath + '?id=' + actuatorID, true);
    xhttp.send();
}


function refreshActuatorControls() {
    $('label[name="currentdate"]').text(fullDate);
    $('label[name="currenttime"]').text(time);

    if (actuator.localsensor) {
        $('t[name="sensor"]').text("locale");
        $('i[name="currenttemperature"]').text(localtemperature + "°");
    } else {
        $('t[name="sensor"]').text(activeSensorName);
        $('i[name="currenttemperature"]').text(remotetemperature + "°");
    }
    $('t[name="localtemperature"]').text(localtemperature + "°");
    $('t[name="remotetemperature"]').text(remotetemperature + "°");

    $('i[name="relestatusimage"]').removeClass("icon-rele-on icon-rele-off");
    if (actuator.relestatus) {
        $('i[name="relestatusimage"]').addClass("icon-rele-on");
        $('t[name="relestatus"]').text("Acceso");
    } else {
        $('i[name="relestatusimage"]').addClass("icon-rele-off");
        $('t[name="relestatus"]').text("Spento");
    }

    $('i[name="statusimage"]').removeClass("icon-hand-hold icon-magic icon-idle");
    if (actuator.status == "program") {
        $('i[name="statusimage"]').addClass("icon-magic");
        $('t[name="status"]').text("Auto");
    } else if (actuator.status == "manual") {
        $('i[name="statusimage"]').addClass("icon-hand-hold");
        $('t[name="status"]').text("Manuale");
    } else if (actuator.status == "idle") {
        $('i[name="statusimage"]').addClass("icon-idle");
        $('t[name="status"]').text("Idle");
    }


}
function updateActuator(json) {

    actuator = jQuery.parseJSON(json);

    targetTemperature = actuator.target;
    targetTemperature = +targetTemperature.toFixed(1);
    manualDuration = actuator.duration;
    localSensor = actuator.localsensor;
    localTemperature = actuator.avtemperature;

    var temp = actuator.temperature;
    localtemperature = +temp.toFixed(1);
    temp = actuator.remotetemperature;
    remotetemperature = +temp.toFixed(1);

    fullDate = actuator.fulldate;
    time = actuator.time;

    oldsensorId = activeSensorId;
    activeSensorId = actuator.sensorID;
    if (oldsensorId != activeSensorId) {
        getActiveSensorFromID(activeSensorId);
    }

    //prova = new Date("2010-06-0915:20:00Z");
    //curdate = new Date(actuator.UTCdate);
    refreshActuatorControls();
}
/*function loadActuator(id) {

 $.getJSON(actuatorServletPath + '?id=' + actuatorID, function (actuator) {
 console.log("success");

 updateActuator(actuator);
 updateManualProgram(actuator);


 })
 .done(function () {
 console.log("succes");
 })
 .fail(function () {
 console.log("error1");
 alert("error1");
 })
 .always(function () {
 console.log("error2");
 });
 //return res;
 }*/
function loadActiveProgramList() {

    var res;

    $.getJSON(programServletPath + '?next=true', function (data) {
            console.log("success");

            tr = $activeProgram.find('tr[name="activeprogram"]');
            res = data;
        })
        .done(function () {
            console.log("succes");
        })
        .fail(function () {
            console.log("error1");
            alert("error1");
        })
        .always(function () {
            console.log("error2");
        });

    return res;
}
var modal;

function modalKeypad(div, title, items) {

    modal = div;
    var event = onSelectSensorClicked;

    $.get("keypad.html", function (data) {

            modal.html(data);
            modal.css("display", "block");
            modal.find('p').text(title);
            item = modal.find('a');
            items.forEach(function (elem, index) {

                    last = item;
                    if (index > 0) {
                        //last = item;
                        item = item.clone();
                        last.last().after(item);

                    } else {
                        item = item;
                    }

                    //id = 1;
                    item.attr("id", elem.id);
                    item.text(elem.description);
                    item.on('click', {id: elem.id, description: elem.description}, event)
                        .on('mouseup', function () {
                            modal.css("display", "none");
                        });
                }
            );

            // Get the <span> element that closes the modal
            var span = document.getElementsByClassName("close")[0];
            // When the user clicks on <span> (x), close the modal
            span.onclick = function () {
                //modal.style.display = "none";
                modal.css("display", "none");
                //selectsensor
            }
            // When the user clicks anywhere outside of the modal, close it
            window.onclick = function (event) {
                if (event.target == modal) {
                    //modal.style.display = "none";
                    modal.css("display", "none");
                }
            }
        }
    )
    ;
}

function modalNumericKeypad(div, title, items) {

    modal = div;
    var event = onSelectSensorClicked;

    $.get("numericpad.html", function (data) {

            modal.html(data);
            modal.css("display", "block");
            modal.find('p').text(title+"bohhh");
            item = modal.find('a');
            //item = modal.find('a[id="one"]');
            /*item.on('click', {id: 0, description:"description"}, event)
                .on('mouseup', function () {
                modal.css("display", "none");
            });*/
            //item = modal.find('a[name="two"]');
            for (i = 0; i < item.length; i++) {
            //item.forEach(function (elem, index) {

                    item.on('click', {id: i, description: "description"}, event)
                        .on('mouseup', function () {
                            modal.css("display", "none");
                        });
                }
            //);


            // Get the <span> element that closes the modal
            var span = document.getElementsByClassName("close")[0];
            // When the user clicks on <span> (x), close the modal
            span.onclick = function () {
                //modal.style.display = "none";
                modal.css("display", "none");
                //selectsensor
            }
            // When the user clicks anywhere outside of the modal, close it
            window.onclick = function (event) {
                if (event.target == modal) {
                    //modal.style.display = "none";
                    modal.css("display", "none");
                }
            }
        }
    )
    ;
}


function onSelectSensorClicked(event) {

    //alert(event.data.id);

    activeSensorId = event.data.id;
    activeSensorName = event.data.description;

    $('label[id="activesensor"]').text(activeSensorName + "(id: " + activeSensorId + ")");
    //updateManualProgram();
}

function load() {

    sensorItemElem = $('div[name="sensoritem"]');

    getSensors();
    getActuator();

    //updateTime();


    $('button[id="selectsensor"]').on('click', function () {
        div = $('div[id="selectSensorModal"]');
        var items = [{id: 1, description: "xxx"}, {id: 2, description: "yyy"}, {id: 3, description: "zzz"}];
        modalKeypad(div, "titolo della finestra", sensorList);
    });

    $('button[id="temperaturebutton"]').on('click', function () {
        div = $('div[id="selectSensorModal"]');
        var items = [{id: 1, description: "xxx"}, {id: 2, description: "yyy"}, {id: 3, description: "zzz"}];
        modalNumericKeypad(div, "titolo della finestra num", sensorList);
    });

    // start command button
    $('a[id="startbutton"]').click(start);


    $('a[name="targetplus"]').click({param1: 0.1, param2: "World"}, onTargetButtonClicked);
    $('a[name="targetminus"]').click({param1: -0.1, param2: "World"}, onTargetButtonClicked);

    $('a[name="durationplus"]').click({param1: 10, param2: "World"}, onDurationButtonClicked);
    $('a[name="durationminus"]').click({param1: -10, param2: "World"}, onDurationButtonClicked);


    setInterval(getActuator, 1000);
    //setInterval(getSensors, 5000);

}

function start() {

    var command = {
        "id": 1,
        "command": "start",
        "duration": 30,
        "target": 22,
        "sensor": 0
    };

    $.ajax({
        type: "POST",
        url: actuatorServletPath,
        data: JSON.stringify(command),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        processData: true,
        success: function (data, status, jqXHR) {

            //alert(xhr.responseText);
            //updateActuator(data.actuator);
            heater = jQuery.parseJSON(data.actuator);

            //heater.push(data.actuator);
            //updateActuator(heater);
            updateManualProgram(heater);

        },
        error: function (xhr) {
            alert(xhr.responseText);
        }
    });

}