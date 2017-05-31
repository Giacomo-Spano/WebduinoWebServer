/**
 * Created by giaco on 29/05/2017.
 */

var page;

function load() {

    loadForm("prova4.html", function (/*json*/) {
        //$("#result").find("label").after( "<strong>id: </strong><input name='id' value='1' placeholder='id'/>" );
        //var fieldset = $("#result").find("label").after( "<fieldset name='sensors'><legend>sensor</legend><input name='id' value='1' placeholder='id'/></fieldset>" );
        addTextInput("id", "id");
        addTextInput("name", "name");
        var fieldset = createSubitem("sensors","sensor");
        addSubitemTextInput(fieldset, "sid", "ssid");
        addSubitemTextInput(fieldset, "sname", "ssname");

        var fieldset = createSubitem("sensors","sensor2");
        addSubitemTextInput(fieldset, "sid", "ssid2");
        addSubitemTextInput(fieldset, "sname", "ssname2");
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

function addTextInput(key, val) {
    $("#result").find("label").after( "<strong>id: </strong><input name="+key+" value="+val+" placeholder="+key+"/>" );
}

function createSubitem(key, val)  {
    var fieldset = $("#result").find("label").after( "<fieldset name="+key+"><legend>"+val"+</legend><input name="+subkey+" value="+val+" placeholder="+subkey+"/></fieldset>" );
    return fieldset;
}

function addSubitemTextInput(fieldset, subkey, val)  {
    fieldset.add( "<input name="+subkey+" value="+val+" placeholder="+subkey+"/>" );
}

/*function addSubitemTextInput(key, subkey, val)  {
    var fieldset = $("#result").find("label").after( "<fieldset name="+key+"><legend>sensor</legend><input name="+subkey+" value="+val+" placeholder="+subkey+"/></fieldset>" );
}*/

$.fn.toJSO = function () {
    var obj = {},
        $kids = $(this).children('[name]');
    if (!$kids.length) {
        return $(this).val();
    }
    $kids.each(function () {
        var $el = $(this),
            name = $el.attr('name');
        if ($el.siblings("[name=" + name + "]").length) {
            if (!/radio|checkbox/i.test($el.attr('type')) || $el.prop('checked')) {
                obj[name] = obj[name] || [];
                obj[name].push($el.toJSO());
            }
        } else {
            obj[name] = $el.toJSO();
        }
    });
    return obj;
};

var func = function(obj){
    console.log( JSON.stringify( obj ) );
};
