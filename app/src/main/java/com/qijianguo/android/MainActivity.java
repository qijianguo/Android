package com.qijianguo.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // getMenuInflater能够得到MunuINflater对象，再调用inflate就可以创建菜单了
        // inflate参数： 1. 指定哪个资源文件来创建菜单（这里是res->menu->main.xml） 2.onCreateOptionsMenu的参数
        // 返回值： true创建菜单成功，反之亦然
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    /**
     * 创建Activity
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: ");
        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            /**
             * param1: Context,即Toast要求的上下文
             * param2: Toast显示的文本
             * param3： Toast显示的时间
             */
            public void onClick(View view) {
                // Toast 提示
                // Toast.makeText(MainActivity.this, "you clicked button", Toast.LENGTH_SHORT).show();

                // 显式Intent
                Intent intent = new Intent(MainActivity.this, FirstActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item:
                Toast.makeText(MainActivity.this, "add_item is clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.remove_item:
                Toast.makeText(MainActivity.this, "remove_item is clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.exit:
                finish();
            default:
                Toast.makeText(MainActivity.this, "........", Toast.LENGTH_SHORT).show();
        }
        return true;
    }




}
