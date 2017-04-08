var $mProgramForm;
var $mProgramPanel;

var programservletpath = "../program";
var sensorServletPath = "../sensor";


var $sensors = [];


function load() {

    $mProgramForm = $(this).find('div[id="programformlist"]');
    $mProgramForm.attr('style', 'display: none');
    $mProgramPanel = $(this).find('div[id="programlist"]');
    $mProgramPanel.attr('style', 'display: none');

    $(this).find('button#addprogram').click(onAddProgramButtonClick);

    var jqxhr = $.getJSON(programservletpath, function (data) {
        console.log("success");

        var $lastlistelem = $('.row#programlist');

        a = data;
        $.each(a, function (idx, elem) {

                $program = createProgramPanel(elem/*, $lastlistelem*/)

                $lastlistelem.after($program);
                $lastlistelem = $program;
            }
        );
    })
        .done(function () {
        })
        .fail(function () {
            alert("error1");
        })
        .always(function () {
        });

    var jqxhr = $.getJSON(sensorServletPath, function (data) {

        for (i = 0; i < data.length; i++) {
            $sensors.push(data[i]);
        }


    })
        .done(function () {
            console.log("succes");
        })
        .fail(function () {
            console.log("error1");
            alert("error1");
        })
        .always(function () {
            console.log("error2");
        });
}

function createProgramPanel(elem) {

    var $program = $mProgramPanel.clone();
    $($program).attr('style', 'display: visible');
    $($program).find('button#change').click(onChangeButtonClicked);
    $($program).find(".panel-heading").text("Programma " + elem.id + elem.name);
    $($program).find('div[id="programid"]').text(elem.id);// questo serve, non eliminare

    if (elem.active) {

        if (elem.dateenabled) {
            $($program).find('td[id="active"]').text("da - a");
            if (elem.startdate && elem.starttime) {
                $($program).find('td[id="startdate"]').text(elem.startdate + " " + elem.starttime);
            }
            if (elem.enddate && elem.endtime) {
                $($program).find('td[id="enddate"]').text(elem.enddate + " " + elem.endtime);
            }

        } else {

            $($program).find('td[id="active"]').text("Sempre");
            $($program).find('td[id="startdate"]').text("--");
            $($program).find('td[id="enddate"]').text("--");
        }
    } else {
        $($program).find('td[id="active"]').text("Mai");
    }

    var day = getDays(elem);
    $($program).find('td[id="days"]').text(day);

    var $tr = $($program).find('tr[id="timerange"]');
    starttime = "00:00";
    for (i = 0; i < elem.timeranges.length; i++) {

        if (i > 0) {
            $tr2 = $tr.clone();
            $tr.after($tr2);
            $tr = $tr2;
        }
        $tr.find('td[id="timerangename"]').text(elem.timeranges[i].name);
        $tr.find('td[id="starttime"]').text(starttime);
        $tr.find('td[id="id"]').text(elem.timeranges[i].id);
        $tr.find('td[id="endtime"]').text(elem.timeranges[i].endtime);
        $tr.find('td[id="sensor"]').text(elem.timeranges[i].sensorid);
        $tr.find('td[id="temperature"]').text(elem.timeranges[i].temperature);

        starttime = elem.timeranges[i].endtime;
    }
    $($program).find('tr[id="tr"]');
    return $program;
}

