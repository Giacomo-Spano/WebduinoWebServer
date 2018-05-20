/**
 * Created by gs163400 on 13/05/2018.
 */

function loadWebduinoSystemActuator(webduinosystemactuator, webduinosystem) {

    $("#result").load("webduinosystemactuator.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getWebduinoSystem(webduinosystemactuator.webduinosystemid, function (webduinosystem) {
                loadWebduinoSystem(webduinosystem);
            })
        });
        pagetitle.text('Attuatore');
        notification.hide();

        var panel = $(this).find('div[id="actuatorpanel"]');
        panel.find('p[name="headingright"]').text(webduinosystemactuator.scenarioid + "." + webduinosystemactuator.id);

        getSensors(function (sensors) {
            $.each(sensors, function (val, actuator) {
                panel.find('select[name="actuatorid"]').append(new Option(actuator.name, actuator.id));
            });
            panel.find('select[name="actuatorid"]').val(webduinosystemactuator.actuatorid);
        });

        // save button
        var savebutton = panel.find('button[name="save"]');
        savebutton.click(function () {
            webduinosystemactuator.actuatorid = panel.find('select[name="actuatorid"]').val();
            postData("webduinosystemactuator", webduinosystemactuator, function (result, response) {
                if (result) {
                    var json = jQuery.parseJSON(response);
                    getWebduinoSystem(json.webduinosystemid, function (webduinosystem) {
                        loadWebduinoSystem(webduinosystem);
                    })
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });

        var cancelbutton = panel.find('button[name="cancel"]');
        cancelbutton.click(function () {
            getWebduinoSystem(webduinosystemactuator.webduinosystemid, function (webduinosystem) {
                loadWebduinoSystem(webduinosystem);
            });
            loadWebduinoSystem(webduinosystem);
        });

        var $webduinosystemactuator = webduinosystemactuator;
        var deletebutton = panel.find('button[name="delete"]');
        deletebutton.click(function () {

            postData("webduinosystemactuator", $webduinosystemactuator, function (result, response) {
                if (result) {
                    var json = jQuery.parseJSON(response);
                    getWebduinoSystem(json.id, function (webduinosystem) {
                        loadWebduinoSystem(webduinosystem);
                    });
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            }, "delete");
        });
        if (webduinosystemactuator.id == 0)
            deletebutton.prop('disabled', true);
    });
}
