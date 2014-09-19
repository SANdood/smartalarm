/**
 *  Smart Alarm.
 *
 *  Smart Alarm turns SmartThings into a multi-zone home security system with
 *  up to 16 independent security zones. You can assign any number of contact,
 *  motion, moisture and smoke sensors to each zone. Please visit
 *  <https://github.com/statusbits/smartalarm/> for more information.
 *
 *  --------------------------------------------------------------------------
 *
 *  Copyright (c) 2014 Statusbits.com
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option)
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  --------------------------------------------------------------------------
 *
 *  The latest version of this file can be found on GitHub at:
 *  https://github.com/statusbits/smartalarm/
 *
 *  Version 1.2.0 (2014-09-18)
 */

import groovy.json.JsonSlurper

definition(
    name: "Smart Alarm",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Turn SmartThings into a smart, multi-zone home security system.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Cat-SafetyAndSecurity/App-IsItSafe.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-IsItSafe@2x.png",
    oauth: [displayName:"Smart Alarm", displayLink:"https://github.com/statusbits/smartalarm/"]
)

mappings {
    path("/armaway") {
        action: [ GET: "restArmAway" ]
    }

    path("/armstay") {
        action: [ GET: "restArmStay" ]
    }

    path("/disarm") {
        action: [ GET: "restDisarm" ]
    }

    path("/panic") {
        action: [ GET: "restPanic" ]
    }

    path("/status") {
        action: [ GET: "restStatus" ]
    }
}

preferences {
    page name:"setupInit"
    page name:"pageSetupMenu"
    page name:"pageAbout"
    page name:"pageAlarmSettings"
    page name:"pageZoneSettings"
    page name:"pageZoneBypass"
    page name:"pagePanelStatus"
    page name:"pageButtonRemote"
}

def setupInit() {
    TRACE("setupInit()")

    if (state.installed) {
        return pageSetupMenu()
    }

    // the app is not installed yet
    state.zones = []
    return pageAbout()
}

// Show 'Setup Menu' page
def pageSetupMenu() {
    TRACE("pageSetupMenu()")

    def pageProperties = [
        name:       "pageSetupMenu",
        title:      "Control Panel",
        install:    true,
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            buttons name:"buttonRow1", required:false,
                buttons:[
                    [label:"Arm Away", action:"armAway"],
                    [label:"Arm Stay", action:"armStay"],
                ]
            buttons name:"buttonRow2", required:false,
                buttons:[
                    [label:"Disarm", action:"disarm"],
                    [label:"Panic", action:"panic", backgroundColor:"red"]
                ]
        }
        section("Setup Menu") {
            href "pagePanelStatus", title:"Alarm Status", description:"Tap to open"
            href "pageAlarmSettings", title:"Alarm Settings", description:"Tap to open"
            href "pageZoneSettings", title:"Zone Settings", description:"Tap to open"
            href "pageZoneBypass", title:"Quick Zone Bypass", description:"Tap to open"
            href "pageButtonRemote", title:"Configure Remote Control", description:"Tap to open"
            href "pageAbout", title:"About Smart Alarm", description:"Tap to open"
        }
        section([title:"Options", mobileOnly:true]) {
            label title:"Assign a name", required:false
            mode title:"Enable only for specific mode(s)", required:false
        }
    }
}

// Show 'About' page
def pageAbout() {
    TRACE("pageAbout()")

    def textAbout =
        "Smart Alarm turns SmartThings into a multi-zone home security " +
        "system with up to 16 independent security zones. Any number of " +
        "sensors can be assigned to each zone."

    def pageProperties = [
        name:       "pageAbout",
        title:      "About",
        nextPage:   state.installed ? "pageSetupMenu" : "pageAlarmSettings",
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph textAbout
            paragraph "Smart Alarm ${textVersion()}\n${textCopyright()}"
        }
        section("License") {
            paragraph textLicense()
        }
    }
}

