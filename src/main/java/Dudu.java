import java.util.Scanner;
import java.lang.StringBuilder;

public class Dudu {
    public static void main(String[] args) {
        String welcomeMessage = LineWrapper.wrap("Hello! I'm dudu\n"
                                                + "What can I do for you?");
        String goodbyeMessage = LineWrapper.wrap("Bye. Hope to see you again soon!");

        System.out.println(welcomeMessage);

        Scanner sc = new Scanner(System.in);
        Task[] tasks = new Task[100];
        int count = 0;

        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            if (input.equals("list")) {
                if (count == 0) {
                    System.out.println(LineWrapper.wrap("No tasks"));
                } else {
                    StringBuilder output = new StringBuilder("Here are the tasks in your list:");
                    for (int i = 0; i < count; i++) {
                        output.append("\n" + (i + 1) + ". " + tasks[i]);
                    }
                    System.out.println(LineWrapper.wrap(output.toString()));
                }
            } else if (input.equals("bye")) {
                break;
            } else if (input.matches("mark \\d")) {
                int pos = Integer.parseInt(input.replaceAll("\\D+","")) - 1;
                tasks[pos].markCompleted();
                String output = LineWrapper.wrap(String.format("Nice! I've marked this task as done:\n    %s", tasks[pos]));
                System.out.println(output);
            } else if (input.matches("unmark \\d")){
                int pos = Integer.parseInt(input.replaceAll("\\D+","")) - 1;
                tasks[pos].markUncompleted();
                String output = LineWrapper.wrap(String.format("OK, I've marked this task as not done yet:\n    %s", tasks[pos]));
                System.out.println(output);
            } else if (input.matches("^todo.*")) {
                ToDo task = new ToDo(input.split("todo ")[1]);
                tasks[count] = task;
                count++;
                String output = LineWrapper.wrap(String.format("Got it. I've added this task:\n    %s\nNow you have %d tasks in the list.", task, count));
                System.out.println(output);
            } else if (input.matches("^deadline.*")) {
                String[] details = input.split("deadline ")[1].split(" /by ");
                Deadline task = new Deadline(details[0], details[1]);
                tasks[count] = task;
                count++;
                String output = LineWrapper.wrap(String.format("Got it. I've added this task:\n    %s\nNow you have %d tasks in the list.", task, count));
                System.out.println(output);
            } else if (input.matches("^event.*")) {
                String details = input.split("event ")[1];
                String description = details.split("/from ")[0];
                String[] date = details.split("/from ")[1].split(" /to ");
                Event task = new Event(description, date[0], date[1]);
                tasks[count] = task;
                count++;
                String output = LineWrapper.wrap(String.format("Got it. I've added this task:\n    %s\nNow you have %d tasks in the list.", task, count));
                System.out.println(output);
            } else {
                Task task = new Task(input);
                tasks[count] = task;
                count++;
                String output = LineWrapper.wrap(String.format("added: %s", input));
                System.out.println(output);
            }
        }

        System.out.println(goodbyeMessage);
    }
}
