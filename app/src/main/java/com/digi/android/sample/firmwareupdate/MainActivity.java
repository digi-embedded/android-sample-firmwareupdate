/*
 * Copyright (c) 2021-2025, Digi International Inc. <support@digi.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.digi.android.sample.firmwareupdate;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.digi.android.firmwareupdate.FirmwareUpdateManager;
import com.digi.android.firmwareupdate.FirmwareUpdaterStatus;
import com.digi.android.firmwareupdate.IFirmwareUpdateListener;

import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Firmware Update sample application.
 *
 * <p>This example demonstrates how to update an Android A/B system using
 * the Firmware Update API.</p>
 *
 * <p>For a complete description on the example, refer to the 'README.md' file
 * included in the example directory.</p>
 */
public class MainActivity extends AppCompatActivity implements IFirmwareUpdateListener {

    // Constants.
    private final static String TAG = "FirmwareUpdateSample";

    // Variables.
    private FirmwareUpdateManager firmwareUpdateManager;

    private TextView textViewBuild;
    private TextView textViewJsonFile;
    private TextView textViewUpdaterState;
    private TextView textViewUpdaterDetails;
    private TextView textViewUpdateInfo;

    private Button buttonBrowse;
    private Button buttonApply;
    private Button buttonCancel;
    private Button buttonReset;
    private Button buttonSuspend;
    private Button buttonResume;
    private Button buttonSwitchSlot;

    private ProgressBar progressBar;

    private Uri jsonFileUri;

    /**
     * This activity launcher is used to launch an activity with "getContent" contract based on
     * the given mime type and receive the user selection.
     */
    ActivityResultLauncher<String> getContentLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Handle the returned Uri
                    jsonFileUri = uri;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the UI components.
        initializeUI();

