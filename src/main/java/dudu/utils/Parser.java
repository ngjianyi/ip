package dudu.utils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Stack;

import dudu.command.*;
import dudu.exception.InvalidFormatException;
import dudu.exception.MissingDateTimeException;
import dudu.exception.MissingDescriptionException;
import dudu.task.Deadline;
import dudu.task.Event;
import dudu.task.ToDo;

/**
 * Represents the class that parses user commands
 */
public class Parser {
    enum CommandType {
        BYE, LIST, MARK, UNMARK, DELETE, TODO, DEADLINE, EVENT, FIND, HELP, UNDO, INVALID
    }

    private static final Stack<Command> undoStack = new Stack<>();
    private static final String missingDescriptionMessage = "Please include a description";
    private static final String invalidNumberMessage = "Please input a valid number";

    /**
     * Parses a user command and returns the corresponding Command instance
     *
     * @param command User input
     * @return Respective Command instance
     * @throws MissingDescriptionException If description is missing
     * @throws InvalidFormatException If command format is invalid
     * @throws DateTimeParseException If date is invalid
     * @throws MissingDateTimeException If date is missing
     */
    public static Command parse(String command) throws MissingDescriptionException,
            InvalidFormatException, DateTimeParseException, MissingDateTimeException {
        CommandType commandType = getCommandTypeFromInput(command);
        switch (commandType) {
        case BYE:
            return new CommandBye();
        case LIST:
            return new CommandList();
        case HELP:
            return new CommandHelp();
        case TODO:
            return new AddCommand(createTodo(command), false);
        case DEADLINE:
            return new AddCommand(createDeadline(command), false);
        case EVENT:
            return new AddCommand(createEvent(command), false);
        case MARK: {
            return new CommandMark(parseIndex(command), false);
        } case UNMARK: {
            return new CommandUnmark(parseIndex(command), false);
        } case DELETE: {
            return new CommandDelete(parseIndex(command), false);
        } case FIND:
            return new CommandFind(parseContent(command));
        case UNDO:
            return parseUndoCommand();
        case INVALID:
            return new InvalidCommand();
        default:
            System.out.println("Something went wrong");
            return new InvalidCommand();
        }
    }

