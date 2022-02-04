package com.test_progect.mvno_linphone_demo.core_ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

interface CoroutineJobDelegate : CoroutineScope {

    fun initializeCoroutineJob()
    fun cancelCoroutineJob()

}

class CoroutineJobDelegateImpl : CoroutineJobDelegate {

    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun initializeCoroutineJob() {
        if (!::job.isInitialized || job.isCancelled) job = SupervisorJob()
    }

    override fun cancelCoroutineJob() {
        if (::job.isInitialized) job.cancel()
    }

}