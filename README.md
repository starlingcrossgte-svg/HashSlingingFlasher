# ECUFlasher-Pro

ECUFlasher-Pro is an experimental Android-based ECU communication and flashing tool designed to run entirely from a mobile device.

The long-term goal of the project is to create a fully mobile Subaru ECU tuning platform capable of reading, editing, logging, and flashing ECU firmware directly from an Android phone using a Tactrix OpenPort 2.0 cable with a USB-C OTG adapter.

This project is currently in early development and is focused on building a stable Android application base before implementing ECU communication.

---

## Project Goals

• Run ECU flashing and diagnostics directly from a phone  
• Communicate with Subaru ECUs through a Tactrix OpenPort 2.0 interface  
• Allow ROM reading and backup  
• Allow ROM editing and flashing  
• Provide real-time logging and monitoring  
• Eventually support live tuning adjustments

---

## Current Development Stage

The current focus is establishing a stable Android application environment.

Completed so far:

• Android project structure created  
• Gradle build system configured  
• GitHub Actions CI configured to automatically build APKs  
• Successful APK generation  
• Installation testing on Samsung S25 Ultra (Android 16)  
• Basic MainActivity application structure created  
• Development roadmap established

The application currently builds successfully but crashes immediately on launch.  
Debugging the base Android activity is the current priority before implementing USB communication.

---

## Planned Development Phases

1. Stable Android application base  
2. USB communication with OpenPort hardware  
3. Subaru ECU communication (SSM protocol)  
4. ECU ROM reading and backup  
5. ECU flashing capability  
6. Real-time ECU logging  
7. ROM editing tools  
8. Live tuning capabilities

---

## Hardware Target

• Android device with USB-C OTG support  
• Tactrix OpenPort 2.0 cable  
• Subaru ECU compatible with OpenPort interface

---

## Development Environment

This project is currently being developed directly on an Android device using:

• Samsung S25 Ultra  
• Termux  
• GitHub for version control and CI builds  
• Gradle build system

---

## Project Status

Early development / proof-of-concept stage.

Contributions, suggestions, and feedback are welcome.