// Show panel configuration page
def pageAlarmSettings() {
    TRACE("pageAlarmSettings()")

    def helpNumZones =
        "A security zone is an area of your home protected by one or more " +
        "sensors, for example a single room or an entire floor in a " +
        "multistory building. You can configure up to 16 security zones."

    def helpArming =
        "Smart Alarm can be armed and disarmed by simply setting the home " +
        "'Mode'. There are two arming options - Stay and Away. Interior " +
        "zones are not armed in Stay mode, allowing you to freely move " +
        "inside your home."

    def helpExitDelay =
        "Exit delay allows you to exit the premises without setting off an " +
        "alarm within specified time after the alarm has been armed."

    def helpEntryDelay =
        "Entry delay allows you to enter the premises when Smart Alarm is " +
        "armed and disarm it within specified time without setting of an " +
        "alarm."

    def helpAlarm =
        "When an alarm is set off, Smart Alarm can execute a 'Hello, " +
        "Home' action and/or turn on sirens and light switches."

    def helpSilent =
        "Enable Silent mode if you wish to temporarily disable sirens and " +
        "switches. You will still receive push notifications and/or text " +
        "messages, if configured."

    def helpNotify =
        "Smart Alarm can notify you via push messages and/or text messages " +
        "whenever it is armed, disarmed or when an alarm is set off."

    def inputNumZones = [
        name:           "numZones",
        type:           "enum",
        title:          "How many zones?",
        metadata:       [values:["4","8","12","16"]],
        defaultValue:   "4",
        required:       true
    ]

    def inputAwayModes = [
        name:           "awayModes",
        type:           "mode",
        title:          "Arm Away in these Modes",
        multiple:       true,
        required:       false
    ]

    def inputStayModes = [
        name:           "stayModes",
        type:           "mode",
        title:          "Arm Stay in these Modes",
        multiple:       true,
        required:       false
    ]

    def inputDisarmModes = [
        name:           "disarmModes",
        type:           "mode",
        title:          "Disarm in these Modes",
        multiple:       true,
        required:       false
    ]

    def inputExitDelay = [
        name:           "exitDelay",
        type:           "enum",
        metadata:       [values:["0","15","30","45","60"]],
        title:          "Exit delay (in seconds)",
        defaultValue:   "30",
        required:       true
    ]

    def inputEntryDelay = [
        name:           "entryDelay",
        type:           "enum",
        metadata:       [values:["0","15","30","45","60"]],
        title:          "Entry delay (in seconds)",
        defaultValue:   "30",
        required:       true
    ]

    def hhActions = getHHActions()
    def inputHelloHome = [
        name:           "helloHomeAction",
        type:           "enum",
        title:          "Execute this Hello Home action",
        metadata:       [values: hhActions],
        required:       false
    ]

    def inputAlarms = [
        name:           "alarms",
        type:           "capability.alarm",
        title:          "Activate these alarms",
        multiple:       true,
        required:       false
    ]

    def inputSwitches = [
        name:           "switches",
        type:           "capability.switch",
        title:          "Turn on these switches",
        multiple:       true,
        required:       false
    ]

    def inputSilent = [
        name:           "silent",
        type:           "bool",
        title:          "Enable silent mode",
        defaultValue:   false
    ]

    def inputPushMessage = [
        name:           "pushMessage",
        type:           "bool",
        title:          "Send push notifications",
        defaultValue:   true
    ]

    def inputPhone1 = [
        name:           "phone1",
        type:           "phone",
        title:          "Primary phone number",
        required:       false
    ]

    def inputPhone2 = [
        name:           "phone2",
        type:           "phone",
        title:          "Secondary phone number",
        required:       false
    ]

    def pageProperties = [
        name:       "pageAlarmSettings",
        title:      "Configure Smart Alarm",
        nextPage:   state.zones.size() ? "pageSetupMenu" : "pageZoneSettings",
        uninstall:  state.installed
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph helpNumZones
            input inputNumZones
        }
        section("Arming Options") {
            paragraph helpArming
            input inputAwayModes
            input inputStayModes
            input inputDisarmModes
            paragraph helpExitDelay
            input inputExitDelay
            paragraph helpEntryDelay
            input inputEntryDelay
        }
        section("Alarm Options") {
            paragraph helpAlarm
            input inputHelloHome
            input inputAlarms
            input inputSwitches
            paragraph helpSilent
            input inputSilent
        }
        section("Notification Options") {
            paragraph helpNotify
            input inputPushMessage
            input inputPhone1
            input inputPhone2
        }
    }
}

