/**
 *  AutoArm
 *
 *  Copyright 2019 michael antici
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "AutoArm",
    namespace: "mantici",
    author: "michael antici",
    description: "Arms system when all presence sensors leave",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home2-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home2-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home2-icn@3x.png")


preferences {
	section("Devices:") {
		input "panel", "capability.securitySystem", title:"Security Panel:", required: true
		input "presenceSensors", "capability.presenceSensor",title: "Monitor the presense of:", required: true, multiple:true
	}

    section( "Notifications" ) {
 		input "sendPushNotification", "bool", title: "Send a Push Notification"
        input "phoneNumber", "phone", title: "Send a Text Message to:", required: false
    }    
    section("App Details:") {
    		input "active", "bool", title: "Active"
	}
}

def installed() {
	//state.status = 0
	initialize()
}

def updated() {
	unsubscribe()
    initialize()
}

def initialize()
{
    subscribe(presenceSensors, "presence", presenceSensorHandler)
    //log.trace "someone is home: ${isSomeoneHome()}"
    //log.trace "securityPanel: ${panel.currentSecuritySystemStatus}"
}


def presenceSensorHandler(evt) {

	if (active == false){return}
    
	if (panel.currentSecuritySystemStatus == "disarmed")
    {
        if (isSomeoneHome() == false)
        {
        	panel.armAway(armedAway)
        	sendMessage("System Auto-Armed")
        }
    }
}

private boolean isSomeoneHome()
{
	def someoneHome = false;
    presenceSensors.each {sensor ->
        if (sensor.currentValue("presence") == "present"){
        	someoneHome = true
        }
    } 
    return someoneHome
}

private sendMessage(msg) {
    if (sendPushNotification == true ) {
        sendPush( msg )
    }

    if ( phoneNumber ) {
        sendSms( phoneNumber, msg )
    }

}