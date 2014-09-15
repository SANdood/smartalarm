/**
 *  Smart Alarm.
 *
 *  Smart Alarm is a home security application for the SmartThings home
 *  automation system. You can configure up to 16 security zones and assign
 *  any number of contact, motion, moisture and smoke sensors to each zone.
 *  The alarm is armed and disarmed by simply setting the home 'mode'.
 *  For more information, please visit
 *  <https://github.com/statusbits/smartalarm/>.
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
 *  Version 1.1.2 (2014-09-14)
 */

import groovy.json.JsonSlurper

definition(
    name: "Smart Alarm",
    namespace: "statusbits",
    author: "geko@statusbits.com",
    description: "Turn SmartThings into a smart, multi-zone home security system.",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Cat-SafetyAndSecurity/App-IsItSafe.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/SafetyAndSecurity/App-IsItSafe@2x.png"
)

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
        title:      "Action Buttons",
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
            href "pageAbout", title:"About", description:"Tap to open"
            href "pagePanelStatus", title:"Alarm Panel Status", description:"Tap to open"
            href "pageAlarmSettings", title:"Smart Alarm Settings", description:"Tap to open"
            href "pageZoneSettings", title:"Zone Settings", description:"Tap to open"
            href "pageZoneBypass", title:"Quick Zone Bypass", description:"Tap to open"
            href "pageButtonRemote", title:"Configure Button Remote", description:"Tap to open"
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
        "Smart Alarm turns SmartThings into a multi-zone alarm panel with " +
        "up to 16 security zones. Any number of sensors can be assigned to " +
        "each zone. The alarm can be armed and disarmed by simply setting " +
        "the home 'mode'."

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
        "You can configure up to 16 security zones. A security zone is an " +
        "area of your home protected by one or more sensors, for example a " +
        "room or an entire floor in a multistorey building. Any number of " +
        "sensors (contact, motion, smoke or moisture) can be assigned to " +
        "each zone."

    def helpArming =
        "Smart Alarm can be armed and disarmed by simply setting the home " +
        "'mode'. There are two arming options - Stay and Away. Interior " +
        "zones are not armed in Stay mode, allowing you to freely move " +
        "inside your home."

    def helpExitDelay =
        "Exit delay allows you to exit premises within 45 seconds after " +
        "arming the alarm panel without setting of an alarm."

    def helpEntryDelay =
        "Entry delay allows you to enter premises when Smart Alarm is " +
        "armed and disarm it within 45 seconds without setting of an alarm."

    def helpAlarm =
        "When an alarm is set off, Smart Alarm can turn on some sirens " +
        "and light switches."

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
        multiple:       true
    ]

    def inputStayModes = [
        name:           "stayModes",
        type:           "mode",
        title:          "Arm Stay in these Modes",
        multiple:       true,
        required:       false
    ]

    def inputExitDelay = [
        name:           "exitDelay",
        type:           "bool",
        title:          "Enable exit delay",
        defaultValue:   true,
        required:       true
    ]

    def inputEntryDelay = [
        name:           "entryDelay",
        type:           "bool",
        title:          "Enable entry delay",
        defaultValue:   true,
        required:       true
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
            paragraph helpExitDelay
            input inputExitDelay
            paragraph helpEntryDelay
            input inputEntryDelay
        }
        section("Alarm Options") {
            paragraph helpAlarm
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
    def statusExitDelay = settings.exitDelay ? "On" : "Off"
    def statusEntryDelay = settings.entryDelay ? "On" : "Off"
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
        title:      "Configure Button Remote",
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
            runIn(state.entryDelay, activateAlarm)
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
    def armed = false
    def stay = atomicState.stay
    if (settings.awayModes?.contains(mode)) {
        armed = true
        stay = false
    } else if (settings.stayModes?.contains(mode)) {
        armed = true
        stay = true
    }

    if (armed == atomicState.armed && state == atomicState.stay) {
        return
    }

    if (armed) {
        if (state.exitDelay) {
            // See Issue #1.
            unschedule()
            if (stay) {
                runIn(state.exitDelay, armStay)
            } else {
                runIn(state.exitDelay, armAway)
            }
        } else {
            state.armed = true
            state.stay = stay
            resetPanel()
        }
    } else {
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

    state.armed = true
    state.stay = false
    resetPanel()
}

def armStay() {
    TRACE("armStay()")

    state.armed = true
    state.stay = true
    resetPanel()
}

def disarm() {
    TRACE("disarm()")

    state.armed = false
    resetPanel()
}

def panic() {
    TRACE("panic()")

    state.alarm = true;
    activateAlarm()
}

def activateAlarm() {
    if (!state.alarm) {
        TRACE("activateAlarm: Hmm... False alarm?")
        return
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
    runIn(180, resetPanel)
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

private def textVersion() {
    def text = "Version 1.1.2"
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
    log.debug "settings: ${settings}"
    log.debug "state: ${state}"
}
