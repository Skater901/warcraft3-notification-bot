package au.com.skater901.wc3.api.scheduled

/**
 * A scheduled task that will be run on the provided schedule.
 */
public interface ScheduledTask {
    /**
     * The frequency this task will be run, in seconds. IE, a value of 1 means the task will be run every second.
     */
    public val schedule: Int

    /**
     * The task to run.
     */
    public suspend fun task()
}