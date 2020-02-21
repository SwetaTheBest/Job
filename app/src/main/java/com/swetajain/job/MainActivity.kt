package com.swetajain.job

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import java.util.concurrent.CancellationException


class MainActivity : AppCompatActivity() {

    private val PROGRESS_START = 0
    private val PROGRESS_END = 100
    private val JOB_TIME = 4000
    private lateinit var job: CompletableJob


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start_button.setOnClickListener {
            if (!::job.isInitialized) {
                initJob()
            }

            job_progress_bar.startJobOrCancel(job)
        }

    }

    private fun ProgressBar.startJobOrCancel(job: Job) {
        if (this.progress > 0) {
            println("$job is already active! Cancelling ...")
            resetJob()
        }else {
            start_button.text = "Cancel Job!"
            CoroutineScope(IO + job).launch {
                println("Coroutine $this is active with the job: $job")
                for (i in PROGRESS_START..PROGRESS_END){
                       delay((JOB_TIME/PROGRESS_END).toLong())
                    this@startJobOrCancel.progress = i
                }
                    updateJobCompleteTextView("Job is complete!")

            }
        }
    }

    private fun updateJobCompleteTextView(text: String) {
        CoroutineScope(Main).launch {
            text_view.text = text
        }
    }

    private fun resetJob() {
        if (job.isActive || job.isCompleted){
            job.cancel(CancellationException("Resetting job"))
        }
        initJob()
    }

    private fun initJob() {
        start_button.text = getString(R.string.start_job)
        Log.d("JOB", "inside init job")
        updateJobCompleteTextView("")

       // text_view.setText("Job started")
        job = Job()
        job.invokeOnCompletion {
            it?.message.let {
                var msg = it
                if (msg.isNullOrBlank()) {
                    msg = "unknown cancellation error!"
                }
                println("$job was cancelled! Reason: $msg")
                showToast(msg)
                text_view.text = "$job was cancelled! Reason: $msg"
            }
        }
        job_progress_bar.max = PROGRESS_END
        job_progress_bar.progress = PROGRESS_START


    }

    private fun showToast(s: String) {
        CoroutineScope(Main).launch {
            Toast.makeText(
                this@MainActivity,
                "Toast: $s",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}
