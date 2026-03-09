# ECUFlasher-Pro

ECUFlasher-Pro is an experimental Android application designed to interact with automotive ECUs directly from a mobile device.

The long term goal is to allow users to read ECU data, log parameters, edit ROM files, and eventually flash ECUs directly from an Android phone using a USB interface such as the Tactrix OpenPort 2.0.

This project is currently in early development and is focused on building a stable Android foundation before implementing ECU communication features.

---

## Project Goals

• Mobile ECU diagnostics  
• ECU ROM reading  
• Live data logging  
• ROM editing support  
• ECU flashing from Android devices  
• Support for professional interfaces like Tactrix OpenPort

---

## Current Status

The Android application environment is successfully running and building through GitHub Actions.

Current focus:

• Application structure  
• UI development  
• USB communication layer  
• ECU protocol research

---

## Development Environment

This project is being developed in an unconventional mobile-first workflow:

• Development environment: Termux (Android)  
• Code editing: mobile device  
• Build system: Gradle + GitHub Actions  
• Debugging: ADB / logcat  
• Test devices: Android phones

---

## Hardware Target

Primary hardware interface planned:

Tactrix OpenPort 2.0 (USB OTG)

Future possibilities:

• CAN interfaces  
• Bluetooth adapters  
• standalone ECU support

---

## Open Source

Parts of this project will be made open source to encourage collaboration and community development.

Contributions, ideas, and feedback are welcome.

---

## Disclaimer

This project is experimental and intended for research and development purposes.

Flashing ECUs carries inherent risks and should only be performed with proper knowledge and precautions.

---

## Author

Starling Cross
