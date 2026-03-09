# ECUFlasher-Pro Development Roadmap

Goal:
Create a fully mobile Subaru ECU tuning tool that allows ROM reading, editing, flashing, logging, and live tuning directly from an Android phone using a Tactrix OpenPort 2.0 cable and a USB-C OTG adapter.

---

## Phase 1 – Stable Android App (Current Stage)

Objective:
Create a stable base Android application that launches reliably.

Tasks:
- Fix launch crash
- Verify MainActivity launches correctly
- Ensure Gradle builds consistently
- Confirm APK installs and launches on Android 16
- Establish a basic UI

Result:
Stable Android base application.

---

## Phase 2 – USB Hardware Communication

Objective:
Detect and communicate with the OpenPort cable.

Tasks:
- Implement Android USB Host permissions
- Detect OpenPort when connected
- Identify vendor/product ID
- Establish USB communication
- Send and receive test data

Result:
Phone communicates with OpenPort hardware.

---

## Phase 3 – ECU Communication

Objective:
Communicate with Subaru ECU.

Tasks:
- Implement Subaru SSM protocol
- Request ECU ID
- Confirm stable ECU connection
- Handle timeouts and retries

Result:
Phone can communicate with ECU.

---

## Phase 4 – ROM Access

Objective:
Read ECU firmware.

Tasks:
- Read ROM from ECU
- Verify data integrity
- Save ROM backup to phone storage

Result:
User can back up ECU ROM.

---

## Phase 5 – ECU Flashing

Objective:
Safely write firmware to ECU.

Tasks:
- Write ROM to ECU
- Verify flash integrity
- Add recovery and safety checks

Result:
User can reflash ECU from phone.

---

## Phase 6 – Data Logging

Objective:
Monitor live ECU data.

Tasks:
- Read live sensor values
- Display data in UI
- Record logs

Result:
Real-time engine monitoring.

---

## Phase 7 – ROM Editing

Objective:
Allow editing of ECU maps.

Tasks:
- Load ROM file
- Display editable tables
- Modify map values
- Save modified ROM

Result:
User can edit ROM maps.

---

## Phase 8 – Live Tuning

Objective:
Real-time adjustments.

Tasks:
- Modify parameters while engine is running
- Send adjustments without full flash
- Display live results

Result:
Phone-based live tuning.

---

## Long Term Vision

- Fully mobile Subaru tuning platform
- Flash, log, and tune directly from a phone
- No laptop required
