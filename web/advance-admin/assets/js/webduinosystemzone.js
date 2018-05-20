/**
 * Created by gs163400 on 13/05/2018.
 */

function loadWebduinoSystemZone(webduinosystemzone) {

    $("#result").load("webduinosystemzone.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getWebduinoSystem(webduinosystemzone.webduinosystemid, function (webduinosystem) {
                loadWebduinoSystem(webduinosystem);
            })
        });
        pagetitle.text('Zona');
        notification.hide();

        var panel = $(this).find('div[id="zonepanel"]');
        panel.find('p[name="headingright"]').text(webduinosystemzone.scenarioid + "." + webduinosystemzone.id);

        getZones(function (zones) {
            $.each(zones, function (val, zone) {
                panel.find('select[name="zoneid"]').append(new Option(zone.name, zone.id));
            });
            panel.find('select[name="zoneid"]').val(webduinosystemzone.zoneid);
        });

        // save button
        var savebutton = panel.find('button[name="save"]');
        savebutton.click(function () {
            webduinosystemzone.zoneid = panel.find('select[name="zoneid"]').val();
            postData("webduinosystemzone", webduinosystemzone, function (result, response) {
                if (result) {
                    var json = jQuery.parseJSON(response);
                    getWebduinoSystem(json.webduinosystemid, function (webduinosystem) {
                        loadWebduinoSystem(webduinosystem);
                    });
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            });
        });

        var cancelbutton = panel.find('button[name="cancel"]');
        cancelbutton.click(function () {
            getWebduinoSystem(webduinosystemzone.webduinosystemid, function (webduinosystem) {
                loadWebduinoSystem(webduinosystem);
            });
        });

        var deletebutton = panel.find('button[name="delete"]');
        deletebutton.click(function () {

            postData("webduinosystemzone", webduinosystemzone, function (result, response) {
                if (result) {
                    var json = jQuery.parseJSON(response);
                    loadWebduinoSystem(json);
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            }, "delete");
        });

        if (webduinosystemzone.id == 0)
            deletebutton.prop('disabled', true);
    });
}
