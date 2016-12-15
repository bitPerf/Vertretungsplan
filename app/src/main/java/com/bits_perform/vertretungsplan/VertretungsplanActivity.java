package com.bits_perform.vertretungsplan;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Xml;
import android.view.View;
import android.webkit.WebView;

public class VertretungsplanActivity extends AppCompatActivity {

    private static final String WOW = "EVERYTHING IS AWESOME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertretungsplan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String table = intent.getStringExtra(LoginActivity.TABLE);
        WebView tableView = (WebView) findViewById(R.id.tableView);
        tableView.loadData(table,"text/html", "ISO-8859-1");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    }
}
