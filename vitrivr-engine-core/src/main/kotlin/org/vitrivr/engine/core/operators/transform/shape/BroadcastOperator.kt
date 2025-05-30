package org.vitrivr.engine.core.operators.transform.shape

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import org.vitrivr.engine.core.operators.Operator

/**
 * A [Operator.Unary] that broadcasts the results of a single [Operator] to multiple consumers.
 *
 * @author Ralph Gasser
 * @version 1.0.0
 */
class BroadcastOperator<I>(override val input: Operator<I>, override val name: String = "broadcast") : Operator.Unary<I, I> {

    /** Reference to the [SharedFlow] backing this [BroadcastOperator]. */
    private var sharedFlow: SharedFlow<I>? = null

    /**
     * Generates a [SharedFlow] from this [BroadcastOperator].
     */
    @Synchronized
    override fun toFlow(scope: CoroutineScope): SharedFlow<I> {
        if (this.sharedFlow == null) {
            val parentScope = CoroutineScope(scope.coroutineContext)
            this.sharedFlow = this.input.toFlow(parentScope).shareIn(parentScope, SharingStarted.Lazily, 0)
        }
        return this.sharedFlow!!
    }
}
