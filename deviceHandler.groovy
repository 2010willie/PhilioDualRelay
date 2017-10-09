/**
 *  Philio PAN06 / TKB TZ06 Dual Relay in-wall module
 *
 *	Author: Steven Tomlinson (TmpR)
 *	Email: steven@nemiah.uk
 *	Date: 09/07/2017a
 *
 *	Based on the Fibaro FGS-222 Double Relay Device Type Handler by Chris Charles and Robin Winbourne
 *  
 *  Once you have installed the device handler click the cog in the top right corner of the device 
 *  screen on your phone and save the settings, this will create the 2 child devices in SmartThings.
 */

 
metadata {
definition (name: "Philio Dual Relay", namespace: "nemiah", author: "Steven Tomlinson") {
capability "Switch"
capability "Relay Switch"
capability "Polling"
capability "Configuration"
capability "Refresh"
capability "Zw Multichannel"

attribute "switch2", "string"
attribute "switch3", "string"

command "childOn"
command "childOff"
command "on2"
command "off2"
command "on3"
command "off3"

fingerprint deviceId: "0x1001", inClusters:"0x86, 0x72, 0x85, 0x60, 0x8E, 0x25, 0x20, 0x70, 0x27"

}

simulator {
status "on": "command: 2003, payload: FF"
status "off": "command: 2003, payload: 00"

// reply messages
reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
reply "200100,delay 100,2502": "command: 2503, payload: 00"
}

tiles(scale: 2){

    multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
				attributeState "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
                attributeState "turningOn", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
				attributeState "turningOff", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
			}
	}
	standardTile("switch2", "device.switch2",canChangeIcon: false, width: 2, height: 2) {
		state "on", label: 'S1', action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
		state "off", label: 'S1', action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
        state "turningOn", label: 'S1', action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
		state "turningOff", label: 'S1', action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
        
    }
	standardTile("switch3", "device.switch3",canChangeIcon: false, width: 2, height: 2) {
		state "on", label: 'S2', action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		state "off", label: 'S2', action: "on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        state "turningOn", label: 'S2', action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
		state "turningOff", label: 'S2', action: "on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
        
    }
    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }

    

    main(["switch","switch2", "switch3"])
    details(["switch","switch2","switch3","refresh"])
}
   
   
   preferences {
        def paragraph = "Device Settings"
        
       
        input name: "param2", type: "number", range: "1..3", defaultValue: "1", required: false,
            title: "Change the physical button mode (e.g. Toggle/Momentary switch).\n" +
                   "1 - Edge mode,\n" +
                   "2 - Pulse mode,\n" +
                   "3 - Edge-Toggle mode\n" +
                   "Default value: 1."
       
       
    }
}

def parse(String description) {
    def result = []
    def cmd = zwave.parse(description)
    if (cmd) {
        result += zwaveEvent(cmd)
        log.debug "Parsed ${cmd} to ${result.inspect()}"
    } else {
        log.debug "Non-parsed event: ${description}"
    }
    return result
}


def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    response(delayBetween(result, 1000)) // returns the result of reponse()
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
    sendEvent(name: "switch", value: cmd.value ? "on" : "off", type: "digital")
    def result = []
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    result << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    response(delayBetween(result, 1000)) // returns the result of reponse()
}


