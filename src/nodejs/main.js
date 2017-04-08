var express = require('express');
var app = express();
var fs = require("fs");


var user = {
   "user4" : {
      "name" : "mohit",
      "password" : "password4",
      "profession" : "teacher",
      "id": 4
   }
}

app.get('/addUser', function (req, res) {
   // First read existing users.
   fs.readFile( __dirname + "/" + "users.json", 'utf8', function (err, data) {
       data = JSON.parse( data );
       data["user4"] = user["user4"];
       console.log( data );
       res.end( JSON.stringify(data));
   });
})

app.get('/listUsers', function (req, res) {
   fs.readFile( __dirname + "/" + "users.json", 'utf8', function (err, data) {
       console.log( data );
       res.end( data );
   });
})



var server = app.listen(8081, function () {

  var host = server.address().address
  var port = server.address().port

  console.log("Example app listening at http://%s:%s", host, port)
  
  console.log("set timer")
  setInterval(function(){
    console.log('test');

      var id = 1;
      temp = random(17, 23);//21.21;
      avtemp = 24.23;
      sendSensorStatus(id, avtemp, temp);

      var id = 1;
      avtemp = random(17, 23);//21.21;
      sendActuatorStatus(id, avtemp, temp, "program", true);
    
    
  }, 30000); // update every 30 seconds      

})

//------------------------

var request = require('request');

function sendSensorStatus(id, temp, avtemp){
    // Make a request to the server
    console.log('sendstatus');
    
    
    
    var myJSONObject = {
                        "id":id,
                        "avtemperature":avtemp,
                        "temperature":temp
                        };
    
    request({
        url: 'http://localhost:8080/webduino/sensor',
        method: "POST",
        json: true,   // <--Very important!!!
        body: myJSONObject
    }, function (error, response, body){
        console.log(body);
    });

}

function sendActuatorStatus(id, temp, avtemp, status, relestatus){
    // Make a request to the server
    console.log('sendActuatorstatus');

    var myJSONObject = {
        "command" : "status",
        "id" : id,
        "avtemperature" : avtemp,
        "status" : '"' + status + '"',
        "relestatus" : relestatus

    };

    request({
        url: 'http://localhost:8080/webduino/actuator',
        method: "POST",
        json: true,   // <--Very important!!!
        body: myJSONObject
    }, function (error, response, body){
        console.log(body);
    });

}

app.post('/rele', function(req, res) {

    console.log('post rele');      // your JSON
    console.log(req.body);      // your JSON
    res.send('result');    // echo the result back

})

/*app.post('/temp', function(req, res) {

    console.log('post temp');      // your JSON
    console.log(JSON.stringify(req.body));    // your JSON
    console.log('req.body.name', req.body['name']);
    res.send('result post temp');    // echo the result back

})*/

//app.use(express.bodyParser());
app.route('/temp', function(req, res) {
    var text = req.body; // I expect text to be a string but it is a JSON
});

app.post('/somepath', function(req, res) {

    console.log(JSON.stringify(req.body));

    console.log('req.body.name', req.body['name']);
});

app.get('/update', function (req, res) {   
      //sendStatus("xx"); 
})

function random (low, high) {
    return Math.random() * (high - low) + low;
}