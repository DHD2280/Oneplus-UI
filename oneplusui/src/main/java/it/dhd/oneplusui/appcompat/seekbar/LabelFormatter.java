package it.dhd.oneplusui.appcompat.seekbar;

/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.annotation.NonNull;

/**
 * Interface definition for applying custom formatting to the text displayed inside the bubble
 * shown when a mOplusSlider is used in discrete mode.
 */
public interface LabelFormatter {

    int LABEL_FLOATING = 0;
    int LABEL_WITHIN_BOUNDS = 1;
    int LABEL_GONE = 2;
    int LABEL_VISIBLE = 3;

    @NonNull
    String getFormattedValue(float value);
}
