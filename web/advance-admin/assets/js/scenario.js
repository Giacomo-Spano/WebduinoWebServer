var shieldId = 0;

var scenarioPanel;
var scenarioRow;

var listDiv;
var temperatureDiv;
var doorDiv;
var genericSensorDiv;

function load() {

    /*shieldId = getUrlVars()["id"];

    document.getElementById('shield').onsubmit = function (event) {
        event.preventDefault();
        sendPost(this, commandResponse);
    };
    getJson(settingsPath+"&id="+shieldId, refreshFunction);*/

    loadScenario();
}

function commandResponse(json) {
    getJson(settingsPath+"&id="+shieldId, refreshFunction);
}

var refreshFunction = function refresh(json) {
    document.getElementById('summary').innerHTML = JSON.stringify(json);

    shieldId = json.shieldid;
    document.getElementById('shieldid').value = shieldId;
    document.getElementById('localport').value = json.port;
    document.getElementById('shieldname').value = json.shieldname;
    //document.getElementById('ssid').value = json.ssid;
    //document.getElementById('password').value = json.password;
    document.getElementById('server').value = json.server;
    document.getElementById('serverport').value = json.serverport;
    document.getElementById('mqttserver').value = json.mqttserver;
    document.getElementById('mqttport').value = json.mqttport;

    refreshSensorsFunction(json);
};

function sendPost(form, callback) {
    var data = formInputToJSON(form);
    sendCommand(data, callback);
}

//---------------

var refreshSensorsFunction = function refresh(json) {

    listDiv.innerHTML = '';

    for (j = 0; j < json.sensors.length; j++) {
        div = addSensor(json.sensors[j]);
    }

    document.getElementById('addSensorButton').addEventListener("click", function () {
        addSensor(null);
    }, false);

    document.getElementById('saveButton').addEventListener("click", function () {
        save();
    }, false);

    sensorDiv.style.display = 'none';
    temperatureDiv.style.display = 'none';
    doorDiv.style.display = 'none';
    genericSensorDiv.style.display = 'none';
};

function loadScenario() {
    //scenarioTable = document.getElementById('scenarioTable');
    scenarioPanel = $(this).find('div[id="scenarioPanel"]');
    //scenarioRow = document.getElementById('scenarioRow');
    scenarioRow = scenarioPanel.find('tr[name="scenarioRow"]');


    var newRow = scenarioRow.clone();
    scenarioTable.last().after(newRow);


    genericSensorDiv = document.getElementById('genericsensor');
    temperatureDiv = document.getElementById('onewiresensor');
    doorDiv = document.getElementById('doorsensor');
    listDiv = document.getElementById('sensorList');
}

function addSensor(sensor) {

    var newSensor = sensorDiv.cloneNode(true); // true means clone all childNodes and all event handlers
    newSensor.style.display = 'block';
    newSensor.getElementsByTagName('input')['deleteButton'].addEventListener("click", function () {
        deleteSensor(newSensor);
    }, false);

    if (sensor != null) {
        newSensor.setAttribute('id','sensor' + sensor.id);
        newSensor.setAttribute('name','sensor'); // il nome 'sensor' viene utilizzzato nella save per trovare tutti i sensori
        newSensor.getElementsByTagName('input')['sensorid'].value = sensor.id;
        newSensor.getElementsByTagName('div')['sensorTitle'].innerHTML = 'Sensore ' + sensor.id + ': ' + sensor.name;
        newSensor.getElementsByTagName('input')['enabled'].checked = true;
        newSensor.getElementsByTagName('input')['name'].value = sensor.name;
        newSensor.getElementsByTagName('select')['sensorTypeSelect'].value = sensor.type;
        newSensor.getElementsByTagName('select')['pin'].value = sensor.pin;
    } else {
        newSensor.setAttribute('id','sensor' + 0);
        newSensor.setAttribute('name','sensor'); // il nome 'sensor' viene utilizzzato nella save per trovare tutti i sensori
        newSensor.getElementsByTagName('input')['sensorid'].value = 0;
    }
    newSensor.getElementsByTagName('select')['sensorTypeSelect'].addEventListener("change", function () {
        onChangeSensorType(this);
    }, false);

    div = newSensor.getElementsByTagName('div')['sensorproperties'];
    if (sensor != null) {
        addSensorProperties(div, sensor.type, sensor);
    } else {
        addSensorProperties(div, "onewiresensor", null);
    }

    listDiv.appendChild(newSensor);
    return newSensor;
}

