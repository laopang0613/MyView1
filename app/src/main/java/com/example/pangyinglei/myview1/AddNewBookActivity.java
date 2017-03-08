package com.example.pangyinglei.myview1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AddNewBookActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView phoneMemoryTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_book);
        init();
    }

    private void init(){
        phoneMemoryTv = (TextView)findViewById(R.id.addnewbook_tv);
        phoneMemoryTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setClass(this,FileScanActivity.class);
        this.startActivity(intent);
    }
}
