/**
 * Created by gs163400 on 13/05/2018.
 */

function loadWebduinoSystemService(webduinosystemservice) {

    $("#result").load("webduinosystemservice.html", function () {

        // back button
        backbutton.unbind("click");
        backbutton.click(function () {
            getWebduinoSystem(webduinosystemservice.webduinosystemid, function (webduinosystem) {
                loadWebduinoSystem(webduinosystem);
            })
        });
        pagetitle.text('Servizio');
        notification.hide();

        var panel = $(this).find('div[id="servicepanel"]');
        panel.find('p[name="headingright"]').text(webduinosystemservice.scenarioid + "." + webduinosystemservice.id);

        getServices(function (service) {
            $.each(service, function (val, service) {
                panel.find('select[name="serviceid"]').append(new Option(service.name, service.id));
            });
            panel.find('select[name="serviceid"]').val(webduinosystemservice.serviceid);
        });

        // save button
        var savebutton = panel.find('button[name="save"]');
        savebutton.click(function () {

            webduinosystemservice.serviceid = panel.find('select[name="serviceid"]').val();
            postData("webduinosystemservice", webduinosystemservice, function (result, response) {
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
            getWebduinoSystem(webduinosystemservice.webduinosystemid,function (system) {
                loadWebduinoSystem(system);
            });
        });

        var deletebutton = panel.find('button[name="delete"]');
        deletebutton.click(function () {

            postData("webduinosystemservice", webduinosystemservice, function (result, response) {
                if (result) {
                    getWebduinoSystem(webduinosystemservice.webduinosystemid, function (scenario) {
                        loadWebduinoSystem(scenario);
                    })
                } else {
                    notification.show();
                    notification.find('label[name="description"]').text(response);
                }
            },"delete");
        });
        if (webduinosystemservice.id == 0)
            deletebutton.prop('disabled', true);
    });
}
