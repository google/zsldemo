# ZSLDemo

## This is not an officially supported Google product

A proof-of-concept application for implementing application-side Zero Shutter Latency (ZSL) image capture in Camera2 on Android devices 23+.

## Operation

- Open the app
- If your device is not capable of reprocessing you will receive a toast message and see a blank preview
- If you see a preview stream your device is now filling the ZSL buffer
- Click "Capture Photo" to save a photo to disk
- The photo will be saved in DCIM/ZSLDemo/ZSLDemo-timestamp
- The total capture time from the button press to the full-quality image being available is recorded in the log (about 50-100ms on Pixel phones). This time includes the image reprocessing and jpeg encoding.

## Overview

ZSL is achieved in this demo my maintaining a circular buffer of full-size private format images coming from the camera device 
at the same time that the preview stream is running. When the shutter button is pressed, the "best" image from the
buffer is chosen, sent through the camera device for hardware processing and encoding, and then saved to disk.

![ZSL Overview](ZSLDemo.jpg?raw=true "ZSL Overview")

## Technique
When the global capture session is created three output streams and one input stream are created:
[output] preview TextureView to show camera preview
[output] full-quality private format image reader -> input to circular image buffer
[output] full-quality jpeg format image reader -> final, reprocessed image
[input] full-quality private format image writer -> selected image from buffer for reprocessing to create final jpeg output 

When the user presses the shutter button, the ZSLCoordinator class looks for the “best” frame from the queue. The frame
is returned to the camera hardware for “reprocessing” - OEM enhancements and hardware encoding to jpeg or YUV. This
frame then appears on the jpeg image reader.

The criteria for “best” frame of the 10-frame circular buffer is:
- Prefer a frame that has continuous auto-focus and exposure converged
- Ensure frame does not need a flash 
- Prefer frame #3 to avoid jitter/blur from finger touch on screen
- If frame #3 is not good, search 4-10, then 1-2

## Next Steps
- Add “shutter” screen flash
- Add settings with log and save/don’t save function
- Add front/back toggle
- If we need flash, do full capture with flash
- Any other situation we want to do full capture?

## Notes
 - Only tested on a handful of devices
 - This is a demonstration of one ZSL technique, do not use this code in production

## LICENSE

***

Copyright 2019 Google LLC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


