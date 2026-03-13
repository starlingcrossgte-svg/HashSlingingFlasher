# ECUFlasher-Pro Architecture Plan

## Current Situation

Right now most communication logic is concentrated in `UsbDeviceManager.kt`.

That was acceptable for rapid prototyping, but the project now needs clearer separation so future development stays stable and maintainable.

---

## Planned Layer Split

### 1. UsbTransport
Responsibility:
- discover USB device
- open USB device
- claim interface
- locate endpoints
- bulk write
- bulk read
- close/release connection

Purpose:
Provide a clean low-level transport layer with no OpenPort-specific command knowledge.

---

### 2. OpenPortClient
Responsibility:
- send OpenPort firmware commands
- manage command/session flow
- handle commands such as:
  - `ati`
  - `ata`
  - `ato...`
- return parsed firmware responses

Purpose:
Contain all OpenPort command-channel logic in one place.

---

### 3. CanProtocol
Responsibility:
- build CAN frames
- send CAN frames through OpenPort
- parse raw CAN responses
- handle standard OBD query framing

Purpose:
Separate CAN transport behavior from OpenPort command logic.

---

### 4. EcuProtocol
Responsibility:
- ECU identification requests
- Subaru-specific protocol helpers
- future SSM / ROM / flash operations

Purpose:
Contain all ECU-specific behavior separate from transport and interface control.

---

## Refactor Order

1. Extract UsbTransport
2. Extract OpenPortClient
3. Extract CanProtocol
4. Extract EcuProtocol

---

## Rule Going Forward

Transport, interface control, vehicle network handling, and ECU logic should remain separated.

This project should avoid putting all future behavior into a single manager file.
