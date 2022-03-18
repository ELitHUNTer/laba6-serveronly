package main;

/**
 * Типы команд
 */
public enum CommandType{
    help("help"),
    info("info"),
    show("show"),
    add("add"),
    update("update"),
    remove_by_id("remove_by_id"),
    clear("clear"),
    save("save"),
    execute_script("execute_script"),
    exit("exit"),
    remove_head("remove_head"),
    add_if_max("add_if_max"),
    add_if_min("add_if_min"),
    filter_by_annual_turnover("filter_by_annual_turnover"),
    filter_starts_with_name("filter_starts_with_name"),
    print_unique_type("print_unique_type");

    String value;

    CommandType(String value){
        this.value = value;
    }
}