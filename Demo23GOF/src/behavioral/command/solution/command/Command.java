package behavioral.command.solution.command;

// Interface khai báo execute() để thực hiện yêu cầu và getDescription() để mô tả yêu cầu đó.
public interface Command {
    void execute();

    String getDescription();
}