// Show zone configuration page
def pageZoneSettings() {
    TRACE("pageZoneSettings()")

    def numZones = settings.numZones.toInteger()
    assert numZones > 0

    def helpPage =
        "Each zone can be designated as an Exterior, Interior or an Alert " +
        "zone. Exterior zones are armed when the alarm panel is armed in " +
        "either Away or Stay mode, Interior zones are armed only in Away " +
        "mode and Alert zones are always armed and are typically used for " +
        "fire and flood alarms.\n\n" +
        "You can assign any number of sensors to each zone. When a sensor " +
        "is activated, a zone will set off an alarm if it's armed.\n\n" +
        "You can also assign one or more security cameras to each zone. " +
        "The cameras will take a snapshot whenever a zone is breached.\n\n" +
        "A zone can be temporarily disabled by turning on zone bypass."

    def pageProperties = [
        name:       "pageZoneSettings",
        title:      "Configure Zones",
        nextPage:   "pageSetupMenu",
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph helpPage
        }
        for (int n = 1; n <= numZones; n++) {
            section("Zone ${n}", hideable:true) {
                input "z${n}_name", "string", title:"Give this zone a descriptive name", defaultValue:"Zone ${n}"
                input "z${n}_type", "enum", title:"Select zone type", metadata:[values:["Exterior","Interior","Alert"]], defaultValue:"Exterior"
                input "z${n}_contact", "capability.contactSensor", title:"Which contact sensors?", multiple:true, required:false
                input "z${n}_motion", "capability.motionSensor", title:"Which motion sensors?", multiple:true, required:false
                input "z${n}_smoke", "capability.smokeDetector", title:"Which smoke sensors?", multiple:true, required:false
                input "z${n}_water", "capability.waterSensor", title:"Which moisture sensors?", multiple:true, required:false
                input "z${n}_camera", "capability.imageCapture", title:"Which cameras?", multiple:true, required:false
                input "z${n}_bypass", "bool", title:"Enable zone bypass", defaultValue:false
            }
        }
    }
}

// Show panel status page
def pagePanelStatus() {
    TRACE("pagePanelStatus()")

    def pageProperties = [
        name:       "pagePanelStatus",
        title:      "Alarm Panel Status",
        nextPage:   "pageSetupMenu",
        uninstall:  false
    ]

    def statusArmed
    if (state.armed) {
        statusArmed = "Armed "
        statusArmed += state.stay ? "Stay" : "Away"
    } else {
        statusArmed = "Disarmed"
    }
    def statusExitDelay = settings.exitDelay ? settings.exitDelay.toInteger() : 0
    def statusEntryDelay = settings.entryDelay ? settings.exitDelay.toInteger() : 0
    def statusSilent = settings.silent ? "On" : "Off"
    def statusPushMsg = settings.pushMessage ? "On" : "Off"

    return dynamicPage(pageProperties) {
        section {
            paragraph "Alarm is now ${statusArmed}"
            paragraph "Exit delay: ${statusExitDelay}"
            paragraph "Entry delay: ${statusEntryDelay}"
            paragraph "Silent mode: ${statusSilent}"
            paragraph "Push messages: ${statusPushMsg}"
        }
        section("Zone Status") {
            for (zone in state.zones) {
                def zoneStatus = "${zone.name}: "
                if (zone.alert) {
                    zoneStatus += "alert"
                } else if (zone.interior) {
                    zoneStatus += "interior"
                } else {
                    zoneStatus += "exterior"
                }

                if (zone.bypass) {
                    zoneStatus += ", bypassed"
                } else if (zone.armed) {
                    zoneStatus += ", armed"
                } else {
                    zoneStatus += ", disarmed"
                }

                paragraph zoneStatus
            }
        }
    }
}