function createProgramForm(elem) {
    var $newform = $mProgramForm.clone();
    $newform.attr('style', 'display: visible');

    $idinput = $newform.find('input[name="programid"]');
    $idinput.val(elem.id);

    $idinput = $newform.find('input[name="name"]');
    $idinput.val(elem.name);

    $input = $newform.find('input[name="active"]');
    if (elem.active) {
        if (elem.dateenabled)
            $newform.find('input:radio[name="active"][value=daterange]').click();
        else
            $newform.find('input:radio[name="active"][value=enabled]').click();
    } else {
        $newform.find('input:radio[name="active"][value=disabled]').click();
    }

    $input = $newform.find('input[name="startdate"]');
    $input.val(elem.startdate);

    $input = $newform.find('input[name="starttime"]');
    $input.val(elem.starttime);

    $input = $newform.find('input[name="enddate"]');
    $input.val(elem.enddate);

    $input = $newform.find('input[name="endtime"]');
    $input.val(elem.endtime);

    $input = $newform.find('option[name="Su"]').attr('selected', elem.sunday);
    $input = $newform.find('option[name="Mo"]').attr('selected', elem.monday);
    $input = $newform.find('option[name="Tu"]').attr('selected', elem.tuesday);
    $input = $newform.find('option[name="We"]').attr('selected', elem.wednesday);
    $input = $newform.find('option[name="Th"]').attr('selected', elem.thursday);
    $input = $newform.find('option[name="Fr"]').attr('selected', elem.friday);
    $input = $newform.find('option[name="Sa"]').attr('selected', elem.saturday);

    $tr = $newform.find('tr[id="timerange"]');//.html();

    var starttime = "00:00";
    jQuery.each(elem.timeranges, function (i, value) {


        if (i > 0) {
            $tr2 = $tr.clone();
            $tr.after($tr2);
            $tr = $tr2;
        }

        $tr.find('input[name="timerangename"]').val(value.name);
        $tr.find('input[name="temperature"]').val(value.temperature);
        $tr.find('input[name="endtime"]').val(value.endtime);


        sensorCombo = $tr.find('select[name="sensor"]');//.val(value.sensorid);

        sensorCombo.empty();
        for (i = 0; i < $sensors.length; i++) {
            sensorCombo.append("<option value='" + $sensors[i].id + "'>" + $sensors[i].name + "</option>");
            /*if (value.sensorid == $sensors[i].id)
                curval = $sensors[i].id*/


        }
        sensorCombo.val(value.sensorid);

        $tr.find('td[id="timerangestarttime"]').text(starttime);

        $tr.find('input[name="endtime"]').change(function () { // set next timerange starttime to this endtime

            if ($(this).parent().parent().next().length != 0)
                $(this).parent().parent().next().find('td[id="timerangestarttime"]').text($(this).val());
        });
        $deletetimerangebutton = $tr.find('button[name="deletebutton"]');
        $deletetimerangebutton.click(onDeleteTimeRangeButtonClick);

        $insertbutton = $tr.find('button[name="insertbutton"]');
        $insertbutton.click(onAddTimeRangeButtonClick);

        starttime = value.endtime;

    });

    $savebutton = $newform.find('button[name="save"]');
    $savebutton.click(onSaveButtonClick);

    $deletebutton = $newform.find('button[name="delete"]');
    $deletebutton.click(onDeleteProgramButtonClick);

    $cancelbutton = $newform.find('button[name="cancel"]');
    $cancelbutton.click(onCancelButtonClick);

    return $newform;
}

/*function setSubAddress(node, id, subaddress) {


    sub = node.find('select[name="subaddress"]');
    sub.val(subaddress);
    sub.empty();
    //sub.append("<option value='6'>" + id + "</option>");

    for (i = 0; i < $shields.length; i++) {
        if ($shields[i].id == id) {
            for (k = 0; k < $shields[i].sensors.length; k++) {
                sub.append("<option value='" + $shields[i].sensors[k].subaddress + "'>" + $shields[i].sensors[k].subaddress + "</option>");
            }
        }
    }

}*/

function onChangeButtonClicked() { // clicked 'modifica' button. Enable save button and inpu controls

    var $form = $(this).parent().parent();
    programid = $form.find('div[id="programid"]').html();

    var json = $.getJSON(programservletpath + '?id=' + programid, function (elem) {

        var $newform = createProgramForm(elem);

        $form.after($newform);
        $form.remove();
    })
        .done(function () {
            console.log("succes");
        })
        .fail(function () {
            console.log("error1");
            alert("impossibile salvare programma " + jsondata.id);
        })
        .always(function () {
            console.log("error2");
        });
}

function saveProgram(program, form, callback) {

    $.ajax({
        type: "POST",
        url: programservletpath,
        data: JSON.stringify(program),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        processData: true,
        success: function (data, status, jqXHR) {

            callback(data, form);
        },
        error: function (xhr) {
            alert(xhr.responseText);
        }
    });
}

function onDeleteProgramButtonClick() {

    programid = $(this).parent().parent().find("input[name='programid']").val();
    form = $(this).parent();

    $.ajax({
        type: "POST",
        url: programservletpath + '?delete=true&id=' + programid,
        data: JSON.stringify(program),
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        processData: true,
        success: function (data, status, jqXHR) {

            form.parent().remove();
            //commanCallback(data, form);

        },
        error: function (xhr) {
            alert(xhr.responseText);
        }
    });


}

function onCancelButtonClick() {

    programid = $(this).parent().parent().find("input[name='programid']").val();
    form = $(this).parent();

    var json = $.getJSON(programservletpath + '?id=' + programid, function (elem) {

        $program = createProgramPanel(elem)

        form.parent().replaceWith($program);

    })
        .done(function () {
            console.log("succes");
        })
        .fail(function () {
            console.log("error1");
            alert("impossibile salvare programma " + jsondata.id);
        })
        .always(function () {
            console.log("error2");
        });
}

