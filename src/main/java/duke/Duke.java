package duke;

import duke.data.Storage;
import duke.task.Deadline;
import duke.task.Event;
import duke.task.Task;
import duke.task.Todo;

import java.util.ArrayList;
import java.util.Scanner;

public class Duke {
    private static final String LINE = "____________________________________________________________";
    private static final String ADD_TASK_MSG = "Got it. I've added this duke.task: ";
    private static final String ERROR_MSG = "☹ OOPS!!! I'm sorry, but I don't know what that means :-(";
    private static final String TODO_ERROR = "The description of a todo cannot be empty.";
    private static final String DEADLINE_ERROR = "The description of a deadline cannot be empty and must have a '/by'.";
    private static final String EVENT_ERROR = "The description of an event cannot be empty and must have a '/at'.";
    private static final String SEARCH_ERROR = "Cannot find such a thing, please try again!";
    private static final String FILE_PATH = "tasks.txt";
    private static ArrayList<Task> tasks;
    private static Storage storage;

    public static void main(String[] args) {
        showHelloGreeting();
        fileManager();
        executeResponses();
        showByeGreeting();
    }

    private static void fileManager() {
        try {
            storage = new Storage(FILE_PATH);
            tasks = storage.loadTasksFromFile();
        } catch (DukeException err) {
            System.out.println("Unable to load file.");
        }
    }

    private static void executeResponses() {
        Scanner in = new Scanner(System.in);
        String text;
        text = in.nextLine();
        while (!text.equals("bye")) {
            System.out.println(LINE);
            String[] words = text.split(" ");
            int numOfTasks = tasks.size();
            try {
                switch (words[0]) {
                case "list":
                    System.out.println("Here are the tasks in your list:");
                    for (int i = 0; i < numOfTasks; i++) {
                        System.out.println((i + 1) + "." + tasks.get(i).toString());
                    }
                    break;
                case "done":
                    int taskNum = Integer.parseInt(words[words.length - 1]);
                    Task taskToSetDone = tasks.get(taskNum - 1);
                    taskToSetDone.setDone();
                    System.out.println("Nice! I've marked this task as done:");
                    System.out.println(taskToSetDone.getStatusIcon() + taskToSetDone.getDescription());
                    break;
                case "delete":
                    int taskNumToDelete = Integer.parseInt(words[words.length - 1]);
                    Task taskToDelete = tasks.remove(taskNumToDelete - 1);
                    System.out.println("Noted. I've removed this task: ");
                    System.out.println(taskToDelete.getStatusIcon() + taskToDelete.getDescription());
                    System.out.println("Now you have " + tasks.size() + " task(s) in the list.");
                    break;
                case "todo":
                    addTodo(text);
                    System.out.println("Now you have " + tasks.size() + " task(s) in the list.");
                    break;
                case "deadline":
                    addDeadline(text);
                    System.out.println("Now you have " + tasks.size() + " task(s) in the list.");
                    break;
                case "event":
                    addEvent(text);
                    System.out.println("Now you have " + tasks.size() + " task(s) in the list.");
                    break;
                case "find":
                    findTask(text);
                    break;
                default:
                    showErrorMessage();
                    break;
                }
            } catch (DukeException error) {
                System.out.println(error.getMessage());
            }
            System.out.println(LINE);
            text = in.nextLine();
        }

        try {
            storage.saveTasksToFile(tasks);
        } catch (DukeException err) {
            System.out.println("Unable to save file.");
        }
    }

    private static void addTodo(String text) throws DukeException {
        if (text.length() <= "todo".length()) {
            throw new DukeException(TODO_ERROR);
        }
        String[] todoTaskInfo = extractInfo(text, "todo");
        Task newTodo = new Todo(todoTaskInfo[0], false);
        tasks.add(newTodo);
        System.out.println(ADD_TASK_MSG);
    }

    private static void addDeadline(String text) throws DukeException {
        if (text.length() <= "deadline".length()) {
            throw new DukeException(DEADLINE_ERROR);
        }

        if (!text.contains("/by")) {
            throw new DukeException(DEADLINE_ERROR);
        }
        String[] deadlineTaskInfo = extractInfo(text, "deadline");
        Task newDeadline = new Deadline(deadlineTaskInfo[0], deadlineTaskInfo[1], false);
        tasks.add(newDeadline);
        System.out.println(ADD_TASK_MSG);
    }

    private static void addEvent(String text) throws DukeException {
        if (text.length() <= "event".length()) {
            throw new DukeException(EVENT_ERROR);
        }

        if (!text.contains("/at")) {
            throw new DukeException(EVENT_ERROR);
        }
        String[] eventTaskInfo = extractInfo(text, "event");
        Task newEvent = new Event(eventTaskInfo[0], eventTaskInfo[1], false);
        tasks.add(newEvent);
        System.out.println(ADD_TASK_MSG);
    }

    private static void findTask(String text) throws DukeException {
        if (text.length() <= "find".length()) {
            throw new DukeException(SEARCH_ERROR);
        }
        ArrayList<Task> tasksFound = new ArrayList<>();
        String[] taskToFind = extractInfo(text, "find");
        for (Task task : tasks) {
            if (task.getDescription().contains(taskToFind[0])) {
                tasksFound.add(task);
            }
        }
        if (tasksFound.size() == 0) {
            System.out.println(SEARCH_ERROR);
            return;
        }
        System.out.println("Here are the matching tasks in your list:");
        for (int i = 0; i < tasksFound.size(); i++) {
            System.out.println((i + 1) + "." + tasksFound.get(i).toString());
        }
    }

    private static void showByeGreeting() {
        System.out.println(" Bye. Hope to see you again soon!");
        System.out.println(LINE);
    }

    private static void showHelloGreeting() {
        String logo = " ____        _        \n"
                + "|  _ \\ _   _| | _____ \n"
                + "| | | | | | | |/ / _ \\\n"
                + "| |_| | |_| |   <  __/\n"
                + "|____/ \\__,_|_|\\_\\___|\n";
        System.out.println("Hello from\n" + logo);
        System.out.println(LINE);
        System.out.println(" Hello! I'm Duke\n" +
                " What can I do for you?");
        System.out.println(LINE);
    }

    private static String[] extractInfo(String taskString, String taskType) {
        String[] taskInfo = new String[2];
        int slashPos = taskString.indexOf('/');
        switch (taskType) {
        case "todo":
        case "find":
            taskInfo[0] = taskString.substring(5);
            break;
        case "deadline":
            taskInfo[0] = taskString.substring(9, slashPos - 1);
            taskInfo[1] = taskString.substring(slashPos + 4);
            break;
        case "event":
            taskInfo[0] = taskString.substring(6, slashPos - 1);
            taskInfo[1] = taskString.substring(slashPos + 4);
            break;
        default:
            break;
        }
        return taskInfo;
    }

    private static void showErrorMessage() throws DukeException {
        throw new DukeException(ERROR_MSG);
    }
}
