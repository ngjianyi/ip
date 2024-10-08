package dudu.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import dudu.exception.DuduException;
import dudu.exception.InvalidFormatException;
import dudu.task.Deadline;
import dudu.task.Event;
import dudu.task.Task;
import dudu.task.ToDo;

/**
 * Represents the class that reads and updates the local file of tasks.
 */
public class Storage {
    enum TaskType {
        T, D, E
    }

    private static final String fileReadingError = "Error reading the file";
    private static final String invalidFormatMessage = "Invalid format found";

    private String filePath;

    /**
     * Creates a Storage.
     *
     * @param filePath File path of the local file that contains existing tasks.
     */
    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Reads the existing tasks from a local file at the stored file path.
     *
     * @return List of tasks currently in the local file.
     */
    public ArrayList<Task> load() throws DuduException {
        File file = loadFile();
        return loadTask(file);
    }

    /**
     * Loads the file containing tasks stored locally.
     *
     * @return File containing tasks.
     * @throws DuduException If there is an error loading the file.
     */
    private File loadFile() throws DuduException {
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new DuduException(fileReadingError);
        }
        return file;
    }

    /**
     * Reads and collates each task from the local file.
     *
     * @param file File containing tasks stored locally.
     * @return List of tasks stored locally.
     * @throws DuduException If task is stored has invalid format.
     */
    private ArrayList<Task> loadTask(File file) throws DuduException {
        ArrayList<Task> tasks = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNext()) {
                String line = sc.nextLine();
                try {
                    addTask(line, tasks);
                } catch (InvalidFormatException e) {
                    throw new DuduException(fileReadingError);
                }
            }
        } catch (FileNotFoundException e) {
            throw new DuduException(fileReadingError);
        }
        return tasks;
    }

    /**
     * Adds a task represented by the line to a list of tasks.
     *
     * @param line Line containing task to be added in storage format.
     * @param tasks Current added lists of tasks.
     * @throws InvalidFormatException If line has invalid storage format.
     */
    private void addTask(String line, ArrayList<Task> tasks) throws InvalidFormatException {
        checkLineFormat(line);
        TaskType type = parseTaskType(line);
        switch (type) {
        case T:
            tasks.add(parseTodoTask(line));
            break;
        case D:
            tasks.add(parseDeadlineTask(line));
            break;
        case E: {
            tasks.add(parseEventTask(line));
            break;
        }
        default:
            System.out.println("Wrong task type was not detected earlier");
        }
    }

    /**
     * Checks that the line is has a correct command type, marked, and description.
     *
     * @param line Line containing a task from local file.
     * @throws InvalidFormatException If line is not of correct format specified previously.
     */
    private void checkLineFormat(String line) throws InvalidFormatException {
        if (!line.matches("^[TDE] \\| .+")) {
            throw new InvalidFormatException("Invalid task type found in file");
        }
        if (!line.matches("^[TDE] \\| [01] \\| .+")) {
            throw new InvalidFormatException("Format is [T/D/E] | [0/1] | [description]");
        }
    }

    /**
     * Retrieves task type from line.
     *
     * @param line Line containing a task in local file.
     * @return Task type from TaskType enum.
     * @throws InvalidFormatException If task type is invalid.
     */
    private TaskType parseTaskType(String line) throws InvalidFormatException {
        String[] input = line.split("\\|", 2);
        try {
            return TaskType.valueOf(input[0].trim());
        } catch (IllegalArgumentException exception) {
            throw new InvalidFormatException("Invalid task type found in file");
        }
    }

    /**
     * Retrieves content from line by stripping the task type and marked status.
     *
     * @param line Line containing a task in local file.
     * @return Content from line.
     */
    private String parseTaskContent(String line) {
        String[] input = line.split("\\|", 3);
        return input[2].trim();
    }

    /**
     * Retrieves marked status from line.
     *
     * @param line Line containing a task in local file.
     * @return Marked status.
     * @throws InvalidFormatException If status is not represented by 0 or 1.
     */
    private boolean parseTaskMarked(String line) throws InvalidFormatException {
        String[] input = line.split("\\|", 3);
        try {
            return Integer.parseInt(input[1].trim()) == 1;
        } catch (NumberFormatException exception) {
            throw new InvalidFormatException(invalidFormatMessage);
        }
    }

    /**
     * Parses line and creates a To-do instance.
     *
     * @param line Line containing a task in local file.
     * @return To-do instance represented by the line.
     * @throws InvalidFormatException If line is not of to-do storage format.
     */
    private ToDo parseTodoTask(String line) throws InvalidFormatException {
        checkTodoFormat(line);
        String content = parseTaskContent(line);
        boolean isMarked = parseTaskMarked(line);
        return createTodoTask(content, isMarked);
    }

    /**
     * Checks if line has a valid format for to-do task.
     *
     * @param line Line containing a task in local file.
     * @throws InvalidFormatException If line format is invalid.
     */
    private void checkTodoFormat(String line) throws InvalidFormatException {
        if (!line.matches("^T \\| [01] \\| .+")) {
            throw new InvalidFormatException("Format should be T | [1/0] | [description]");
        }
    }

    /**
     * Creates to-do task and marks it as completed if appropriate.
     *
     * @param description Description of task.
     * @param isMarked Marked status of task.
     * @return Created to-do task.
     */
    private ToDo createTodoTask(String description, boolean isMarked) {
        ToDo task = new ToDo(description);
        if (isMarked) {
            task.markCompleted();
        }
        return task;
    }

    /**
     * Parses line and creates a Deadline instance.
     *
     * @param line Line containing a task in local file.
     * @return Deadline instance represented by the line.
     * @throws InvalidFormatException If line is not of deadline storage format.
     */
    private Deadline parseDeadlineTask(String line) throws InvalidFormatException {
        checkDeadlineFormat(line);
        String content = parseTaskContent(line);
        String description = parseDescription(content);
        LocalDate byDate = parseDeadlineDate(content);
        boolean isMarked = parseTaskMarked(line);
        return createDeadlineTask(description, byDate, isMarked);
    }

    /**
     * Checks if line has a valid format for deadline task.
     *
     * @param line Line containing a task in local file.
     * @throws InvalidFormatException If line format is invalid.
     */
    private void checkDeadlineFormat(String line) throws InvalidFormatException {
        if (!line.matches("^D \\| [01] \\| .+ \\| \\d{4}-\\d{2}-\\d{2}")) {
            throw new InvalidFormatException("Format should be D | [1/0] | [description] | yyyy-mm-dd");
        }
    }

    /**
     * Retrieves description for a task.
     *
     * @param content Line containing task without task type and marked status.
     * @return Remaining content for task.
     */
    private String parseDescription(String content) {
        String[] splitContent = content.split("\\|", 2);
        return splitContent[0].trim();
    }

    /**
     * Retrieves by date for deadline task.
     *
     * @param content Line containing task without task type and marked status.
     * @return By date for deadline task.
     * @throws InvalidFormatException If date format is invalid.
     */
    private LocalDate parseDeadlineDate(String content) throws InvalidFormatException {
        String byDate = content.split("\\|")[1].trim();
        try {
            return LocalDate.parse(byDate);
        } catch (DateTimeParseException exception) {
            throw new InvalidFormatException(invalidFormatMessage);
        }
    }

    /**
     * Creates deadline task and marks it as completed if appropriate.
     *
     * @param description Description of task.
     * @param byDate By date of task.
     * @param isMarked Marked status of task.
     * @return Created deadline task.
     */
    private Deadline createDeadlineTask(String description, LocalDate byDate, boolean isMarked) {
        Deadline task = new Deadline(description, byDate);
        if (isMarked) {
            task.markCompleted();
        }
        return task;
    }

    /**
     * Parses line and creates an Event instance.
     *
     * @param line Line containing a task in local file.
     * @return Event instance represented by the line.
     * @throws InvalidFormatException If line is not of event format.
     */
    private Event parseEventTask(String line) throws InvalidFormatException {
        checkEventFormat(line);
        String content = parseTaskContent(line);
        String description = parseDescription(content);
        LocalDate fromDate = parseEventFromDate(content);
        LocalDate toDate = parseEventToDate(content);
        boolean isMarked = parseTaskMarked(line);
        return createEventTask(description, fromDate, toDate, isMarked);
    }

    /**
     * Checks if line has a valid format for event task.
     *
     * @param line Line containing a task in local file.
     * @throws InvalidFormatException If line format is invalid.
     */
    private void checkEventFormat(String line) throws InvalidFormatException {
        if (!line.matches("^E \\| [01] \\| .+ \\| \\d{4}-\\d{2}-\\d{2} \\| \\d{4}-\\d{2}-\\d{2}")) {
            throw new InvalidFormatException("Format should be E | [1/0] | "
                    + "[description] | yyyy-mm-dd | yyyy-mm-dd");
        }
    }

    /**
     * Retrieves from date for event task.
     *
     * @param content Line containing task without task type and marked status.
     * @return From date for event task.
     * @throws InvalidFormatException If date format is invalid.
     */
    private LocalDate parseEventFromDate(String content) throws InvalidFormatException {
        String fromDate = content.split("\\|")[1].trim();
        try {
            return LocalDate.parse(fromDate);
        } catch (DateTimeParseException exception) {
            throw new InvalidFormatException(invalidFormatMessage);
        }
    }

    /**
     * Retrieves to date for event task.
     *
     * @param content Line containing task without task type and marked status.
     * @return To date for event task.
     * @throws InvalidFormatException If date format is invalid.
     */
    private LocalDate parseEventToDate(String content) throws InvalidFormatException {
        String toDate = content.split("\\|")[2].trim();
        try {
            return LocalDate.parse(toDate);
        } catch (DateTimeParseException exception) {
            throw new InvalidFormatException("");
        }
    }

    /**
     * Creates event task and marks it as completed if appropriate.
     *
     * @param description Description of task.
     * @param fromDate From date of task.
     * @param toDate To date of task.
     * @param isMarked Marked status of task.
     * @return Created event task.
     */
    private Event createEventTask(String description, LocalDate fromDate, LocalDate toDate, boolean isMarked) {
        Event task = new Event(description, fromDate, toDate);
        if (isMarked) {
            task.markCompleted();
        }
        return task;
    }

    /**
     * Rewrites to file to reflect any changes to the tasks.
     *
     * @param taskList The TaskList instance containing the updated list of tasks.
     * @throws IOException If there is an error during rewriting the file.
     */
    public void rewriteFile(TaskList taskList) throws DuduException {
        ArrayList<Task> tasks = taskList.getTasks();
        try {
            FileWriter fw = new FileWriter(this.filePath);
            for (Task task : tasks) {
                fw.write(String.format("%s\n", task.toStorageString()));
            }
            fw.close();
        } catch (IOException exception) {
            throw new DuduException("Error writing into file");
        }
    }
}