    /**
     * Get command type from user input
     *
     * @param command User input
     * @return Command type
     */
    private static CommandType getCommandTypeFromInput(String command) {
        String[] splitCommand = command.split(" ", 2);
        try {
            return CommandType.valueOf(splitCommand[0].trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommandType.INVALID;
        }
    }

    /**
     * Removes command from user input
     *
     * @param command User input
     * @return Content after commandType
     * @throws MissingDescriptionException If content is missing
     */
    private static String parseContent(String command) throws MissingDescriptionException {
        String[] splitCommand = command.split(" ", 2);
        if (splitCommand.length <= 1) {
            throw new MissingDescriptionException(missingDescriptionMessage);
        }
        String content = splitCommand[1].trim();
        if (content.isEmpty()) {
            throw new MissingDescriptionException(missingDescriptionMessage);
        }
        return content;
    }

    /**
     * Create a To-do task
     *
     * @param command User input without command at the front
     * @return Created to-do task
     * @throws MissingDescriptionException If description is missing
     */
    private static ToDo createTodo(String command) throws MissingDescriptionException {
        return new ToDo(parseContent(command));
    }

    /**
     * Create a Deadline task
     *
     * @param command User input without command at the front
     * @return Created deadline event
     * @throws MissingDescriptionException If description is missing
     * @throws InvalidFormatException If command format is invalid
     * @throws MissingDateTimeException If deadline is missing
     */
    private static Deadline createDeadline(String command) throws MissingDescriptionException,
            InvalidFormatException, MissingDateTimeException {
        String content = parseDeadlineContent(command);
        String description = parseDeadlineDescription(content);
        LocalDate by = parseDeadlineDate(content);
        return new Deadline(description, by);
    }

    /**
     * Removes command from user input and check for valid deadline task format
     *
     * @param command User input
     * @return Content after commandType
     * @throws MissingDescriptionException If description is missing
     * @throws InvalidFormatException If deadline command format is invalid
     */
    private static String parseDeadlineContent(String command) throws MissingDescriptionException,
            InvalidFormatException {
        String content = parseContent(command);
        if (!content.matches(".*/by.*")) {
            throw new InvalidFormatException("Please use deadline [description] /by [time]");
        }
        return content;
    }

    /**
     * Retrieves description from deadline commands
     *
     * @param content User input stripped of command
     * @return Description for deadline task
     * @throws MissingDescriptionException If no description is available
     */
    private static String parseDeadlineDescription(String content) throws MissingDescriptionException {
        String[] splitContent = content.split("/by", 2);
        String description = splitContent[0].trim();
        if (description.isEmpty()) {
            throw new MissingDescriptionException(missingDescriptionMessage);
        }
        return description;
    }

    /**
     * Retrieves deadline from deadline commands
     *
     * @param content User input stripped of command
     * @return Description for deadline task
     * @throws MissingDateTimeException If no deadline is available
     */
    private static LocalDate parseDeadlineDate(String content) throws MissingDateTimeException {
        String[] splitContent = content.split("/by", 2);
        String deadlineDate = splitContent[1].trim();
        if (deadlineDate.isEmpty()) {
            throw new MissingDateTimeException("Missing by time");
        }
        return LocalDate.parse(deadlineDate);
    }

    /**
     * Create an event task
     *
     * @param command User input
     * @return Created event task
     * @throws MissingDescriptionException If description is missing
     * @throws InvalidFormatException If event command format is invalid
     * @throws MissingDateTimeException If from date or to date is missing
     */
    private static Event createEvent(String command) throws MissingDescriptionException,
            InvalidFormatException, MissingDateTimeException {
        String content = parseEventContent(command);
        String description = parseEventDescription(content);
        String dates = parseEventDates(content);
        LocalDate from = parseEventFromDate(dates);
        LocalDate to = parseEventToDate(dates);
        return new Event(description, from, to);
    }

    /**
     * Removes command from event command and check for valid event task format
     *
     * @param command User input
     * @return Content after commandType
     * @throws MissingDescriptionException If description is missing
     * @throws InvalidFormatException If event command format is invalid
     */
    private static String parseEventContent(String command) throws MissingDescriptionException, InvalidFormatException {
        String content = parseContent(command);
        if (!content.matches(".*/from.*/to.*")) {
            throw new InvalidFormatException("Please use event [description] /from [time] /to [time]");
        }
        return content;
    }

    /**
     * Retrieves description from event command
     *
     * @param content User input stripped of command
     * @return Description for event task
     * @throws MissingDescriptionException If description is missing
     */
    private static String parseEventDescription(String content) throws MissingDescriptionException {
        String[] splitContent = content.split("/from", 2);
        String description = splitContent[0].trim();
        if (description.isEmpty()) {
            throw new MissingDescriptionException(missingDescriptionMessage);
        }
        return description;
    }

    /**
     * Retrieves from date and to date from event command
     *
     * @param content User input stripped of command
     * @return Dates from event command
     */
    private static String parseEventDates(String content) {
        String[] splitContent = content.split("/from", 2);
        return splitContent[1].trim();
    }

    /**
     * Retrieves from date from event command dates
     *
     * @param dates Event command dates
     * @return From date as LocalDate instance
     * @throws MissingDateTimeException If from date is missing
     * @throws DateTimeParseException If from date is invalid
     */
    private static LocalDate parseEventFromDate(String dates) throws MissingDateTimeException, DateTimeParseException {
        String[] splitDates = dates.split("/to", 2);
        String fromDate = splitDates[0].trim();
        if (fromDate.isEmpty()) {
            throw new MissingDateTimeException("Missing from date");
        }
        return LocalDate.parse(fromDate);
    }

    /**
     * Retrieves to date from event command dates
     *
     * @param dates Event command dates
     * @return To date as LocalDate instance
     * @throws MissingDateTimeException If to date is missing
     * @throws DateTimeParseException If to date is invalid
     */
    private static LocalDate parseEventToDate(String dates) throws MissingDateTimeException, DateTimeParseException {
        String[] splitDates = dates.split("/to", 2);
        String toDate = splitDates[1].trim();
        if (toDate.isEmpty()) {
            throw new MissingDateTimeException("Missing to date");
        }
        return LocalDate.parse(toDate);
    }

    /**
     * Retrieves index from mark, unmark and delete commands
     *
     * @param command User input
     * @return Index from command
     * @throws MissingDescriptionException If no index is available
     * @throws IllegalArgumentException If index is not a number or number is zero or negative
     */
    private static int parseIndex(String command) throws MissingDescriptionException, IllegalArgumentException {
        String content = parseContent(command);
        int index;
        try {
            index = Integer.parseInt(content) - 1;
            if (index < 0) {
                throw new IllegalArgumentException(invalidNumberMessage);
            }
            return index;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(invalidNumberMessage);
        }
    }

    private static UndoCommand parseUndoCommand() {
        if (undoStack.isEmpty()) {
            return new UndoCommand();
        } else {
            return new UndoCommand(undoStack.pop());
        }
    }

    public static void pushToUndoStack(Command command) {
        undoStack.push(command);
    }
}
