package com.angcyo.animcheckviewdemo

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    var subscribe: Subscription? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val tc: AnimCheckView = findViewById(R.id.check_0)
        val lc1: AnimCheckView = findViewById(R.id.lc1)
        val rc1: AnimCheckView = findViewById(R.id.rc1)

        button_end.setOnClickListener {
            subscribe?.unsubscribe()
            subscribe = Observable.just("")
                .delay(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    lc1.drawGradientProgress.progressStatus = RDrawGradientProgress.STATUS_SUCCEED
                }
                .delay(2, TimeUnit.SECONDS)
                .map {
                    rc1.drawGradientProgress.progressStatus = RDrawGradientProgress.STATUS_SUCCEED
                }
                .delay(2, TimeUnit.SECONDS)
                .map {
                    tc.drawGradientProgress.progressStatus = RDrawGradientProgress.STATUS_SUCCEED
                }
                .subscribe()
        }

        button_restart.setOnClickListener {
            subscribe?.unsubscribe()

            tc.drawGradientProgress.progressStatus = RDrawGradientProgress.STATUS_GRADIENT
            lc1.drawGradientProgress.progressStatus = RDrawGradientProgress.STATUS_GRADIENT
            rc1.drawGradientProgress.progressStatus = RDrawGradientProgress.STATUS_GRADIENT
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
