gDay
====

Based on Google's Analog sample code, this gDay WatchFace takes a different approach to the
5 second user experience. It's intended for Google Experiments 2016. See the screenshots!

gDay presents a 24 hour clock face, no minute or second hand. The face is divided into 24 hours,
midnight at the bottom and noon at the top. Your day's calendar events appear on an outer ring 
and two weather related indicators on two rings within.
At a glance, you can see busy times for the rest of the day and upcoming weather. Calendar
and weather information are wiped if they're in the past.
It's a glance so you'll have a gDay.

The red, long segment that looks like a minute hand identifies the current time on the circle.

This includes a phone-side configuration app that allows for a manual weather fetch in
addition to an hourly service to scrape the weather and push it to the watch through the
data API.

Pre-requisites
--------------

- Android SDK v23
- Android Build Tools v23.0.2
- Android Support Repository

Getting Started
---------------
This sample uses the Gradle build system. To build this project, use the
"gradlew build" command or use "Import Project" in Android Studio.

License
-------

Copyright 2016 Thomas Baker

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.
