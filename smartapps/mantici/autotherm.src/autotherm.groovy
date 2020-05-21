/**
 *  Humidity
 *
 *  Copyright 2020michael antici
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
 *  http://scripts.3dgo.net/smartthings/icons/  (use this for icons)
 */
definition(
    name: "AutoTherm",
    namespace: "mantici",
    author: "Michael Antici",
    description: "Turn off thermostat when windows or doors are open",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather2-icn@3x.png",
    pausable: true
)


preferences {
	section("Doors and Windows to monitor:") {
		input "doorsAndWindows", "capability.contactSensor", required: true, multiple:true
	}
	section("Turn off this thermostat when doors or windows are open") {
        input "allThermostats", "capability.thermostat", title:"Thermostats:", required: true, multiple:true

		input "delay", "number", title: "After This many Minutes:",required:true
	}

    section( "Notifications" ) {
 	input "sendPush", "bool", title: "Send a Push Notification"
        input "phoneNumber", "phone", title: "Send a Text Message to:", required: false
    }    
    section("App Details:") {}
}

def installed() {
	state.targetState = 1
	state.currentState = 1
    subscribe(doorsAndWindows, "contact.open", temperatureHandler)
    subscribe(doorsAndWindows, "contact.closed", doorCheck)


}

def updated() {
	unsubscribe()
    log.trace "reverse currently set to ${reverse}"
    subscribe(doorsAndWindows, "contact.open", contactOpened)
    subscribe(doorsAndWindows, "contact.closed", contactClosed)
}

def contactOpened(evt) {
	if (isOpen() == true && state.targetState != 0)
    {
    	state.targetState = 0;
	    runIn(delay*60,setState)
    }
}

def contactClosed(evt) {
	if (isOpen() == false && state.targetState != 1)
    {
    	state.targetState = 1;
	    runIn(5,setState)
    }
    
}

def isOpen()
{
	def count = 0;
    doorsAndWindows.each {dw ->
         if (dw.currentValue("contact").contains("open") == true)
         {
         	count = count + 1
         }
    } 
    
    return (count > 0)
}



private setState(){

	if (state.targetState == 1 && state.currentState != 1) {
    	allThermostats.each {thermostat ->
        	thermostat.auto()    
        } 
        sendMessage("Thermostats turned on, all doors and windows closed")
    }
    
    if (state.targetState == 0  && state.currentState != 0) {
    	allThermostats.each {thermostat ->
        	thermostat.off()    
        } 
        sendMessage("Thermostats turned off, window or door open")
    }
    state.currentState = state.targetState

}


private sendMessage(msg) {
    if (sendPush == true ) {
        sendPush( msg )
    }

    if ( phoneNumber ) {
        sendSms( phoneNumber, msg )
    }

}