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

                getWebduinoSystemTypes(function (types) {
                    var system = {
                        "id": 0,
                        "type": types[0].type,
                        "name": "sistema nuovo",
                        "enabled": true,
                    };
                    loadWebduinoSystem(system);
                })
            });
        });
    });
}

function setSystemElement(idx, element, system) {

    element.find('td[name="id"]').text(system.id);
    element.find('td[name="name"]').text(system.name);
    element.find('input[name="systemenabled"]').prop('checked',system.enabled);
    element.find('td[name="type"]').text(system.type);
    element.find('td[name="status"]').text(system.status);
    element.attr("idx", idx);

    element.click(function () {
        var index = $(this).attr("idx");
        loadWebduinoSystem($systems[index]);
    });
}