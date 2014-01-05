
// Declaring global variables.
var ws;
var name, id;

function onOpenWebSocket(){
	console.log("web socket connection opened.");
	displayMessage("connected");
}

function onCloseWebSocket(){
	console.log("web socket connection closed.");
	displayMessage("disconnected");
	$.mobile.navigate("#entrance");
}

function onMessageWebSocket(event){
	console.log("web socket message received.");
	var message = event.data;
	if (message == "") { return; }
	console.log(event);
	var cmd = message.substring(0, 5);
	var parameters = message.substring(6).split(",");
	console.log(parameters);

	// get registration result
	if (cmd == "REGIS") {
		id = parseInt(parameters[0], 10);
		name = parameters.slice(1, parameters.length).join();
		$("#entrance_old_id").val(id);
		$("#lounge_id").text(id);
		$("#lounge_name").text(name);
		$.mobile.loading( "hide");
		$.mobile.navigate("#lounge");
	}

	// send
	else if (cmd == "GIFTX") {
		var give_name = parameters[0];
		$("#gift_receiver_name").text(give_name);
		$.mobile.navigate("#send");
	}

	// receive
	else if (cmd == "GIFTY") {
		var give_name = parameters[0];
		$("#gift_sender_name").text(give_name);
		$.mobile.navigate("#receive");
	}

	displayMessage("> " + message);
}

function displayMessage(message){
	var messageDiv = $("<div></div>");
	messageDiv.text(message);
	$("#messages").append(messageDiv);
}

function register(){
	var name = $("#entrance_name").val();
	if (name == "") { return; }
	$.mobile.loading( "show");
	ws.send("REGIS " + name);
	// Wait for the REGIS response.
}

function login(){
	var oldId = $("#entrance_old_id").val();
	if (oldId == "") { return; }
	$.mobile.loading( "show");
	ws.send("LOGIN " + oldId);
	// Wait for the REGIS response.
}

$(function() {
	// check protocol
	var protocol = location.protocol == "https:" ?"wss" : "ws";
	// get host + port number
	var host = location.host;
	// generate url
	var url = protocol + "://" + host + "/ws/";
	// create WebSocket instance
	ws = new WebSocket(url);

	// register WebSocket event listeners
	ws.addEventListener("open", onOpenWebSocket, false);
	ws.addEventListener("close", onCloseWebSocket, false);
	ws.addEventListener("message", onMessageWebSocket, false);

	// disconnect when the window is unloaded
	window.addEventListener("unload", function() {
		ws.close();
	}, false);

	$("#entrance_register").click(register);
	$("#entrance_login").click(login);

	console.log("page initialized");
});
