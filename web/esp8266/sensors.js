var sensorDiv;
var listDiv;
var temperatureDiv;
var doorDiv;

var temperatureSensorsCheckBox;
var pinSelect;

var onChangeTemperatureSensorsEnabledFunction = function onChangeTemperatureSensorsEnabled() {

    var jsonString;
    var enabled = false;
    if (this.checked == true) {
        enabled = true;
    }
    jsonString = '{"command" : "temperaturesensorsettings", "temperaturesensorsenabled" : ' + enabled + '}';
    var json = JSON.parse(jsonString);
    sendCommand(json, refreshFunction);
};

var onChangePinSelectFunction = function onChangePinSelect() {

    pin = this.value;
    var jsonString = '{"command" : "temperaturesensorsettings", "temperaturepin" : "' + pin + '"}';
    var json = JSON.parse(jsonString);
    sendCommand(json, refreshFunction);
};


var refreshFunction = function refresh(json) {


    for (i = 0; i < json.sensors.length; i++) {

        div = addSensor();
        updateSensorDiv(div, json.sensors[i]);

        /*var clone = sensorDiv.cloneNode(true); // true means clone all childNodes and all event handlers
         clone.id = sensorDiv.id + i;
         updateSensorDiv(clone, json.sensors[i]);
         listDiv.appendChild(clone);*/
    }

    document.getElementById('addSensorButton').addEventListener("click", function () {
        addSensor(div);
    }, false);
    document.getElementById('saveButton').addEventListener("click", function () {
        save();
    }, false);

    sensorDiv.style.display = 'none';
    temperatureDiv.style.display = 'none';
    doorDiv.style.display = 'none';

    //var pinSelectControl = document.getElementById('pinSelect');
    //pinSelectControl.value = json.temperaturesensorspin;
};

function load() {

    sensorDiv = document.getElementById('sensor');
    //sensorDiv.style.display = 'none';

    temperatureDiv = document.getElementById('onewiresensor');
    //temperatureDiv.style.display = 'none';

    doorDiv = document.getElementById('doorsensor');
    //doorDiv.style.display = 'none';

    listDiv = document.getElementById('sensorList');
    listDiv.innerHTML = '';


    //temperatureSensorsCheckBox = document.getElementById('temperatureSensorsCheckBox');
    //temperatureSensorsCheckBox.onchange = onChangeTemperatureSensorsEnabledFunction;
    //pinSelect = document.getElementById('pinSelect').onchange = onChangePinSelectFunction;
    getJson(sensorsStatusPath, refreshFunction);
}

function updateSensorDiv(div, sensor) {

    div.getElementsByTagName('h2')[0].innerHTML = 'Sensore ' + sensor.name;
    div.getElementsByTagName('input')['enabled'].checked = true;
    div.getElementsByTagName('input')['name'].value = sensor.name;
    //div.getElementsByTagName('input')['addr'].value = sensor.addr;

    div.getElementsByTagName('select')['sensorTypeSelect'].addEventListener("click", function () {
        onChangeSensorType(this);
    }, false);

    if (sensor.type == 'onewiresensor') {

    }
    /*nameForm = div.getElementsByTagName('form')['nameForm'];
     nameForm.id = 'nameFrom'+n;
     nameForm.onsubmit = function () {
     event.preventDefault();
     sendPost(this, commandResponse);
     };*/
}

/*var onChangeTemperatureSensorNumberFunction = function onChangeTemperatureSensorNumber() {

 n = this.value;
 div = temperatureDiv.getElementsByTagName('div')['subsensor'];

 };*/

function addTemperatureProperties(div) {
    var newDiv = temperatureDiv.cloneNode(true);
    numdiv = newDiv.getElementsByTagName('input')['temperatureSensorNumber'];
    numdiv.addEventListener('input', function () {
        //onChangeTemperatureSensorNumberFunction(this.value);
        subSensorDiv = newDiv.getElementsByTagName('div')['subsensor'];
        subSensorDiv.style.display = 'none';
        subSensorList = newDiv.getElementsByTagName('div')['subsensorlist'];
        subSensorList.innerHTML = '';
        for (i = 0; i < this.value; i++) {
            newsubdiv = subSensorDiv.cloneNode(true);
            subSensorDiv.style.display = 'block';
            subSensorList.appendChild(newsubdiv);
        }
    });
    newDiv.style.display = 'block';
    div.innerHTML = '';
    div.appendChild(newDiv);
}
function onChangeSensorType(elem, value) {

    type = elem.value;
    div = elem.parentNode.getElementsByTagName('div')['sensorproperties'];
    if (type == 'onewiresensor') {

        addTemperatureProperties(div);


    } else {

        var newDiv = doorDiv.cloneNode(true);
        newDiv.style.display = 'block';
        div.innerHTML = '';
        div.appendChild(newDiv);
    }
};

function deleteSensor(element) {
    element.parentNode.removeChild(element);
}

function addSensor() {
    var newSensor = sensorDiv.cloneNode(true); // true means clone all childNodes and all event handlers
    newSensor.style.display = 'block';
    newSensor.getElementsByTagName('input')['deleteButton'].addEventListener("click", function () {
        deleteSensor(newSensor);
    }, false);
    //newSensor.id = sensorDiv.id + i;
    //updateSensorDiv(clone, json.sensors[i]);
    listDiv.appendChild(newSensor);
    return newSensor;
}

function save() {

    list = listDiv.getElementsByClassName('box');
    var sensorsJson = {
        'command': 'updatesensorlist',
        sensors: []
    };
    for (i = 0; i < list.length; i++) {

        var item = list[i];
        type = item.getElementsByTagName('select')['sensortype'].value;
        pin = item.getElementsByTagName('select')['pin'].value;
        name = item.getElementsByTagName('input')['name'].value;
        enabled = item.getElementsByTagName('input')['enabled'].checked;

        var poperties = {};
        if (type == 'onewiresensor') {

            sublist = item.getElementsByTagName('div')['onewiresensor'].getElementsByTagName('div');
            //poperties ['title'] = 'id';
            //poperties ['email'] = 'email';
            var str = '';
            for(k = 0; k < sublist.length;k++) {

                temperaturesensor = {};
                temperaturesensor ['name'] = 'nome';
                if (k > 0)
                    str += ',';
                str += JSON.stringify(temperaturesensor);
            }
            poperties ['temperaturesensors'] = str;

        } else {

        }
        sensorsJson.sensors.push({
            'name': name,
            'type': type,
            'enabled': enabled,
            'properties' : poperties
        });
    }

    sendCommand(sensorsJson, commandResponse);
}

function commandResponse(json) {
    document.getElementById('command').innerHTML += 'command result' + JSON.stringify(json);
    getJson(temperatureSensorsStatusPath, refreshFunction);
}

function sendPost(form, callback) {
    var data = formInputToJSON(form);
    sendCommand(data, callback);
}