function onAddTimeRangeButtonClick() {

    //var $tr = $(this).parent().parent();
    //$tr.remove();

    //$tr = $(this).parent().find('tr[id="timerange"]');
    tr = $(this).parent().parent();
    newtr = tr.clone();
    newtr.find('input[name="temperature"]').val("20");
    newtr.find('select[name="sensor"]').val("0");
    newtr.find('input[name="endtime"]').val("00:00");
    newtr.find('input[name="endtime"]').change(function () { // set next timerange starttime to this endtime
        newtr.find('td[id="starttime"]').text($(this).val());
    });
    $deletetimerangebutton = newtr.find('button[name="deletebutton"]');
    $deletetimerangebutton.click(onDeleteTimeRangeButtonClick);
    tr.after(newtr);
}

function onAddProgramButtonClick() {

    var prgm = {
        "id": "1",
        "name": "nxome",
        "active": true,
        "dateenabled": false,
        "startdate": "",
        "starttime": "",
        "enddate": "",
        "endtime": "",
        "sunday": true,
        "monday": true,
        "tuesday": true,
        "wednesday": true,
        "thursday": true,
        "friday": true,
        "saturday": true,
        timeranges: []
    };

    prgm.timeranges.push({
        "endtime": "19:17",
        "sensor": "1",
        "temperature": "20"
    });


    var newform = createProgramForm(prgm);


    var programlist = $('.row#programlist');
    lastprogram = programlist.siblings(':last-child');

    lastprogram.after(newform);


}

function onDeleteTimeRangeButtonClick() {

    var $tr = $(this).parent().parent();
    $tr.remove();
}

function onSaveButtonClick() {

    var programid = $(this).attr("programid");
    event.preventDefault();

    var $form = $(this).parent();
    var $tr = $form.find(".table#tbl tr#tr");

    if (($tr.find("input:radio[name='active'][value='enabled']").is(":checked"))) {
        active = true;
        dateenabled = false;
    } else if (($tr.find("input:radio[name='active'][value='daterange']").is(":checked"))) {
        active = true;
        dateenabled = true;
    } else {
        active = false;
        dateenabled = false;
    }


    var program = {
        "id": $form.find("input[name='programid']").val(),
        "name": $form.find("input[name='name']").val(),
        "active": active,
        "dateenabled": dateenabled,
        "startdate": $tr.find("input[name='startdate']").val(),
        "starttime": $tr.find("input[name='starttime']").val(),
        "enddate": $tr.find("input[name='enddate']").val(),
        "endtime": $tr.find("input[name='endtime']").val(),
        "sunday": $tr.find("option[name=Su]").is(':selected'),
        "monday": $tr.find("option[name=Mo]").is(':selected'),
        "tuesday": $tr.find("option[name=Tu]").is(':selected'),
        "wednesday": $tr.find("option[name=We]").is(':selected'),
        "thursday": $tr.find("option[name=Th]").is(':selected'),
        "friday": $tr.find("option[name=Fr]").is(':selected'),
        "saturday": $tr.find("option[name=Sa]").is(':selected'),
        timeranges: []
    };

    var $tr2 = $form.find(".table#tbl-range tr#timerange");
    $.each($tr2, function (idx, elem) {

        endtime = $(this).find("input[name=endtime]").val();
        sensor = $(this).find("select[name=sensor]").val();
        subaddress = $(this).find("select[name=subaddress]").val();
        temperature = $(this).find("input[name=temperature]").val();
        name = $(this).find("input[name=timerangename]").val();

        program.timeranges.push({
            "endtime": endtime,
            "sensorid": sensor,
            "subaddress": subaddress,
            "temperature": temperature,
            "name": name
        });

    });
    saveProgram(program, $form, function (jsondata, form) {

            var json = $.getJSON(programservletpath + '?id=' + jsondata.id, function (elem) {

                $program = createProgramPanel(elem)

                form.parent().replaceWith($program);
                //form.parent().after($program);

            })
                .done(function () {
                    console.log("succes");
                })
                .fail(function () {
                    console.log("error1");
                    alert("impossibile salvare programma " + jsondata.id);
                })
                .always(function () {
                    console.log("error2");
                });
        }
    )
}

function getDays(elem) {
    var day = "";
    if (elem.sunday)
        day += "D";
    else
        day += "-";
    if (elem.monday)
        day += "L";
    else
        day += "-";
    if (elem.tuesday)
        day += "M";
    else
        day += "-";
    if (elem.wednesday)
        day += "M";
    else
        day += "-";
    if (elem.thursday)
        day += "G";
    else
        day += " ";
    if (elem.friday)
        day += "V";
    else
        day += "-";
    if (elem.saturday)
        day += "S";
    else
        day += "-";
    return day;
}