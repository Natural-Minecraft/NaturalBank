package id.naturalsmp.naturalbank.commands;

public abstract class NBCmdExecution {

    public ExecutionType executionType = ExecutionType.VALID_EXECUTION;

    public abstract void execute();

    public enum ExecutionType {
        INVALID_EXECUTION, // When the cmd does not specify enough arguments, or specify incorrect
                           // arguments.
        VALID_EXECUTION // When the cmd has correct arguments and is ready to execute.
    }

    public static NBCmdExecution invalidExecution() {
        NBCmdExecution execution = new NBCmdExecution() {
            @Override
            public void execute() {
            }
        };
        execution.executionType = ExecutionType.INVALID_EXECUTION;
        return execution;
    }
}