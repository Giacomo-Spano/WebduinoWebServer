/**
 * Created by giaco on 29/05/2017.
 */

var page;
var elemStyleMinWidth = " style='min-width:300px'";

function load() {

    loadForm("prova4.html", function (/*json*/) {
        addTextInput("id", "id", "Id");
        addTextInput("name", "name", "Nome");

        id = createSubitem(0, "calendar", "calendar");
        //addSubitemTextInput(id, "id", "ssid", "Id");

        id = createSubitem(0, "sensors", "sensore 1");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname", "Nome");

        id = createSubitem(0, "sensors", "sensore 2");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname2", "Nome");

        addTextInput("idxx", "id", "Idxx");
        addTextInput("namexx", "name", "Nomexx");

        id = createSubitem(0, "scenario", "sensore 1");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname", "Nome");

        id = createSubitem(0, "scenario", "sensore 2");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname2", "Nome");

        addTextInput("idyy", "id", "Idyy");
        addTextInput("nameyy", "name", "Nomeyy");

        addSeparator(" ");

    }, function (json) {

    });
}

function loadForm(htmlpage, func, callback) {

    $("#result").load(htmlpage, function () {
        page = htmlpage;
        $("input[type='button']").click(function () {
            var jso = $("form").toJSO();
            var txt = JSON.stringify(jso)
            callback(jso, this.name);
            return false;
        });
        /*$("input[type='button']").click(function () {
         callback(null,this.name);
         return false;
         });*/
        func();
    });
}

function addTextInput(key, val, legend, maxlength) {
    $("#result").find('div[id="bookmark"]').before(textInput(key, val, legend, maxlength));
}

function addCheckBoxInput(key, val, legend, callback) {
    elem = $("#result").find('div[id="bookmark"]').before(checkBoxInput(key, val, legend));

    elem.prev().find('input').change(function () {
        if (callback != null)
            callback($(this).is(':checked'));
    });
}

function addDateInput(key, date, legend) {
    $("#result").find('div[id="bookmark"]').before(dateInput(key, date, legend));
}

function addTimeInput(key, time, legend) {
    $("#result").find('div[id="bookmark"]').before(timeInput(key, time, legend));
}

function addNumInput(key, val, legend, min, max, step, disabled) {
    $("#result").find('div[id="bookmark"]').before(numInput(key, val, legend, min, max, step, disabled));
}

function addSubitemTextInput(id, subkey, val, legend, maxlength) {
    var legendElement = $("#result").find('#' + id).find('legend');
    //key = legendElement.attr("name");
    var element = $("#result").find('#' + id).find('input[type="hidden"]');
    element.first().before(textInput(subkey, val, legend, maxlength));
}

function addSubitemSelect(id, subkey, list, legend, selected) {
    var element = $("#result").find('#' + id).find('input[type="hidden"]');
    element.first().before(selectInput(subkey, list, legend, selected));
}


function addSubitemNumInput(id, subkey, val, legend, min, max, step, disabled) {
    var legendElement = $("#result").find('#' + id).find('legend');
    key = legendElement.attr("name");
    //var element = $("#result").find('#' + id).find('input[type="button"]');
    var element = $("#result").find('#' + id).find('input[type="hidden"]');
    element.first().before(numInput(subkey, val, legend, min, max, step, disabled));
}

function addSubitemCheckInput(id, subkey, val, legend, callback) {
    var legendElement = $("#result").find('#' + id).find('legend');
    key = legendElement.attr("name");
    var element = $("#result").find('#' + id).find('input[type="hidden"]');
    elem = element.first().before(checkBoxInput(subkey, val, legend));
    elem.prev().find('input').change(function () {
        if (callback != null)
            callback($(this).is(':checked'));
    });
}

function addSubitemDateInput(id, subkey, date, legend) {
    var legendElement = $("#result").find('#' + id).find('legend');
    key = legendElement.attr("name");
    var element = $("#result").find('#' + id).find('input[type="hidden"]');
    element.first().before(dateInput(subkey, date, legend));
}

function addSubitemTimeInput(id, subkey, time, legend) {
    var legendElement = $("#result").find('#' + id).find('legend');
    key = legendElement.attr("name");
    var element = $("#result").find('#' + id).find('input[type="hidden"]');
    element.first().before(timeInput(subkey, time, legend));
}


