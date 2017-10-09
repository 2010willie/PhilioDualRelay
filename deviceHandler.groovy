/**
 *  Philio PAN06 / TKB TZ06 Dual Relay in-wall module
 *
 *	Author: Steven Tomlinson (TmpR)
 *	email: steven@nemiah.uk
 *	Date: 09/07/2017a
 *
 *	Based on the Generic Dual Relay Device Type by Eric Maycock (erocm123)
 *  
 *  To use this device as 2 seperate devices in SmartThings you need to install a smart app to create 2 virtual devices here: 
 *  https://community.smartthings.com/t/release-virtual-device-sync-create-virtual-devices-and-keep-them-in-sync-with-a-physical-device/57089
 *
 *  I will be updating this device handler to include config settings for the relay module and a better UI when I get more time!
 */
 
metadata {
definition (name: "Philio Dual Relay", namespace: "nemiah", author: "Steven Tomlinson") {
capability "Switch"
capability "Polling"
capability "Refresh"
capability "Zw Multichannel"

attribute "switch2", "string"
attribute "switch3", "string"


command "on2"
command "off2"
command "on3"
command "off3"
command "reset"

//fingerprint deviceId: "0x1001", inClusters:"0x5E, 0x86, 0x72, 0x5A, 0x85, 0x59, 0x73, 0x25, 0x20, 0x27, 0x71, 0x2B, 0x2C, 0x75, 0x7A, 0x60, 0x32, 0x70"
}

simulator {
status "on": "command: 2003, payload: FF"
status "off": "command: 2003, payload: 00"

// reply messages
reply "2001FF,delay 100,2502": "command: 2503, payload: FF"
reply "200100,delay 100,2502": "command: 2503, payload: 00"
}

tiles {


        
	standardTile("switch2", "device.switch2",canChangeIcon: false) {
		state "on", label: '${name}', action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
		state "off", label: '${name}', action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
        state "turningOn", label: '${name}', action: "off2", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
		state "turningOff", label: '${name}', action: "on2", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
        
    }
	
	standardTile("switch3", "device.switch3",canChangeIcon: false) {
		state "on", label: '${name}', action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821"
		state "off", label: '${name}', action: "on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
        state "turningOn", label: '${name}', action: "off3", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState:"turningOff"
		state "turningOff", label: '${name}', action: "on3", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState:"turningOn"
        
    }
    standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
		state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
    }



    main(["switch2", "switch3"])
    details(["switch2","switch3","refresh"])
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

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd)
{
    /*def result
    if (cmd.value == 0) {
        result = createEvent(name: "switch", value: "off")
    } else {
        result = createEvent(name: "switch", value: "on")
    }
    return result*/
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
    if (cmd.endPoint == 3 ) {
        def currstate = device.currentState("switch2").getValue()
        if (currstate == "on")
        	sendEvent(name: "switch3", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        	sendEvent(name: "switch3", value: "on", isStateChange: true, display: false)
    }
    else if (cmd.endPoint == 2 ) {
        def currstate = device.currentState("switch1").getValue()
        if (currstate == "on")
        sendEvent(name: "switch2", value: "off", isStateChange: true, display: false)
        else if (currstate == "off")
        sendEvent(name: "switch2", value: "on", isStateChange: true, display: false)
    }
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
   def map = [ name: "switch$cmd.sourceEndPoint" ]
    
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
         createEvent(map)
         break
    }
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
    // This will capture any commands not handled by other instances of zwaveEvent
    // and is recommended for development so you can see every command the device sends
    return createEvent(descriptionText: "${device.displayName}: ${cmd}")
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
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:1, parameter:[255]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    ], 1000)
}

def off3() {
log.debug "CH3 off"
    
    delayBetween([
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:1, parameter:[0]).format(),
        zwave.multiChannelV3.multiChannelCmdEncap(sourceEndPoint:1, destinationEndPoint:3, commandClass:37, command:2).format()
    ], 1000)
}
