# ECUFlasher-Pro

ECUFlasher-Pro is an experimental Android application designed to communicate with automotive ECUs directly from a mobile device.

This project is focused on building a clean Android-first communication stack for professional vehicle interfaces such as the Tactrix OpenPort 2.0, without relying on the traditional PC-based driver workflow.

## Current Status

Confirmed milestones:

- Android USB Host communication with Tactrix OpenPort 2.0
- Direct OpenPort firmware command communication
- OpenPort command channel confirmed with:
  - `ati`
  - `ata`
  - `ato6 0 500000 0`
- CAN bus open confirmed on a 2006 Subaru Outback 3.0R
- Raw vehicle-side response observed during first ECU query testing

Current focus:

- stabilizing the communication pipeline
- improving protocol separation
- implementing clean ECU response parsing
- preparing for safe bench ECU development

## Project Goals

- mobile ECU diagnostics
- live data logging
- ECU identification
- ROM reading
- ROM flashing from Android devices
- support for professional USB vehicle interfaces

## Development Direction

The long-term architecture is being built in layers:

- USB transport layer
- OpenPort command layer
- CAN / vehicle transport layer
- ECU protocol layer
- ROM read / flash layer

The current development priority is building a stable communication foundation before advancing to full ECU interaction.

## Development Environment

This project is being developed in a mobile-first workflow using Android devices, Termux, GitHub, and GitHub Actions.

## License

ECUFlasher-Pro is released under the GNU General Public License v3.0 (GPLv3).

See the `LICENSE` file for full details.

## Author

Cross Starling

This project is in active early development and should be treated as experimental.
