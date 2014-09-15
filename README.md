## Smart Alarm

Smart Alarm is a home security application for the
[SmartThings](http://fbuy.me/bb9pe) home automation system. You can configure
up to 16 security zones and assign any number of contact, motion, moisture and
smoke sensors to each zone. The alarm is armed and disarmed simply by setting
the home 'mode'.


### Features

* Up to 16 independent security zones.
* Unlimited number of sensors (contact, motion, moisture, smoke) per zone.
* Two arming modes - Away and Stay.
* Optional entry and exit delays.
* Zones can be designated as Interior (armed in Away mode only) Exterior
(armed in both Away and Stay modes) and Alert (always armed).
* Zone bypass allows quickly exclude select zones.
* Setting of an alarm can activate sirens, turn on switches, send push
notifications and text messages.
* Silent mode disables sirens and switches, but leaves push notifications and
text messages on.
* Panic button.

Smart Alarm can be armed and disarmed either using a remote control, for
example [Aeon Labs Minimote](http://www.amazon.com/Aeon-Labs-DSA03202-v1-Minimote/dp/B00KU7ERAW)
or by simply setting the home 'Mode'. For example, you can set it up to arm in
Away mode when the home mode is set to 'Away' and to arm in Stay mode when the
home mode is set to 'Night'. Setting home to any other mode, for example
'Home', will automatically disarm Smart Alarm.

Using home Mode to arm and disarm Smart Alarm is a very flexible and powerful
technique because home modes can be changed by other Smart Apps and 'Hello,
Home' actions. For example, 'Good Night!' action activates the 'Night' mode,
thus automatically arming Smart Alarm in Stay mode.


### Screenshots

![](https://sites.google.com/site/statusbits/pictures/SmartAlarm1.jpg)

![](https://sites.google.com/site/statusbits/pictures/SmartAlarm2.jpg)


### Installation

Smart Alarm app is available in the "Safety & Security" section of the Shared
Smart Apps in [SmartThings IDE](https://graph.api.smartthings.com).

1. Go to "My SmartApps" section and click on the "+ New SmartApp" button on the
right.
2. On the "New SmartApp" page, fill out mandatory "Name" and "Description"
fields (it does not matter what you put there), then click the "Create" button
at the bottom.
3. When a new app template opens in the IDE, click on the "Browse SmartApps"
drop-down list in the upper right corner and select "Browse Shared SmartApps".
A list of shared SmartApps will appear on the left side of the editor window.
4. Scroll down to "Safety & Security" section and click on it.
5. Select "Smart Alarm" app from the list and click the red "Overwrite" button
in the bottom right corner.
6. Click the blue "Save" button above the editor window.
7. Click the "Publish" button next to it and select "For Me". You have now
self-published your SmartApp.
8. Open SmartThings mobile app on iPhone or Android and go to the Dashboard.
9. Tap on the round "+" button and navigate to "My Apps" section by swiping
the menu ribbon all the way to the left.
10. "Smart Alarm" app should be available in the list of SmartApps that
appears below the menu ribbon. Tap it and follow setup instructions.


### Revision History

**Version 1.1.2. Released 2014-09-14**
* You can now arm, disarm and trigger panic alarm using button remote (e.g.
Aeon Labs Minimote).
* Modified Setup Menu work flow and some help text.

**Version 1.1.0. Released 2014-09-12**
* Released under GPLv3 License.
* Added 'About' page in the setup menu.
* Merged changes from Barry Burke (SANdood) branch
(https://github.com/SANdood/smartthings/tree/master/SmartAlarm). Thanks!

**Version 1.0.1. Released 2014-08-28**
* Fixed spelling mistakes, formatting, etc.

**Version 1.0.0. Released 2014-07-04**
* First public release.


License
-------

Copyright (C) 2014 Statusbits.com

This program is free software: you can redistribute it and/or modify it
under the terms of the GNU General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option)
any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program.  If not, see <http://www.gnu.org/licenses/>.
