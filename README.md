Firmware Update Sample Application
=========================================

This application demonstrates the usage of the Firmware Update API to apply
OTA update packages to the system. The application uses JSON configuration
files that describe the OTA update package and process.

Demo requirements
-----------------

To run this example you need:

* A compatible development board to host the application.
* A USB connection between the board and the host PC in order to transfer and
  launch the application.
* An OTA update package. See online documentation to learn how to generate this
  file.
* The JSON configuration file that describes the OTA update package and process.
  See online documentation to learn how to generate this file.

Demo setup
----------

Make sure the hardware is set up correctly:

1. The development board is powered on.
2. The board is connected directly to the PC by the micro USB cable.

Make sure the JSON configuration file is present in the device. You can transfer
it to the internal memory or place it in a USB stick and then connect it to the
device.

Depending on the firmware update process (streaming or not streaming) you have to
make sure that:

For streaming updates:

1. The web server is correctly configured and running.
2. The OTA package is located in the web server in the configured path.
3. The device can reach the web server.

For non-streaming updates:

1. The OTA package is located in the configured device path.

Demo run
--------

The example is already configured, so all you need to do now is to build and
launch the project.

The application displays two panels:

The left panel configures the firmware update. It allows you to choose the JSON
configuration file to apply to the device using the "Browse" button. Once the
JSON file is selected, you can start the update by clicking the "Apply Update"
button.

The right panel manages the update process. It displays useful information about
the current firmware update process such as the general status, detailed status
and a progress bar. This panel also holds several buttons that allows you to
control the firmware update process:

* Cancel: Permanently cancels an in-progress update.
* Reset: Resets the update engine to IDLE state. If an update has been applied it
is reverted.
* Suspend: Suspends a running update.
* Resume: Resumes a suspended update.
* Switch Slot: For updates that do not force the A/B slot switching, this button
allows you to perform this action manually.

Compatible with
---------------

* ConnectCore 8X SBC Pro
* ConnectCore 8M Mini Development Kit

License
-------

Copyright (c) 2021, Digi International Inc. <support@digi.com>

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