        // Create firmware update manager.
        firmwareUpdateManager = new FirmwareUpdateManager(this);
    }

    @Override
    protected void onResume() {
        firmwareUpdateManager.registerFirmwareUpdateListener(this);
        statusUpdate(firmwareUpdateManager.getUpdaterStatus(), "");
        super.onResume();
    }

    @Override
    protected void onPause() {
        firmwareUpdateManager.removeFirmwareUpdateListener(this);
        super.onPause();
    }

    /**
     * Instantiates and initializes all the UI components.
     */
    private void initializeUI() {
        this.textViewBuild = findViewById(R.id.textViewBuild);
        this.textViewJsonFile = findViewById(R.id.textViewJson);
        this.textViewUpdaterState = findViewById(R.id.textViewUpdaterState);
        this.textViewUpdaterDetails = findViewById(R.id.textViewUpdaterDetails);
        this.textViewUpdateInfo = findViewById(R.id.textViewUpdateInfo);
        this.buttonBrowse = findViewById(R.id.buttonBrowse);
        this.buttonApply = findViewById(R.id.buttonApply);
        this.buttonCancel = findViewById(R.id.buttonCancel);
        this.buttonReset = findViewById(R.id.buttonReset);
        this.buttonSuspend = findViewById(R.id.buttonSuspend);
        this.buttonResume = findViewById(R.id.buttonResume);
        this.buttonSwitchSlot = findViewById(R.id.buttonSwitchSlot);
        this.progressBar = findViewById(R.id.progressBar);

        uiResetWidgets();
    }

    /**
     * Handles what happens when the "Browse" button is clicked.
     */
    public void onBrowseButtonClick(View view) {
        getContentLauncher.launch("application/json");
    }

    /**
     * Handles what happens when the "Apply" button is clicked.
     */
    public void onApplyButtonClick(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Apply Update")
                .setMessage("Do you really want to apply this update?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    try {
                        firmwareUpdateManager.applyUpdate(readJsonConfigFile());
                        uiResetWidgets();
                        uiResetDetailsText();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to apply update", e);
                        Toast.makeText(this, "Failed to apply update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Handles what happens when the "Cancel" button is clicked.
     */
    public void onCancelButtonClick(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Update")
                .setMessage("Do you really want to cancel running update?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> {
                    try {
                        firmwareUpdateManager.cancelUpdate();
                    } catch (IllegalStateException e) {
                        Log.e(TAG, "Failed to cancel running update", e);
                        Toast.makeText(this, "Failed to cancel running update: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    /**
     * Handles what happens when the "Reset" button is clicked.
     */
    public void onResetButtonClick(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Reset Update")
                .setMessage("Do you really want to cancel running update"
                        + " and restore old version?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.ok, (dialog, whichButton) -> firmwareUpdateManager.resetUpdate())
                .setNegativeButton(android.R.string.cancel, null).show();
    }

    /**
     * Handles what happens when the "Suspend" button is clicked.
     */
    public void onSuspendButtonClick(View view) {
        try {
            firmwareUpdateManager.suspendUpdate();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to suspend running update", e);
            Toast.makeText(this, "Failed to suspend running update: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles what happens when the "Resume" button is clicked.
     */
    public void onResumeButtonClick(View view) {
        try {
            firmwareUpdateManager.resumeUpdate();
            uiResetWidgets();
            uiResetDetailsText();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to resume running update", e);
            Toast.makeText(this, "Failed to resume running update: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handles what happens when the "Switch Slot" button is clicked.
     */
    public void onSwitchSlotButtonClick(View view) {
        try {
            firmwareUpdateManager.switchBootPartition();
            uiResetWidgets();
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to switch boot partition", e);
            Toast.makeText(this, "Failed to switch boot partition: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Returns the JSON configuration file name.
     *
     * @return The JSON configuration file name.
     */
    private String getJsonConfigFileName() {
        if (jsonFileUri == null)
            return this.getResources().getString(R.string.no_file_selected);

        Cursor returnCursor = getContentResolver().query(jsonFileUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String fileName = returnCursor.getString(nameIndex);
        returnCursor.close();
        return fileName;
    }

    /**
     * Reads and returns the contents of the JSON configuration file.
     *
     * @return The contents of the JSON configuration file as string.
     *
     * @throws IOException If there is any error reading the file.
     */
    private String readJsonConfigFile() throws IOException {
        if (jsonFileUri == null)
            return "";

        String line;
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getContentResolver().openInputStream(jsonFileUri)));
        while ((line = reader.readLine()) != null)
            sb.append(line);
        reader.close();

        return sb.toString();
    }

    /**
     * Resets all the UI widgets to its default status.
     */
    private void uiResetWidgets() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE)
            textViewBuild.setText(Build.DISPLAY);
        buttonBrowse.setEnabled(true);
        buttonCancel.setEnabled(false);
        buttonReset.setEnabled(false);
        buttonSuspend.setEnabled(false);
        buttonResume.setEnabled(false);
        progressBar.setEnabled(false);
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        buttonSwitchSlot.setEnabled(false);
        textViewUpdateInfo.setTextColor(Color.parseColor("#aaaaaa"));
        buttonApply.setEnabled(jsonFileUri != null);
        textViewJsonFile.setText(getJsonConfigFileName());
    }

    /**
     * Resets the details text.
     */
    private void uiResetDetailsText() {
        textViewUpdaterDetails.setText("");
    }

    /**
     * Configures the UI elements to match the update idle state.
     */
    private void uiStateIdle() {
        uiResetWidgets();
        buttonReset.setEnabled(true);
        progressBar.setProgress(0);
    }

    /**
     * Configures the UI elements to match the update running state.
     */
    private void uiStateRunning() {
        uiResetWidgets();
        progressBar.setEnabled(true);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        buttonCancel.setEnabled(true);
        buttonSuspend.setEnabled(true);
        buttonBrowse.setEnabled(false);
        buttonApply.setEnabled(false);
    }

    /**
     * Configures the UI elements to match the update paused state.
     */
    private void uiStatePaused() {
        uiResetWidgets();
        buttonReset.setEnabled(true);
        progressBar.setEnabled(true);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        buttonResume.setEnabled(true);
    }

    /**
     * Configures the UI elements to match the update switch slot required state.
     */
    private void uiStateSlotSwitchRequired() {
        uiResetWidgets();
        buttonReset.setEnabled(true);
        progressBar.setEnabled(true);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        buttonSwitchSlot.setEnabled(true);
        textViewUpdateInfo.setTextColor(Color.parseColor("#777777"));
    }

    /**
     * Configures the UI elements to match the update error state.
     */
    private void uiStateError() {
        uiResetWidgets();
        buttonReset.setEnabled(true);
        progressBar.setEnabled(true);
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    /**
     * Configures the UI elements to match the update reboot required state.
     */
    private void uiStateRebootRequired() {
        uiResetWidgets();
        buttonReset.setEnabled(true);
    }

    @Override
    public void statusUpdate(FirmwareUpdaterStatus status, String details) {
        runOnUiThread(() -> {
            textViewUpdaterState.setText(status.getDescription());
            textViewUpdaterDetails.setText(details);
            switch (status) {
                case IDLE:
                    uiStateIdle();
                    break;
                case RUNNING:
                    uiStateRunning();
                    break;
                case PAUSED:
                    uiStatePaused();
                    break;
                case ERROR:
                    uiStateError();
                    break;
                case SLOT_SWITCH_REQUIRED:
                    uiStateSlotSwitchRequired();
                    break;
                case REBOOT_REQUIRED:
                    uiStateRebootRequired();
                    break;
            }
        });
    }

    @Override
    public void onProgress(int progress) {
        progressBar.setProgress(progress);
    }
}
