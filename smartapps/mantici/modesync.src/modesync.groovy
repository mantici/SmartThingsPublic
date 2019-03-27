/**
 *  ModeSync
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
    name: "ModeSync",
    namespace: "mantici",
    author: "michael antici",
    description: "Syncs things when mode changes, automatically arms when sensors leave",
    category: "Safety & Security",
    iconUrl: "http://cdn.device-icons.smartthings.com/Home/home1-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Home/home1-icn@3x.png")


preferences {


    page(name: "mainSettings", title: "Manage", nextPage: "presenceSettings", uninstall: true) {
        section("Devices") {
			input "panel", "capability.securitySystem", title:"Security Panel:", required: false
			input "allThermostats", "capability.thermostat", title:"Thermostats:", required: false, multiple:true
			input "allLocks", "capability.lock", title:"Locks:", required: false, multiple:true
        }
        section("App") {
      		label title: "Assign a name", required: false
            //mode title: "Set for specific mode(s)", required: false
        }
    }
    
     page(name: "presenceSettings", title: "Auto set by presence", nextPage: "homeSettings", uninstall: false) {
        section("Devices") {
	   		input "presenceSensors", "capability.presenceSensor",title: "When these sensors all leave:", required: false, multiple:true
            input "autoArm", "bool", title: "Set panel to Arm:Away \n(if disarmed)"
            input "autoAway", "bool", title: "Set mode to Away"
            input "presenceNotification", "bool", title: "Send a Push Notification"
            input "presencePhoneNumber", "phone", title: "Send a Text Message Notification", required: false
            //input "presenseSet", "enum", title: "Set Security Panel to:",options: ["Armed: Away", "Armed: Stay", "Disarmed"], required: false
            //input "presenceMode", "enum", title: "Set mode to:",options: ["Home", "Away", "Night"], required: false

		}
	}

    page(name: "homeSettings")
    page(name: "awaySettings")
    page(name: "nightSettings")

    

}
def awaySettings()
{
    dynamicPage(name: "awaySettings", title: "When mode changes to Away", nextPage: "nightSettings", uninstall: false) {
        section {
            input "sendPushAway", "bool", title: "Send a Push Notification"
            input "panelSetAway", "enum", title: "Set Security Panel to:",options: ["Armed: Away", "Armed: Stay", "Disarmed"], required: false
            input "locksAway", "enum", title: "Locks:",options: ["Unlock", "Lock Immediately", "Lock after 1 Minute", "Lock after 2 Minutes","Lock after 3 Minutes", "Lock after 5 Minutes","Lock after 10 Minutes"], required: false
        }
        section ("Thermostat") {
            input "tstatResumeAway", "bool", title: "Resume Program",submitOnChange: true
        }
        if (tstatResumeAway != true)
        {
            section ("Thermostat Temperatures") {
                input "heatAway", "number", title: "Heat Setpoint:", required: false
                input "coolAway", "number", title: "Cool Setpoint:", required: false
            }
        }        
    }
}


def nightSettings()
{
	dynamicPage(name: "nightSettings", title: "When mode changes to Night", install: true, uninstall: false) {
        section {
            input "sendPushNight", "bool", title: "Send a Push Notification"
            input "panelSetNight", "enum", title: "Set Security Panel to:",options: ["Armed: Away", "Armed: Stay", "Disarmed"], required: false
            input "locksNight", "enum", title: "Locks:",options: ["Unlock", "Lock Immediately", "Lock after 1 Minute", "Lock after 2 Minutes","Lock after 3 Minutes", "Lock after 5 Minutes","Lock after 10 Minutes"], required: false
        }
        section ("Thermostat") {
            input "tstatResumeNight", "bool", title: "Resume Program",submitOnChange: true
        }
        if (tstatResumeNight != true)
        {
            section ("Thermostat Temperatures") {
                input "heatNight", "number", title: "Heat Setpoint:", required: false
                input "coolNight", "number", title: "Cool Setpoint:", required: false
            }
        }
    }
}




def homeSettings()
{
    dynamicPage(name: "homeSettings", title: "When mode changes to Home:", nextPage: "awaySettings", uninstall: false) {
        section {
            input "sendPushHome", "bool", title: "Send a Push Notification"
            input "panelSetHome", "enum", title: "Set Security Panel to:",options: ["Armed: Away", "Armed: Stay", "Disarmed"], required: false
            input "locksHome", "enum", title: "Locks:",options: ["Unlock", "Lock Immediately", "Lock after 1 Minute", "Lock after 2 Minutes","Lock after 3 Minutes", "Lock after 5 Minutes","Lock after 10 Minutes"], required: false
       }
        section ("Thermostat") {
            input "tstatResumeHome", "bool", title: "Resume Program",submitOnChange: true
        }

            if (tstatResumeHome != true)
            {
            	section ("Thermostat Temperatures") {
                input "heatHome", "number", title: "Heat Setpoint:", required: false
                input "coolHome", "number", title: "Cool Setpoint:", required: false
            }
        }
	}
}

def installed() {
	initialize()
}

def updated() {

	unsubscribe()
	initialize()
}

def initialize() {
	//log.debug "initialized with mode ${panel.currentSecuritySystemStatus}"
        
    subscribe(location, "mode", modeChangeHandler)
    subscribe(presenceSensors, "presence", presenceSensorHandler)

}
def presenceSensorHandler(evt) {
  
  	if (autoArm == true)
    {
        if (panel.currentSecuritySystemStatus == "disarmed")
        {
            if (isSomeoneHome() == false)
            {
                panel.armAway(armedAway)
                if (presenceNotification == true ) {
                    sendPush( "System Auto-Armed" )
                }
                if ( presencePhoneNumber ) {
                    sendSms( presencePhoneNumber, "System Auto-Armed" )
                }
            }
        }
    }
    if (autoAway == true)
    {
    	location.setMode("Away")
    }
    
}


def modeChangeHandler(evt)
{
	//return
		log.debug "mode Change ${evt}: ${location.mode}"
		switch (location.mode.toLowerCase())
        {
            case "home":
            	handleChange(sendPushHome, panelSetHome,locksHome, tstatResumeHome,heatHome,coolHome)
                break;
            case "away":
            	handleChange(sendPushAway, panelSetAway,locksAway, tstatResumeAway,heatAway,coolAway)
                break;
            case "night":
            	handleChange(sendPushNight, panelSetNight,locksNight, tstatResumeNight,heatNight,coolNight)
                break;
        }
}

def handleChange(sendPushNotification, panelSet,locks, tstatResume,heat,cool)
{
	if (sendPushNotification == true)
    {
    	sendPush("Mode changed to ${location.mode}")
    }
    
    switch (panelSet)
    {
    	case "Armed: Away":
        	if (panel.currentSecuritySystemStatus != "armedAway"){panel.armAway(armedAway)}
         	break;
    	case "Armed: Stay":
        	if (panel.currentSecuritySystemStatus != "armedStay"){panel.armStay(armedStay)}
        	break;
    	case "Disarmed":
        	if (panel.currentSecuritySystemStatus != "disarmed"){panel.disarm()}
        	break;
    }
    
    if (tstatResume == true)
    {
    	allThermostats.each {thermostat ->
        	thermostat.resumeProgram()    
    	} 
    }
    else
    {
        allThermostats.each {thermostat ->
            if (heat > 0){thermostat.setHeatingSetpoint(heat)}
            if (cool > 0){thermostat.setCoolingSetpoint(cool)}
        } 
    }
    
    switch (locks)
    {
    	case "Unlock":
        	unlockAll()
        	break;
    	case "Lock Immediately":
        	lockAll()
        	break;
    	case "Lock after 1 Minute":
        	runIn(60*1, lockAll)
        	break;
    	case "Lock after 2 Minutes":
        	runIn(60*2, lockAll)
        	break;
    	case "Lock after 3 Minutes":
        	runIn(60*3, lockAll)
        	break;
    	case "Lock after 5 Minutes":
        	runIn(60*5, lockAll)
        	break;
    	case "Lock after 10 Minutes":
        	runIn(60*10, lockAll)
        	break;
    }
    
}

def lockAll()
{
    allLocks.each {lock ->
        lock.lock()    
    } 
}

def unlockAll()
{
    allLocks.each {lock ->
        lock.unlock()    
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