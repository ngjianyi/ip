package dudu.command;

import java.io.IOException;
import java.util.ArrayList;

import dudu.task.Task;
import dudu.utils.Storage;
import dudu.utils.TaskList;
import dudu.utils.UI;


/**
 * Represents a find user command into the chatbot
 */
public class CommandFind extends Command {
    private String query;

    public CommandFind(String query) {
        this.query = query;
    }


    @Override
    public void execute(TaskList taskList, UI ui, Storage storage) throws IOException {
        ArrayList<Task> filteredTasks = taskList.findTasks(this.query);
        ui.findTasks(filteredTasks);
        storage.rewriteFile(taskList);
    }

    @Override
    public boolean isExit() {
        return false;
    }
}
