# ECUFlasher-Pro Bench Setup

## Hardware
- [ ] 12V regulated power supply
- [ ] OBD2 female breakout connector
- [ ] ECU harness / connector pigtails
- [ ] Tactrix OpenPort 2.0
- [ ] USB-C OTG adapter

## ECU Power Wiring
- [ ] Constant 12V (BATT)
- [ ] Ignition switched 12V (IGN)
- [ ] Ground connections
- [ ] Verify no short circuits

## OBD2 Bench Wiring
- [ ] Pin 4 -> Ground
- [ ] Pin 5 -> Ground
- [ ] Pin 16 -> 12V Power
- [ ] Pin 7 -> K-Line (if required)
- [ ] CAN High -> Pin 6
- [ ] CAN Low -> Pin 14

## Bench Communication Test
- [ ] ECU powers on
- [ ] OpenPort detected by Android
- [ ] App opens USB device
- [ ] Interface endpoints identified
- [ ] Raw communication test

## Safety Before Flashing
- [ ] Confirm ECU ID
- [ ] Confirm protocol
- [ ] Full ROM read backup
- [ ] Voltage stability confirmed