function textInput(key, val, legend, maxlength=50) {
    var dec = val.toString().replace(/\s/g, '&nbsp;');
    //return "<div><label " + elemStyleMinWidth + "><strong>" + legend + "</strong></label><input  type='text' name=" + key + " value=" + dec + " maxlength='" + maxlength + "' placeholder=" + key + "/></div>";
    return "<label " + elemStyleMinWidth + "><strong>" + legend + "</strong></label><input  type='text' name=" + key + " value=" + dec + " maxlength='" + maxlength + "' placeholder=" + key + "/><br>";
}

function selectInput(key, list, legend, selected) {
    var selectList = "<label " + elemStyleMinWidth + "><strong>" + legend + "</strong></label><select name='" + key + "'>";
    for (var i = 0; i < list.length; i++) {
        if (selected == list[i][0]) {
            selectList += "<option selected value='" + list[i][0] + "'>" + list[i][1] + "</option>";
        } else {
            selectList += "<option value='" + list[i][0] + "'>" + list[i][1] + "</option>";
        }
    }
    selectList += "</select><br>";
    return selectList
}

function inputLegend(legend) {
    return "<strong>" + legend + "</strong>";
}

function numInput(key, val, legend, min=0, max=99, step="1", disabled=false) {
    disabledkey = "";
    if (disabled)
        disabledkey = " disabled";
    return "<label " + elemStyleMinWidth + "><strong>" + legend + "</strong></label><input type='number' name=" + key + " value=" + val + " min='" + min + "' max='" + max + "' step='" + step + "' placeholder=" + key + disabledkey + "/><br>";
}

function checkBoxInput(key, val, legend) {
    return "<label " + elemStyleMinWidth + "><strong>" + legend + "</strong></label><input type='checkbox' name=" + key + " value=" + val + " placeholder=" + key + "/><br>";
}

function dateInput(key, date, legend) {

    if (date == null)
        date = new Date();
    strdate = date.toString("yyyy-MM-ddTHH:mm");
    return "<label " + elemStyleMinWidth + "><strong>" + legend + "</strong></label><input type='datetime-local' name=" + key + " value=" + strdate + " placeholder=" + key + "/><br>";
}

function timeInput(key, time, legend) {
    if (time == null)
        time = "00:00";
    return "<label " + elemStyleMinWidth + "><strong>" + legend + "</strong></label><input type='time' name=" + key + " value=" + time + " placeholder=" + key + "/><br>";
}


function addSeparator(name) {
    $("#result").find('div[id="bookmark"]').before("<hr name='" + name + "'>");
}

function addTag(tag) {
    $("#result").find('div[id="bookmark"]').before(tag);
}

function addNewLine() {
    $("#result").find('div[id="bookmark"]').before("<BR>");
}

function addSection(name) {
    $("#result").find('div[id="bookmark"]').before("<h>" + name + "</h><BR>");
}


function createItem(name, legend) {
    var uuid = getUniqueId();
    var fieldset = "<fieldset name=" + name + " id='" + uuid + "'><legend>" + legend + "</legend></fieldset>";
    $("#result").find('div[id="bookmark"]').before(fieldset);
    addSubitemSeparator(uuid);
    return uuid;
}

function createSubitem(id, key, legend) {

    var hr = $("#result").find("hr[name='" + key + "']");
    if (hr.length == 0) {

        var elem;

        if (id == 0) {
            var elem = $("#result").find('div[id="bookmark"]');
            elem.before("<hr><strong>" + legend + "</strong><hr name='" + key + "'>");
        } else {
            var elem = $("#result").find('#' + id);
            elem[0].innerHTML += "<hr><strong>" + legend + "</strong><hr name='" + key + "'>";
        }

        hr = $("#result").find("hr[name='" + key + "']");
    }

    var uuid = getUniqueId();
    hr.before("<legend>" + legend + "</legend><fieldset name=" + key + " id='" + uuid + "'></fieldset>");

    addSubitemSeparator(uuid);
    addSubitemInsertButton(uuid);
    addSubitemDeleteButton(uuid);
    addSubitemMoveUpButton(uuid);
    addSubitemMoveDownButton(uuid);
    return uuid;
}

function addSubitemSeparator(id) {

    var fieldset = $("#result").find('#' + id);

    btn = $('<input />', {
        type: 'hidden',
        value: ' ',// + id,
        name: 'separator'
    });
    fieldset.append(btn);
}

