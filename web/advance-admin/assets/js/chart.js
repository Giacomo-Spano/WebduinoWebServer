/**
 * Created by giaco on 05/01/2016.
 */
var datalogServletPath = "../datalog?id=1";
//var chartDiv;
var chartDiv;
var dataTable;
var materialChart;
var materialOptions;

function load() {

    $(this).find('button#last5minutes').click(onLast5Minutes);
    $(this).find('button#last30minutes').click(onLast30Minutes);
    $(this).find('button#lasthour').click(onLastHour);
    $(this).find('button#last6hours').click(onLast6Hours);
    $(this).find('button#last24hours').click(onLast24Hours);
    $(this).find('button#last3days').click(onLast3Days);
    $(this).find('button#last5days').click(onLast5Days);

    chartDiv = document.getElementById('chart_div');
    loadChart();
}

function loadChart() {
    google.charts.load('current', {'packages': ['line', 'corechart']});
    google.charts.setOnLoadCallback(function () {
        drawChart(chartDiv, getDate(), 60);
    });
}

function onLast5Minutes() {
    var date = new Date();
    materialChart.draw(dataTable, materialOptions);
    loadData(chartDiv, getDate(), 5);
}
function onLast30Minutes() {
    var date = new Date();
    materialChart.draw(dataTable, materialOptions);
    loadData(chartDiv, getDate(), 30);
}

function onLastHour() {
    var date = new Date();
    materialChart.draw(dataTable, materialOptions);
    loadData(chartDiv, getDate(), 60);
}

function onLast6Hours() {
    var date = new Date();
    materialChart.draw(dataTable, materialOptions);
    loadData(chartDiv, getDate(), 6*60);
}
function onLast24Hours() {
    var date = new Date();
    materialChart.draw(dataTable, materialOptions);
    loadData(chartDiv, getDate(), 24 * 60);
}
function onLast3Days() {
    var date = new Date();
    materialChart.draw(dataTable, materialOptions);
    loadData(chartDiv, getDate(), 3 * 24 * 60);
}
function onLast5Days() {
    var date = new Date();
    materialChart.draw(dataTable, materialOptions);
    loadData(chartDiv, getDate(), 5 * 24 * 60);
}

function getDate() {
    var d = new Date();
    dateStr = ("0" + d.getDate()).slice(-2) + "-" + ("0"+(d.getMonth()+1)).slice(-2) + "-" +
        d.getFullYear() + " " + ("0" + d.getHours()).slice(-2) + ":" + ("0" + d.getMinutes()).slice(-2)+ ":" + ("0" + d.getSeconds()).slice(-2);

    return dateStr;
}

function loadData(chartDiv, endDate, elapsed) {
    path = datalogServletPath + "&enddate=" + endDate + "&elapsed=" + elapsed;

    var dataLog = $.getJSON(path, function (data) {

        console.log("success");

        //div = $(this).find('div#chartstatus');
        //div = $(this).find('div[name="chartstatus"]');
        div = $("[name=chartstatus]");
        div.text("loading chart.... " + endDate + " " + elapsed/60);

        numberOfRows = dataTable.getNumberOfRows();
        if (numberOfRows > 0)
            dataTable.removeRows(0,numberOfRows);

        dataTable.addRows(data.length);

        $.each(data, function (idx, elem) {

            dataTable.setCell(idx, 0, new Date(elem.date));
            dataTable.setCell(idx, 1, elem.remote);
            dataTable.setCell(idx, 2, elem.local);
            dataTable.setCell(idx, 3, elem.target);
            if (elem.relestatus)
                dataTable.setCell(idx, 4, 'acceso');
            else
                dataTable.setCell(idx, 4, 'spento');

        });

        materialChart = new google.charts.Line(chartDiv);
        materialChart.draw(dataTable, materialOptions);

        div.text("chart loaded" + endDate + " " + elapsed/60);

    }).done(function () {
        console.log("succes");
    }).fail(function () {
        console.log("error1");
        alert("error1");
    }).always(function () {
        console.log("error2");

    });
}
function drawChart(chartDiv, endDate, elapsed) {

    dataTable = new google.visualization.DataTable();
    dataTable.addColumn('datetime', 'Month');
    dataTable.addColumn('number', "Temperatura sensore remoto");
    dataTable.addColumn('number', "Temperatura sensore locale");
    dataTable.addColumn('number', "Temperatura target");
    dataTable.addColumn('string', "Rele");

    materialOptions = {
        chart: {
            title: 'Grafico temperatura riscaldamento'
        },
        width: 1000,
        height: 500,
        series: {
            // Gives each series an axis name that matches the Y-axis below.
            0: {axis: 'Temps'},
            1: {axis: 'Temps'},
            2: {axis: 'Temps'},
            3: {axis: 'Rele'}
        },
        axes: {
            // Adds labels to each axis; they don't have to match the axis names.
            y: {
                Temps: {label: 'Temps (Celsius)'},
                Temps: {label: 'Temps (Celsius2)'},
                Temps: {label: 'Temps (Celsius3)'},
                Rele: {label: 'Rele status'}
            }
        }
    };

    __materialOptions = {
        title: 'Grafico temperatura riscaldamento classic',
        width: 1000,
        height: 500,
        // Gives each series an axis that matches the vAxes number below.
        series: {
            0: {targetAxisIndex: 0},
            1: {targetAxisIndex: 1},
            2: {targetAxisIndex: 2},
            3: {targetAxisIndex: 3}
        },
        vAxes: {
            // Adds titles to each axis.
            0: {title: 'Temps (Celsius)'},
            1: {title: 'Temps (Celsius)'},
            2: {title: 'Temps (Celsius)'},
            3: {title: 'rele status'}
        },
        /*hAxis: {
            ticks: [new Date(2014, 0), new Date(2014, 1), new Date(2014, 2), new Date(2014, 3),
                new Date(2014, 4),  new Date(2014, 5), new Date(2014, 6), new Date(2014, 7),
                new Date(2014, 8), new Date(2014, 9), new Date(2014, 10), new Date(2014, 11)
            ]
        },*/
        vAxis: {
            viewWindow: {
                max: 30
            }
        }
    };
    loadData(chartDiv, endDate, elapsed);
}


