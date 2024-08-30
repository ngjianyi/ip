package dudu.command;

import dudu.command.Command;
import dudu.task.Task;
import dudu.utils.Storage;
import dudu.utils.TaskList;
import dudu.utils.UI;

import java.io.IOException;

public class CommandDeadline extends Command {
    Task task;

    /**
     * Constructs a CommandDeadline with the specified task.
     *
     * @param task The deadline task to be added.
     */
    public CommandDeadline(Task task) {
        this.task = task;
    }

    /**
     * Executes the command by adding the deadline task to the task list, updating
     * the user interface with the task details, and saving the updated task list to storage.
     *
     * @param taskList The task list on which the command is executed.
     * @param ui The user interface to interact with the user.
     * @param storage The storage to save the task.
     * @throws IOException If there is an error during saving the task to storage.
     */
    @Override
    public void execute(TaskList taskList, UI ui, Storage storage) throws IOException {
        int size = taskList.addTask(task);
        ui.addTask(task, size);
        storage.rewriteFile(taskList);
    }

    /**
     * Indicates that this command will not cause the application to exit.
     *
     * @return false, as this command always does not cause the application to exit.
     */
    @Override
    public boolean isExit() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || !(o instanceof CommandDeadline)) {
            return false;
        }
        CommandDeadline other = (CommandDeadline) o;
        return this.task.equals(other.task);
    }
}
