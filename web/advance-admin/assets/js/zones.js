var $zones;

function loadZones() {

    $("#result").load("zones.html", function () {
        $panel = $(this).find('div[id="panel"]');
        $row = $panel.find('tr[name="row"]');

        var tbody = $panel.find('tbody[name="list"]');
        $.getJSON(systemServletPath + "?requestcommand=zones", function (data) {

            $zones = data;
            tbody[0].innerHTML = "";
            $.each(data, function (idx, elem) {
                var newtr = $row.clone();
                setZoneElement(idx, newtr, elem);
                tbody.append(newtr);
            });


            $panel.find('button[name="add"]').click(function () {

                var zone = {
                    "id": 0,
                    "name": "zona nuova",
                };
                postData("zone", zone, function (result, response) {
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

function setZoneElement(idx, element, zone) {

    element.find('td[name="id"]').text(zone.id);
    element.find('td[name="name"]').text(zone.name);


    element.find('button[name="delete"]').attr("idx", idx);
    element.find('button[name="delete"]').click(function () {

        var index = $(this).attr("idx");
        var json = $zones[index];

        postData("zone", json, function (result, response) {
            if (result) {
                var json = jQuery.parseJSON(response);
                loadZones();
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        }, "delete");

    });

    element.find('button[name="details"]').attr("idx", idx);
    element.find('button[name="details"]').click(function () {
        var index = $(this).attr("idx");
        loadZone($zones[index]);


    });
}