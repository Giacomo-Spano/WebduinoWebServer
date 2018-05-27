/**
 * Created by giaco on 27/10/2017.
 */
var $triggerPanel;
var $triggerRow;
var $triggers;

function getTimerange(id, callback) {
    $.getJSON(systemServletPath + "?requestcommand=timerange&id=" + id, function (program) {
        callback(program);
    });
}

function addTrigger(idx, elem) {
    var trigger = $triggerRow.clone();

    trigger.find('td[name="id"]').text(elem.id);
    trigger.find('td input[name="name"]').val(elem.name);
    trigger.find('td[name="status"]').text(elem.status);
    trigger.find('button[name="deletetrigger"]').attr("idx", idx);
    trigger.find('button[name="deletetrigger"]').click(function () {
        var index = $(this).attr("idx");
        $triggers.splice(index, 1);
        loadTriggerList($triggers);
        triggersDisableEdit(false);
    });

    trigger.find('button[name="enabletrigger"]').click(function () {
        var triggerCommand = {
            "id" : elem.id,
            "status" : "enabled"
        };
        postData("trigger", triggerCommand, function (result, response) {
            if (result) {
                loadTriggers();
                notification.show();
                notification.find('label[name="description"]').text("trigger disabilitato");
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        });
    });

    trigger.find('button[name="disabletrigger"]').click(function () {
        var triggerCommand = {
            "id" : elem.id,
            "status" : "disabled"
        };
        postData("trigger", triggerCommand, function (result, response) {
            if (result) {
                loadTriggers();
                notification.show();
                notification.find('label[name="description"]').text("trigger abilitato");
            } else {
                notification.show();
                notification.find('label[name="description"]').text(response);
            }
        });
    });

    $triggerPanel.find('tbody[name="list"]').append(trigger);
}

function triggersDisableEdit(enabled) {
    $triggerPanel.find('input').prop('disabled', enabled);
    $triggerPanel.find('textarea').prop('disabled', enabled);
    $triggerPanel.find('select').prop('disabled', enabled);

    if (!enabled)
        $triggerPanel.find('p[class="help-block"]').hide();
    else
        $triggerPanel.find('p[class="help-block"]').show();

    $triggerPanel.find('button[name="addactio"]').prop('disabled', enabled);
    $triggerPanel.find('button[name="deleteaction"]').prop('disabled', enabled);
    $triggerPanel.find('button[name="editaction"]').prop('disabled', !enabled);

}
function loadTriggers() {

    $.getJSON(systemServletPath + "?requestcommand=triggers", function (triggers) {

        $triggers = triggers;

        $("#result").load("triggers.html", function () {
                // back button
                backbutton.unbind("click");
                backbutton.click(function () {
                    /*getProgram(timerange.programid, function (program) {
                     loadProgram(program);
                     })*/
                });
                pagetitle.text('Trigger');
                notification.hide();
                notificationsuccess.hide();

                $triggerPanel = $(this).find('div[id="panel"]');
                $triggerRow = $triggerPanel.find('tr[name="row"]');

                // save button
                var savebutton = $triggerPanel.find('button[name="save"]');
                savebutton.hide();
                triggersDisableEdit(true);
                savebutton.click(function () {

                    var triggerList = {
                        "triggers" : $triggers,
                    };

                    updateTriggerData();
                    postData("triggers", triggerList, function (result, response) {
                        if (result) {
                            var json = jQuery.parseJSON(response);
                            loadTriggers();
                        } else {
                            notification.show();
                            notification.find('label[name="description"]').text(response);
                        }
                    });


                });

                var cancelbutton = $triggerPanel.find('button[name="cancel"]');
                cancelbutton.hide();
                cancelbutton.click(function () {
                    getTimerange($scenario.id, function (timerange) {
                        //loadTimeRange(timerange);
                    })
                });

                var editbutton = $triggerPanel.find('button[name="edit"]');
                editbutton.click(function () {
                    savebutton.show();
                    cancelbutton.show();
                    editbutton.hide();
                    addbutton.show();
                    triggersDisableEdit(false);
                });

                // triggers
                loadTriggerList($triggers);

                var addbutton = $triggerPanel.find('button[name="add"]');
                addbutton.hide();


                addbutton.click(function () {
                    updateTriggerData(); // questo serve per aggiornare eventuali modifiche manuali
                    var trigger = {
                        "id": 0,
                        "name": "nuovo action",
                        "status": false,
                    };
                    $triggers.push(trigger);

                    $triggerPanel.find('tbody[name="list"]').innerHTML = "";
                    if ($triggers != null) {
                        loadTriggerList($triggers);
                    }
                    triggersDisableEdit(false);
                });
            }
        );
    });

}


function loadTriggerList(triggers) {
    var tbody = $triggerPanel.find('tbody[name="list"]');
    tbody[0].innerHTML = "";
    if (triggers != null) {
        $.each(triggers, function (idx, elem) {
            addTrigger(idx, elem);
        });
    }
}

function updateTriggerData() {
    if ($triggers != undefined) {
        var i = 0;
        $triggerPanel.find('tr[name="row"]').each(function (idx, elem) {
            var elem = $triggers[i];
            elem.name = $(this).find('td input[name="name"]').val();
            i++;
        });
    }
}