def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCapabilityReport cmd) 
{
    log.debug "multichannelv3.MultiChannelCapabilityReport $cmd"
    if (cmd.endPoint == 2 ) {
        def currstate = device.currentState("switch2").getValue()
        if (currstate == "on")
        	sendEvent(name: "switch2", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        	sendEvent(name: "switch2", value: "on", isStateChange: true, display: false)
    }
    else if (cmd.endPoint == 3 ) {
        def currstate = device.currentState("switch3").getValue()
        if (currstate == "on")
        sendEvent(name: "switch3", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        sendEvent(name: "switch3", value: "on", isStateChange: true, display: false)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   def map = [ name: "switch$cmd.sourceEndPoint" ]
   def curswitch, curstate
   
   switch(cmd.commandClass) {
      case 32:
         if (cmd.parameter == [0]) {
            map.value = "off"
         }
         if (cmd.parameter == [255]) {
            map.value = "on"
         }
         createEvent(map)
         break
      case 37:
         if (cmd.parameter == [0]) {
            map.value = "off"
         }
         if (cmd.parameter == [255]) {
            map.value = "on"
         }
         curstate = map.value
         curswitch = cmd.sourceEndPoint
         break
    }
    
    //Now if there is a child device then send it a state update
    try {
        def childDevice = getChildDevices()?.find { it.deviceNetworkId == "$device.deviceNetworkId-sw${curswitch}"}
           if (childDevice)
              childDevice.sendEvent(name: "switch", value: curstate)
    } catch (e) {
        log.error "Couldn't find child device, probably doesn't exist and hence no problem: ${e}"
    }
    
    
    
    def events = [createEvent(map)]
    if (map.value == "on") {
            events += [createEvent([name: "switch", value: "on"])]
    } else {
         def allOff = true
         (2..3).each { n ->
             if (n != cmd.sourceEndPoint) {
                 if (device.currentState("switch${n}").value != "off") allOff = false
             }
         }
         if (allOff) {
             events += [createEvent([name: "switch", value: "off"])]
         }
    }
    events
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
}

def zwaveEvent(physicalgraph.zwave.commands.switchallv1.SwitchAllReport cmd) {
   log.debug "SwitchAllReport $cmd"
}

def refresh() {
	def cmds = []
    cmds << zwave.manufacturerSpecificV2.manufacturerSpecificGet().format()
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
	delayBetween(cmds, 1000)
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	log.debug "msr: $msr"
    updateDataValue("MSR", msr)
}

def poll() {
	def cmds = []
	cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    cmds << zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
	delayBetween(cmds, 1000)
}

def configure() {
	log.debug "Executing 'configure'"
    delayBetween([
          zwave.configurationV1.configurationSet(parameterNumber:2, configurationValue:[param2.value]).format(),
    ])
}

/**
* Triggered when Done button is pushed on Preference Pane
*/
def updated()
{
	log.debug "Preferences have been changed. Attempting configure() and update"
    def cmds = configure()

    if (!childDevices) {
		createChildDevices()
	}
    response(cmds)
}

def childRefresh(String dni) {
    log.debug "childRefresh($dni)"
    refresh()
}
def childOn(String dni) {
    log.debug "childOn($dni)"
    def switchnum = "on${channelNumber(dni)}"
    "$switchnum"()
}
def childOff(String dni) {
    log.debug "childOff($dni)"
    def switchnum = "off${channelNumber(dni)}"
    "$switchnum"()
}

def on() { 
   delayBetween([
        zwave.switchAllV1.switchAllOn().format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    ], 1000)
}
def off() {
   delayBetween([
        zwave.switchAllV1.switchAllOff().format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    ], 1000)
}

def on2() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:1, parameter:[255]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 1000)
}

def off2() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:1, parameter:[0]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:2, commandClass:37, command:2).format()
    ], 1000)
}

def on3() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:3, destinationEndPoint:3, commandClass:37, command:1, parameter:[255]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:3, destinationEndPoint:3, commandClass:37, command:2).format()
    ], 1000)
}

def off3() {
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:3, destinationEndPoint:3, commandClass:37, command:1, parameter:[0]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:3, destinationEndPoint:3, commandClass:37, command:2).format()
    ], 1000)
}

private channelNumber(String dni) {
	dni.split("-sw")[-1] as Integer
}

private void createChildDevices() {
	state.oldLabel = device.label
     try {
        for (i in 2..3) {
        
	       addChildDevice("erocm123", "Switch Child Device", "${device.deviceNetworkId}-sw${i}", device.hub.id,
		      [completedSetup: true, name: "${device.displayName} (S${i-1})", isComponent: false])
        }
    } catch (e) {
	    runIn(2, "sendAlert")
    }
}

private sendAlert() {
   sendEvent(
      descriptionText: "Child device creation failed. Please make sure that the \"Metering Switch Child Device\" is installed and published.",
	  eventType: "ALERT",
	  name: "childDeviceCreation",
	  value: "failed",
	  displayed: true,
   )
}
