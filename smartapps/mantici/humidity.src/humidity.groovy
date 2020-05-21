/**
 *  Humidity
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
    name: "Humidity",
    namespace: "mantici",
    author: "Michael Antici",
    description: "control a switch based on humidity",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Weather/weather12-icn@3x.png",
    pausable: true
)


preferences {
	section("Monitor the humidity of:") {
		input "humiditySensor1", "capability.relativeHumidityMeasurement"
	}
	section("Humidity Percent Levels") {
		input "humidityHigh", "number", title: "When the humidity rises above:"
		input "humidityLow", "number", title: "When the humidity falls below:"
	}
	section("Device Control") {
		input "switches", "capability.switch",title:"Control These Devices", required: false, multiple:true
		input "reverse", "bool", title: "Reverse on/off function"
        paragraph "Turns on switch when humidity falls below limit, off when humidity rises above limit"
	}
    section( "Notifications" ) {
 		input "sendPush", "bool", title: "Send a Push Notification"
        input "phoneNumber", "phone", title: "Send a Text Message to:", required: false
    }    
    section("App Details:") {}
}

def installed() {
	state.status = 0
	subscribe(humiditySensor1, "humidity", humidityHandler)
}

def updated() {
	unsubscribe()
    log.trace "reverse currently set to ${reverse}"
	subscribe(humiditySensor1, "humidity", humidityHandler)
}

def humidityHandler(evt) {
	def humidity = Double.parseDouble(evt.value.replace("%", ""))

	if (humidity >= humidityHigh && state.status != 1) {
    	state.status = 1
		sendMessage("${humiditySensor1.label} humidity now ${evt.value} - over ${tooHumid}")
		setDevices(!reverse)
	}

    if (humidity <= humidityLow && state.status != 2) {
    	state.status = 2
		sendMessage("${humiditySensor1.label} humidity now ${evt.value} - below ${tooHumid}")
		setDevices(reverse)
	}
}

private setDevices(stOn){
    for (device in switches) {
        if (stOn == true)
        {
            device.on()
        }
        else
        {
            device.off()
        }
    }
}


private sendMessage(msg) {
    if (sendPush == true ) {
        sendPush( msg )
    }

    if ( phoneNumber ) {
        sendSms( phoneNumber, msg )
    }

}