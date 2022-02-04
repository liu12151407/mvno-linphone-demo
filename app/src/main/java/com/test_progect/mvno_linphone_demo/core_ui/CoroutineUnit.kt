package ru.tcsbank.mvno.coroutines

import androidx.annotation.CheckResult
import com.test_progect.mvno_linphone_demo.core_ui.tryLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

typealias CoroutineAction = suspend CoroutineScope.() -> Unit
typealias CoroutineFinally = () -> Unit
typealias CoroutineError = Throwable.() -> Unit

/**
 * Контейнер для лямбд, исполняемых в корутине. В явном виде, как правило, использовать не нужно.
 * Можно пользоваться расширениями [onLaunch], [onFinish] и [onError].
 *
 * Пример использования:
 * ```
 * scope.onLaunch {
 *  // suspend code
 * } onError {
 *  // error handling
 * } onFinish {
 *  // finalization
 * }
 * ```
 */
data class CoroutineUnit(
    val scope: CoroutineScope,
    val coroutineAction: CoroutineAction,
    val coroutineFinally: CoroutineFinally? = null
)

/**
 * Возвращает CoroutineUnit с указанным CoroutineAction. Не запускает корутину.
 *
 * @see onError
 */
@CheckResult
fun CoroutineScope.onLaunch(
    action: CoroutineAction
): CoroutineUnit =
    CoroutineUnit(this, action)

infix fun Job.onFinish(finally: CoroutineFinally): Job =
    apply {
        invokeOnCompletion { finally() }
    }

/**
 * Терминальная операция для CoroutineUnit, запускает корутину с его содержимым.
 * В качестве обработчика ошибок используется [doOnError].
 */
infix fun CoroutineUnit.onError(doOnError: CoroutineError): Job =
    scope.tryLaunch(coroutineAction, doOnError, coroutineFinally)