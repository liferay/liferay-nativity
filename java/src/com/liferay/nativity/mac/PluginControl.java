/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */


package com.liferay.nativity.mac;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class PluginControl {
    private Socket serviceSocket;
    private DataInputStream serviceInputStream;
    private DataOutputStream serviceOutputStream;

    private Socket callbackSocket;
    private DataInputStream callbackInputStream;
    private DataOutputStream callbackOutputStream;
    private ReadThread callbackThread = null;
    private String[] currentFiles;

    private class ReadThread extends Thread {
        private PluginControl control;

        public ReadThread(PluginControl ctl)
        {
               control = ctl;
        }

        @Override
        public void run() {
            control.DoCallbackLoop();
        }
    }

    private void DoCallbackLoop()
    {
        while (callbackSocket.isConnected())
        {
            try {
                String data = callbackInputStream.readLine();

                if (data.startsWith("menuQuery:"))
                {
                    currentFiles = data.substring(10, data.length()).split(":");

                    String[] items = getMenuItems(currentFiles);
                    String itemsStr = new String();
                    if (items != null)
                    {
                        for (int i=0;i<items.length;++i)
                        {
                            if (i > 0)
                                itemsStr += ":";

                            itemsStr += items[i];
                        }
                    }
                    callbackOutputStream.writeBytes(itemsStr+"\r\n");
                }
                if (data.startsWith("menuExec:"))
                {
                    menuItemExecuted(Integer.parseInt(data.substring(9, data.length())),currentFiles);
                }

            } catch (IOException e)
            {
            }
        }
    }

    /**
     * Callback method that executes when user selects custom menu item
     * @param index index of menu item (index in the array returned by previous getMenuItems call)
     * @param files array on which context menu item executed
     */
    protected void menuItemExecuted(int index, String[] files) {
    }

    /**
     * Callback method called by native plugin when context menu executed on one or more files
     * User code can override this method to add a number of additional items to context menu
     * @param files array of file names on which context menu executed
     * @return array of menu items that should be added to context menu, or null if additional context menu not needed
     */
    protected String[] getMenuItems(String[] files) {
        return null;
    }

    /**
     * Initialize connection with native service
     * @return true if connection is successfull
     */
    public boolean connect() {
        try {
            serviceSocket = new Socket("127.0.0.1",33001);
            serviceInputStream = new DataInputStream(serviceSocket.getInputStream());
            serviceOutputStream = new DataOutputStream(serviceSocket.getOutputStream());

            callbackSocket = new Socket("127.0.0.1",33002);
            callbackInputStream = new DataInputStream(callbackSocket.getInputStream());
            callbackOutputStream = new DataOutputStream(callbackSocket.getOutputStream());

            callbackThread = new ReadThread(this);
            callbackThread.start();

        } catch (IOException e) {
            return false;
        }

        return true;
    }

    /**
     * disconnects from plugin service
     */
    public void disconnect() {
        try {
            serviceSocket.close();
        }
        catch (IOException e) {
        }
    }

    /**
     * Enable/Disable icon overlay feature.
     * @param enable pass true is overlay feature should be enabled
     */
    public void enableOverlays(boolean enable) {
        try {
            String cmd = new String("enableOverlays:" + (enable ? "1" : "0") + "\r\n");
            serviceOutputStream.writeBytes(cmd);
            serviceInputStream.readLine();
        } catch (IOException e) {
        }
    }

    /**
     * Register icon in the service
     * @param path to icon file
     * @return registered icon id or 0 in case error
     */
    public int registerIcon(String path) {
        String cmd = new String("registerIcon:" + path + "\r\n");
        try {
            serviceOutputStream.writeBytes(cmd);
            String reply = serviceInputStream.readLine();
            return Integer.parseInt(reply);
        } catch (IOException e) {
            return 0;
        }
    }

    /**
     * Unregister icon in the service
     * @param id of icon previously registered by registerIcon method
     */
    public void unregisterIcon(int id)
    {
        String cmd = new String("unregisterIcon:" + id + "\r\n");
        try {
            serviceOutputStream.writeBytes(cmd);
            String reply = serviceInputStream.readLine();
        } catch (IOException e) {
        }
    }

    /**
     * Associate icon with fileName
     * @param fileName target file name
     * @param iconId id of icon that should be associated with file
     */
    public void setIconForFile(String fileName, int iconId)
    {
        String cmd = new String("setFileIcon:" + fileName + ":" + iconId + "\r\n");
        try {
            serviceOutputStream.writeBytes(cmd);
            String reply = serviceInputStream.readLine();
        } catch (IOException e) {
        }
    }

    /**
     * Set title of root context menu item, all other items will be added as children of it
     * @param title new title of item
     */
    public void setContextMenuTitle(String title)
    {
        String cmd = new String("setMenuTitle:" + title + "\r\n");
        try {
            serviceOutputStream.writeBytes(cmd);
            String reply = serviceInputStream.readLine();
        } catch (IOException e) {
        }
    }

    /**
     * Remove icon overlay from file (previously set by setIconForFile)
     * @param fileName name of file
     */
    public void removeFileIcon(String fileName)
    {
        String cmd = new String("remove  FileIcon:" + fileName + "\r\n");
        try {
            serviceOutputStream.writeBytes(cmd);
            String reply = serviceInputStream.readLine();
        } catch (IOException e) {
        }
    }

   