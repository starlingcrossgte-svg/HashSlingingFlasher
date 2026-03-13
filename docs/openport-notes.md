# OpenPort Communication Notes

## Confirmed Working Environment
Android device using USB Host API communicating directly with Tactrix OpenPort 2.0.

Vehicle tested:
2006 Subaru Outback 3.0R

---

## Confirmed OpenPort Firmware Commands

### ATI
Requests firmware identification.

Command sent:
ati\r\n

Example response:
ari main code version : 1.17.4877

---

### ATA
Initial interface activation command.

Command sent:
ata\r\n

Example response:
aro

---

### ATO6
Open CAN channel.

Command sent:
ato6 0 500000 0\r\n

Meaning:
- Channel: 6 (CAN)
- Mode: 0
- Baud: 500000
- Flags: 0

Example response:
aro

---

## First CAN Query Attempt

Command sent through OpenPort:

Mode 01 PID 00

Purpose:
Request supported OBD-II parameters from ECU.

Status:
Raw vehicle-side response observed but parsing not yet implemented.

---

## Current Milestone

Android → USB → OpenPort → CAN bus communication confirmed.

Next milestone:
Stable ECU query and response parsing.