function addSensorProperties(div,type,sensor) {

    if (type == 'onewiresensor') {
        addTemperatureProperties(div,sensor);
    } else {
        div.innerHTML = '';
    }
}

function addTemperatureProperties(div,sensor) {

    var newDiv = temperatureDiv.cloneNode(true);
    addSubTemperatureSensor(newDiv,sensor);
    newDiv.style.display = 'block';
    div.innerHTML = '';
    div.appendChild(newDiv);
}

function addSubTemperatureSensor(div,sensor) {
    subSensorDiv = div.getElementsByTagName('div')['subsensor'];
    subSensorList = div.getElementsByTagName('div')['subsensorlist'];
    subSensorList.innerHTML = '';

    var numsensors = 1;

    if (sensor != null) {
        for (i = 0; i < sensor.childsensors.length; i++) {
            newsubdiv = subSensorDiv.cloneNode(true);
            newsubdiv.getElementsByTagName('input')['name'].value = sensor.childsensors[i].name;
            subSensorDiv.style.display = 'block';
            subSensorList.appendChild(newsubdiv);
        }
        numsensors = i;
    } else {
        newsubdiv = subSensorDiv.cloneNode(true);
        newsubdiv.getElementsByTagName('input')['name'].value = "nome";
        subSensorDiv.style.display = 'block';
        subSensorList.appendChild(newsubdiv);
        numsensors = 1;
    }

    var numdiv = div.getElementsByTagName('input')['temperatureSensorNumber'];
    numdiv.addEventListener('input', function () {
        //onChangeTemperatureSensorNumberFunction(this.value);

    });
}

function onChangeSensorType(elem/*, value*/) {

    type = elem.value;
    div = elem.parentNode.getElementsByTagName('div')['sensorproperties'];

    addSensorProperties(div,type,null);
};

function deleteSensor(element) {
    element.parentNode.removeChild(element);
}

function save() {

    var sensorsJson = {
        'command': 'saveshieldsettings',
        'shieldid': document.getElementById('shieldid'),
        'localport': document.getElementById('localport'),
        'shieldname': document.getElementById('shieldname'),
        'server': document.getElementById('server'),
        'serverport': document.getElementById('serverport'),
        'mqttserver': document.getElementById('mqttserver'),
        'mqttport': document.getElementById('mqttport'),
        sensors: []
    };

    list = document.getElementsByName('sensor');
    for (i = 0; i < list.length; i++) {

        var item = list[i];
        var sensorid = item.getElementsByTagName('input')['sensorid'].value;
        var type = item.getElementsByTagName('select')['sensortype'].value;
        var pin = item.getElementsByTagName('select')['pin'].value;
        var name = item.getElementsByTagName('input')['name'].value;
        var enabled = item.getElementsByTagName('input')['enabled'].checked;
        var address = i+1;

        if (type == 'onewiresensor') {

            properties = item.getElementsByTagName('div')['sensorproperties'];
            subsensorlist = properties.getElementsByClassName('subsensorbox');

            sensorsJson.sensors.push({
                'sensorid': sensorid,
                'name': name,
                'type': type,
                'enabled': enabled,
                'pin': pin,
                'addr': '' + address,
                'childsensors': []
            });

            for(k = 0; k < subsensorlist.length;k++) {


                var childname = subsensorlist[k].getElementsByTagName('input')['name'].value;

                var childjson = {
                    "name"  :  "childname",
                    "id"   :  (k+1),
                    "enabled"      :  true,
                    "pin"  :  pin,
                    "addr"  :  '' + address + '.' + (k+1),
                    "type"  :  "temperaturesensor"
                }

                sensorsJson.sensors[i].childsensors.push(childjson)
            }




        } else if (type == 'doorsensor') {

            //properties  = "";
            sensorsJson.sensors.push({
                'sensorid': sensorid,
                'name': name,
                'type': type,
                'enabled': enabled,
                'addr': '' + address,
                'pin': pin
            });

        } else
        {
            sensorsJson.sensors.push({
                'sensorid': sensorid,
                'name': name,
                'type': type,
                'enabled': enabled,
                'addr': address,
                'pin': pin
            });
        }

    }

    sendCommand(sensorsJson, commandResponse);
}

function commandResponse(json) {
    document.getElementById('command').innerHTML += 'command result' + JSON.stringify(json);
    getJson(sensorsStatusPath+"&id="+shieldId, refreshFunction);
}

