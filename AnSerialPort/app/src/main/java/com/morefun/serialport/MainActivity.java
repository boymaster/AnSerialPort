package com.morefun.serialport;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android_serialport_api.SerialPortFinder;


public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private Button btnClear, btnSend;
    private Spinner spComport;
    private EditText edtComport, edtCommand;
    private Button btnComport;
    private CheckBox cbHexSend;
    private TextView tv;
    private SerialPortUtil serialPortUtil;
    private boolean isOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //edtComport = (EditText) findViewById(R.id.editText);
        spComport = (Spinner)findViewById(R.id.spinner);

        SerialPortFinder portFinder = new SerialPortFinder();
        String[] listComport = portFinder.getAllDevices();
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item, listComport);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spComport.setAdapter(adapter);

        edtCommand = (EditText) findViewById(R.id.editCmd);

        tv = (TextView) findViewById(R.id.tv);
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());

        cbHexSend = (CheckBox) findViewById(R.id.cbHexSend);

        serialPortUtil = new SerialPortUtil();
        //注册EventBus
        EventBus.getDefault().register(this);

        btnComport = (Button) findViewById(R.id.btnComport);
        btnComport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isOpened) {
                    serialPortUtil.closeSerialPort();
                    btnComport.setText(getString(R.string.btn_open));
                    setCtrlStatus(false);
                    isOpened = false;
                    return;
                }

                //String comport = edtComport.getText().toString();
                String comport = spComport.getSelectedItem().toString();
                int pos = comport.indexOf(" (");
                if ( pos > 0 ) {
                    comport = String.format("/dev/%s", comport.substring(0, pos));
                }
                if ( serialPortUtil.openSerialPort(comport, 115200, 0) ) {
                    btnComport.setText(getString(R.string.btn_close));
                    setCtrlStatus(true);
                    isOpened = true;
                }

            }
        });

        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cmd = edtCommand.getText().toString();
                serialPortUtil.sendSerialPort(cmd, cbHexSend.isChecked());
                if (cbHexSend.isChecked()) {
                    tv.append("SendHex(" + Integer.toString(cmd.length() / 2) + "):\n");
                } else {
                    tv.append("Send(" + Integer.toString(cmd.length()) + "):\n");
                }
                tv.append(cmd);
                tv.append("\n");
                tvGoBottom();
            }
        });

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText("");
            }
        });

        setCtrlStatus(false);
    }

    /**
     * 用EventBus进行线程间通信，也可以使用Handler
     * @param string
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(String string){
        tv.append("Recv(" + Integer.toString(string.length() / 2) + "):\n");
        tv.append(string);
        tv.append("\n");
        tvGoBottom();
    }

    private void tvGoBottom() {
        int offset = tv.getLineCount()*tv.getLineHeight();
        if(offset > tv.getHeight()){
            tv.scrollTo(0,offset-tv.getHeight());
        }
    }

    private void setCtrlStatus(boolean enabled) {
        btnSend.setEnabled(enabled);
        cbHexSend.setEnabled(enabled);
    }
}
