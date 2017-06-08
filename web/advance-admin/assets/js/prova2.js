/**
 * Created by giaco on 29/05/2017.
 */

var page;

function load() {

    loadForm("prova4.html", function (/*json*/) {
        addTextInput("id", "id", "Id");
        addTextInput("name", "name", "Nome");

        id = createSubitem("sensors", "sensore 1");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname", "Nome");

        id = createSubitem("sensors", "sensore 2");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname2", "Nome");

        addTextInput("idxx", "id", "Idxx");
        addTextInput("namexx", "name", "Nomexx");

        id = createSubitem("scenario", "sensore 1");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname", "Nome");

        id = createSubitem("scenario", "sensore 2");
        addSubitemTextInput(id, "id", "ssid", "Id");
        addSubitemTextInput(id, "name", "ssname2", "Nome");

        addTextInput("idyy", "id", "Idyy");
        addTextInput("nameyy", "name", "Nomeyy");

        addSeparator(" ");



    });
}

function loadForm(htmlpage, func) {

    $("#result").load(htmlpage, function () {
        page = htmlpage;
        $("input[type='submit']").click(function () {

            var jso = $("form").toJSO();
            var txt = JSON.stringify(jso)
            var ser = $("form").serialize();
            var txt = JSON.stringify(ser)
            return false;
        });

        $("input[type='button']").click(function () {


            return false;
        });

        func(/*txt*/);
    });

}

function addTextInput(key, val, legend) {
    $("#result").find('input[name="submit"]').before("<strong>" + legend + "</strong><input name=" + key + " value=" + val + " placeholder=" + key + "/>");
}

function addSeparator(name) {
    $("#result").find('input[name="submit"]').before("<hr name='" + name + "'>");
}

function createSubitem(key, legend) {

    var hr = $("#result").find("hr[name='" + key + "']");
    if (hr.length == 0) {
        $("#result").find('input[name="submit"]').before("<hr><strong>" + key + "</strong>");

        $("#result").find('input[name="submit"]').before("<hr name='" + key + "'>");
        hr = $("#result").find("hr[name='" + key + "']");
    }

    var uuid = getUniqueId();
    hr.before("<fieldset name=" + key + " id='" + uuid + "'><legend>" + legend /*+ " " + uuid */+ "</legend></fieldset>");
    addSubitemInsertButton(uuid);
    addSubitemDeleteButton(uuid);
    addSubitemMoveUpButton(uuid);
    addSubitemMoveDownButton(uuid);
    return uuid;
}

function addSubitemTextInput(id, subkey, val, legend) {

    var legendElement = $("#result").find('#' + id).find('legend');
    key = legendElement.attr("name");
    var element = $("#result").find('#' + id).find('input[type="button"]');


    element.first().before("<strong>" + legend + "</strong><input name=" + subkey + " value=" + val + " legend='" + legend + "'" + " placeholder=" + key + "/>");
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
                id = createSubitem(key, legend);

                fields = fieldset.find('input');
                for (i=0; i < fields.length-1; i++) {
                    input = fields[i];
                    if (input.type == 'text') {
                        //input.value= ' ';
                        addSubitemTextInput(id, input.name, input.value, input.getAttribute("legend"));
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
    var obj = {},
        $kids = $(this).children('[name]');
    if (!$kids.length) {
        return $(this).val();
    }
    $kids.each(function () {
        var $el = $(this),
            name = $el.attr('name');
        if (name != 'submit' && name != 'add' && name != 'delete' && name != 'delete' && name != 'up' && name != 'down') {
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