// Show zone bypass page
def pageZoneBypass() {
    TRACE("pageZoneBypass()")

    def pageProperties = [
        name:       "pageZoneBypass",
        title:      "Quick Zone Bypass",
        install:    true,
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section {
            for (int n = 0; n < state.numZones; n++) {
                def zone = state.zones[n]
                input "z${n + 1}_bypass", "bool", title:"${zone.name}", defaultValue:false
            }
        }
    }
}

// Show "Configure Button Remote" page
def pageButtonRemote() {
    TRACE("pageButtonRemote()")

    def textHelp =
        "You can use remote controls such as Aeon Labs Minimote to arm " +
        "and disarm Smart Alarm."

    def inputButtons = [
        name:       "buttons",
        type:       "capability.button",
        title:      "Which remote controls?",
        multiple:   true,
        required:   false
    ]

    def inputArmAway = [
        name:           "buttonArmAway",
        type:           "enum",
        title:          "Which button to Arm Away?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "1",
        required:       false
    ]

    def inputArmStay = [
        name:           "buttonArmStay",
        type:           "enum",
        title:          "Which button to Arm Stay?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "2",
        required:       false
    ]

    def inputDisarm = [
        name:           "buttonDisarm",
        type:           "enum",
        title:          "Which button to Disarm?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "3",
        required:       false
    ]

    def inputPanic = [
        name:           "buttonPanic",
        type:           "enum",
        title:          "Which button to Panic?",
        metadata:       [values:["1","2","3","4"]],
        defaultValue:   "4",
        required:       false
    ]

    def pageProperties = [
        name:       "pageButtonRemote",
        title:      "Configure Remote Control",
        nextPage:   "pageSetupMenu",
        install:    false,
        uninstall:  false
    ]

    return dynamicPage(pageProperties) {
        section {
            paragraph textHelp
            input inputButtons
            input inputArmAway
            input inputArmStay
            input inputDisarm
            input inputPanic
        }
    }
}

def installed() {
    state.installed = true
    initialize()
}

def updated() {
    unschedule()
    unsubscribe()
    initialize()
}

private initialize() {
    TRACE("initialize()")
    log.debug "settings: ${settings}"
    log.trace "${app.name}. ${textVersion()}. ${textCopyright()}"

    state.restEndpoint = "https://graph.api.smartthings.com/api/smartapps/installations/${app.id}"
    getAccessToken()

    state.numZones = settings.numZones.toInteger()
    state.exitDelay = settings.exitDelay ? 45 : 0
    state.entryDelay = settings.entryDelay ? 45 : 0
    state.offSwitches = []
    state.zones = []
    for (int n = 0; n < state.numZones; n++) {
        state.zones[n] = zoneInit(n)
        if (state.zones[n].alert) {
            zoneArm(n)
        }
    }

    // setup button actions
    state.buttonActions = [:]
    if (settings.buttons) {
        if (settings.buttonArmAway) {
            state.buttonActions[settings.buttonArmAway] = "armAway"
        }
        if (settings.buttonArmStay) {
            state.buttonActions[settings.buttonArmStay] = "armStay"
        }
        if (settings.buttonDisarm) {
            state.buttonActions[settings.buttonDisarm] = "disarm"
        }
        if (settings.buttonPanic) {
            state.buttonActions[settings.buttonPanic] = "panic"
        }

        subscribe(settings.buttons, "button.pushed", onButtonPushed)
    }

    if (settings.awayModes?.contains(location.mode)) {
        state.armed = true
        state.stay = false
    } else if (settings.stayModes?.contains(location.mode)) {
        state.armed = true
        state.stay = true
    } else {
        state.armed = false
        state.stay = false
    }
    resetPanel()

    subscribe(location, onLocation)

    STATE()
}

