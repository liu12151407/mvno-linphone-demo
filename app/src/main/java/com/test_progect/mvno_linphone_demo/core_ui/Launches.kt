package com.test_progect.mvno_linphone_demo.core_ui

import kotlinx.coroutines.*

fun CoroutineScope.tryLaunch(
    doOnLaunch: suspend CoroutineScope.() -> Unit,
    doOnError: Throwable.() -> Unit,
    doFinally: (() -> Unit)? = null,
): Job =
    launch(
        CoroutineExceptionHandler { _, error ->
            error.doOnError()
        }
    ) {
        supervisorScope { doOnLaunch() }
    }
        .apply {
            doFinally?.let { doFinally ->
                invokeOnCompletion {
                    doFinally()
                }
            }
        }

fun Throwable.checkCancellation() {
    if (this is CancellationException) throw this
}