/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UnnecessaryAbstractClass")  // No idea why we're getting this

package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.annotations.ExtensionDSL
import com.kotlindiscord.kord.extensions.commands.events.CommandEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.Lockable
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

/**
 * Abstract base class representing the few things that command objects can have in common.
 *
 * This should be used as a base class only for command types that aren't related to the other command types.
 *
 * @property extension The extension object this command belongs to.
 */
@ExtensionDSL
public abstract class Command(public val extension: Extension) : Lockable {
    /**
     * The name of this command, for invocation and help commands.
     */
    public open lateinit var name: String

    /** Set this to `true` to lock command execution with a Mutex. **/
    public override var locking: Boolean = false

    override var mutex: Mutex? = null

    /**
     * An internal function used to ensure that all of a command's required arguments are present and correct.
     *
     * @throws InvalidCommandException Thrown when a required argument hasn't been set or is invalid.
     */
    @Throws(InvalidCommandException::class)
    public open fun validate() {
        if (!::name.isInitialized || name.isEmpty()) {
            throw InvalidCommandException(null, "No command name given.")
        }

        if (locking && mutex == null) {
            mutex = Mutex()
        }
    }

    /** Quick shortcut for emitting a command event without blocking. **/
    public open suspend fun emitEventAsync(event: CommandEvent<*, *>): Job =
        event.launch {
            extension.bot.send(event)
        }
}
