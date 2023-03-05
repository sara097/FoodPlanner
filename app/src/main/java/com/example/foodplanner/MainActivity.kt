package com.example.foodplanner

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.foodplanner.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.log.LogLevel
import io.realm.log.RealmLog
import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.Credentials
import io.realm.mongodb.User
import io.realm.mongodb.mongo.MongoClient
import io.realm.mongodb.mongo.MongoCollection
import io.realm.mongodb.mongo.MongoDatabase
import io.realm.mongodb.mongo.options.InsertManyResult
import org.bson.Document


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

//    companion object{
//        init {
//            System.loadLibrary("mongodbrealmforandroid")
//        }
//    }

    private var appId = "foodplanner-odvwn"

    //MongoDb fields
    private var app: App? = null
    private var mongoDatabase: MongoDatabase? = null
    private var mongoClient: MongoClient? = null
    private var mongoCollection: MongoCollection<Document>? = null
    private var user: User? = null

    //Views
    lateinit var containerView: LinearLayout
    lateinit var runTestBtn: Button
    lateinit var readBtn: Button
    lateinit var progressBar: ProgressBar
    lateinit var accMeterReading: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        containerView = findViewById(R.id.container)
        runTestBtn = findViewById(R.id.test_btn)
        readBtn = findViewById(R.id.read_btn)
        progressBar = findViewById(R.id.progress_bar)
        accMeterReading = findViewById(R.id.acc_meter_reading)
        // Important: should be initialized only once
        Realm.init(this)

//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)

//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)
//
//        binding.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }

        app = App(AppConfiguration.Builder(appId).build())

        val credential = Credentials.anonymous()
//        setupRealm()
        app!!.loginAsync(
            credential
        ) { result ->
            if (result.isSuccess) {
                showStatus("User Logged In Successfully")

                //current user
                user = app!!.currentUser()


                mongoClient = user!!.getMongoClient("mongodb-atlas")    //same for everyone

                //database name and collection name which we have created in mongoDB account
                mongoDatabase = mongoClient!!.getDatabase("TestDB")
                mongoCollection = mongoDatabase!!.getCollection("TestC")


                /** Uncomment to perform any query */
//                insertData()          // 10000 data
//                readData()            // filtered data, sample6
//                updateData()          // update, sample6 -> new sample
//                countData()           //count new sample data
//                deleteData()          //delete all
            } else {
                showStatus("User Failed to Login: ${result.error}")
            }
        }

        runTestBtn.setOnClickListener {
            insertData()
        }
        readBtn.setOnClickListener {
            readData()
        }
    }


    private fun setupRealm() {
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name("TestDB.db")
            .allowQueriesOnUiThread(true)
            .allowWritesOnUiThread(true)
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)

        if (BuildConfig.DEBUG)
            RealmLog.setLevel(LogLevel.ALL)
    }

    private fun insertData() {
        progressBar.visibility = VISIBLE
        runTestBtn.isClickable = false
        readBtn.isClickable = false


        //inserting data as documents: (Key - Value) pair
        val sampleData: ArrayList<String> = arrayListOf(
            "sample1",
            "sample2",
            "sample3",
            "sample4",
            "sample5",
            "sample6",
            "sample7",
            "sample8",
            "sample9",
            "sample10"
        )

        val list = mutableListOf<Document>()

        val count = 10
        for (i in 1..count) {
            sampleData.shuffle()
            list.add(
                Document("userid", user!!.id).append(
                    "data",
                    sampleData[0]
                )
            )
        }

        // insert query: start time
        val start = System.nanoTime()
        showStatus("Insert Query started for 10000 sample data")

        mongoCollection?.insertMany(list)
            ?.getAsync { r: App.Result<InsertManyResult?> ->
                if (r.isSuccess) {
                    showStatus("$count Data Inserted Successfully")

                    // insert query: end time
                    val end = System.nanoTime()
                    showStatus("Time taken to insert $count data: " + (end - start).toString() + " nanoseconds")
                    progressBar.visibility = GONE
                    runTestBtn.isClickable = true
                    readBtn.isClickable = true
                } else {
                    showStatus("Error in inserting data: " + r.error.toString())
                    progressBar.visibility = GONE
                    runTestBtn.isClickable = true
                    readBtn.isClickable = true
                }
            }
    }


    // reading with condition: data == sample6
    private fun readData() {
        progressBar.visibility = VISIBLE
        runTestBtn.isClickable = false
        readBtn.isClickable = false
        showStatus("started read query for data : sample6")

        val start = System.nanoTime()

        val queryFilter = Document("data", "sample6")
        val findTask = mongoCollection?.find(queryFilter)?.iterator()
        findTask?.getAsync { task ->
            if (task.isSuccess) {
                val results = task.get()
                var count = 0
                while (results.hasNext()) {
                    results.next()
                    count++
                }
                showStatus("successfully found all sample6 data, $count occurrence")
                showStatus("Get Query: Time taken = " + (System.nanoTime() - start).toString() + " nanoseconds for $count data")
                progressBar.visibility = GONE
                runTestBtn.isClickable = true
                readBtn.isClickable = true
            } else {
                showStatus("Error in finding: ${task.error}")
                progressBar.visibility = GONE
                runTestBtn.isClickable = true
                readBtn.isClickable = true
            }
        }
    }


    //delete all data of current user
    private fun deleteData() {
        showStatus("delete all query started")
        val queryFilter = Document("userid", "62b91eec822c2bddbd44229c")
        mongoCollection?.deleteMany(queryFilter)?.getAsync { task ->
            if (task.isSuccess) {
                val count = task.get().deletedCount
                if (count != 0L) {
                    showStatus("successfully deleted $count documents.")
                } else {
                    showStatus("did not delete any documents.")
                }
            } else {
                "failed to delete documents with error: ${task.error}"
            }
        }
    }

    //count data with condition: data == sample2
    private fun countData() {
        showStatus("counting started for sample2 data")
        val queryFilter = Document("data", "sample2")
        mongoCollection?.count(queryFilter)?.getAsync { task ->
            if (task.isSuccess) {
                val count = task.get()
                showStatus("successfully counted, number of documents in the collection: $count")
            } else {
                showStatus("failed to count documents with: ${task.error}")
            }
        }
    }

    //update query: sample7 -> new sample
    private fun updateData() {
        showStatus("update query started: sample7 -> new sample")
        progressBar.visibility = VISIBLE

        val queryFilter = Document("data", "sample7")
        val updateDocument = Document("\$set", Document("data", "new sample"))

        val start = System.nanoTime()

        mongoCollection?.updateMany(queryFilter, updateDocument)?.getAsync { task ->
            if (task.isSuccess) {
                progressBar.visibility = GONE
                val count = task.get().modifiedCount
                if (count != 0L) {
                    showStatus("successfully updated $count documents.")
                    showStatus("Time Taken: ${System.nanoTime() - start} nanoseconds for $count data")
                } else {
                    showStatus("did not update any documents.")
                }
            } else {
                showStatus("failed to update documents with error: ${task.error}")
            }
        }
    }


    private fun showStatus(text: String) {
        Log.i("Status", text)
        val textView = TextView(this)
        textView.setPadding(10, 10, 10, 10)
        textView.text = text
        containerView.addView(textView)
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}