function addSubitemInsertButton(id) {

    var fieldset = $("#result").find('#' + id);

    btn = $('<input />', {
        type: 'button',
        value: 'add ',// + id,
        name: 'add',
        on: {
            click: function () {

                key = fieldset.attr("name");
                legend = fieldset.find('legend')[0].innerHTML;
                id = createSubitem(0, key, legend);

                fields = fieldset.find('input,select,strong');
                for (i = 0; i < fields.length - 1; i++) {
                    input = fields[i];
                    if (input.tagName == 'INPUT') {
                        if (input.type == 'text') {
                            addSubitemTextInput(id, input.name, input.value, fields[i - 1].innerHTML);
                        } else if (input.type == 'number') {
                            addSubitemNumInput(id, input.name, input.value, fields[i - 1].innerHTML, input.getAttribute("min"), input.getAttribute("max"), input.getAttribute("step"));
                        } else if (input.type == 'time') {
                            addSubitemTimeInput(id, input.name, input.value, fields[i - 1].innerHTML);
                        } else if (input.type == 'checkbox') {
                            addSubitemCheckInput(id, input.name, input.value, fields[i - 1].innerHTML);
                        }
                    } else if (input.tagName == 'SELECT') {
                        addSubitemSelect(id, input.name, input.value, fields[i - 1].innerHTML);
                    }

                }
            }
        }
    });
    fieldset.append(btn);
}

function addSubitemDeleteButton(id) {

    var fieldset = $("#result").find('#' + id);

    btn = $('<input />', {
        type: 'button',
        value: 'delete ',// + id,
        id: 'delete ' + id,
        name: 'delete',
        on: {
            click: function () {
                fieldset.remove();
                //alert ( this.value );
            }
        }
    });
    fieldset.append(btn);
}

function addSubitemMoveDownButton(id) {

    var fieldset = $("#result").find('#' + id);

    btn = $('<input />', {
        type: 'button',
        value: 'move down ',// + id,
        name: 'down',
        on: {
            click: function () {

                next = fieldset.next();
                if (!next.is("hr")) {
                    fieldset.insertAfter(next);
                }
            }
        }
    });
    fieldset.append(btn);
}

function addSubitemMoveUpButton(id) {

    var fieldset = $("#result").find('#' + id);

    btn = $('<input />', {
        type: 'button',
        value: 'move up ',// + id,
        name: 'up',
        on: {
            click: function () {

                prev = fieldset.prev();
                if (!prev.is("hr")) {
                    fieldset.insertBefore(prev);
                }
            }
        }
    });
    fieldset.append(btn);
}

$.fn.toJSO = function () {
    var obj = {};
    var $kids = $(this).children('[name]');
    if (!$kids.length) {

        if ($(this).attr('type') == 'checkbox') {
            return ($(this).prop('checked'))
        } else if ($(this).attr('type') == 'number') {

            if ($(this).attr('step') != '1')
                return parseFloat($(this).val());
            else
                return parseInt($(this).val());
        } else if ($(this).attr('type') == 'datetime-local') {
            ret = Date.parseExact($(this).val(), 'yyyy-MM-ddTHH:mm');
            return ret.toString("dd-MM-yyyy HH:mm");
        } else if ($(this).attr('type') == 'time') {
            return $(this).val();
        }

        var str = $(this).val();
        //var dec = decodeURI(str);
        //sostituisci lo spazio codificato con html ion spazio normalke
        var dec = str.replace(/\s/g, ' ');
        //str = decodeURI(str);;
        return dec;
    }
    $kids.each(function () {
        var $el = $(this),
            name = $el.attr('name');
        if (name != 'save' && name != 'add' && name != 'delete' && name != 'delete' && name != 'up' && name != 'down' && name != 'separator' && !$el.is('hr')) {
            if ($el.siblings("[name=" + name + "]").length) {
                if (!/radio|checkbox/i.test($el.attr('type')) || $el.prop('checked')) {
                    obj[name] = obj[name] || [];
                    obj[name].push($el.toJSO());
                }
            } else {
                obj[name] = $el.toJSO();
            }
        }
    });
    return obj;
};

var func = function (obj) {
    console.log(JSON.stringify(obj));
};

var getUniqueId = function (prefix) {
    var d = new Date().getTime();
    d += (parseInt(Math.random() * 100)).toString();
    if (undefined === prefix) {
        prefix = 'uid-';
    }
    d = prefix + d;
    return d;
};
