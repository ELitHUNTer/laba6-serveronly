package main;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

class Main {

    private static PriorityQueue<Organization> organizations;
    private static Scanner consoleScanner, dataFileScanner;

    private static int PORT = 2222;
    private static Socket client;
    private static ServerSocket server;
    private static BufferedReader input;
    private static BufferedWriter output;
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static Gson gsonParser = new Gson();
    private static Thread consoleTerminal;

    public static void main(String[] args) {
        log("Server start");
        organizations = new PriorityQueue<>();
        onStart();

        try {
            try {
                server = new ServerSocket(PORT);
                client = server.accept();
                input = new BufferedReader(new InputStreamReader(client.getInputStream()));
                output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                while (true) {
                    //break;
                    String command = input.readLine();
                    log("Команда: " + command);
                    if (command != null)
                        executeCommand(gsonParser.fromJson(command, Command.class));
                    else
                        break;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                client.close();
                server.close();
                input.close();
                output.close();
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    /**
     * Действия, выполняемые при старте программы
     */
    private static void onStart(){
        consoleScanner = new Scanner(System.in);
        try {
            dataFileScanner = new Scanner(new File("input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Отсутствует файл с данными");
            return;
        }
        while (dataFileScanner.hasNext()){
            String[] org = dataFileScanner.nextLine().split(",");
            for (int i = 0; i< org.length; i++)
                if (org[i].equals("\\n"))
                    org[i] = null;
            if (org.length != 11){
                System.out.println("Неверный формат представления данных(количество) в строке " + (organizations.size()+1));
                continue;
            }
            try {
                organizations.add(new Organization(org[0], org[1], Long.valueOf(org[2]), Long.parseLong(org[3]), Integer.parseInt(org[4]), org[5], org[6], org[7], Long.valueOf(org[8]), Long.valueOf(org[9]), org[10]));
            } catch (Exception exception){
                System.out.println(exception.getMessage());
                System.out.println("Неверный формат представления данных в строке " + (organizations.size()+1));
            }
        }
        dataFileScanner.close();
        consoleTerminal = new Thread(new Console());
        consoleTerminal.start();
        //System.out.println(organizations.peek().toCSV());
    }

    /**
     * Выполнить команду
     * @param command команда
     */

    private static void executeCommand(Command command, boolean toConsole){
        CommandType commandType = command.getType();
        switch (commandType){
            case help: {
                if (toConsole)
                    System.out.println("help : вывести справку по доступным командам\n"+
                            "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)\n"+
                            "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n"+
                            "add {element} : добавить новый элемент в коллекцию\n"+
                            "update id {element} : обновить значение элемента коллекции, id которого равен заданному\n"+
                            "remove_by_id id : удалить элемент из коллекции по его id\n"+
                            "clear : очистить коллекцию\n"+
                            "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.\n"+
                            "exit : завершить программу (без сохранения в файл)\n"+
                            "remove_head : вывести первый элемент коллекции и удалить его\n"+
                            "add_if_max {element} : добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции\n"+
                            "add_if_min {element} : добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции\n"+
                            "filter_by_annual_turnover annualTurnover : вывести элементы, значение поля annualTurnover которых равно заданному\n"+
                            "filter_starts_with_name name : вывести элементы, значение поля name которых начинается с заданной подстроки\n"+
                            "print_unique_type : вывести уникальные значения поля type всех элементов в коллекции");
                else
                    send(new Message(
                        "help : вывести справку по доступным командам",
                        "info : вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)",
                        "show : вывести в стандартный поток вывода все элементы коллекции в строковом представлении",
                        "add {element} : добавить новый элемент в коллекцию",
                        "update id {element} : обновить значение элемента коллекции, id которого равен заданному",
                        "remove_by_id id : удалить элемент из коллекции по его id",
                        "clear : очистить коллекцию",
                        "execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.",
                        "exit : завершить программу (без сохранения в файл)",
                        "remove_head : вывести первый элемент коллекции и удалить его",
                        "add_if_max {element} : добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции",
                        "add_if_min {element} : добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции",
                        "filter_by_annual_turnover annualTurnover : вывести элементы, значение поля annualTurnover которых равно заданному",
                        "filter_starts_with_name name : вывести элементы, значение поля name которых начинается с заданной подстроки",
                        "print_unique_type : вывести уникальные значения поля type всех элементов в коллекции"));
                break;
            }
            case info: {
                if (toConsole)
                    System.out.println("Тип: " + organizations.getClass().getName() + '\n' +
                            "Количество элементов: " + organizations.size());
                else
                    send(new Message("Тип: " + organizations.getClass().getName(),
                        "Количество элементов: " + organizations.size()));
                break;
            }
            case show: {
                String[] s = new String[organizations.size()];
                int iterator = 0;
                for (Organization o : organizations)
                    s[iterator++] = o.toString();
                if (toConsole)
                    Arrays.stream(s).forEach(System.out::println);
                else
                    send(new Message(s));
                break;
            }
            case add: {
                Organization o = createOrganization(command.getArgs());
                if (o != null) {
                    organizations.add(o);
                    log("Добавлена организация " + o);
                    send("организация добавлена");
                } else
                    send("Невозможно добавить организацию");
                break;
            }
            case update: {
                Organization org = createOrganization(command.getArgs());
                for (Organization o : organizations) {
                    if (org != null && o.getID() == Long.valueOf(command.getArgs()[0])) {
                        Organization newOrganization = createOrganization(Arrays.copyOfRange(command.getArgs(), 1, command.getArgs().length));
                        log("Организация " + o + " обновлена до " + newOrganization);
                        o.update(newOrganization);
                        send("Организация обновлена");
                    }
                }
                break;
            }
            case remove_by_id: {
                Organization org = createOrganization(command.getArgs());
                for (Organization o : organizations) {
                    if (org != null && o.getID() == Long.valueOf(command.getArgs()[0])) {
                        log("Организация " + o + " удалена");
                        organizations.remove(o);
                        break;
                    }
                }
                break;
            }
            case clear: {
                organizations.clear();
                log("Коллекция очищена");
                break;
            }
            case save: {
                try (PrintWriter pw = new PrintWriter(new File("input.txt"))) {
                    for (Organization o : organizations) {
                        pw.println(o.toCSV());
                    }
                    log("Коллекция сохранена в файл");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    log("Ошибка при сохранении коллекции " + e.getMessage());
                }
                break;
            }
            case execute_script: {
                try (Scanner scriptScanner = new Scanner(new File(command.getArgs()[0]))) {
                    while (scriptScanner.hasNext()) {
                        String[] splittedCommand = scriptScanner.nextLine().split(" ");
                        Command c = new Command(CommandType.valueOf(splittedCommand[0]), Arrays.copyOfRange(splittedCommand, 1, splittedCommand.length));
                        executeCommand(c);
                    }
                } catch (FileNotFoundException e) {
                    if (toConsole)
                        send("Указанного файла не существует");
                    else
                        System.out.println("Указанного файла не существует");
                }
                break;
            }
            case exit: {
                log("Завершение работы");
                consoleScanner.close();
                System.exit(0);
                break;
            }
            case remove_head: {
                log("Удалена организация " + organizations.stream().findFirst());
                send(organizations.peek().toString());
                break;
            }
            case add_if_max: {
                Organization o = createOrganization(command.getArgs());
                if (o != null) {
                    Object[] obj = organizations.toArray();
                    if (o.getAnnualTurnover() > ((Organization) obj[obj.length - 1]).getAnnualTurnover()) {
                        organizations.add(o);
                        log("Добавлена организация " + o);
                    } else {
                        System.out.println("Организация не добавлена");
                    }
                } else {
                    log("Организация не могла быть создана");
                }
                break;
            }
            case add_if_min: {
                Organization o = createOrganization(command.getArgs());
                if (o != null) {
                    if (o.getAnnualTurnover() < organizations.peek().getAnnualTurnover()) {
                        organizations.add(o);
                        System.out.println("Организация добавлена");
                        log("Добавлена организация " + o);
                    } else
                        System.out.println("Организация не добавлена");
                } else {
                    System.out.println("Организация не создана");
                    log("Организация не могла быть создана");
                }
                break;
            }
            case filter_by_annual_turnover: {
                    try {
                        ArrayList<String> arrayList = new ArrayList<>();
                        int at = Integer.parseInt(command.getArgs()[0]);
                        for (Organization o : organizations) {
                            if (o.getAnnualTurnover() == at)
                                arrayList.add(o.toString());
                        }
                        send(Arrays.toString(arrayList.toArray()));
                        log("отправлены организации " + arrayList.toString());
                    } catch (NumberFormatException ex) {
                        System.out.println("Ошибка. " + command.getArgs()[0] + " не является числом");
                        send("Ошибка. " + command.getArgs()[0] + " не является числом");
                        log("Ошибка " + ex.getMessage());
                    }
                break;
            }
            case filter_starts_with_name: {
                String arr = Arrays.toString(organizations.stream().filter(s -> s.getName().startsWith(command.getArgs()[0])).toArray());
                send(arr);
                log("Отправлено " + arr.toString());
                break;
            }
            case print_unique_type: {
                ArrayList<String> arrayList = new ArrayList();
                organizations.forEach(s -> arrayList.add(s.getType().toString()));
                send(Arrays.toString(arrayList.toArray()));
                log("Отправлено " + Arrays.toString(arrayList.toArray()));
                break;
            }
            default:
                send("Неверный формат команды. Введите help для просмотра списка команд");
                log("Неверный формат команды");
                break;
        }
    }

    private static void executeCommand(Command command) {
        executeCommand(command, false);
    }

    /**
     * создать организацию
     * @param params параметры
     * @return организация
     */
    private static Organization createOrganization(String[] params){
        try {
            return new Organization(
                    params[0],
                    params[1],
                    Long.valueOf(params[2]),
                    Long.parseLong(params[3]),
                    Integer.parseInt(params[4]),
                    params[5],
                    params[6],
                    params[7],
                    Long.valueOf(params[8]),
                    Long.valueOf(params[9]),
                    params[10]);
        } catch (Exception e) {
            send(e.getMessage());
            return null;
        }
    }

    private static void send(String message){
        send(new Message(message));
    }

    private static void send(Message message){
        try {
            if (output != null) {
                output.write(gsonParser.toJson(message) + '\n');
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void log(String logMessage){
        System.out.println(logMessage);
        logger.info(logMessage);
    }

    private static class Console implements Runnable{

        @Override
        public void run() {
            while (true){
                String[] s = consoleScanner.nextLine().split(" ");
                executeCommand(new Command(CommandType.valueOf(s[0]), Arrays.copyOfRange(s, 1, s.length)), true);
            }
        }
    }
}