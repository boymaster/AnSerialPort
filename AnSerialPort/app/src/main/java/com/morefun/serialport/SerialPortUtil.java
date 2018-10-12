package com.morefun.serialport;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;

/**
 * @author by AllenJ on 2018/4/20.
 *
 * 通过串口用于接收或发送数据
 */

public class SerialPortUtil {
    private static final String LOG_TAG = "SerialPortUtil";
    private SerialPort serialPort = null;
    private InputStream inputStream = null;
    private OutputStream outputStream = null;
    private ReceiveThread mReceiveThread = null;
    private boolean isStart = false;

    /**
     * 打开串口，接收数据
     * 通过串口，接收单片机发送来的数据
     */
    public boolean openSerialPort(String comport, int baudrate, int flags) {
        try {
            Log.i(LOG_TAG, "openSerialPort: " + comport
                    + "," + Integer.toString(baudrate)
                    + "," + Integer.toString(flags));
            serialPort = new SerialPort(new File(comport), baudrate, flags);
            //调用对象SerialPort方法，获取串口中"读和写"的数据流
            inputStream = serialPort.getInputStream();
            outputStream = serialPort.getOutputStream();
            isStart = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        getSerialPort();
        return isStart;
    }

    /**
     * 关闭串口
     * 关闭串口中的输入输出流
     */
    public void closeSerialPort() {
        Log.i(LOG_TAG, "closeSerialPort");
        try {
            //mReceiveThread.interrupt();
            //mReceiveThread.join();
            isStart = false;
            Thread.sleep(100);
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送数据
     * 通过串口，发送数据到单片机
     *
     * @param data 要发送的数据
     */
    public void sendSerialPort(String data, boolean isHex) {
        Log.i(LOG_TAG, "sendSerialPort: " + data);
        try {
            byte[] sendData;
            if (isHex) {
                sendData = DataUtils.HexToByteArr(data);
            } else {
                sendData = data.getBytes();
            }
            outputStream.write(sendData);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getSerialPort() {
        mReceiveThread = new ReceiveThread();
        mReceiveThread.start();
    }

    /**
     * 接收串口数据的线程
     */

    private class ReceiveThread extends Thread {
        @Override
        public void run() {
            super.run();
            //条件判断，只要条件为true，则一直执行这个线程
            while (isStart) {
                try {
                    if ( (inputStream !=null)
                            && (inputStream.available() > 0) )
                    {
                        Log.i(LOG_TAG, "readData begin ########");
                        byte[] readData = new byte[1024];
                        try {
                            int size = inputStream.read(readData);
                            if (size > 0) {
                                String readString = DataUtils.ByteArrToHex(readData, 0, size);
                                EventBus.getDefault().post(readString);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i(LOG_TAG, "readData end ########");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            Log.i(LOG_TAG, "ReceiveThread end.");
        }
    }

}
