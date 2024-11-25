package com.htetznaing.adbotg;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.content.Context;
import android.app.PendingIntent;
import android.content.Intent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import com.cgutman.adblib.AdbConnection;
import com.cgutman.adblib.AdbCrypto;
import com.cgutman.adblib.AdbStream;
import com.cgutman.adblib.TcpChannel;
import com.cgutman.adblib.UsbChannel;
import com.cgutman.adblib.AdbBase64;

    public class SpywareDetector {

        // Variables
        private UsbManager mManager; // USB manager
        private static AdbConnection adbConnection; // ADB connection
        private UsbDevice mDevice; // USB device
        private AdbCrypto adbCrypto; // ADB crypto
        private boolean isAdbConnected = false; // Flag to check if the ADB connection is established


        /**
         * Get the detected spyware apps from the CSV data
         * @param csvData - The CSV data
         * @param isTargetDevice - Flag to check if the target device is the current device
         * @return The list of detected spyware apps
         **/
        public static List<Map<String, Object>> getDetectedSpywareApps(List<List<String>> csvData, boolean isTargetDevice) {
            List<String> ids = new ArrayList<>(); // List to store the app IDs
            Map<String, String> types = new HashMap<>(); // Map to store the app types (i.e. spyware, dual-use, etc.)

            for (int i = 1; i < csvData.size(); i++) { // Iterate through the CSV data
                List<String> line = csvData.get(i);
                if (!line.isEmpty()) {
                    String appId = line.get(0).trim();
                    ids.add(appId); // Add the app ID to the list
                    Log.d("SpywareDetector", "App ID:" + appId); 
                    if (line.size() > 2) {
                        types.put(appId, line.get(2).trim()); // Add the app type to the map
                    }
                }
            }

            List<Map<String, Object>> detectedSpywareApps = new ArrayList<>(); // List to store apps detected as spyware
            List<String> installedApps; // List to store the installed app IDs from device

            SpywareDetector detector = new SpywareDetector(); // Create a new instance of SpywareDetector

            if (isTargetDevice) { // If the target device is the current device
                installedApps = detector.fetchAppsFromTargetDevice(AppContextHolder.getContext()); // Fetch the installed app IDs from the target device
            } else { // If the target device is not the current device
                PackageManager packageManager = AppContextHolder.getContext().getPackageManager(); // Get the package manager
                List<ApplicationInfo> appInfoList = packageManager.getInstalledApplications(PackageManager.GET_META_DATA); // Get the list of installed applications from source device
                installedApps = new ArrayList<>(); 
                // Iterate through the list of installed applications & add app ids to list
                for (ApplicationInfo appInfo : appInfoList) {
                    installedApps.add(appInfo.packageName); 
                }
            }

            Log.d("SpywareDetector", "Installed Apps:" + installedApps.toString());
            
            // Iterate through the list of installed apps & check if they are in the list of spyware / dual-use / offstore apps
            for (String appID : installedApps) {
                if (ids.contains(appID)) {
                    try { 
                        Map<String, String> appMetadata;
                        String iconBase64;
                        if (isTargetDevice) { // fetch app metadata from target device
                            appMetadata = detector.fetchAppMetadataFromTarget(AppContextHolder.getContext(), appID);
                            Drawable placeholderIcon = AppContextHolder.getContext().getDrawable(R.drawable.placeholder_icon);
                            iconBase64 = getBase64IconFromDrawable(placeholderIcon);
                        } else { // fetch app metadata from source device
                            PackageManager packageManager = AppContextHolder.getContext().getPackageManager();
                            String appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(appID, 0)).toString();
                            iconBase64 = getBase64IconFromDrawable(packageManager.getApplicationIcon(appID));
                            String installer = getInstallerPackageName(packageManager, appID);
                            
                            // If the installer is null, set it to "Unknown Installer"
                            if (installer == null) {
                                installer = "Unknown Installer";
                            }

                            // Initialize the app metadata map
                            appMetadata = new HashMap<>();
                            appMetadata.put("name", appName);
                            appMetadata.put("installer", installer);
                        }

                        // Get the store link & app type
                        String storeLink = getStoreLink(appID, appMetadata.get("installer"));
                        String appType = types.getOrDefault(appID, "Unknown");
                        List<Map<String, String>> permissions = AppDetailsFetcher.getAppPermissions(appID);

                        // Initialize the app info map
                        Map<String, Object> appInfo = new HashMap<>();
                        appInfo.put("id", appID);
                        appInfo.put("name", appMetadata.get("name"));
                        appInfo.put("icon", iconBase64);
                        appInfo.put("installer", appMetadata.get("installer"));
                        appInfo.put("storeLink", storeLink);
                        appInfo.put("type", appType);
                        appInfo.put("permissions", permissions);

                        // Add the app info to the list of detected spyware apps
                        detectedSpywareApps.add(appInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        Log.w("SpywareDetector", "Package not found: " + appID, e);
                    }
                }
            }
            // Log the detected spyware apps (for debugging purposes)
            for (int i = 0; i < detectedSpywareApps.size(); i++) {
                Log.d("SpywareDetector", "Detected spyware apps: " + detectedSpywareApps.get(i));
            }
            return detectedSpywareApps;
        }

        
        /**
         * Get the base64 encoded icon from the drawable
         * @param drawable - The drawable
         * @return The base64 encoded icon
         **/
        private static String getBase64IconFromDrawable(Drawable drawable) {
            Bitmap bitmap; // Bitmap to store the icon
            if (drawable instanceof BitmapDrawable) { // Check if the drawable is a BitmapDrawable
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                if (bitmapDrawable.getBitmap() != null) { // Check if the bitmap is not null & set it to the bitmap
                    bitmap = bitmapDrawable.getBitmap(); 
                } else { // If the bitmap is null, create a new bitmap with the drawable's intrinsic width and height
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                }
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            // Ensure the bitmap is mutable
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            // Create a canvas to draw the drawable on the bitmap
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);

            // Create a ByteArrayOutputStream to store the bitmap as a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // Compress the bitmap as a PNG image & store it in the ByteArrayOutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            // Return the base64 encoded icon
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        }


        /**
         * Get the installer package name of the app
         * @param packageManager - The package manager
         * @param appID - The package ID of the app
         * @return The installer package name of the app
         **/
        private static String getInstallerPackageName(PackageManager packageManager, String appID) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Check if the device is running Android 12 or higher
                try {
                    return packageManager.getInstallSourceInfo(appID).getInstallingPackageName(); // Get the installer package name
                } catch (PackageManager.NameNotFoundException e) {
                    return "Unknown"; // Return "Unknown" if the installer package name is not found
                }
            } else { // If the device is running Android 11 or lower, get the installer package name using the deprecated method
                return packageManager.getInstallerPackageName(appID) != null ? packageManager.getInstallerPackageName(appID) : "Unknown";
            }
        }


        /**
         * Get link to the store where the app was dowloaded from 
         * @param packageName - App ID
         * @param installer - name of the installer app was downloaded from
         * @return The link to the 
         */
        private static String getStoreLink(String packageName, String installer) {
            if (installer == null) {
                return "Unknown Installer"; // or handle it as needed
            }
            
            switch (installer) {
                case "com.android.vending": // google play store
                    return "https://play.google.com/store/apps/details?id=" + packageName;
                case "com.amazon.venezia": // amazon store
                    return "https://www.amazon.com/gp/mas/dl/android?p=" + packageName;
                default: // unknown (unsecure download)
                    return "Unknown Installer"; 
            }
        }

    

        /**
         * Fetch the list of all installed app Ids on the target device
         * @param context - The context of the application
         * @return The list of installed app Ids
         **/
        public List<String> fetchAppsFromTargetDevice(Context context) {
            List<String> packageNames = new ArrayList<>(); // List to store the installed app Ids
            mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE); // Get the USB manager
    
            try {
                if (adbCrypto == null) { // Generate the ADB key pair if it's not already generated
                    adbCrypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                        @Override
                        public String encodeToString(byte[] data) {
                            return Base64.encodeToString(data, Base64.NO_WRAP);
                        }
                    });
                }
            } catch (NoSuchAlgorithmException e) { // Catch any exceptions that occur while generating the ADB key pair
                Log.e("SpywareDetector", "Error generating ADB key pair", e);
                return packageNames;
            }
    
            for (String k : mManager.getDeviceList().keySet()) { // Iterate through all the devices in the USB manager
                UsbDevice usbDevice = mManager.getDeviceList().get(k); // Get the USB device
                if (mManager.hasPermission(usbDevice)) { // Check if the device has permission
                    UsbInterface intf = findAdbInterface(usbDevice); // Find the ADB interface on the device
                    if (intf != null) { // Check if the ADB interface is found
                        try {
                            if (setAdbInterface(usbDevice, intf)) {
                                Log.d("SpywareDetector", "ADB interface set successfully");
                                packageNames.addAll(executeCommand("pm list packages")); // Execute the ADB command to fetch the list of installed app Ids
                            } else { // Log an error if the ADB interface is not set
                                Log.e("SpywareDetector", "Failed to set ADB interface");
                            }
                        } catch (IOException | InterruptedException e) { // Catch any exceptions that occur while setting the ADB interface
                            Log.e("SpywareDetector", "Error setting ADB interface", e);
                        }
                    } else { // Log an error if no ADB interface is found
                        Log.e("SpywareDetector", "No ADB interface found");
                    }
                } else { // Request permission if the device does not have permission
                    Log.d("SpywareDetector", "Requesting USB permission");
                    mManager.requestPermission(usbDevice, PendingIntent.getBroadcast(context, 0, new Intent("com.htetznaing.adbotg.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE));
                }
            }
            return packageNames; // Return the list of installed app Ids
        }
    

        /**
         * Find the ADB interface on the device
         * @param device - The USB device
         * @return The ADB interface
         **/
        private UsbInterface findAdbInterface(UsbDevice device) {
            int count = device.getInterfaceCount(); // Get the number of interfaces on the device
            for (int i = 0; i < count; i++) { // Iterate through all the interfaces
                UsbInterface intf = device.getInterface(i); // Get the interface
                // Check if the interface is the ADB interface
                if (intf.getInterfaceClass() == 255 && intf.getInterfaceSubclass() == 66 && intf.getInterfaceProtocol() == 1) {
                    return intf; 
                }
            }
            return null;
        }
    

        /**
         * Set the ADB interface
         * @param device - The USB device
         * @param intf - The ADB interface
         * @return True if the interface is set successfully, false otherwise
         * @throws IOException
         * @throws InterruptedException
         **/
        private synchronized boolean setAdbInterface(UsbDevice device, UsbInterface intf) throws IOException, InterruptedException {
            // Close the existing ADB connection if it exists
            if (adbConnection != null) {
                adbConnection.close();
                adbConnection = null;
                mDevice = null;
            }
    
            // Set the ADB interface if the device and interface are not null
            if (device != null && intf != null) {
                UsbDeviceConnection connection = mManager.openDevice(device);
                if (connection != null) { // Check if the connection is not null
                    if (connection.claimInterface(intf, false)) { // Claim the interface
                        adbConnection = AdbConnection.create(new UsbChannel(connection, intf), adbCrypto); // Create the ADB connection
                        adbConnection.connect(); // Connect to the ADB server
                        mDevice = device; // Set the device
                        return true; // Return true if the interface is set successfully
                    } else { // Log an error if the interface is not claimed
                        connection.close();
                    }
                }
            }
            return false;
        }
    

        /**
         * Execute the ADB command and return the list of all installed app Ids on the target device
         * @param command - The ADB command to execute
         * @return The list of detected packages
         **/
        private List<String> executeCommand(String command) {
            List<String> detectedPackages = new ArrayList<>();

            // Create a new thread to execute the command
            Thread commandThread = new Thread(() -> {
                boolean success = false; // Flag to check if the command is executed successfully
                int retryCount = 0; // Retry count
                final int maxRetries = 3; // Maximum number of retries

                while (!success && retryCount < maxRetries) {
                    AdbStream stream = null; // Stream to read the command output
                    try {
                        // Open the stream
                        stream = adbConnection.open("shell:" + command);
                        StringBuilder output = new StringBuilder();
                        while (!stream.isClosed()) {
                            byte[] data = stream.read();
                            if (data == null) {
                                break;
                            }
                            output.append(new String(data, StandardCharsets.US_ASCII));
                        }

                        // Process the output line by line
                        String[] lines = output.toString().split("\n");
                        for (String line : lines) {
                            if (line.startsWith("package:")) {
                                // Extract the package name from the line
                                String packageName = line.replace("package:", "").trim();
                                // Add the package name to the list if it's not empty
                                if (!packageName.isEmpty()) {
                                    detectedPackages.add(packageName);
                                    Log.d("SpywareDetector", "Detected package: " + packageName);
                                }
                            }
                        }
                        success = true; // Mark as successful if stream closes normally
                    } catch (IOException | InterruptedException e) {
                        Log.e("SpywareDetector", "Error executing ADB command, retrying...", e);
                    } // Close the stream after the command is executed
                    finally {
                        if (stream != null) {
                            try {
                                stream.close();
                            } catch (IOException e) {
                                Log.e("SpywareDetector", "Error closing ADB stream", e);
                            }
                        }
                    }
                    // If the command is not executed successfully, retry
                    if (!success) {
                        retryCount++;
                        Log.d("SpywareDetector", "Retry attempt " + retryCount + " for command: " + command);
                        try {
                            Thread.sleep(1000); // Sleep for 1 second before retrying
                        } catch (InterruptedException e) {
                            Log.e("SpywareDetector", "Retry sleep interrupted", e);
                        }
                    }
                }
                // If the command is not executed successfully after the maximum number of retries, log an error
                if (!success) {
                    Log.e("SpywareDetector", "Failed to execute command after " + maxRetries + " attempts: " + command);
                }
            });
            // Start the thread
            commandThread.start();
            try {
                commandThread.join(); // Wait for the thread to complete before returning the result
            } catch (InterruptedException e) {
                Log.e("SpywareDetector", "Command thread interrupted", e);
            }
            return detectedPackages; // Return the list of app Ids
        }


        /**
         * Fetch the metadata of the app from the target device
         * @param context - The context of the application
         * @param packageName - The package name of the app
         * @return The metadata of the app
         **/                    
        private Map<String, String> fetchAppMetadataFromTarget(Context context, String packageName) {
            Map<String, String> appMetadata = new HashMap<>();
            CountDownLatch latch = new CountDownLatch(1);

            // Create a new thread to fetch the app metadata
            Thread metadataThread = new Thread(() -> {
                try {
                    // If the ADB connection is not established, establish it
                    if (!isAdbConnected) {
                        if (!establishAdbConnection(context)) {
                            Log.e("SpywareDetector", "Failed to establish ADB connection");
                            return;
                        }
                    }

                    // Prepare the ADB commands to fetch the app metadata
                    String labelCommand = "dumpsys package " + packageName + " | grep \"label=\""; // Command to fetch the app name
                    String installerCommand = "pm list packages -i | grep " + packageName; // Command to fetch the installer package name

                    // Execute the ADB commands and fetch the app metadata
                    String labelOutput = executeAdbCommand(labelCommand); // Output of the app name command
                    String installerOutput = executeAdbCommand(installerCommand); // Output of the installer package name command

                    // Extract the app name from the label output
                    if (labelOutput.contains("label=")) {
                        String label = labelOutput.split("label=")[1].trim();
                        appMetadata.put("name", label);
                    }

                    // Extract the installer package name from the installer output
                    if (installerOutput.contains("installer=")) {
                        String installer = installerOutput.split("installer=")[1].trim();
                        appMetadata.put("installer", installer);
                    }

                    // Handle fetching of other app metadata here (i.e. permissions, icon, etc.)

                } finally {
                    latch.countDown(); // Signal the latch that the thread has completed
                }
            });
            metadataThread.start(); // Start the thread
            try {
                latch.await(); // Wait for the thread to complete before returning the result
            } catch (InterruptedException e) {
                Log.e("SpywareDetector", "Metadata thread interrupted", e);
            }
            return appMetadata;
        }

    
        /**
         * Execute the ADB command and return the output
         * @param command - The ADB command to execute
         * @return The output of the command
         **/
        private static String executeAdbCommand(String command) {
            StringBuilder output = new StringBuilder(); // StringBuilder to store the output
            boolean success = false; // Flag to check if the command is executed successfully
            int retryCount = 0; // Retry count
            final int maxRetries = 3; // Maximum number of retries

            // Execute the command until it's successful or the maximum number of retries is reached
            while (!success && retryCount < maxRetries) {
                try {
                    AdbStream stream = adbConnection.open("shell:" + command); // Open the stream
                    while (!stream.isClosed()) {
                        try {
                            byte[] data = stream.read(); // Read the stream
                            if (data == null) {
                                Log.d("SpywareDetector", "No more data to read, closing stream.");
                                success = true;
                                break;
                            }
                            output.append(new String(data, StandardCharsets.US_ASCII)); // Append the output to the StringBuilder
                        } catch (IOException e) {
                            Log.e("SpywareDetector", "Error reading from stream, retrying...", e);
                            break;
                        }
                    }
                    stream.close(); // Close the stream after the command is executed
                    success = true; // Mark as successful if stream closes normally
                } catch (IOException | InterruptedException e) {
                    Log.e("SpywareDetector", "Error executing ADB command, retrying...", e);
                }

                if (!success) {
                    retryCount++; // Increment the retry count
                    Log.d("SpywareDetector", "Retry attempt " + retryCount + " for command: " + command);
                    try {
                        Thread.sleep(1000); // Sleep for 1 second before retrying
                    } catch (InterruptedException e) {
                        Log.e("SpywareDetector", "Retry sleep interrupted", e);
                    }
                }
            }

            // If the command is not executed successfully after the maximum number of retries, log an error
            if (!success) {
                Log.e("SpywareDetector", "Failed to execute command after " + maxRetries + " attempts: " + command);
            }

            return output.toString(); // Return the output of the command as a String
        }

        /**
         * Establish the ADB connection to the target device
         * @param context - The context of the application
         * @return True if the connection is established successfully, false otherwise
         **/
        private boolean establishAdbConnection(Context context) {
            mManager = (UsbManager) context.getSystemService(Context.USB_SERVICE); // Get the USB manager

            // Generate the ADB key pair (public and private keys to authenticate the connection) if it's not already generated
            try {
                if (adbCrypto == null) {
                    adbCrypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
                    @Override
                    public String encodeToString(byte[] data) {
                        return Base64.encodeToString(data, Base64.NO_WRAP);
                    }
                });
            }
            // Catch any exceptions that occur while generating the ADB key pair
        } catch (NoSuchAlgorithmException e) {
            Log.e("SpywareDetector", "Error generating ADB key pair", e);
            return false;
        }

        // Iterate through all the devices in the USB manager
        for (String k : mManager.getDeviceList().keySet()) {
            UsbDevice usbDevice = mManager.getDeviceList().get(k); // Get the USB device
            if (mManager.hasPermission(usbDevice)) { // Check if the device has permission
                UsbInterface intf = findAdbInterface(usbDevice); // Find the ADB interface on the device
                if (intf != null) {
                    try {
                        if (setAdbInterface(usbDevice, intf)) { // Set the ADB interface
                            Log.d("SpywareDetector", "ADB interface set successfully");
                            isAdbConnected = true; // Set the flag
                            return true; // Return true if the connection is established successfully
                        } 
                        else { // Log an error if the ADB interface is not set
                            Log.e("SpywareDetector", "Failed to set ADB interface");
                        }
                    } catch (IOException | InterruptedException e) { // Catch any exceptions that occur while setting the ADB interface
                        Log.e("SpywareDetector", "Error setting ADB interface", e);
                    }
                } else { // Log an error if no ADB interface is found
                    Log.e("SpywareDetector", "No ADB interface found");
                }
            } else { // Request permission if the device does not have permission
                Log.d("SpywareDetector", "Requesting USB permission");
                mManager.requestPermission(usbDevice, PendingIntent.getBroadcast(context, 0, new Intent("com.htetznaing.adbotg.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE));
            }
        }
        isAdbConnected = false; // Reset the flag if connection fails
        return false;
    }
}
