var actuatorServletPath = "../actuator";
var $mTemperature;

function loadTemperature() {
    $.getJSON(actuatorServletPath + "?id=1", function (data) {

            $mTemperature.text(data.remotetemperature);
            $mRele.removeClass("main-box mb-red");
            $mRele.addClass("main-box mb-blue");

        })
        .done(function () {
        })
        .fail(function () {
        });
}

function load() {

    //$mTemperature = $(this).find('.temperature');
    $mTemperature = $(this).find('h5[id="temperature"]');
    $mRele = $(this).find('div[id="rele"]');
    //$mSensorRow = $mSensorPanel.find('tr[name="sensor"]');

    loadTemperature();
    //loadActiveProgram();

    //setTimeout(getSensors, 20000);

    //setTimeout(loadActuators, 20000);
    //setTimeout(loadActiveProgram, 20000);


}