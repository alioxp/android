package com.example.lishan.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import android.util.Log;

import com.wilko.TTFTP.TTFTPProcess;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("aaa", "Haha , this is a INFO of MyAndroid. ");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.i("bbb", "Haha , this is a INFO of MyAndroid. ");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //sendCardTest();
        getfile();

        int id = item.getItemId();
        Log.i("ccc", "Haha , this is a INFO of MyAndroid. ");
        //noinspection SimplifiableIfStatement
       // if (id == R.id.action_settings) {
       //     return true;
       // }

        return super.onOptionsItemSelected(item);
    }

    public boolean getfile()
    {
        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    //TFTPSession.TFTP_PORT = 30000;
                    TTFTPProcess t = new TTFTPProcess();
                    t.tftpGet("224.0.1.88","aaa.txt",openFileOutput("1.txt", MODE_PRIVATE)); //new FileOutputStream(file););
                }
                catch(Exception e){//已经读完文档
                    Log.i("abcerr",e.getMessage());
                }
            }
        });
        thread.start();
        return true;
    }
    public boolean sendCardTest()
    {
        Thread thread=new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    Log.i("aaa1", "Haha , this is a INFO of MyAndroid. ");
                    //           byte[] buff = "QQ".getBytes("utf-8");//设定报文信息
                    Log.i("aaa2", "Haha , this is a INFO of MyAndroid. ");
                    //           DatagramSocket socket=new DatagramSocket();//建立套接字，参数端口号不填写，系统会自动分配一个可用端口
//创建报文，包括报文内容，内容长度，报文地址（这里全1地址即为广播），端口号（接受者需要使用该端口）
                    Log.i("aaa3", "Haha , this is a INFO of MyAndroid. ");
                    //          DatagramPacket packet=new DatagramPacket(buff,buff.length,InetAddress.getByName("255.255.255.255"), 30000);
                    Log.i("aaa4", "Haha , this is a INFO of MyAndroid. ");
                    //          socket.send(packet);//发送报文
                    Log.i("aaa5", "Haha , this is a INFO of MyAndroid. ");
                    //          socket.disconnect();//断开套接字
                    //          socket.close();//关闭套接字
                    Log.i("abc", "Haha , this is a INFO of MyAndroid. ");

                    MulticastSocket mSocket = new MulticastSocket(30000);//生成套接字并绑定30001端口
                    InetAddress group=InetAddress.getByName("224.0.1.88");//设定多播IP
                    byte[] buff = "QQ".getBytes("utf-8");//设定多播报文的数据
                    mSocket.joinGroup(group);//加入多播组，发送方和接受方处于同一组时，接收方可抓取多播报文信息
                    mSocket.setTimeToLive(4);//设定TTL
//设定UDP报文（内容，内容长度，多播组，端口）
                    DatagramPacket packet = new DatagramPacket(buff,buff.length,group,30000);
                    mSocket.send(packet);//发送报文
                    mSocket.close();//关闭套接字

                    setTitle("udp sent");
                }
                catch(Exception e){//已经读完文档
                    Log.i("abcerr",e.getMessage());
                }
            }
        });
        thread.start();
        return true;
    }
}
