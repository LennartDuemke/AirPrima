package com.softwareengineering.airprimaapp.menues;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.softwareengineering.airprimaapp.R;

/**
 * Activity that lets the user choose between the product information and the particulate matter information.
 */
public class InformationActivity extends AppCompatActivity {

    /**
     * Setup for the activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        // Find view instances
        LinearLayout productinfoLayout = findViewById(R.id.information_productinfo);
        LinearLayout finedustinfoLayout = findViewById(R.id.information_finedustinfo);

        // Register event listeners
        productinfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformationActivity.this, ProductInformationActivity.class);
                startActivity(intent);
            }
        });

        finedustinfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InformationActivity.this, ParticulateMatterInfoActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Create the options menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Change the options menu (Disable item)
     */
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        menu.findItem(R.id.item_info).setVisible(false);
        return true;
    }

    /**
     * Handling click events of menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_home:    // Home button
                Intent homeIntent = new Intent(this, MainActivity.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
                return true;
            case R.id.item_info:    // Information button
                Intent informationIntent = new Intent(this, InformationActivity.class);
                startActivity(informationIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