def resetPanel() {
    TRACE("resetPanel()")

    unschedule()
    alarms*.off()

    // only turn back off those switches that we turned on
    if (state.offSwitches) {
        state.offSwitches*.off()
        state.offSwitches = []
    }

    state.alarm = false
    for (int n = 0; n < state.numZones; n++) {
        zoneReset(n)
    }

    panelStatus()
}

private def panelStatus() {
    TRACE("panelStatus()")

    def msg = "${app.label} "
    if (state.armed) {
        def mode = state.stay ? "Stay" : "Away"
        msg += "armed '${mode}'."
    } else {
        msg += "disarmed."
    }

    for (zone in state.zones) {
        msg += "\n${zone.name}: "
        if (zone.bypass) {
            msg += "bypass"
        } else if (zone.armed) {
            msg += "armed"
        } else {
            msg += "disarmed"
        }

    }

    // use sendNotificationEvent instead of Push/SMS on panel status change
    //notify(msg)
    sendNotificationEvent(msg)
}

private def zoneInit(n) {
    def z = n + 1
    def handlers = [
        onZone1, onZone2, onZone3, onZone4,
        onZone5, onZone6, onZone7, onZone8,
        onZone9, onZone10, onZone11, onZone12,
        onZone13, onZone14, onZone15, onZone16,
    ]

    def zone = [:]
    zone.name       = settings."z${z}_name"
    zone.alert      = settings."z${z}_type" == "Alert" ? true : false
    zone.interior   = settings."z${z}_type" == "Interior" ? true : false
    zone.bypass     = settings."z${z}_bypass"
    zone.evHandler  = handlers[n]
    zone.armed      = false
    zone.alarm      = null

    return zone
}

private def zoneReset(n) {
    TRACE("zoneReset(${n})")

    def zone = state.zones[n]
    if (!zone.bypass && (zone.alert || (state.armed && !(state.stay && zone.interior)))) {
        if (!zone.armed) {
            zoneArm(n)
        }
    } else {
        if (zone.armed) {
            zoneDisarm(n)
        }
    }
}

private def zoneArm(n) {
    def zone = state.zones[n]
    def devices = getZoneDevices(n)

    if (devices.contact) {
        subscribe(devices.contact, "contact.open", zone.evHandler)
    }

    if (devices.motion) {
        subscribe(devices.motion, "motion.active", zone.evHandler)
    }

    if (devices.smoke) {
        subscribe(devices.smoke, "smoke.detected", zone.evHandler)
        subscribe(devices.smoke, "smoke.tested", zone.evHandler)
        subscribe(devices.smoke, "carbonMonoxide.detected", zone.evHandler)
        subscribe(devices.smoke, "carbonMonoxide.tested", zone.evHandler)
    }

    if (devices.water) {
        subscribe(devices.water, "water.wet", zone.evHandler)
    }

    if (devices.camera) {
        subscribe(devices.camera, "image", onImageCapture)
    }

    state.zones[n].armed = true
    state.zones[n].alarm = null

    log.debug "Zone '${zone.name}' armed"
}

private def zoneDisarm(n) {
    def zone = state.zones[n]
    def devices = getZoneDevices(n)

    if (devices.motion) unsubscribe(devices.motion)
    if (devices.contact) unsubscribe(devices.contact)
    if (devices.water) unsubscribe(devices.water)
    if (devices.smoke) unsubscribe(devices.smoke)
    if (devices.camera) unsubscribe(devices.camera)

    state.zones[n].armed = false
    state.zones[n].alarm = null

    log.debug "Zone '${zone.name}' disarmed"
}

