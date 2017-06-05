/**
 * Created by giaco on 29/05/2017.
 */

var page;

function load() {

    loadForm("prova4.html", function (/*json*/) {
        addTextInput("id", "id", "Id");
        addTextInput("name", "name", "Nome");

        createSubitem("sensors","sensor", "sensore 1");
        addSubitemTextInput("sensors", "id", "ssid", "Id");
        addSubitemTextInput("sensors", "name", "ssname", "Nome");
        addSubitemInputButton("sensors");

        createSubitem("sensors","sensor2", "sensore 2");
        addSubitemTextInput("sensors", "id", "ssid","Id");
        addSubitemTextInput("sensors", "name", "ssname2", "Nome");
        addSubitemInputButton("sensors");
    });
}

function loadForm(htmlpage, func) {

    $("#result").load(htmlpage, function () {
        page = htmlpage;
        $("input[type='submit']").click(function () {

            var jso = $("form").toJSO();
            var txt = JSON.stringify( jso )
            var ser = $("form").serialize();
            var txt = JSON.stringify( ser )
            return false;
        });
        func(/*txt*/);
    });

}

function addTextInput(key, val, legend) {
    $("#result").find("label").after( "<strong>"+legend+"</strong><input name="+key+" value="+val+" placeholder="+key+"/>" );
}

function createSubitem(key, val, legend)  {
    $("#result").find('input[name="submit"]').before( "<fieldset name="+key+"><legend>"+legend+"</legend></fieldset>" );
}

function addSubitemTextInput(key, subkey, val, legend)  {

    prova = $("#result").find('fieldset[name="' + key  + '"]');
    prova[prova.length-1].innerHTML += " <strong>"+legend+"</strong><input name="+subkey+" value="+val+" placeholder="+key+"/>"
}

function addSubitemInputButton(key)  {

    prova = $("#result").find('fieldset[name="' + key  + '"]');
    button = "<input type='button' value='add' >";
    prova[prova.length-1].innerHTML += button;
    //button.click(function () {


   //});
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
        if (name != 'submit') {
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

var func = function(obj){
    console.log( JSON.stringify( obj ) );
};
