var $systems;

function loadWebduinoSystems() {

    $("#result").load("webduinosystems.html", function () {
        $panel = $(this).find('div[id="panel"]');
        $row = $panel.find('tr[name="row"]');

        var tbody = $panel.find('tbody[name="list"]');
        $.getJSON(systemServletPath + "?requestcommand=webduinosystems", function (data) {

            $systems = data;
            tbody[0].innerHTML = "";
            $.each(data, function (idx, elem) {
                var newtr = $row.clone();
                setSystemElement(idx, newtr, elem);
                tbody.append(newtr);
            });

            $panel.find('button[name="add"]').click(function () {

                var system = {
                    "id": 0,
                    "name": "sistema nuovo",
                };
                postData("webduinosystem", system, function (result, response) {
                    if (result) {
                        var json = jQuery.parseJSON(response);
                        loadZones();
                    } else {
                        notification.show();
                        notification.find('label[name="description"]').text(response);
                    }
                });
            });
        });
    });
}

function setSystemElement(idx, element, system) {

    element.find('td[name="id"]').text(system.id);
    element.find('td[name="name"]').text(system.name);
    element.attr("idx", idx);

    element.click(function () {
        var index = $(this).attr("idx");
        loadWebduinoSystem($systems[index]);
    });
}