private def getZoneDevices(n) {
    if (n >= state.numZones)
        return null

    n++

    def devices = [:]
    devices.contact = settings."z${n}_contact"
    devices.motion  = settings."z${n}_motion"
    devices.smoke   = settings."z${n}_smoke"
    devices.water   = settings."z${n}_water"
    devices.camera  = settings."z${n}_camera"

    return devices
}

private def onAlarm(n, evt) {
    TRACE("onAlarm(${n}, ${evt.displayName})")

    if (n >= state.numZones) {
        return
    }

    def zone = state.zones[n]
    if (!zone.armed) {
        TRACE("onAlarm: Hmm... False alarm?")
        return
    }

    // Set zone to alarm state
    state.zones[n].alarm = evt.displayName

    // Take security camera snapshot
    def devices = getZoneDevices(n)
    devices.camera*.take()

    // Activate alarm
    if (!state.alarm) {
        state.alarm = true
        if (zone.alert || !state.entryDelay) {
            activateAlarm()
        } else {
            // See Issue #1.
            unschedule()
            myRunIn(state.entryDelay, activateAlarm)
        }
    }
}

// these must be public!
def onZone1(evt)  { onAlarm(0,  evt) }
def onZone2(evt)  { onAlarm(1,  evt) }
def onZone3(evt)  { onAlarm(2,  evt) }
def onZone4(evt)  { onAlarm(3,  evt) }
def onZone5(evt)  { onAlarm(4,  evt) }
def onZone6(evt)  { onAlarm(5,  evt) }
def onZone7(evt)  { onAlarm(6,  evt) }
def onZone8(evt)  { onAlarm(7,  evt) }
def onZone9(evt)  { onAlarm(8,  evt) }
def onZone10(evt) { onAlarm(9,  evt) }
def onZone11(evt) { onAlarm(10, evt) }
def onZone12(evt) { onAlarm(11, evt) }
def onZone13(evt) { onAlarm(12, evt) }
def onZone14(evt) { onAlarm(13, evt) }
def onZone15(evt) { onAlarm(14, evt) }
def onZone16(evt) { onAlarm(15, evt) }

def onLocation(evt) {
    TRACE("onLocation(${evt.displayName})")

    def mode = evt.value
    if (settings.awayModes?.contains(mode)) {
        armAway()
    } else if (settings.stayModes?.contains(mode)) {
        armStay()
    } else if (settings.disarmModes?.contains(mode)) {
        disarm()
    }
}

def onButtonPushed(evt) {
    TRACE("onButtonPushed(${evt.displayName})")

    if (!evt.data) {
        return
    }

    def slurper = new JsonSlurper()
    def data = slurper.parseText(evt.data)
    def button = data.buttonNumber
    if (button) {
        TRACE("Button '${button}' was pushed.")
        def action = state.buttonActions["${button}"]
        log.trace "Executing button action ${action}()"
        "${action}"()
    }
}

def onImageCapture(evt) {
    TRACE("onImageCapture(${evt.displayName})")
    log.trace("onImageCapture not implemented!")
}

def armAway() {
    TRACE("armAway()")

    if (state.armed && !state.stay) {
        return
    }

    state.armed = true
    state.stay = false
    if (state.exitDelay) {
        unschedule()
        myRunIn(state.exitDelay, resetPanel)
        log.trace "Smart Alarm scheduled delayed 'Away' mode"
    } else {
        resetPanel()
        log.trace "Smart Alarm armed in 'Away' mode"
    }
}

def armStay() {
    TRACE("armStay()")

    if (state.armed && state.stay) {
        return
    }

    state.armed = true
    state.stay = true
    if (state.exitDelay) {
        unschedule()
        myRunIn(state.exitDelay, resetPanel)
        log.trace "Smart Alarm scheduled delayed 'Stay' mode"
    } else {
        resetPanel()
        log.trace "Smart Alarm armed in 'Stay' mode"
    }
}

def disarm() {
    TRACE("disarm()")

    if (state.armed) {
        state.armed = false
        resetPanel()
        log.trace "Smart Alarm disarmed"
    }
}

def panic() {
    TRACE("panic()")

    state.alarm = true;
    activateAlarm()
}

// .../armaway REST endpoint
def restArmAway() {
    TRACE("restArmAway()")

    armAway()
    return restStatus()
}

// .../armstay REST endpoint
def restArmStay() {
    TRACE("restArmStay()")

    armStay()
    return restStatus()
}

// .../disarm REST endpoint
def restDisarm() {
    TRACE("restDisarm()")

    disarm()
    return restStatus()
}

// .../panic REST endpoint
def restPanic() {
    TRACE("restPanic()")

    panic()
    return restStatus()
}

// .../status REST endpoint
def restStatus() {
    TRACE("restStatus()")

    def status = [:]
    status.status = state.armed ? (state.stay ? "armed stay" : "armed away") : "disarmed"
    status.alarm = state.alarm

    return status
}

def activateAlarm() {
    if (!state.alarm) {
        TRACE("activateAlarm: Hmm... False alarm?")
        return
    }

    // Execute Hello Home action
    if (settings.helloHomeAction) {
        log.trace "Executing HelloHome action \'${settings.helloHomeAction}\'"
        location.helloHome.execute(settings.helloHomeAction)
    }

    // Activate alarms and switches
    if (!settings.silent) {
        alarms*.both()

        // Only turn on those switches that are currently off
        state.offSwitches = switches.findAll { it?.currentSwitch == "off" }
        if (state.offSwitches) {
            state.offSwitches*.on()
        }
    }

    // Send notifications
    def msg = "Alarm at location '${location.name}'!"
    for (zone in state.zones) {
        if (zone.alarm) {
            msg += "\n${zone.name}: ${zone.alarm}"
        }
    }
    notify(msg)

    // Reset panel in 3 minutes
    // See Issue #1.
    unschedule()
    myRunIn(180, resetPanel)
}

private def notify(msg) {
    log.trace "[notify] ${msg}"

    if (settings.pushMessage) {
        sendPush(msg)
    }

    if (settings.phone1) {
        sendSms(phone1, msg)
    }

    if (settings.phone2) {
        sendSms(phone2, msg)
    }
}

private def getHHActions() {
    def actions = []
    location.helloHome?.getPhrases().each {
        actions << "${it.label}"
    }

    return actions.sort()
}

private def getAccessToken() {
    if (atomicState.accessToken) {
        return atomicState.accessToken
    }

    def token = createAccessToken()
    TRACE("Created new access token: ${token})")

    return token
}

private def myRunIn(delay_s, func) {
    if (delay_s > 0) {
        def tms = now() + (delay_s * 1000)
        def date = new Date(tms)
        runOnce(date, func)
        TRACE("runOnce() scheduled for ${date}")
    }
}

private def textVersion() {
    def text = "Version 1.2.0"
}

private def textCopyright() {
    def text = "Copyright (c) 2014 Statusbits.com"
}

private def textLicense() {
    def text =
        "This program is free software: you can redistribute it and/or " +
        "modify it under the terms of the GNU General Public License as " +
        "published by the Free Software Foundation, either version 3 of " +
        "the License, or (at your option) any later version.\n\n" +
        "This program is distributed in the hope that it will be useful, " +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU " +
        "General Public License for more details.\n\n" +
        "You should have received a copy of the GNU General Public License " +
        "along with this program. If not, see <http://www.gnu.org/licenses/>."

    return text
}

private def TRACE(message) {
    //log.debug message
}

private def STATE() {
    log.trace "settings: ${settings}"
    log.trace "state: ${state}"
}
